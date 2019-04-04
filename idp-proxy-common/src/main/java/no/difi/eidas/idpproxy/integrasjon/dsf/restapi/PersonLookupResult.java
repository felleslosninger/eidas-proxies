package no.difi.eidas.idpproxy.integrasjon.dsf.restapi;

import com.google.common.base.Optional;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;

public class PersonLookupResult {
    private Status status;
    private Optional<Person> person;

    public PersonLookupResult(Status status, Optional<Person> person) {
        this.status = status;
        this.person = person;
    }

    public Status status() {
        return status;
    }

    public Optional<Person> person() {
        return person;
    }

    public enum Status {
        OK("OK"), MULTIPLEFOUND("FLERETREFF"), ERROR("SYSTEMFEIL"), NODSFSEARCH("IKKESJEKKET") ;

        private final String attributeValue;

        Status(String attributeValue) {
            this.attributeValue = attributeValue;
        }

        public String value() {
            return attributeValue;
        }
    }
}
