package no.difi.eidas.cproxy.integration.node;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.cproxy.domain.node.NodeRequestedAttributes;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeRequestParser {

    @Autowired
    private ProtocolEngineI engine;
    @Autowired
    private AuditLog auditLog;
    @Autowired
    private IdpMetadataFetcher idpMetadataFetcher;

    public ProtocolEngineI getEngine() {
        return engine;
    }

    public void setEngine(ProtocolEngineI engine) {
        this.engine = engine;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public IAuthenticationRequest parse(String samlToken) throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest authnRequest;
        try {
            byte[] decodedSamlToken = EidasStringUtil.decodeBytesFromBase64(samlToken);
            auditLog.requestFromNode(decodedSamlToken);
            authnRequest =
                    (IEidasAuthenticationRequest) engine.unmarshallRequestAndValidate(
                            decodedSamlToken,
                            SamlEngineProvider.COUNTRY
                    );
        } catch (Exception e) {
            throw new InvalidParameterEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        }

        EidasAuthenticationRequest.Builder builder = new EidasAuthenticationRequest.Builder(authnRequest);
        String callback = getAssertionConsumerServiceURL(idpMetadataFetcher, (MetadataSignerI) engine.getSigner(), authnRequest);
        builder.assertionConsumerServiceURL(callback);
        return builder.build();
    }

    public String getAssertionConsumerServiceURL(
            MetadataFetcherI metadataFetcher,
            MetadataSignerI metadataSigner,
            ILightRequest authnRequest
    ) throws EIDASSAMLEngineException {
        return MetadataUtil.getAssertionConsumerUrlFromMetadata(metadataFetcher, metadataSigner, authnRequest);
    }

    public NodeAuthnRequest toInternal(IAuthenticationRequest eidasRequest) {
        return NodeAuthnRequest.builder()
                .requestedAttributes(toInternal(eidasRequest.getRequestedAttributes()))
                .spCountry(eidasRequest.getServiceProviderCountryCode())
                .correlationId(eidasRequest.getId())
                .forceAuthn(true) // eIDAS is always forceAuthn (STORKSAMLEngine sets it to true always)
                .assertionConsumerServiceAddress(eidasRequest.getAssertionConsumerServiceURL())
                .issuer(eidasRequest.getIssuer())
                .levelOfAssurance(eidasRequest.getLevelOfAssurance())
                .build();
    }

    private NodeRequestedAttributes toInternal(ImmutableAttributeMap requestedAttributeList) {
        NodeRequestedAttributes.Builder builder = NodeRequestedAttributes.builder();
        for (AttributeDefinition<?> attr : requestedAttributeList.getDefinitions()) {
            SubjectBasicAttribute attribute = SubjectBasicAttribute.fromAttributeName(attr.getNameUri().toString());
            if (attr.isRequired())
                builder.required(attribute);
            else
                builder.optional(attribute);
        }
        return builder.build();
    }

}
