package no.difi.eidas.cproxy.integration.mf;

import com.google.common.base.Optional;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import no.difi.eidas.idpproxy.integrasjon.mf.MFPersonResource;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
public class MFService {
    private static final String CLIENT_ID_HEADER = "Client-Id";
    private static final Logger log = LoggerFactory.getLogger(MFService.class);
    private static final PersonLookupResult PERSON_LOOKUP_RESULT_NOT_FOUND = new PersonLookupResult(
            PersonLookupResult.Status.MULTIPLEFOUND,
            Optional.absent());

    private final ConfigProvider configProvider;

    private RestTemplate restTemplate;

    @Autowired
    public MFService(ConfigProvider configProvider,
                     RestTemplate restTemplate) {
        this.configProvider = configProvider;
        this.restTemplate = restTemplate;
    }

    public PersonLookupResult lookup(String uid) {
        if (uid != null) {
            String uriString = UriComponentsBuilder.fromHttpUrl(configProvider.getMfGatewayUrl() + "/person/eidas")
                    .pathSegment(uid)
                    .toUriString();
            MFPersonResource person = null;
            try {
                HttpHeaders headers = createHttpHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);
                log.debug("URl. " + uriString);
                ResponseEntity<MFPersonResource> response = restTemplate.exchange(uriString, HttpMethod.GET, entity, MFPersonResource.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    log.debug("status ok " + response.getBody());
                    person = response.getBody();
                }
            } catch (RuntimeException e) { //RestClientException
                log.error("Failed to match person from eIDAS in MF-Gateway", e);
            }
            if (person != null
                    && person.getPersonIdentifikator() != null
                    && person.getNavn().getForkortetNavn() != null
                    && person.getFoedselsdato() != null) {
                return new PersonLookupResult(
                        PersonLookupResult.Status.OK,
                        Optional.of(Person.builder()
                                .fødselsnummer(person.getPersonIdentifikator())
                                .etternavn(person.getNavn().getEtternavn())
                                .fornavn(person.getNavn().getFornavn())
                                .fødselsdato(parseFodselsdatoToLocalDate(person.getFoedselsdato()))
                                .fødeland(person.getFoedested())
                                .build()));
            }
        }
        return PERSON_LOOKUP_RESULT_NOT_FOUND;
    }

    private LocalDate parseFodselsdatoToLocalDate(String fodselsdato) {
        return LocalDate.parse(fodselsdato, DateTimeFormat.forPattern("YYYY-MM-dd"));
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setBasicAuth(configProvider.getMfGatewayUsername(), configProvider.getMfGatewayPassword());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.add(CLIENT_ID_HEADER, "eidas-cidp");
        return headers;
    }


}
