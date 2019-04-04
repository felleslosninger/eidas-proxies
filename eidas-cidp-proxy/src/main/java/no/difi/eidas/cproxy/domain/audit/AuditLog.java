package no.difi.eidas.cproxy.domain.audit;

import no.difi.eidas.cproxy.domain.idp.IdPAuthnResponse;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.difi.opensaml.util.AuthnRequestUtil;
import no.idporten.log.audit.AuditLogger;
import org.opensaml.saml2.core.AuthnRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLog {
    public static final String ingenSsn = "";
    private final AuthnRequestUtil authnRequestUtil;
    private final AuditLogger auditLogger;

    @Autowired
    public AuditLog(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
        this.authnRequestUtil = new AuthnRequestUtil();
    }

    protected String ssn(IdPAuthnResponse idPAuthnResponse) {
        if (idPAuthnResponse != null && idPAuthnResponse.uid() != null) {
            return idPAuthnResponse.uid();
        }
        return ingenSsn;
    }

    protected String ssn(NodeAttributes idPAuthnResponse) {
        if (idPAuthnResponse != null && idPAuthnResponse.get(SubjectBasicAttribute.PersonIdentifier).isPresent()) {
            return idPAuthnResponse.get(SubjectBasicAttribute.PersonIdentifier).value().get();
        }
        return ingenSsn;
    }

    public void responseToPepsOk(byte[] response) {
        auditLogger.log(AuditMessages.responseToPepsOk.id(), ingenSsn, withoutLineEndings(new String(response)));
    }

    public void responseToPepsAccepted(NodeAttributes nodeAttributes) {
        auditLogger.log(
                AuditMessages.responseToPepsAccepted.id(),
                ssn(nodeAttributes),
                withoutLineEndings(nodeAttributes.toString()));
    }

    public void responseToPepsRejected(NodeAttributes nodeAttributes) {
        auditLogger.log(
                AuditMessages.responseToPepsRejected.id(),
                ssn(nodeAttributes),
                withoutLineEndings(nodeAttributes.toString()));
    }

    public void requestToIdPorten(AuthnRequest request) {
        auditLogger.log(AuditMessages.requestToIdPorten.id(), ingenSsn, withoutLineEndings(authnRequestUtil.toXml(request)));
    }

    public void requestFromNode(byte[] request) {
        auditLogger.log(AuditMessages.requestFromPeps.id(), ingenSsn, withoutLineEndings(new String(request)));
    }

    public IdPAuthnResponse idpResponse(IdPAuthnResponse response) {
        auditLogger.log(AuditMessages.responseFromIdPorten.id(), ssn(response), withoutLineEndings(response.getSourceXml()));
        return response;
    }

    protected String withoutLineEndings(String s) {
        return s != null ? s.replaceAll("\\R", "") : s;
    }

    public enum AuditMessages {

        responseToPepsOk("C-IDP-PROXY-1"),
        responseToPepsRejected("C-IDP-PROXY-2"),
        requestToIdPorten("C-IDP-PROXY-3"),
        requestFromPeps("C-IDP-PROXY-4"),
        responseFromIdPorten("C-IDP-PROXY-5"),
        responseToPepsAccepted("C-IDP-PROXY-6");

        private final String id;

        AuditMessages(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}
