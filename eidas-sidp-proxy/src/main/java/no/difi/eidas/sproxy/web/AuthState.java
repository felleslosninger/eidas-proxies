package no.difi.eidas.sproxy.web;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthState {

    private List<String> identityMatch = Arrays.asList("UNAMBIGUOUS");
    private String countryCode;

    public List<String> getIdentityMatch() {
        return identityMatch;
    }

    public void setIdentityMatch(List<String> identityMatch) {
        this.identityMatch = identityMatch;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
