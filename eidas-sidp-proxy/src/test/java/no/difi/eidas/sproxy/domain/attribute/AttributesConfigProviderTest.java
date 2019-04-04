package no.difi.eidas.sproxy.domain.attribute;

import no.difi.eidas.sproxy.ResourceReader;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.config.FileReader;
import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Attribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AttributesConfigProviderTest {

    private static final CountryCode sweden = new CountryCode("se");
    private static File dummyFile = new File("/dummy/nonexisting/file");

    @Mock
    private FileReader fileReader;

    @Mock
    private ConfigProvider configProvider;

    private AttributesConfigProvider attributesConfigProvider;

    @Before
    public void setUp() {
        when(configProvider.fileConfigCountriesAttributes()).thenReturn(dummyFile);
        when(fileReader.read(dummyFile)).thenReturn(ResourceReader.countriesAttributesJson());
        attributesConfigProvider = new AttributesConfigProvider(configProvider, fileReader);
    }

    @Test
    public void providesConfig() {
        assertConfig();
        verify(fileReader).read(dummyFile);
    }

    @Test
    public void attributesForCountry() {
        Set<Attribute> attributes = attributesConfigProvider.forCountry(sweden);
        Set<String> attributeNames = attributes.stream().map(attribute -> attribute.name()).collect(Collectors.toSet());
        assertThat(
                attributeNames.containsAll(
                        Arrays.asList("FamilyName", "FirstName", "PersonIdentifier", "PlaceOfBirth")
                ), is(true)
        );
    }

    @Test
    public void attributesSpecificToCountry() {
        assertThat(attributesConfigProvider.isAttributeCountrySpecific("PersonIdentifier", sweden), is(false));
        assertThat(attributesConfigProvider.isAttributeCountrySpecific("PlaceOfBirth", sweden), is(true));
    }

    @Test
    public void reloadsConfigPeriodically() throws InterruptedException {
        assertConfig();
        Thread.sleep(2L);
        assertConfig();
        verify(fileReader, times(2)).read(dummyFile);
    }

    private void assertConfig() {
        Set<Attribute> attributes = attributesConfigProvider.forCountry(sweden);
        assertThat(attributes.size()>0, is(true));
    }


}
