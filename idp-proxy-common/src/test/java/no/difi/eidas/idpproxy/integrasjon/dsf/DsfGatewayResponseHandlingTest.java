package no.difi.eidas.idpproxy.integrasjon.dsf;

import no.difi.dsfgateway.DSFPersonResource;
import no.difi.dsfgateway.DSFResponseResource;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.DsfGatewayRestApi;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DsfGatewayResponseHandlingTest {
    private static final String foedselsnr = "01011512345";
    private static final String foedselsDato = "20150101";
    private static final String ssn = "someSsn";

    @Mock
    private DsfGatewayRestApi restApi;

    private DsfGateway dsfGateway;

    @Before
    public void setUp() {
        dsfGateway = new DsfGateway(restApi);
    }

    @Test
    public void testFindBySsn() {
        mockPersonResponse(1);
        PersonLookupResult result = dsfGateway.bySsn(ssn);
        assertThat(result.status(), is(PersonLookupResult.Status.OK));
        assertThat(result.person().isPresent(), is(true));
        assertThat(result.person().get().fødselsnummer(), is(equalTo(foedselsnr)));
        verify(restApi).bySsn(ssn);
    }

    @Test
    public void testReturnsEmptyResultIfPersonNotFoundBySsn() {
        mockPersonResponse(0);
        PersonLookupResult result = dsfGateway.bySsn(ssn);
        assertThat(result.status(), is(PersonLookupResult.Status.OK));
        assertThat(result.person().isPresent(), is(false));
        verify(restApi).bySsn(ssn);
    }

    @Test
    public void testMultiplePeopleFoundReturned() {
        mockPersonResponse(2);
        PersonLookupResult result = dsfGateway.bySsn(ssn);
        assertThat(result.status(), is(PersonLookupResult.Status.MULTIPLEFOUND));
        assertThat(result.person().isPresent(), is(false));
        verify(restApi).bySsn(ssn);
    }

    @Test
    public void testErrorReturned() {
        when(restApi.bySsn(ssn)).thenThrow(new WebApplicationException("Error coming from the rest client"));
        PersonLookupResult result = dsfGateway.bySsn(ssn);
        assertThat(result.status(), is(PersonLookupResult.Status.ERROR));
        assertThat(result.person().isPresent(), is(false));
        verify(restApi).bySsn(ssn);
    }

    private void mockPersonResponse(Integer personCount) {
        DSFResponseResource dsfResponseResource = new DSFResponseResource();
        dsfResponseResource.setAntallTreff(personCount);
        final DSFPersonResource personResource;
        if(personCount == 1) {
            personResource = new DSFPersonResource();
            personResource.setFoedselsdato(foedselsDato);
            personResource.setFoedselsnr(foedselsnr);
            personResource.setSivilstatusKode(-1);
        } else {
            personResource = null;
        }
        dsfResponseResource.setDsfPersonResource(personResource);
        when(restApi.bySsn(ssn)).thenReturn(dsfResponseResource);
    }
}
