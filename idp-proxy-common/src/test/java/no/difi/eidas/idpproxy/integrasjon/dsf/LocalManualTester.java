package no.difi.eidas.idpproxy.integrasjon.dsf;

import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import org.junit.Test;

import static no.difi.eidas.idpproxy.integrasjon.Urls.url;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LocalManualTester {
    private static final String localUrl = "http://eid-vag-admin.difi.local:10009/dsf-gateway";
    private static final String clientId = "IdpProxy";
    private static final String ssn = "01017100552";
    private static final String personNr = "01017100552";
    private static final String firstName = "Harald";
    private static final String lastName = "Krane";
    private static final String birth = "010171";

    @Test
    public void lookupBySsn() {
        PersonLookupResult result = dsfGateway().bySsn(ssn);
        assertResultOfSuccessfulLookup(result);
    }

    @Test
    public void lookupByName() {
        PersonLookupResult result = dsfGateway().byNameAndBirth(firstName, lastName, birth);
        assertResultOfSuccessfulLookup(result);
    }

    private void assertResultOfSuccessfulLookup(PersonLookupResult result) {
        assertThat(result.status(), is(PersonLookupResult.Status.OK));
        assertThat(result.person().isPresent(), is(true));
        assertThat(result.person().get().fødselsnummer(), is(equalTo(personNr)));
    }

    private DsfGateway dsfGateway() {
        return new DsfGatewayBuilder(
                url(localUrl),
                clientId,
                10000
        ).retry(5).build();
    }
}
