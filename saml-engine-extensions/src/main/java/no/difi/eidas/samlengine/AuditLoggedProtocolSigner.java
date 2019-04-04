package no.difi.eidas.samlengine;

import eu.eidas.auth.engine.core.impl.SignSW;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.signature.SignableXMLObject;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;

/**
 * Protocol signer auditing SAML authn requests.
 */
public class AuditLoggedProtocolSigner extends SignSW {

    private SamlAuditLogger auditLogger;

    public AuditLoggedProtocolSigner(Map<String, String> properties, String defaultPath) throws EIDASSAMLEngineException {
        super(properties,defaultPath);
        this.auditLogger = new SamlAuditLogger();
    }

    @Override
    public <T extends SignableXMLObject> T validateSignature(T signedObject, Collection<X509Certificate> trustedCertificateCollection) throws EIDASSAMLEngineException {
        T validatedSignedObject = super.validateSignature(signedObject, trustedCertificateCollection);
        if (isAuthnRequest(signedObject)) {
            auditLogger.auditRecieveAuthnRequest(signedObject);
        }
        return validatedSignedObject;
    }

    @Override
    public <T extends SignableXMLObject> T sign(T signableObject) throws EIDASSAMLEngineException {
        T signedObject = super.sign(signableObject);
        if (isAuthnRequest(signedObject)) {
            auditLogger.auditSendAuthnRequest(signedObject);
        }
        return signedObject;
    }

    protected boolean isAuthnRequest(SignableXMLObject signableObject) {
        return signableObject != null && signableObject instanceof AuthnRequest;
    }

}
