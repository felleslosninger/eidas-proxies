package no.difi.eidas.cproxy.integration.oidc;

import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.Nonce;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class OIDCState {

    private State state = new State();
    private Nonce nonce = new Nonce();

    public State getState() {
        return state;
    }

    public Nonce getNonce() {
        return nonce;
    }
}
