package no.difi.eidas.cproxy.domain.authentication;

import com.google.common.base.Preconditions;
import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.domain.event.EventLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsentHandler {
    private EventLog eventLog;
    private AuditLog auditLog;

    @Autowired
    public ConsentHandler(EventLog eventLog, AuditLog auditLog) {
        this.eventLog = eventLog;
        this.auditLog = auditLog;
    }

    public void accept(AuthenticationContext context) {
        Preconditions.checkNotNull(context.assembledAttributes(), "assembled attributes not in AuthenticationContext");
        Preconditions.checkNotNull(context.nodeRequest(), "pepsAuthnRequest not in AuthenticationContext");
        eventLog.attributeConsentOk(context.nodeRequest(), context.assembledAttributes());
        auditLog.responseToPepsAccepted(context.assembledAttributes());
    }

    public void reject(AuthenticationContext context) {
        Preconditions.checkNotNull(context.assembledAttributes(), "assembled attributes not in AuthenticationContext");
        Preconditions.checkNotNull(context.nodeRequest(), "pepsAuthnRequest not in AuthenticationContext");
        eventLog.attributeConsentRejected(context.nodeRequest(), context.assembledAttributes());
        auditLog.responseToPepsRejected(context.assembledAttributes());
    }
}
