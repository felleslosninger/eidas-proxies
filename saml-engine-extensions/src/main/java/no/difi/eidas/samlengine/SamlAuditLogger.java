package no.difi.eidas.samlengine;

import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.idporten.log.audit.AuditLogger;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

public class SamlAuditLogger {

    private AuditLogger auditLogger;

    public SamlAuditLogger() {
        this.auditLogger = AuditLoggerProvider.getInstance().getLogger();
    }

    public void auditSendAuthnRequest(SignableXMLObject samlObject) throws EIDASSAMLEngineException {
        auditLogger.log(AuditMessage.SEND_AUTHN_REQUEST.id(), null, samlString(samlObject));
    }

    public void auditRecieveAuthnRequest(SignableXMLObject samlObject) throws EIDASSAMLEngineException {
        auditLogger.log(AuditMessage.RECIEVE_AUTHN_REQUEST.id(), null, samlString(samlObject));
    }

    public void auditSendResponse(Response samlObject) throws EIDASSAMLEngineException {
        auditLogger.log(AuditMessage.SEND_RESPONSE.id(), null, samlString(samlObject));
    }

    public void auditRecieveResponse(Response samlObject) throws EIDASSAMLEngineException {
        auditLogger.log(AuditMessage.RECIEVE_RESPONSE.id(), null, samlString(samlObject));
    }

    protected String samlString(SignableXMLObject samlObject) throws EIDASSAMLEngineException {
        try {
            Element element = samlObject.getDOM() != null ? samlObject.getDOM() : OpenSamlHelper.marshallToDom(samlObject);
            return withoutLineEndings(XMLHelper.nodeToString(element.cloneNode(true)));
        } catch (Exception e) {
            throw new EIDASSAMLEngineException("Failed to audit saml message", e);
        }
    }

    protected String withoutLineEndings(String s) {
        return s.replaceAll("\\R", "");
    }


    enum AuditMessage {

        SEND_AUTHN_REQUEST("EIDAS-1"),
        RECIEVE_AUTHN_REQUEST("EIDAS-2"),
        SEND_RESPONSE("EIDAS-3"),
        RECIEVE_RESPONSE("EIDAS-4");

        private String id;

        AuditMessage(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

    }
}
