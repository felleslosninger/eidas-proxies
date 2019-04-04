package no.difi.eidas.sproxy.integration.fileconfig.attribute;

import no.difi.eidas.sproxy.domain.country.CountryCode;

import java.util.List;

public class Country implements Comparable<Country>{

    private final String countryCode;
    private final String countryName;
    private final List<Attribute> attributes;
    private final String pidAttributeName;
    private final PidType pidType;
    private final String regex;


    public Country(String countryCode, String countryName, List<Attribute> attributes, String pidAttributeName, PidType pidType, String regex) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.attributes = attributes;
        this.pidAttributeName = pidAttributeName;
        this.pidType = pidType;
        this.regex = regex;
    }

    public CountryCode countryCode() {
        return new CountryCode(countryCode);
    }

    public String countryName() {
        return countryName;
    }

    public List<Attribute> attributes() {
        return attributes;
    }

    public String getPidAttributeName() {
        return pidAttributeName;
    }

    public PidType getPidType() {
        return pidType;
    }

    public String getRegex() {
        return regex;
    }

    @Override
	public int compareTo(Country country) {
		return this.countryName.compareTo(country.countryName);
	}
}

