package no.difi.eidas.idpproxy.integrasjon.dsf;

import org.junit.Test;

import static no.difi.eidas.idpproxy.integrasjon.Urls.url;

public class DsfGatewayBuilderTest {
    private static final String url = "http://localhost/dsfGateway";
    private static final String clientId = "eidas-sidp-proxy";

    @Test
    public void build() {
        new DsfGatewayBuilder(
                url(url),
                clientId,
                5000
        ).build();
    }

    @Test
    public void withRetry() {
        new DsfGatewayBuilder(
                url(url),
                clientId,
                5000
        ).retry(5)
        .build();
    }
}
