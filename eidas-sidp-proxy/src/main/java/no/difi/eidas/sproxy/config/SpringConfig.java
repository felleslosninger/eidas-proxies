package no.difi.eidas.sproxy.config;

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory;
import no.difi.opensaml.util.ConvertUtil;
import no.difi.opensaml.util.SAMLUtil;
import no.idporten.log.audit.AuditLogger;
import no.idporten.log.audit.AuditLoggerELFImpl;
import no.idporten.log.elf.ELFWriter;
import no.idporten.log.elf.FileRollerDailyImpl;
import no.idporten.log.elf.WriterCreator;
import no.idporten.log.event.EventLogger;
import no.idporten.log.event.EventLoggerImpl;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.jms.ConnectionFactory;

@Configuration
@EnableWebMvc
public class SpringConfig implements WebMvcConfigurer {
    public static final String EIDAS_ENGINE = "eidas-sidp-proxy";

    @Value("${samlengine.path}")
    private String samlEnginePath;

    @Value("${mf.gateway.url}")
    private String gatewayUrl;

    @Value("${message.path}")
    private String messagePath;

    @Bean
    public SAMLUtil samlUtil() {
        return new SAMLUtil();
    }

    @Bean
    @Scope
    public ConvertUtil convertUtil() {
        return new ConvertUtil();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webContentInterceptor());
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setViewClass(JstlView.class);
        return viewResolver;
    }


    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(messagePath + "/messages/eidas-sidp-proxy-messages");
        messageSource.setDefaultEncoding("ISO-8859-1");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setCacheSeconds(0);
        return messageSource;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/fonts/**").addResourceLocations("/fonts/");
        registry.addResourceHandler("/images/**").addResourceLocations("/images/");
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
    }

    @Bean
    public WebContentInterceptor webContentInterceptor() {
        WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
        webContentInterceptor.setUseExpiresHeader(true);
        webContentInterceptor.setCacheSeconds(0);
        webContentInterceptor.setUseCacheControlHeader(true);
        webContentInterceptor.setUseCacheControlNoStore(true);
        return webContentInterceptor;
    }


    @Bean
    public AuditLogger auditLogger(ConfigProvider config) {
        ELFWriter elfWriter = new ELFWriter(
                new FileRollerDailyImpl(config.auditLogDir(), config.auditLogFile()),
                new WriterCreator()
        );
        AuditLoggerELFImpl logger = new AuditLoggerELFImpl();
        logger.setELFWriter(elfWriter);
        logger.setDataSeparator(config.auditLogDataSeparator());
        return logger;
    }

    @Bean
    @Scope
    @Qualifier(EIDAS_ENGINE)
    public ProtocolEngineI engine() {
        return nodeProtocolEngineFactory().getProtocolEngine(EIDAS_ENGINE);
    }

    public ProtocolEngineConfigurationFactory nodeSamlEngineConfigurationFactory() {
        return new ProtocolEngineConfigurationFactory("SamlEngine.xml", null, samlEnginePath);
    }

    public ProtocolEngineFactory nodeProtocolEngineFactory() {
        try {
            return new ProtocolEngineFactory(nodeSamlEngineConfigurationFactory());
        } catch (SamlEngineConfigurationException e) {
            throw new RuntimeException("Failed to load SamlEngine configuration for eIDAS Node", e);
        }
    }


    @Bean
    public EventLogger eventLogger(ConfigProvider config) {
        ActiveMQQueue destination = new ActiveMQQueue(config.jmsQueueName());
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(jmsConnectionFactory(config.jmsUrl()));
        jmsTemplate.setDefaultDestination(destination);
        return new EventLoggerImpl(jmsTemplate);
    }

    public ConnectionFactory jmsConnectionFactory(final String jmsUrl) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(jmsUrl);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(10);
        return cachingConnectionFactory;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
