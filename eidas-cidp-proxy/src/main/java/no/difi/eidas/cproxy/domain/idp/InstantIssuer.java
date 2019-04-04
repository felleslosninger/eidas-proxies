package no.difi.eidas.cproxy.domain.idp;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
public class InstantIssuer {
    public DateTime now() {
        return new DateTime();
    }
}
