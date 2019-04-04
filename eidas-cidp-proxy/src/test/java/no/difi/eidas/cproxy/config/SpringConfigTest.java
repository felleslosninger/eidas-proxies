package no.difi.eidas.cproxy.config;

import no.difi.eidas.cproxy.OIDCTestKeyProvider;
import no.difi.eidas.cproxy.web.CitizenController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes={SpringConfigTest.Config.class})
public class SpringConfigTest {

    @Configuration
    @Import(SpringConfig.class)
    public static class Config {
        @Bean
        @Primary
        public RestTemplate restTemplate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, URISyntaxException {
            RestTemplate restTemplate = mock(RestTemplate.class);
            String jwk = OIDCTestKeyProvider.getJWSJSON();
            when(restTemplate.getForObject(
                    eq(new URI("https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/jwk")),
                    eq(String.class)))
                    .thenReturn(jwk);
            when(restTemplate.getForObject(anyString(), eq(String.class)))
                    .thenReturn("{\"issuer\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/\",\"authorization_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/authorize\",\"token_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/token\",\"end_session_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/endsession\",\"revocation_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/revoke\",\"jwks_uri\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/jwk\",\"response_types_supported\":[\"code\"],\"subject_types_supported\":[\"pairwise\"],\"userinfo_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/userinfo\",\"scopes_supported\":[\"openid\",\"profile\"],\"ui_locales_supported\":[\"nb\",\"nn\",\"en\",\"se\"]}");
            return restTemplate;
        }
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CitizenController controller;

    @Test
    public void testControllerCreated() {
        assertNotNull(controller);

    }
}
