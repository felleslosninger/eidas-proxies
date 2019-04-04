package no.difi.eidas.sproxy.integration.eidas.request;

import java.net.URL;
import java.util.Objects;

public class EidasAuthentication {
    private final EidasAuthnRequest authnRequest;
    private final URL eidasNodeUrl;

    public EidasAuthentication(
            EidasAuthnRequest authnRequest,
            URL eidasNodeUrl) {
        Objects.requireNonNull(authnRequest);
        Objects.requireNonNull(eidasNodeUrl);
        this.authnRequest = authnRequest;
        this.eidasNodeUrl = eidasNodeUrl;
    }

    public URL eidasNodeUrl() {
        return eidasNodeUrl;
    }

    public EidasAuthnRequest authnRequest() {
        return authnRequest;
    }
}
