package no.difi.eidas.sproxy.domain.attribute;

import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Attribute;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.CountriesAttributes;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Country;

import java.util.*;
import java.util.stream.Collectors;

public class AttributesConfig {
    private final Map<CountryCode, List<Attribute>> countrySpecifics;
    private final Set<Attribute> defaults;
    private final Set<Country> countries;

    public AttributesConfig(CountriesAttributes countriesAttributes) {
        this.countrySpecifics = countrySpecificAttributes(countriesAttributes);
        this.defaults = unmodifiableSet(countriesAttributes.defaultAttributes());
        this.countries = unmodifiableSet(countriesAttributes.countries());
    }

    public Set<Attribute> forCountry(CountryCode country) {
        return countrySpecifics.containsKey(country) ?
                union(defaults, countrySpecifics.get(country)) :
                defaults;
    }

    public Set<Attribute> forCountryExcludingCommonDefault(CountryCode country) {
        return countrySpecifics.containsKey(country) ?
                unmodifiableSet(countrySpecifics.get(country)) :
                Collections.emptySet();
    }

    public Set<Country> countries() {
        return new TreeSet<>(countries);
    }

    private Map<CountryCode, List<Attribute>> countrySpecificAttributes(CountriesAttributes countriesAttributes) {
        return countriesAttributes.countries().stream().collect(Collectors.toMap(Country::countryCode, Country::attributes));
    }

    protected <T> Set<T> unmodifiableSet(Collection<T> collection) {
        Objects.requireNonNull(collection);
        return Collections.unmodifiableSet(new HashSet(collection));
    }

    protected <T> Set<T> union(Collection<T> collection1, Collection<T> collection2) {
        Set<T> set = new HashSet<T>(collection1);
        set.addAll(collection2);
        return set;
    }


}
