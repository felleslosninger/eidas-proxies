package no.difi.eidas.cproxy.domain.event;

import no.difi.eidas.cproxy.config.ConfigProvider;
import no.idporten.log.event.EventLogger;
import no.idporten.log.event.EventLoggerImpl;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

@Configuration
public class EventLoggerProvider {

    @Bean
    public EventLogger eventLogger(final ConfigProvider config) {
        ActiveMQQueue destination = new ActiveMQQueue(config.jmsQueueName());
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(jmsConnectionFactory(config.jmsUrl()));
        jmsTemplate.setDefaultDestination(destination);
        return new EventLoggerImpl(jmsTemplate);
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory(final String jmsUrl) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(jmsUrl);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(10);
        return cachingConnectionFactory;
    }

}
