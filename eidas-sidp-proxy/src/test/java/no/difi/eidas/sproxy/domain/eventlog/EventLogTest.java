package no.difi.eidas.sproxy.domain.eventlog;

import no.difi.eidas.sproxy.integration.eidas.response.EidasResponse;
import no.idporten.domain.log.LogEntry;
import no.idporten.domain.log.LogType;
import no.idporten.log.event.EventLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EventLogTest {

    @Mock
    private EventLogger eventLogger;

    @Captor
    private ArgumentCaptor<LogEntry> logEntryArgumentCaptor;

    @InjectMocks
    private EventLog eventLog;


    @Test
    public void testLogEidasResponse() {
        final String eidasEidentifier = "FI/NN/land";
        final String country = "fi";
        final EidasResponse eidasResponse = EidasResponse.builder()
                .personIdentifier(eidasEidentifier)
                .build();
        eventLog.eidasResponse(eidasResponse, country);
        verify(eventLogger).log(logEntryArgumentCaptor.capture());
        assertEquals(EventLog.EIDAS_INCOMING_AUTHENTICATION, logEntryArgumentCaptor.getValue().getLogType());
        assertEquals(eidasEidentifier, logEntryArgumentCaptor.getValue().getPersonIdentifierString());
        assertEquals(country, logEntryArgumentCaptor.getValue().getIssuer());
        assertNull(logEntryArgumentCaptor.getValue().getOnBehalfOf());
    }

}
