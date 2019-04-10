package no.difi.eidas.cproxy;

import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractBaseTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
    @AutoConfigureMockMvc
    @ContextConfiguration(classes = Configuration.class)
    public @interface EidasCproxyTest {
    }

    @TestConfiguration
    public abstract static class Configuration {

        @Bean
        @Primary
        public MessageSource messageSource() {
            return mock(MessageSource.class);
        }

        @Bean
        @Primary
        public RestTemplate restTemplate(@Value("${oidc.issuerUri}") String issuerUri) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, URISyntaxException {
            RestTemplate restTemplate = mock(RestTemplate.class);
            String jwk = OIDCTestKeyProvider.getJWSJSON();
            when(restTemplate.getForObject(
                    eq(new URI("https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/jwk")),
                    eq(String.class)))
                    .thenReturn(jwk);
            when(restTemplate.getForObject(
                    eq(issuerUri + "/.well-known/openid-configuration"),
                    eq(String.class)))
                    .thenReturn("{\"issuer\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/\",\"authorization_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/authorize\",\"token_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/token\",\"end_session_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/endsession\",\"revocation_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/revoke\",\"jwks_uri\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/jwk\",\"response_types_supported\":[\"code\"],\"subject_types_supported\":[\"pairwise\"],\"userinfo_endpoint\":\"https://eid-test-oidc-provider.difi.no/idporten-oidc-provider/userinfo\",\"scopes_supported\":[\"openid\",\"profile\"],\"ui_locales_supported\":[\"nb\",\"nn\",\"en\",\"se\"]}");
            return restTemplate;
        }

    }

}
