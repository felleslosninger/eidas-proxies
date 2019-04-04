package no.difi.eidas.cproxy.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Component
public class OIDCProperties {

    @Value("${oidc.enable:false}")
    private boolean enabled = false;
    @Value("${oidc.issuerUri:null}")
    private String issuerUri;
    @Value("${oidc.client.id:null}")
    private String clientId;
    @Value("${oidc.client.secret:null}")
    private String clientSecret;
    @Value("${oidc.redirectUri:null}")
    private String redirectUri;
    private OIDCProviderMetadata metadata;
    private JWKSet oidcProviderJWKSet;

    @Autowired
    private RestTemplate restTemplate;

    public boolean isEnabled() {
        return enabled;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public OIDCProviderMetadata getMetadata() {
        return metadata;
    }
    public JWKSet getOIDCProviderJWKSet() {
        return oidcProviderJWKSet;
    }

    @PostConstruct
    public void init() throws ParseException, java.text.ParseException {
        if (!this.enabled) {
            return;
        }

        String oidcConfig = restTemplate.getForObject(
                this.issuerUri + "/.well-known/openid-configuration",
                String.class);

        this.metadata = OIDCProviderMetadata.parse(oidcConfig);
        String jwkSet = restTemplate.getForObject(
                getMetadata().getJWKSetURI(),
                String.class);
        this.oidcProviderJWKSet = JWKSet.parse(jwkSet);
    }
}
