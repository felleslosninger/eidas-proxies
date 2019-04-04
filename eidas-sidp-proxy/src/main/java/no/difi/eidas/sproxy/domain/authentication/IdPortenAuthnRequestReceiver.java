package no.difi.eidas.sproxy.domain.authentication;

import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.saml.IdPortenKeyProvider;
import no.difi.eidas.sproxy.domain.saml.SamlXml;
import no.difi.opensaml.signature.SamlSigner;
import no.difi.opensaml.util.SAMLUtil;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class IdPortenAuthnRequestReceiver {
    private final ConfigProvider configProvider;
    private final SAMLUtil samlUtil;
    private final AuditLog auditLog;
    private final IdPortenKeyProvider keyProvider;

    @Autowired
    public IdPortenAuthnRequestReceiver(
            ConfigProvider configProvider,
            SAMLUtil samlUtil,
            AuditLog auditLog,
            IdPortenKeyProvider keyProvider) {
        this.configProvider = configProvider;
        this.samlUtil = samlUtil;
        this.auditLog = auditLog;
        this.keyProvider = keyProvider;
    }

    public IdPortenAuthnRequest receive(SamlXml saml) {
        auditLog.idPortenSamlAuthnRequest(saml);
        IdPortenAuthnRequest authnRequest = new IdPortenAuthnRequest(saml);
        verifySignature(authnRequest);
        validateInstant(authnRequest);
        validateAuthnContextClassRef(authnRequest);
        return authnRequest;
    }

    private void verifySignature(IdPortenAuthnRequest authnRequest) {
        try {
            SamlSigner.verify(
                    authnRequest.getOpenSAMLObject().getSignature(),
                    keyProvider.publicKey()
            );
        } catch (ValidationException e) {
            throw new RuntimeException("AuthnRequest validation failed", e);
        }
    }

    private void validateInstant(IdPortenAuthnRequest authnRequest) {
        if (!samlUtil.validateIssueInstant(authnRequest.getIssueInstant(), configProvider.instantIssueTimeToLive(), configProvider.instantIssueTimeSkew())) {
            throw new RuntimeException("Failed to validate IssueInstant");
        }
    }

    private void validateAuthnContextClassRef(IdPortenAuthnRequest authnRequest) {
        if (authnRequest.getOpenSAMLObject().getRequestedAuthnContext() == null
                || authnRequest.getOpenSAMLObject().getRequestedAuthnContext().getAuthnContextClassRefs() == null
                || authnRequest.getOpenSAMLObject().getRequestedAuthnContext().getAuthnContextClassRefs().isEmpty()) {
            throw new RuntimeException("Missing AuthnContextClassRef");
        }
        String authnContextClassRef = authnRequest.authnContextClassRef();
        if (!("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport".equals(authnContextClassRef)
                || "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI".equals(authnContextClassRef))) {
            throw new RuntimeException(String.format("Unknown AuthnContextClassRef [%s]", authnContextClassRef));
        }
    }

}
