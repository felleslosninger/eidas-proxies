package no.difi.eidas.cproxy.saml;

import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.metadata.*;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;

import javax.xml.namespace.QName;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class SAMLUtil {
    public static <T> T createXmlObject(final Class<T> clazz) {
        final XMLObjectBuilderFactory factory = Configuration
                .getBuilderFactory();

        QName defaultElementName;
        try {
            defaultElementName = (QName) clazz.getDeclaredField(
                    "DEFAULT_ELEMENT_NAME").get(null);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to instantiate xml object instance", e);
        }

        @SuppressWarnings("unchecked")
        T xmlObject = (T) factory.getBuilder(defaultElementName).buildObject(
                defaultElementName);

        return xmlObject;
    }

    public static String generateSecureRandomId() {
        try {
            return new SecureRandomIdentifierGenerator().generateIdentifier();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Issuer buildIssuer(String issuerString) {
        Issuer issuer = createXmlObject(Issuer.class);
        issuer.setValue(issuerString);
        return issuer;
    }

    public static String getSessionIndex(Assertion assertion) {
        final List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
        if (authnStatements != null && authnStatements.size() > 0) {
            return authnStatements.get(0).getSessionIndex();
        }
        return null;
    }

    /**
     * Build SAML2 status code.
     *
     * @param sTopLevelStatus The top-level status.
     * @return The constructed status code.
     */

    public static Status constructStatusCode(final String sTopLevelStatus) {
        final StatusCodeBuilder statusCodeBuilder = (StatusCodeBuilder) Configuration
                .getBuilderFactory()
                .getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
        final StatusCode statusCode = statusCodeBuilder.buildObject();
        statusCode.setValue(sTopLevelStatus);
        final StatusBuilder statusBuilder = (StatusBuilder) Configuration
                .getBuilderFactory().getBuilder(Status.DEFAULT_ELEMENT_NAME);
        final Status status = statusBuilder.buildObject();
        status.setStatusCode(statusCode);
        return status;
    }

    public static String resolveSOAPArtifactResolutionServiceLocation(IDPSSODescriptor idpssoDescriptor) {
        for (ArtifactResolutionService service : idpssoDescriptor.getArtifactResolutionServices()) {
            if (service.getBinding().equals(SAMLConstants.SAML2_SOAP11_BINDING_URI)) {
                return service.getLocation();
            }
        }
        return null;
    }

    public static String resolveSingleSignOnServiceLocation(IDPSSODescriptor idpssoDescriptor) {
        for (SingleSignOnService service : idpssoDescriptor.getSingleSignOnServices()) {
            if (service.getBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
                return service.getLocation();
            }
        }
        return null;
    }

    public static String resolveAssertionConsumerServiceLocation(SPSSODescriptor spssoDescriptor) {
        for (AssertionConsumerService service : spssoDescriptor.getAssertionConsumerServices()) {
            if (service.getBinding().equals(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)) {
                return service.getLocation();
            }
        }
        return null;
    }

    public static String resolveIDPSingleSignoutLocation(IDPSSODescriptor idpssoDescriptor) {
        for (SingleLogoutService service : idpssoDescriptor.getSingleLogoutServices()) {
            if (service.getBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
                return service.getLocation();
            }
        }
        return null;
    }

}
