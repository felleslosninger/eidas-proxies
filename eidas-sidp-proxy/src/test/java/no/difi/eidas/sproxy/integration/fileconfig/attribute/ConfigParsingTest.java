package no.difi.eidas.sproxy.integration.fileconfig.attribute;

import com.google.gson.Gson;
import no.difi.eidas.sproxy.ResourceReader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfigParsingTest {
    private Gson gson = new Gson();

    @Test
    public void parse() {
        CountriesAttributes attributes =
                gson.fromJson(ResourceReader.countriesAttributesJson(), CountriesAttributes.class);

        assertThat(attributes.defaultAttributes().size(), is(4));
        assertThat(attributes.defaultAttributes().get(0).name(), is(equalTo("FamilyName")));
        assertThat(attributes.defaultAttributes().get(0).required(), is(true));
        assertThat(attributes.defaultAttributes().get(1).name(), is(equalTo("FirstName")));
        assertThat(attributes.defaultAttributes().get(1).required(), is(true));
        assertThat(attributes.defaultAttributes().get(2).name(), is(equalTo("DateOfBirth")));
        assertThat(attributes.defaultAttributes().get(2).required(), is(true));
        assertThat(attributes.defaultAttributes().get(3).name(), is(equalTo("PersonIdentifier")));
        assertThat(attributes.defaultAttributes().get(3).required(), is(true));

        assertThat(attributes.countries().size(), is(2));
        assertThat(attributes.countries().get(0).countryCode().toString(), is(equalTo("se")));
        assertThat(attributes.countries().get(0).countryName(), is(equalTo("Sweden")));

        assertThat(attributes.countries().get(1).countryCode().toString(), is(equalTo("IT")));
        assertThat(attributes.countries().get(1).countryName(), is(equalTo("Italy")));
        assertThat(attributes.countries().get(1).attributes().size(), is(1));
        assertThat(attributes.countries().get(1).attributes().get(0).name(), is(equalTo("TaxReference")));
        assertThat(attributes.countries().get(1).getRegex(), is(equalTo("(.*)")));
    }
}
