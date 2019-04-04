package no.difi.eidas.cproxy.integration.dsf;

import no.difi.dsfgateway.DsfClientId;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.idpproxy.integrasjon.dsf.DsfGateway;
import no.difi.eidas.idpproxy.integrasjon.dsf.DsfGatewayBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DsfGatewayProvider {
    @Bean
    @Scope
    public DsfGateway dsfGateway(ConfigProvider configProvider) {
        return new DsfGatewayBuilder(
                configProvider.dsfGatewayUrl(),
                DsfClientId.IdpProxy.getClientName(),
                configProvider.dsfGatewayTimeout()
        ).retry(configProvider.dsfRetryCount())
                .build();
    }
}