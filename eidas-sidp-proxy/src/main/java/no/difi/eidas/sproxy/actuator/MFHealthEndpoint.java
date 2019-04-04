package no.difi.eidas.sproxy.actuator;

import no.difi.eidas.sproxy.config.ConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MFHealthEndpoint implements HealthIndicator {

    private ConfigProvider configProvider;

    private RestTemplate restTemplate;

    @Autowired
    public MFHealthEndpoint(RestTemplate restTemplate, ConfigProvider configProvider) {
        this.configProvider = configProvider;
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            String url = configProvider.getMfGatewayUrl();
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url + "/health", String.class);
            if (responseEntity.getBody().contains("UP")) {
                return Health.up().build();
            } else {
                return Health.down()
                        .withDetail("message", "Feil fra MF-Gateway")
                        .withDetail("url", url)
                        .build();
            }
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("message", "Når ikke MF-Gateway")
                    .build();
        }
    }

}
