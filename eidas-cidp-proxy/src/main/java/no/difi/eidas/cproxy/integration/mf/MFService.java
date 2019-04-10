package no.difi.eidas.cproxy.integration.mf;

import com.google.common.base.Optional;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.integration.mf.json.FolkeregisterPerson;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MFService {

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
            String uriString = UriComponentsBuilder.fromHttpUrl(configProvider.getMfGatewayUrl() + "/person")
                    .pathSegment(uid)
                    .toUriString();
            FolkeregisterPerson person = null;
            try {
                person = restTemplate.getForObject(uriString, FolkeregisterPerson.class);
            } catch (RuntimeException e) { //RestClientException
                log.error("Failed to match person from eIDAS in MF-Gateway", e);
            }
            if (person != null
                    && person.getGjeldendeIdentifikasjonsnummer().isPresent()
                    && person.getGjeldendeNavn().isPresent()
                    && person.getGjeldendeFoedsel().isPresent()) {
                return new PersonLookupResult(
                        PersonLookupResult.Status.OK,
                        Optional.of(Person.builder()
                                .fødselsnummer(person.getGjeldendeIdentifikasjonsnummer().get().getFoedselsEllerDNummer())
                                .etternavn(person.getGjeldendeNavn().get().getEtternavn())
                                .fornavn(person.getGjeldendeNavn().get().getFornavn())
                                .fødselsdato(person.getGjeldendeFoedsel().get().getFoedselsdatoAsLocalDate())
                                .fødeland(person.getGjeldendeFoedsel().get().getFoedeland())
                                .build()));
            }
        }
        return PERSON_LOOKUP_RESULT_NOT_FOUND;
    }

}
