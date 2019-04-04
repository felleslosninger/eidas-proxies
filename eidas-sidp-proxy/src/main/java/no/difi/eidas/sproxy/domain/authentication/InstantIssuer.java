package no.difi.eidas.sproxy.domain.authentication;

import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class InstantIssuer {
    public ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}
