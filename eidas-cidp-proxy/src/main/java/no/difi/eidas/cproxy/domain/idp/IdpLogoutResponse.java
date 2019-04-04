package no.difi.eidas.cproxy.domain.idp;

import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.saml.SAMLUtil;
import org.joda.time.DateTime;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.impl.LogoutResponseMarshaller;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

public class IdpLogoutResponse {
    private LogoutRequest logoutRequest;
    private String destination = "";
    private LogoutResponse logoutResponse;

    public IdpLogoutResponse(final LogoutRequest logoutRequest) {
        this.logoutRequest = logoutRequest;
    }

    /**
     * Returns the destination for this LogoutRequest message
     *
     * @return destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Builds the logoutRequest message, and returns a String representation
     * of the generated xml document
     *
     * @return xml as string
     */
    public String getRawXML() {

        final LogoutResponseMarshaller marshaller = new LogoutResponseMarshaller();
        final Element el;
        try {
            el = marshaller.marshall(logoutResponse);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        }
        return XMLHelper.prettyPrintXML(el);
    }

    public void buildLogoutResponse(ConfigProvider configProvider) {
        final LogoutResponse res = SAMLUtil.createXmlObject(LogoutResponse.class);
        destination = resolveLogoutServiceLocation(configProvider);
        res.setID(SAMLUtil.generateSecureRandomId());
        res.setInResponseTo(logoutRequest.getID());
        res.setIssueInstant(new DateTime());
        res.setIssuer(SAMLUtil.buildIssuer(configProvider.spEntityID()));
        res.setStatus(SAMLUtil.constructStatusCode("urn:oasis:names:tc:SAML:2.0:status:Success"));
        res.setDestination(destination);
        logoutResponse = res;
    }

    /**
     * Resolves the logout service location from the IDP metadata
     *
     * @return LogoutServiceLocation
     */
    private String resolveLogoutServiceLocation(ConfigProvider configProvider) {
        for (SingleLogoutService service : configProvider.getIDPSSODescriptor().getSingleLogoutServices()) {
            if (service.getBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
                return service.getLocation();
            }
        }
        return null;
    }
}
