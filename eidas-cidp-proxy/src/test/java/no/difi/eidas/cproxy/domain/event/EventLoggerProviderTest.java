package no.difi.eidas.cproxy.domain.event;

import no.difi.eidas.cproxy.config.ConfigProvider;
import no.idporten.log.event.EventLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventLoggerProviderTest {

    @Mock
    private ConfigProvider configProvider;

    @InjectMocks
    private EventLoggerProvider eventLoggerProvider;

    @Test
    public void getEventLogger() {
        when(configProvider.jmsUrl()).thenReturn("jmsUrl");
        when(configProvider.jmsQueueName()).thenReturn("jmsQueue");
        final EventLogger eventLogger = eventLoggerProvider.eventLogger(configProvider);
        assertNotNull(eventLogger);
        verify(configProvider).jmsUrl();
        verify(configProvider).jmsQueueName();
    }
}
