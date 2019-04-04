package no.difi.eidas.sproxy.domain.saml;

import java.util.Objects;

public class SamlXml {
    private final String samlXml;

    public SamlXml(String samlXml) {
        Objects.requireNonNull(samlXml);
        this.samlXml = samlXml;
    }

    @Override
    public String toString() {
        return samlXml;
    }
}
