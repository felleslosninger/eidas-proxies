package no.difi.eidas.cproxy.domain.event;

import no.difi.eidas.cproxy.domain.idp.IdPAuthnResponse;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.idporten.domain.log.LogEntry;
import no.idporten.domain.log.LogEntryLogType;
import no.idporten.log.event.EventLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventLogTest {

    @Mock
    private EventLogger eventLogger;

    @Mock
    IdPAuthnResponse idPAuthnResponse;

    @Mock
    NodeAuthnRequest nodeAuthnRequest;

    @Captor
    private ArgumentCaptor<LogEntry> logEntryArgumentCaptor;

    @InjectMocks
    private EventLog eventLog;

    @Test
    public void testAttributeConsentOk() {
        final String uid = "07071599919";
        final String country = "SE";
        final String spInstituition = "Slottet";
        when(nodeAuthnRequest.spCountry()).thenReturn(country);
        when(nodeAuthnRequest.spInstitution()).thenReturn(spInstituition);
        NodeAttributes nodeAttributes = NodeAttributes.builder().available(SubjectBasicAttribute.PersonIdentifier, uid).build();
        eventLog.attributeConsentOk(nodeAuthnRequest, nodeAttributes);

        verifyEventLogging(EventLog.EIDAS_ATTRIBUTE_CONSENT_OK, uid, country, spInstituition);
    }

    @Test
    public void testAttributeConsentRejected() {
        final String uid = "07071599919";
        final String country = "DK";
        final String spApplication = "Tivoli";
        SubjectBasicAttribute subjectBasicAttribute = SubjectBasicAttribute.PersonIdentifier;
        NodeAttributes nodeAttributes = NodeAttributes.builder().available(subjectBasicAttribute, uid).build();
        when(nodeAuthnRequest.spCountry()).thenReturn(country);
        when(nodeAuthnRequest.spApplication()).thenReturn(spApplication);

        eventLog.attributeConsentRejected(nodeAuthnRequest, nodeAttributes);

        verifyEventLogging(EventLog.EIDAS_ATTRIBUTE_CONSENT_REJECTED, uid, country, spApplication);
    }

    private void verifyEventLogging(LogEntryLogType logType, String personIdentifikator, String issuer, String onBehalfOf) {
        verify(eventLogger).log(logEntryArgumentCaptor.capture());
        LogEntry logEntry = logEntryArgumentCaptor.getValue();
        assertEquals(logType, logEntry.getLogType());
        assertEquals(personIdentifikator, logEntry.getPersonIdentifierString());
        assertEquals(issuer, logEntry.getIssuer());
        assertEquals(onBehalfOf, logEntry.getOnBehalfOf());
    }

    @Test
    public void testOnBehalfOfUseNull() {
        assertNull(eventLog.onBehalfOf(null, "NA"));
        assertEquals("bar", eventLog.onBehalfOf("", "bar"));
    }

    @Test
    public void testOnBehalfOfUseSpInstitution() {
        assertEquals("foo", eventLog.onBehalfOf("foo", "NA"));
        assertEquals("foo", eventLog.onBehalfOf("foo", "bar"));
    }

    @Test
    public void testOnBehalfOfUseSpApplication() {
        assertEquals("bar", eventLog.onBehalfOf("", "bar"));
        assertEquals("bar", eventLog.onBehalfOf("NA", "bar"));
    }

    @Test
    public void testHasValue() {
        assertFalse(eventLog.hasValue(null));
        assertFalse(eventLog.hasValue(""));
        assertFalse(eventLog.hasValue("NA"));
        assertFalse(eventLog.hasValue("Unknown"));
        assertFalse(eventLog.hasValue("NOT AVAILABLE"));
        assertTrue(eventLog.hasValue("foo"));
        assertTrue(eventLog.hasValue("bar/foo"));
    }

}
