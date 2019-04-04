package no.difi.eidas.cproxy.integration.node;

import com.google.common.base.Preconditions;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SamlEngineProvider {
    private static final String instanceName = "eidas-cidp-proxy";
    public static final String COUNTRY = "NO";

    @Value("${samlengine.path}")
    private String samlEnginePath;

    @Value("${mf.gateway.url}")
    private String gatewayUrl;

    @Bean
    public ProtocolEngineI engine() {
        return Preconditions.checkNotNull(
                nodeProtocolEngineFactory().getProtocolEngine(instanceName),
                String.format("SAML engine instance \"%s\" failed to initialize", instanceName)
        );
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

}
