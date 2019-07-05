package no.difi.eidas.sproxy.domain.eventlog;

import no.difi.eidas.sproxy.integration.eidas.response.EidasResponse;
import no.idporten.domain.log.LogEntry;
import no.idporten.domain.log.LogEntryLogType;
import no.idporten.log.event.EventLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventLog {

    private final EventLogger eventLogger;
    static final LogEntryLogType EIDAS_INCOMING_AUTHENTICATION = new LogEntryLogType("EIDAS_INCOMING_AUTHENTICATION", "eIDAS incoming authentication (via s-proxy)");

    @Autowired
    public EventLog(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    public void eidasResponse(EidasResponse eidasResponse, String citizenCountryCode) {
        LogEntry logEntry = new LogEntry(EIDAS_INCOMING_AUTHENTICATION);
        logEntry.setPersonIdentifier(eidasResponse.personIdentifier());
        logEntry.setIssuer(citizenCountryCode);
        eventLogger.log(logEntry);
    }

}
