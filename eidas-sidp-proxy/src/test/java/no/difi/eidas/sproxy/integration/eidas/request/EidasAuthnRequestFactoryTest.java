package no.difi.eidas.sproxy.integration.eidas.request;

import no.difi.eidas.idpproxy.test.SamlBootstrap;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.attribute.AttributesConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.integration.eidas.TestSamlEngineProvider;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Attribute;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Country;
import no.difi.opensaml.util.ConvertUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.difi.eidas.idpproxy.integrasjon.Urls.url;
import static no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnRequestReceiverTest.idPortenSamlAuthnRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EidasAuthnRequestFactoryTest {

    private static final URL eidasProxyAuthUrl = url("http://host/auth");
    private static final URL sproxyMetadataUrl = url("http://host/");
    private static final URL eidasNodeUrl = url("http://eidas/auth");
    private static final ConvertUtil encoder = new ConvertUtil();

    @Mock
    private AuditLog auditLog;
    @Mock
    private ConfigProvider configProvider;
    @Mock
    private AttributesConfigProvider attributesConfigProvider;

    private EidasAuthnRequestFactory eidasAuthnRequestFactory;

    @Before
    public void setUp() {
        SamlBootstrap.init();
        reset(configProvider, attributesConfigProvider);
        when(configProvider.eidasProxyAuthUrl()).thenReturn(eidasProxyAuthUrl);
        when(configProvider.eidasNodeUrl()).thenReturn(eidasNodeUrl);
        when(configProvider.eidasMetadataUrl()).thenReturn(sproxyMetadataUrl);
        eidasAuthnRequestFactory = new EidasAuthnRequestFactory(
                auditLog,
                configProvider,
                attributesConfigProvider,
                TestSamlEngineProvider.engine()
        );
    }

    public String decode(EidasAuthnRequest request) {
        return encoder.decodeBase64(request.toString());
    }


    protected List<String> setupCountryAttributes(String country, List<Attribute> attributes) {
        when(attributesConfigProvider.forCountry(eq(new CountryCode(country)))).thenReturn(new HashSet<>(attributes));
        return attributes.stream().map(attribute -> attribute.name()).collect(Collectors.toList());
    }

    protected List<Attribute> allEidasAttributes() {
        return TestSamlEngineProvider.engine().getProtocolProcessor().getAllSupportedAttributes()
                .stream()
                .map(attributeDefinition -> new Attribute(attributeDefinition.getFriendlyName(), attributeDefinition.isRequired()))
                .collect(Collectors.toList());
    }

    protected List<Attribute> someEidasAttributes(String... attributeNames) {
        List<String> attributeNameList = Arrays.asList(attributeNames);
        return allEidasAttributes().stream()
                .filter(attribute -> attributeNameList.contains(attribute.name()))
                .collect(Collectors.toList());
    }

    @Test
    public void testRequestAuthnContextClassRef() {
        String country = "se";
        setupCountryAttributes(country, someEidasAttributes("PersonIdentifier"));
        EidasAuthnRequest request = eidasAuthnRequestFactory.create(idPortenSamlAuthnRequest(), new CountryCode(country));
        String saml = decode(request);
        assertThat(saml.contains("<saml2p:RequestedAuthnContext Comparison=\"minimum\">"), is(true));
        assertThat(saml.contains("<saml2:AuthnContextClassRef>http://eidas.europa.eu/LoA/substantial</saml2:AuthnContextClassRef>"), is(true));
    }

    @Test
    public void testRequestAttributes() {
        String country = "ce";
        List<String> attributeNames = setupCountryAttributes(country, allEidasAttributes());
        EidasAuthnRequest request = eidasAuthnRequestFactory.create(idPortenSamlAuthnRequest(), new CountryCode(country));
        String saml = decode(request);
        for (String attributeName : attributeNames) {
            assertThat(saml.contains(String.format("<eidas:RequestedAttribute FriendlyName=\"%s\"", attributeName)), is(true));
        }
    }

    @Test
    public void testRequestPidAttribute() {
        Country country1 = new Country("se", "se", null, "PID", null, null);
        when(attributesConfigProvider.forCountry(eq(country1.countryCode()))).thenReturn(new HashSet<>(someEidasAttributes("PersonIdentifier", "PID")));
        EidasAuthnRequest request = eidasAuthnRequestFactory.create(idPortenSamlAuthnRequest(), country1.countryCode());
        String saml = decode(request);
        assertThat(saml.contains(String.format("<eidas:RequestedAttribute FriendlyName=\"%s\"", "PID")), is(true));
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalToRequireAttributeUnknownToEidas() {
        eidasAuthnRequestFactory.attributes(Arrays.asList(new Attribute("foo", true)));
    }

    @Test
    public void testAttributeUnknownToEidasIsIgnored() {
        assertTrue(eidasAuthnRequestFactory.attributes(Arrays.asList(new Attribute("foo", false))).isEmpty());
    }

}
