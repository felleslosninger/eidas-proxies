package no.difi.eidas.cproxy.integration.idporten;

import no.difi.eidas.cproxy.domain.idp.IdPAuthnResponse;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;

public class ResponseData {

    private final IdPAuthnResponse idPAuthnResponse;
    private final Person person;

    public ResponseData(IdPAuthnResponse idPAuthnResponse, Person person) {
        this.idPAuthnResponse = idPAuthnResponse;
        this.person = person;
    }

    public IdPAuthnResponse getIdPAuthnResponse() {
        return idPAuthnResponse;
    }

    public Person getPerson() {
        return person;
    }
}
