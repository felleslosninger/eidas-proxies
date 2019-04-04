package no.difi.eidas.cproxy.domain.idp;

import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.saml.SAMLUtil;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.LogoutRequestMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

/**
 * Created by est on 06.07.2015.
 */
public class IdpLogoutRequest {

    private IdportenSession session;
    private String destination = "";
    private LogoutRequest logoutRequest;

    public IdpLogoutRequest(final IdportenSession session) {
        this.session = session;
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
        final LogoutRequestMarshaller marshaller = new LogoutRequestMarshaller();
        final Element el;
        try {
            el = marshaller.marshall(logoutRequest);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        }
        return XMLHelper.prettyPrintXML(el);
    }

    public void buildLogoutRequest(ConfigProvider configProvider) {
        final LogoutRequest req = SAMLUtil.createXmlObject(LogoutRequest.class);
        req.setID(SAMLUtil.generateSecureRandomId());
        req.setIssueInstant(new DateTime());
        req.setIssuer(SAMLUtil.buildIssuer(configProvider.spEntityID()));
        req.setNameID(buildNameId());
        req.setDestination(configProvider.getIDPSingleLogoutService());
        req.getSessionIndexes().add(buildSessionIndex());
        logoutRequest = req;
    }


    private SessionIndex buildSessionIndex() {
        final SessionIndex si = SAMLUtil.createXmlObject(SessionIndex.class);
        si.setSessionIndex(session.getSSOSessionIndex());
        return si;
    }

    private NameID buildNameId() {
        final NameID nameId = SAMLUtil.createXmlObject(NameID.class);
        nameId.setValue(session.getSPNameId());
        return nameId;
    }

}
