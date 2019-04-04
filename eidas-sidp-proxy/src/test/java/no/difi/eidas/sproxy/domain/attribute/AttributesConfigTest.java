package no.difi.eidas.sproxy.domain.attribute;

import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Attribute;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.CountriesAttributes;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Country;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.PidType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AttributesConfigTest {
    private static final Attribute defaultAttribute = new Attribute("default", true);
    private static final Attribute swedishAttribute = new Attribute("placeOfBirth", true);

    private static final CountryCode sweden = new CountryCode("se");
    private static final CountryCode norway = new CountryCode("no");
    private static final CountryCode spain 	= new CountryCode("es");
    private static final CountryCode usa 	= new CountryCode("us");
    
    private CountriesAttributes json;

    @Before
    public void setUp() {
        json = new CountriesAttributes(
                of(defaultAttribute),
                of(new Country(sweden.toString(), "Sweden", of(swedishAttribute), "PersonIdentifier", PidType.UTENLANDSK_IDENTIFIKASJONS_NUMMER, "*"))
        );
    }

    @Test
    public void attributesConfigCreatedFromJsonObject() {
        new AttributesConfig(json);
    }

    @Test
     public void readsDefaultOnlyForCountriesNotExtended() {
        AttributesConfig attributesConfig = new AttributesConfig(json);
        Set<Attribute> attributes = attributesConfig.forCountry(norway);
        assertThat(attributes.size(), is(1));
        assertThat(attributes.contains(defaultAttribute), is(true));
    }

    @Test
    public void readsAllAttributesForExtendedCountries() {
        AttributesConfig attributesConfig = new AttributesConfig(json);
        Set<Attribute> attributes = attributesConfig.forCountry(sweden);
        assertThat(attributes.size(), is(2));
        assertThat(attributes.contains(defaultAttribute), is(true));
        assertThat(attributes.contains(swedishAttribute), is(true));
    }
    
    @Test
    public void returnsSortedMapByCountryNameForListingCountries() {
        json = new CountriesAttributes(
                of(defaultAttribute),
                of(new Country(usa.toString(), "USA", of(defaultAttribute), "PersonIdentifier", PidType.SOCIAL_SECURITY_NUMBER, "*"),
                   new Country(sweden.toString(), "Sweden", of(defaultAttribute), "PersonIdentifier", PidType.UTENLANDSK_IDENTIFIKASJONS_NUMMER, "*"),
                   new Country(norway.toString(), "Norway", of(defaultAttribute), "PersonIdentifier", PidType.UTLENDINGSMYNDIGHETENES_IDENTIFIKASJONS_NUMMER, "*"),
                   new Country(spain.toString(), "Spain", of(defaultAttribute), "PersonIdentifier", PidType.UTLENDINGSMYNDIGHETENES_IDENTIFIKASJONS_NUMMER, "*"))
        );

        AttributesConfig attributesConfig = new AttributesConfig(json);
        Set<Country> attributes = attributesConfig.countries();
        assertThat(attributes.size(), is(4));
        assertThat(attributes.stream().map(country -> country.countryCode()).collect(Collectors.toList()), is(Arrays.asList(norway, spain, sweden, usa)));
    }
}
