package no.difi.eidas.idpproxy.integrasjon.dsf;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.difi.eidas.idpproxy.test.ResourceReader;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static no.difi.eidas.idpproxy.integrasjon.Urls.url;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DsfGatewayIntegrationTest {
    private static final Integer port = 8089;
    private static final String host = "localhost";
    private static final String contextPath = "dsf-gateway";
    private static final Integer timeout = 5000;
    private static final String clientId = "IdpProxy";
    private static final String ssn = "01017100552";
    private static final String firstName = "Harald";
    private static final String lastName = "Krane";
    private static final String birth = "010171";

    private static final String personNr = "00552";


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(port);

    @Test
    public void bySsn() {
        stubFor(get(urlEqualTo(String.format("/%s/dsf/%s", contextPath, ssn)))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Client-Id", equalTo(clientId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ResourceReader.read("dsfGatewayResponse.json"))));
        assertPersonSuccessfullyFound(
                dsfGateway().bySsn(ssn)
        );
    }

    @Test
    public void byNameAndBirth() {
        stubFor(get(urlEqualTo(String.format("/%s/dsf/%s/%s/%s", contextPath, firstName, lastName, birth)))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Client-Id", equalTo(clientId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ResourceReader.read("dsfGatewayResponse.json"))));
        assertPersonSuccessfullyFound(
                dsfGateway().byNameAndBirth(firstName, lastName, birth)
        );
    }

    @Test
    public void errorResponseOnTimeoutBySsn() {
        PersonLookupResult result = new DsfGatewayBuilder(
                url("http://" + host + ":" + port + "/" + contextPath),
                clientId,
                timeout)
                .build().bySsn("234234");
        assertThat(result.status(), is(PersonLookupResult.Status.ERROR));
    }

    @Test
    public void errorResponseOnTimeoutByName() {
        PersonLookupResult result = new DsfGatewayBuilder(
                url("http://" + host + ":" + port + "/" + contextPath),
                clientId,
                timeout)
                .build().byNameAndBirth(firstName, lastName, birth);
        assertThat(result.status(), is(PersonLookupResult.Status.ERROR));
    }

    private DsfGateway dsfGateway() {
        return new DsfGatewayBuilder(
                url("http://" + host + ":" + port + "/" + contextPath),
                clientId,
                timeout)
                .build();
    }

    private void assertPersonSuccessfullyFound(PersonLookupResult result) {
        assertThat(result.status(), is(PersonLookupResult.Status.OK));
        assertThat(result.person().isPresent(), is(true));
        assertThat(result.person().get().fødselsnummer(), is(CoreMatchers.equalTo(birth + personNr)));
    }
}
