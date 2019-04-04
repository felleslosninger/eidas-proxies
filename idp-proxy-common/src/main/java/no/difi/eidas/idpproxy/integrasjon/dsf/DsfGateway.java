package no.difi.eidas.idpproxy.integrasjon.dsf;

import com.google.common.base.Optional;
import no.difi.dsfgateway.DSFResponseResource;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.DsfGatewayRestApi;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DsfGateway {
    private static final Logger log = LoggerFactory.getLogger(DsfGateway.class);
    private final DsfGatewayRestApi dsfGateway;

    DsfGateway(DsfGatewayRestApi dsfGateway) {
        this.dsfGateway = dsfGateway;
    }

    public PersonLookupResult bySsn(String ssn) {
        checkNotNull(ssn, "ssn");
        try {
            return response(
                    dsfGateway.bySsn(ssn)
            );
        } catch(WebApplicationException | ProcessingException e) {
            log.error("Error calling dsf gateway", e);
            return errorResponse();
        }
    }

    public PersonLookupResult byNameAndBirth(String firstName, String lastName, String birth) {
        checkNotNull(firstName, "firstName");
        checkNotNull(lastName, "lastName");
        checkNotNull(birth, "birth");
        try {
            return response(
                    dsfGateway.byNameAndBirth(firstName, lastName, birth)
            );
        } catch(WebApplicationException | ProcessingException e) {
            log.error("Error calling dsf gateway", e);
            return errorResponse();
        }
    }

    private PersonLookupResult response(DSFResponseResource dSFResponse) {
        return new PersonLookupResult(
               status(dSFResponse),
               person(dSFResponse)
        );
    }

    private PersonLookupResult errorResponse() {
        return new PersonLookupResult(
                PersonLookupResult.Status.ERROR,
                Optional.absent()
        );
    }

    private Optional<Person> person(DSFResponseResource dSFResponse) {
        return dSFResponse.getAntallTreff() == 1 ?
                Optional.fromNullable(dSFResponse.getDsfPersonResource())
                        .transform(new PersonMapper()).or(Optional.absent()) :
                Optional.absent();
    }

    private PersonLookupResult.Status status(DSFResponseResource response) {
        return response.getAntallTreff() == 1 || response.getAntallTreff() == 0 ?
                PersonLookupResult.Status.OK : PersonLookupResult.Status.MULTIPLEFOUND;

    }
}
