package no.difi.eidas.sproxy.integration.mf;

import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.attribute.AttributesConfigProvider;
import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.integration.eidas.response.EidasResponse;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Country;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.PidType;
import no.difi.eidas.sproxy.web.AuthState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MFServiceTest {

    private static final String TEST_PID = "05068907693";
    private static final String TEST_EIDAS_PERSON_IDENTIFIER = "CE/NO/05061989";
    private static final String TEST_SHORT_EIDAS_PERSON_IDENTIFIER = "05061989";
    private static final String TEST_SHORT_CHARACTER_EIDAS_PERSON_IDENTIFIER = "UTENLANDSK_IDENTIFIKASJONS_NUMMER";
    private static final String EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME = "PersonIdentifier";
    private static final String MF_URL = "http://localhost:8080/eidas/entydig";
    @Mock
    private ConfigProvider configProvider;

    @Mock
    private AttributesConfigProvider attributesConfigProvider;

    @Mock
    private AuthState authState;

    @Mock
    private RestTemplate restTemplate;

    private MFService mfService;

    @Before
    public void before() {
        reset(configProvider, attributesConfigProvider, authState);
        mfService = new MFService(attributesConfigProvider, () -> authState, configProvider, restTemplate);
        when(authState.getCountryCode()).thenReturn("CE");
        when(configProvider.getMfGatewayUrl()).thenReturn("http://localhost:8080");
    }

    @Test
    public void noCountryConfigWithNoMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.empty());
        Map<String, String> attributes = new HashMap<>();
        when(restTemplate.getForObject(MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=test", String.class))
                .thenReturn(null);
        attributes.put(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME, "test");
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();
        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyUnsuccessfulLookup(lookup);
    }

    @Test
    public void noCountryConfigWithMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.empty());
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=05061989";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(TEST_PID);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", TEST_EIDAS_PERSON_IDENTIFIER);
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifySuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithNoSpecialExtractionRulesNoMatchTest() {
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=test";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(null);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifyUnsuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithNoSpecialExtractionRulesMatchTest() {
        countryConfigWithGivenEidasIdTest(TEST_EIDAS_PERSON_IDENTIFIER,
                "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=05061989");
    }

    @Test
    public void countryConfigWithShortEidasIdTest() {
        countryConfigWithGivenEidasIdTest(TEST_SHORT_EIDAS_PERSON_IDENTIFIER,
                "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=05061989");
    }

    @Test
    public void countryConfigWithShortEidasIdentifikator() {
        countryConfigWithGivenEidasIdTest(TEST_SHORT_CHARACTER_EIDAS_PERSON_IDENTIFIER,
                "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=UTENLANDSK_IDENTIFIKASJONS_NUMMER");
    }

    public void countryConfigWithGivenEidasIdTest(String eidasIdentifikator, String url) {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, null, null, null)));
        String expectedUrl = MF_URL + url;
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(TEST_PID);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", eidasIdentifikator);
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifySuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithExtractionAttributeNullNoMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", null, null)));
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=test";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(null);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifyUnsuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithExtractionAttributeNullFallbackMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", null, null)));
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=05061989";
        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(TEST_PID);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", TEST_EIDAS_PERSON_IDENTIFIER);
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifySuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithExtractionAttributeNotNullNoMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", null, null)));
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=test";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(null);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("test", "test");
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifyUnsuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithExtractionAttributeMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", null, null)));
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=05061989";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(TEST_PID);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        attributes.put("test", TEST_EIDAS_PERSON_IDENTIFIER);
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifySuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithInvalidRegexTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", null, "(.*)")));
        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        attributes.put("test", "test");
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyUnsuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithExtractionAttributeAndRegExNoMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", null, "(.*)")));
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=test";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(null);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        attributes.put("test", "test");
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifyUnsuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithExtractionAttributeAndRegExMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", null, "(.*)")));

        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=CE/NO/05061989";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(TEST_PID);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        attributes.put("test", TEST_EIDAS_PERSON_IDENTIFIER);
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifySuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithExtractionAttributeAndRegExAndPidTypeNoMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", PidType.SOCIAL_SECURITY_NUMBER, "(.*)")));
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=05068907693";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(null);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        attributes.put("test", TEST_PID);
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifyUnsuccessfulLookup(lookup);
    }

    @Test
    public void testUsingMockWhenMFResponseThrowsException() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", PidType.SOCIAL_SECURITY_NUMBER, "(.*)")));
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=05068907693";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenThrow(new RestClientException("MF is down"));

        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        attributes.put("test", TEST_PID);
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifyUnsuccessfulLookup(lookup);
    }

    @Test
    public void countryConfigWithExtractionAttributeAndRegExAndPidTypeMatchTest() {
        when(attributesConfigProvider.getCountry(any())).thenReturn(Optional.of(new Country("CE", "Test", null, "test", PidType.SOCIAL_SECURITY_NUMBER, "(.*)")));
        String expectedUrl = MF_URL + "?foedselsdato=19550215&landkode=CEA&utenlandskPersonIdentifikasjon=CE/NO/05061989";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(TEST_PID);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("PersonIdentifier", "test");
        attributes.put("test", TEST_EIDAS_PERSON_IDENTIFIER);
        EidasResponse searchParameters = EidasResponse.builder().dateOfBirth("1955-02-15")
                .attributes(attributes)
                .build();

        PersonLookupResult lookup = mfService.lookup(searchParameters);
        verifyRestTemplateArgument(expectedUrl);
        verifySuccessfulLookup(lookup);
    }

    private void verifySuccessfulLookup(PersonLookupResult lookup) {
        assertTrue(lookup.person().isPresent());
        assertEquals(TEST_PID, lookup.person().get().fødselsnummer());
        assertEquals(PersonLookupResult.Status.OK, lookup.status());
    }

    private void verifyUnsuccessfulLookup(PersonLookupResult lookup) {
        assertFalse(lookup.person().isPresent());
        assertEquals(PersonLookupResult.Status.MULTIPLEFOUND, lookup.status());
    }

    private void verifyRestTemplateArgument(String expectedUrl) {
        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForObject(argCaptor.capture(), eq(String.class));
        assertEquals(expectedUrl, argCaptor.getValue());
    }

    @Test
    public void verifyISOCountryCodeCovertionCRreturnsCRI(){
        assertEquals("CRI",mfService.convertCountryCode("CR"));
    }

    @Test //this is Demo country
    public void verifyISOCountryCodeCovertionCEreturnsCEA(){
        assertEquals("CEA",mfService.convertCountryCode("CE"));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    @Test
    public void shouldThrowRuntimeExceptionWhenCountryCodeIsIllegal() throws RuntimeException {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Couldn't find 3-letter country code for XX");
        mfService.convertCountryCode("XX");
    }


}
