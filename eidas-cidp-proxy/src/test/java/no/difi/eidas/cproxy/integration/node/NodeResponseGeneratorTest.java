package no.difi.eidas.cproxy.integration.node;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.domain.idp.IdPAuthnResponse;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.difi.opensaml.util.ConvertUtil;
import no.difi.opensaml.wrapper.ResponseWrapper;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml2.core.Response;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodeResponseGeneratorTest {
    @Mock
    private AuditLog auditLog;

    @Mock
    ConfigProvider configProvider;

    @Mock
    IdPAuthnResponse idPAuthnResponse;

    NodeResponseGenerator nodeResponseGenerator;

    @Before
    public void setUp() {
        SamlEngineProvider engineProvider = new SamlEngineProvider();
        nodeResponseGenerator = new NodeResponseGenerator(engineProvider.engine(), auditLog, configProvider);
    }

    @Test
    public void testGenerateResponse() throws MalformedURLException, EIDASSAMLEngineException, AttributeValueMarshallingException {

        // given
        final String audienceUri = "TestAudience";
        final String destination = "TestDestination";
        final String correlationId = "TestCorrelationId";
        final String subjectLocality = "TestSubjectLocality";
        final String levelOfAssurance = LevelOfAssurance.SUBSTANTIAL.getValue();

        // when
        when(configProvider.cidpProxyMetadataUrl()).thenReturn(new URL("http://metadata.foo"));

        ImmutableAttributeMap eidasRequestAttributes = ImmutableAttributeMap.builder()
                .put(EidasSpec.Definitions.PERSON_IDENTIFIER)
                .put(EidasSpec.Definitions.DATE_OF_BIRTH)
                .put(EidasSpec.Definitions.CURRENT_GIVEN_NAME)
                .put(EidasSpec.Definitions.CURRENT_FAMILY_NAME)
                .build();

        String personIdentifier = "NO/SE/12345678901";
        String dateOfBirth = "1985-01-01";
        String currentFamilyName = "CURRENT_FAMILY_NAME";
        String currentGivenName = "CURRENT_GIVEN_NAME";

        NodeAttributes nodeResponseAttributes = NodeAttributes.builder()
                .available(SubjectBasicAttribute.PersonIdentifier, personIdentifier)
                .available(SubjectBasicAttribute.DateOfBirth, dateOfBirth)
                .available(SubjectBasicAttribute.CurrentFamilyName, currentFamilyName)
                .available(SubjectBasicAttribute.CurrentGivenName, currentGivenName)
                .build();

        EidasAuthenticationRequest eidasAuthnRequest = EidasAuthenticationRequest.builder()
                .id(correlationId)
                .issuer(audienceUri)
                .destination("http://bar.com")
                .citizenCountryCode("NO")
                .originCountryCode("NO")
                .providerName("Prov")
                .assertionConsumerServiceURL(destination)
                .requestedAttributes(eidasRequestAttributes)
                .build();

        NodeAuthnRequest nodeAuthnRequest = NodeAuthnRequest.builder()
                .issuer(audienceUri)
                .assertionConsumerServiceAddress(destination)
                .correlationId(correlationId)
                .build();

        String xml = nodeResponseGenerator.generate(
                eidasAuthnRequest,
                nodeAuthnRequest,
                nodeResponseAttributes,
                LevelOfAssurance.SUBSTANTIAL.getValue(),
                subjectLocality
        );

        // then
        Response samlResponse = new ResponseWrapper(new ConvertUtil().decodeBase64(xml)).getOpenSAMLObject();
        assertEquals(audienceUri, samlResponse.getAssertions().get(0).getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getAudienceURI());
        assertEquals(personIdentifier, samlResponse.getAssertions().get(0).getAttributeStatements().get(0).getAttributes().get(0).getAttributeValues().get(0).getDOM().getTextContent());
        assertEquals(destination, samlResponse.getDestination());
        assertEquals(correlationId, samlResponse.getAssertions().get(0).getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getInResponseTo());
        assertEquals(subjectLocality, samlResponse.getAssertions().get(0).getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getAddress());
        assertEquals(levelOfAssurance, samlResponse.getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef());
        verify(auditLog).responseToPepsOk(any(byte[].class));
    }

    @Test
    public void testCreateAssembledExternalAttributes() throws AttributeValueMarshallingException {
        ImmutableAttributeMap.Builder expectedPopulatedAttributes = new ImmutableAttributeMap.Builder();

        String personIdentifier = "NO/SE/12345678901";
        String dateOfBirth = "1985-01-01";
        String currentFamilyName = "CURRENT_FAMILY_NAME";
        String currentGivenName = "CURRENT_GIVEN_NAME";

        NodeAttributes internalAttributes = NodeAttributes.builder()
                .available(SubjectBasicAttribute.PersonIdentifier, personIdentifier)
                .available(SubjectBasicAttribute.DateOfBirth, dateOfBirth)
                .available(SubjectBasicAttribute.CurrentFamilyName, currentFamilyName)
                .available(SubjectBasicAttribute.CurrentGivenName, currentGivenName)
                .build();

        try {
            AttributeDefinition<String> attr1 =  EidasSpec.Definitions.PERSON_IDENTIFIER;
            AttributeDefinition<DateTime> attr2 =  EidasSpec.Definitions.DATE_OF_BIRTH;
            AttributeDefinition<String> attr3 =  EidasSpec.Definitions.CURRENT_FAMILY_NAME;
            AttributeDefinition<String> attr4 =  EidasSpec.Definitions.CURRENT_GIVEN_NAME;

            AttributeValue val1 = attr1.getAttributeValueMarshaller().unmarshal(personIdentifier, false);
            AttributeValue val2 = attr2.getAttributeValueMarshaller().unmarshal(dateOfBirth, false);
            AttributeValue val3 = attr3.getAttributeValueMarshaller().unmarshal(currentFamilyName, false);
            AttributeValue val4 = attr4.getAttributeValueMarshaller().unmarshal(currentGivenName, false);

            expectedPopulatedAttributes
                .put(attr1, val1)
                .put(attr2, val2)
                .put(attr3, val3)
                .put(attr4, val4);
        } catch (AttributeValueMarshallingException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedPopulatedAttributes.build(), nodeResponseGenerator.createAssembledExternalAttributes(internalAttributes));
    }
}
