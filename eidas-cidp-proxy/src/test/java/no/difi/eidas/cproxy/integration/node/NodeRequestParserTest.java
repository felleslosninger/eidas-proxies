package no.difi.eidas.cproxy.integration.node;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml2.core.NameIDType;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class NodeRequestParserTest {

    private static ProtocolEngineI engine;

    private static final DateTime instant = new DateTime(2015, 7, 24, 14, 26, 0);

    @Mock
    private AuditLog auditLog;

    @Captor
    private ArgumentCaptor<String> auditXml;

    @Spy
    private NodeRequestParser nodeRequestParser;

    @Before
    public void setUp() {
        engine = new SamlEngineProvider().engine();
        nodeRequestParser.setEngine(engine);
        nodeRequestParser.setAuditLog(auditLog);
    }

    @Test
    public void testParseRequest() throws Exception {
        final String id = "_1";
        final String assertionConsumerServiceURL = "http://assertionConsumerUrl.foo";
        final LevelOfAssurance loa = LevelOfAssurance.SUBSTANTIAL;
        final LevelOfAssuranceComparison loaComparison = LevelOfAssuranceComparison.MINIMUM;
        final String spCountry = "NO";
        final String citizenCountry = "NO";
        final String issuer = "http://metadata.foo";
        final String nodeMetadataUrl = "http://node.metadata.foo";
        final String destination = "http://destination.metadata.foo";
        final String providerName = "provider123";
        final String nameIdFormat = NameIDType.UNSPECIFIED;
        final ImmutableSortedSet<AttributeDefinition<?>> attributes = engine
                .getProtocolProcessor().getMinimumDataSetAttributes()
                .getByFilter(
                        attrDefinition -> attrDefinition.isRequired() && attrDefinition.getPersonType().equals(PersonType.NATURAL_PERSON)
                );

        doReturn(assertionConsumerServiceURL).when(nodeRequestParser).getAssertionConsumerServiceURL(
                any(),
                any(),
                any()
        );

        String samlToken = createSamlToken(
                id,
                assertionConsumerServiceURL,
                loa,
                loaComparison,
                spCountry,
                citizenCountry,
                issuer,
                nodeMetadataUrl,
                destination,
                providerName,
                nameIdFormat,
                attributes
        );

        // when parsing it
        IAuthenticationRequest eidasRequest = nodeRequestParser.parse(samlToken);
        NodeAuthnRequest request = nodeRequestParser.toInternal(eidasRequest);

        // then Request has correct values according to SAMLRequest
        assertEquals(issuer, request.issuer());
        assertEquals(assertionConsumerServiceURL, request.assertionConsumerServiceAddress());
        assertEquals(loa.getValue(), request.levelOfAssurance());
//        assertEquals(spCountry, request.spCountry());
        assertEquals(assertionConsumerServiceURL, request.assertionConsumerServiceAddress());
        assertEquals(true, request.forceAuthn());

//        assertTrue(request.requestedAttributes().required(SubjectBasicAttribute.fromAttributeName(requiredAttribute)));

    }

    private String createSamlToken(
            String id,
            String assertionConsumerUrl,
            LevelOfAssurance loa,
            LevelOfAssuranceComparison loaComparison,
            String spCountry,
            String citizenCountry,
            String issuer,
            String nodeMetadataUrl,
            String destination,
            String providerName,
            String nameIdFormat,
            ImmutableSortedSet<AttributeDefinition<?>> attributes
    ) throws EIDASSAMLEngineException {
        ImmutableAttributeMap attributeMap = new ImmutableAttributeMap.Builder().putAll(attributes).build();

        EidasAuthenticationRequest.Builder reqBuilder = new EidasAuthenticationRequest.Builder()
            .id(id)
            .assertionConsumerServiceURL(assertionConsumerUrl)
            .levelOfAssurance(loa.getValue())
            .levelOfAssuranceComparison(loaComparison.getValue())
            .binding(EidasSamlBinding.EMPTY.getName())
            .serviceProviderCountryCode(spCountry)
            .issuer(issuer)
            .citizenCountryCode(citizenCountry)
            .destination(destination)
            .providerName(providerName)
            .requestedAttributes(attributeMap)
            .nameIdFormat(nameIdFormat);

        IRequestMessage binaryRequestMessage = engine.generateRequestMessage(reqBuilder.build(), nodeMetadataUrl);
        byte[] token = binaryRequestMessage.getMessageBytes();
        return EidasStringUtil.encodeToBase64(token);
    }

}

