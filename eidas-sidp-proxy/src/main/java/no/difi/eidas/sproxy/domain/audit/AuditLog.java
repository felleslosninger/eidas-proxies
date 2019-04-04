package no.difi.eidas.sproxy.domain.audit;

import no.difi.eidas.sproxy.domain.saml.SamlXml;
import no.difi.eidas.sproxy.integration.eidas.response.EidasSamlResponse;
import no.idporten.log.audit.AuditLogger;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class AuditLog {
    private final AuditLogger auditLogger;

    @Autowired
    public AuditLog(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    public void idPortenSamlAuthnRequest(SamlXml samlXml) {
        auditLogger.log(AuditMessages.idPortenAuthnRequest.id(), null, withoutLineEndings(samlXml.toString()));
    }

    public void idPortenSamlAuthnResponse(SamlXml samlXml) {
        auditLogger.log(AuditMessages.idPortenAuthnResponse.id(), null, withoutLineEndings(samlXml.toString()));
    }

    public void eidasSamlResponse(Response eidasSamlResponse) {
       auditLogger.log(AuditMessages.eidasAuthnResponse.id(), null, withoutLineEndings(XMLHelper.nodeToString(eidasSamlResponse.getDOM().cloneNode(true))));
    }

    public void eidasSamlAuthnRequest(byte[] token) {
        auditLogger.log(AuditMessages.eidasAuthnRequest.id(), null, withoutLineEndings(new String(token, StandardCharsets.UTF_8)));
    }

    protected String withoutLineEndings(String s) {
        return s.replaceAll("\\R", "");
    }

    private enum AuditMessages {
        idPortenAuthnRequest("S-IDP-PROXY-1"),
        idPortenAuthnResponse("S-IDP-PROXY-2"),
        eidasAuthnRequest("S-IDP-PROXY-3"),
        eidasAuthnResponse("S-IDP-PROXY-4");

        private final String id;

        AuditMessages(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

}
