package no.difi.eidas.sproxy.integration.mf;

import com.google.common.base.Optional;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.attribute.AttributesConfigProvider;
import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.integration.eidas.response.EidasResponse;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Country;
import no.difi.eidas.sproxy.web.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class MFService {

    private static final Logger log = LoggerFactory.getLogger(MFService.class);
    private static final PersonLookupResult PERSON_LOOKUP_RESULT_NOT_FOUND = new PersonLookupResult(
            PersonLookupResult.Status.MULTIPLEFOUND,
            Optional.absent());
    private static final String CLIENT_ID_HEADER = "Client-Id";
    private static final String DEFAULT_EIDAS_PERSONIDENTIFIER_FIELD = "PersonIdentifier";

    private static final String MF_PARAM_FOEDSELSDATO = "foedselsdato";
    private static final String MF_PARAM_LANDKODE = "landkode";
    private static final String MF_PARAM_UTENLANDSK_ID = "utenlandskPersonIdentifikasjon";

    private static final String GENERAL_PID_REGEX = ".*\\/.*\\/(.*)";

    private final AttributesConfigProvider attributesConfigProvider;
    private final ObjectFactory<AuthState> authStateObjectFactory;
    private final ConfigProvider configProvider;

    private RestTemplate restTemplate;

    @Autowired
    public MFService(AttributesConfigProvider attributesConfigProvider,
                     ObjectFactory<AuthState> authStateObjectFactory,
                     ConfigProvider configProvider,
                     RestTemplate restTemplate) {
        this.attributesConfigProvider = attributesConfigProvider;
        this.authStateObjectFactory = authStateObjectFactory;
        this.configProvider = configProvider;
        this.restTemplate = restTemplate;
    }

    public PersonLookupResult lookup(EidasResponse eidasResponse) {
        Map<String, String> attributes = eidasResponse.attributes();
        String foedselsdato = convertEidasDateToMFFormat(eidasResponse);
        String countryCodeIsoAlpha2 = authStateObjectFactory.getObject().getCountryCode();
        String countryCodeIsoAlpha3 = convertCountryCode(countryCodeIsoAlpha2);
        String personIdentifikator = getEidasPersonIdentifikator(countryCodeIsoAlpha2, attributes);

        if (foedselsdato != null && personIdentifikator != null) {
            String uriString = UriComponentsBuilder.fromHttpUrl(configProvider.getMfGatewayUrl() + "/eidas/entydig")
                    .queryParam(MF_PARAM_FOEDSELSDATO, foedselsdato)
                    .queryParam(MF_PARAM_LANDKODE, countryCodeIsoAlpha3)
                    .queryParam(MF_PARAM_UTENLANDSK_ID, personIdentifikator)
                    .toUriString();
            String norskPersonIdentifikator = null;
            try {
                HttpHeaders headers = createHttpHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(uriString, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    norskPersonIdentifikator = response.getBody();
                }
            } catch (RuntimeException e) { //RestClientException
                log.error("Failed to match person from eIDAS in MF-Gateway", e);
            }
            if (norskPersonIdentifikator != null) {
                return new PersonLookupResult(
                        PersonLookupResult.Status.OK,
                        Optional.of(Person.builder().fødselsnummer(norskPersonIdentifikator).build()));
            } else {
                // Test users mock, property must be empty in hiera in production to avoid using the mock.
                log.debug("Leter i mock-eidasId: key = -" + personIdentifikator + "-");
                norskPersonIdentifikator = configProvider.eIDASIdentifierDnumbers().get(personIdentifikator);
                log.debug("Fant identifikator? " + (norskPersonIdentifikator != null));
                log.warn("Use person from test mock: " + norskPersonIdentifikator + " found based on " + foedselsdato + "." + personIdentifikator);
                if (norskPersonIdentifikator != null) {
                    return new PersonLookupResult(
                            PersonLookupResult.Status.OK,
                            Optional.of(Person.builder().fødselsnummer(norskPersonIdentifikator).build()));
                }

            }
        }
        return PERSON_LOOKUP_RESULT_NOT_FOUND;
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setBasicAuth(configProvider.getMfGatewayUsername(), configProvider.getMfGatewayPassword());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.add(CLIENT_ID_HEADER, "eidas-sidp");
        return headers;
    }

    /**
     * Based on xsd:date i.e. YYYY-MM-DD format.
     * eu.eidas.auth.engine.core.eidas.spec.NaturalPersonSpec
     * <p>
     * Eidas has dates of format yyyy-MM-dd.
     * MF has format yyyyMMdd.
     *
     * @param eidasResponse
     * @return
     */
    private String convertEidasDateToMFFormat(EidasResponse eidasResponse) {
        String dateOfBirth = eidasResponse.dateOfBirth();
        if (dateOfBirth != null) {
            return dateOfBirth.replace("-", "");
        }
        return dateOfBirth;
    }

    private String getEidasPersonIdentifikator(String countryCode, Map<String, String> attributes) {
        java.util.Optional<Country> countryHolder = attributesConfigProvider.getCountry(new CountryCode(countryCode));

        ExtractionConfig extractionConfig;
        if (countryHolder != null && countryHolder.isPresent()) {
            extractionConfig = new ExtractionConfig(countryHolder.get()).invoke();
        } else {
            extractionConfig = new ExtractionConfig(null).invoke();
        }

        String personIdentifikator;
        if (attributes.get(extractionConfig.pidAttributeName) != null) {
            personIdentifikator = attributes.get(extractionConfig.pidAttributeName);
        } else if (attributes.get(DEFAULT_EIDAS_PERSONIDENTIFIER_FIELD) != null) {
            personIdentifikator = attributes.get(DEFAULT_EIDAS_PERSONIDENTIFIER_FIELD);
        } else {
            return null;
        }

        if (extractionConfig.regex == null) {
            String eidasId = getPidFromCountryRegex(GENERAL_PID_REGEX, "general regex", personIdentifikator);
            return eidasId == null ? personIdentifikator : eidasId;
        } else {
            return getPidFromCountryRegex(extractionConfig.regex, countryCode, personIdentifikator);
        }
    }

    private String getPidFromCountryRegex(String regex, String countryCode, String pid) {
        try {
            Matcher matcher = Pattern.compile(regex).matcher(pid);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (PatternSyntaxException e) {
            log.warn(String.format("Regex in country config %s is invalid", countryCode), e);
        }
        return null;
    }

    String convertCountryCode(String iso2Country) {
        Locale tempLocale = new Locale("no", iso2Country);
        try {
            return tempLocale.getISO3Country();
        } catch (MissingResourceException exception) {
            /* CE is the demo country for this application, it is not a valid ISO 3166 country code, so the conversion from ISO 3166 alpha 2 to ISO 3166 alpha 3 will throw a
             *  MissingResourceException. Since we actually want CE to map to something we need this if-else clause inside the exception handling.
             * */
            if (iso2Country.equalsIgnoreCase("CE"))
                return "CEA";
            else {
                log.error(exception.getLocalizedMessage());
                throw new RuntimeException("Couldn't find 3-letter country code for " + iso2Country);
            }
        }
    }

    private class ExtractionConfig {
        private Country country;
        private String pidAttributeName;
        private String regex;

        ExtractionConfig(Country country) {
            this.country = country;
        }

        ExtractionConfig invoke() {
            pidAttributeName = DEFAULT_EIDAS_PERSONIDENTIFIER_FIELD;
            regex = null;

            if (country != null) {
                Country countryConfig = country;
                if (countryConfig.getPidAttributeName() != null) {
                    pidAttributeName = countryConfig.getPidAttributeName();
                }
                regex = countryConfig.getRegex();
            }
            return this;
        }
    }
}
