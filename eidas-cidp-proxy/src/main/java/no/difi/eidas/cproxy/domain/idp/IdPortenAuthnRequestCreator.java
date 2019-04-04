package no.difi.eidas.cproxy.domain.idp;

import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.cproxy.saml.CIDPProxyKeyProvider;
import no.difi.opensaml.util.AuthnRequestUtil;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import java.util.Objects;

@Service
public class IdPortenAuthnRequestCreator {
    private final ConfigProvider configProvider;
    private final CIDPProxyKeyProvider idPortenKeyProvider;
    private final AuditLog auditLog;
    private final InstantIssuer instantIssuer;

    @Autowired
    public IdPortenAuthnRequestCreator(
            ConfigProvider configProvider,
            CIDPProxyKeyProvider cIDPProxyKeyProvider,
            AuditLog auditLog,
            InstantIssuer instantIssuer
    ) {

        this.configProvider = configProvider;
        this.idPortenKeyProvider = cIDPProxyKeyProvider;
        this.auditLog = auditLog;
        this.instantIssuer = instantIssuer;
    }

    public AuthnRequest create(NodeAuthnRequest nodeAuthnRequest) {
        AuthnRequest request = authnRequest(nodeAuthnRequest);
        auditLog.requestToIdPorten(request);
        return request;
    }

    public String buildRedirectURL(AuthnRequest authnRequest, boolean isResponse, boolean signRequest) {
        return String.format(
                "%s?%s&locale=nb",
                configProvider.getSingleSignOnService(),
                redirectParameters(authnRequest, isResponse, signRequest)
        );
    }

    private String redirectParameters(AuthnRequest authnRequest, boolean isResponse, boolean signRequest) {
        return IdPortenRedirectURLFactory.build(
                new AuthnRequestUtil().toXml(authnRequest),
                null,
                idPortenKeyProvider.privateKey(),
                isResponse,
                signRequest);
    }

    private AuthnRequest authnRequest(NodeAuthnRequest pepsRequest) {
        AuthnRequest authnRequest = buildSAMLObject(AuthnRequest.class);
        authnRequest.setDestination(configProvider.getSingleSignOnService());
        authnRequest.setAssertionConsumerServiceURL(configProvider.getAssertionConsumerService());
        authnRequest.setIssuer(buildIssuer());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        authnRequest.setIssueInstant(instantIssuer.now());
        authnRequest.setID(pepsRequest.correlationId());
        authnRequest.setNameIDPolicy(buildNameIdPolicy());
        authnRequest.setForceAuthn(pepsRequest.forceAuthn());
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext(pepsRequest.levelOfAssurance()));
        return authnRequest;
    }

    private NameIDPolicy buildNameIdPolicy() {
        NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);
        nameIDPolicy.setAllowCreate(true);
        nameIDPolicy.setFormat(NameIDType.TRANSIENT);
        return nameIDPolicy;
    }

    private RequestedAuthnContext buildRequestedAuthnContext(String levelOfAssurance) {
        RequestedAuthnContext requestedAuthnContext = buildSAMLObject(RequestedAuthnContext.class);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);

        AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef(authnContextClassRef(levelOfAssurance));

        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        return requestedAuthnContext;
    }

    private String authnContextClassRef(String loaUrl) {
        LevelOfAssurance loa = Objects.requireNonNull(LevelOfAssurance.getLevel(loaUrl), "Level of assurance cannot be null");

        switch (loa) {
            case LOW:
            case SUBSTANTIAL:
                return AuthnContext.PPT_AUTHN_CTX;
            case HIGH:
                return AuthnContext.SMARTCARD_PKI_AUTHN_CTX;
            default:
                throw new IllegalArgumentException("Illegal argument level of assurance for authnContextClassRef mapper");
        }
    }

    public static <T> T buildSAMLObject(final Class<T> clazz) {
        T object;
        try {
            XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
            QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            object = (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        }

        return object;
    }

    private Issuer buildIssuer() {
        Issuer issuer = buildSAMLObject(Issuer.class);
        issuer.setValue(configProvider.spEntityID());
        return issuer;
    }
}
