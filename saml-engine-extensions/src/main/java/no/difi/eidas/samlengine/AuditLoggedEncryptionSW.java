package no.difi.eidas.samlengine;

import eu.eidas.auth.engine.core.ProtocolDecrypterI;
import eu.eidas.auth.engine.core.ProtocolEncrypterI;
import eu.eidas.auth.engine.core.impl.EncryptionSW;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml2.core.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.X509Certificate;
import java.util.Map;

public final class AuditLoggedEncryptionSW implements ProtocolEncrypterI, ProtocolDecrypterI {

    private final EncryptionSW encryptionSW;
    private SamlAuditLogger auditLogger;

    public AuditLoggedEncryptionSW(Map<String, String> properties, String defaultPath) throws EIDASSAMLEngineException {
        encryptionSW = new EncryptionSW(properties,defaultPath);
        auditLogger = new SamlAuditLogger();
    }

    @Nonnull
    public Response decryptSamlResponse(@Nonnull Response authResponse) throws EIDASSAMLEngineException {
        Response decryptedAuthResponse = encryptionSW.decryptSamlResponse(authResponse);
        auditLogger.auditRecieveResponse(decryptedAuthResponse);
        return decryptedAuthResponse;
    }

    @Nonnull
    public Response encryptSamlResponse(@Nonnull Response authResponse, @Nonnull X509Certificate destinationCertificate)
            throws EIDASSAMLEngineException {
        auditLogger.auditSendResponse(authResponse);
        return encryptionSW.encryptSamlResponse(authResponse, destinationCertificate);
    }

    @Nonnull
    public X509Certificate getDecryptionCertificate() throws EIDASSAMLEngineException {
        return encryptionSW.getDecryptionCertificate();
    }

    @Nullable
    public X509Certificate getEncryptionCertificate(@Nullable String destinationCountryCode) throws EIDASSAMLEngineException {
        return encryptionSW.getEncryptionCertificate(destinationCountryCode);
    }

    public boolean isEncryptionEnabled(@Nonnull String countryCode) {
        return encryptionSW.isEncryptionEnabled(countryCode);
    }

    public boolean isCheckedValidityPeriod() {
        return encryptionSW.isCheckedValidityPeriod();
    }

    public boolean isDisallowedSelfSignedCertificate() {
        return encryptionSW.isDisallowedSelfSignedCertificate();
    }

    public boolean isResponseEncryptionMandatory() {
        return encryptionSW.isResponseEncryptionMandatory();
    }
}
