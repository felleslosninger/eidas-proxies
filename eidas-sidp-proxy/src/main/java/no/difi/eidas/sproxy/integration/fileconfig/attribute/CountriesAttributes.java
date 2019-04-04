package no.difi.eidas.sproxy.integration.fileconfig.attribute;

import java.util.List;

public class CountriesAttributes {
    private final List<Attribute> defaultAttributes;
    private final List<Country> countries;

    public CountriesAttributes(List<Attribute> defaultAttributes, List<Country> countries) {
        this.defaultAttributes = defaultAttributes;
        this.countries = countries;
    }

    public List<Attribute> defaultAttributes() {
        return defaultAttributes;
    }

    public List<Country> countries() {
        return countries;
    }


}
