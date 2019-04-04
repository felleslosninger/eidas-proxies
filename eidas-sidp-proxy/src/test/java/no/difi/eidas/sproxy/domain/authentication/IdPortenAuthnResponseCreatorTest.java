package no.difi.eidas.sproxy.domain.authentication;

import no.difi.eidas.idpproxy.integrasjon.dsf.DsfGateway;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import no.difi.eidas.idpproxy.test.SamlBootstrap;
import no.difi.eidas.sproxy.IdportenTestKey;
import no.difi.eidas.sproxy.ResourceReader;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.saml.IdPortenKeyProvider;
import no.difi.eidas.sproxy.domain.saml.IdportenSAMLConstants;
import no.difi.eidas.sproxy.domain.saml.SamlResponseXml;
import no.difi.eidas.sproxy.domain.saml.SamlXml;
import no.difi.eidas.sproxy.integration.eidas.response.EidasResponse;
import no.difi.eidas.sproxy.integration.mf.MFService;
import no.difi.eidas.sproxy.web.AuthState;
import no.difi.opensaml.builder.AttributeStatementBuilder;
import no.difi.opensaml.signature.SamlEncrypter;
import no.difi.opensaml.wrapper.ResponseWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.xml.schema.impl.XSStringImpl;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdPortenAuthnResponseCreatorTest {
    private static final String firstName = "Harald";
    private static final String lastName = "Krane";
    private static final String birth = "1971-01-01";
    private static final String dsfBirth = "010171";
    private static final String personNr = "01017100552";
    private static final String idpEntityName = "IdProxy";
    private static final String issuer = "someIssuer";
    private static final String id = "someId";
    private static final String authContextClassRef = "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI";
    private static final String keyStoreType = "jks";

    @Mock
    private MFService mfService;
    @Mock
    private ConfigProvider configProvider;
    @Mock
    private InstantIssuer instantIssuer;
    @Mock
    private DsfGateway dsfGateway;
    @Mock
    private EidasResponse eidasResponse;
    @Mock
    private IdPortenAuthnRequest authnRequest;
    @Mock
    private IdPortenKeyProvider idPortenKeyProvider;
    @Mock
    private AuditLog auditLog;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    private IdPortenAuthnResponseCreator responseCreator;

    private AuthState authState = new AuthState();

    @Before
    public void setUp() {
        reset(configProvider, instantIssuer, dsfGateway, eidasResponse, auditLog, idPortenKeyProvider, auditLog, mfService);
        SamlBootstrap.init();
        authState.setIdentityMatch(Collections.singletonList("BEST_EFFORT"));
        when(mfService.lookup(any())).thenReturn(new PersonLookupResult(PersonLookupResult.Status.MULTIPLEFOUND, com.google.common.base.Optional.absent()));
        responseCreator = new IdPortenAuthnResponseCreator(configProvider, instantIssuer, dsfGateway, idPortenKeyProvider, auditLog, () -> authState, mfService);
        when(eidasResponse.name()).thenReturn(Optional.of(new EidasResponse.Name(firstName, lastName, birth)));
        when(eidasResponse.authnContextClassRef()).thenReturn(authContextClassRef);
        when(eidasResponse.eidasReponseSaml()).thenReturn(new SamlResponseXml(ResourceReader.eidasAuthnResponse()));
        when(instantIssuer.now()).thenReturn(ZonedDateTime.of(LocalDateTime.of(1990, 4, 12, 20, 35), UTC));
        when(configProvider.idpEntityName()).thenReturn(idpEntityName);
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getID()).thenReturn(id);
        when(dsfGateway.byNameAndBirth(firstName, lastName, dsfBirth)).thenReturn(personLookupResult(PersonLookupResult.Status.OK, personNr));
        when(idPortenKeyProvider.publicKey()).thenReturn(IdportenTestKey.publicKey());
        when(idPortenKeyProvider.privateKey()).thenReturn(IdportenTestKey.privateKey());
    }

    @Test
    public void testSuccessfulResponse() throws Exception {
        IdPortenAuthnResponse authnResponse = responseCreator.create(eidasResponse, authnRequest);
        verify(auditLog).idPortenSamlAuthnResponse(any(SamlXml.class));
        ResponseWrapper response = wrap(authnResponse);
        assertThat(response.getStatusCode(), is(equalTo("urn:oasis:names:tc:SAML:2.0:status:Success")));
        assertThat(response.getOpenSAMLObject().getID(), is(notNullValue()));
        assertThat(response.getOpenSAMLObject().getIssueInstant(), is(notNullValue()));
        assertThat(response.assertions().size(), is(equalTo(1)));
        Assertion assertion = response.assertions().get(0);
        assertThat(assertion.getID(), is(notNullValue()));
        assertThat(assertion.getIssueInstant(), is(notNullValue()));
        assertThat(assertion.getAuthnStatements().size(), is(equalTo(1)));
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef(), is(equalTo(authContextClassRef)));
        assertThat(authnStatement.getSessionIndex(), is(equalTo(assertion.getID())));
    }

    @Test
    public void testSuccessfulResponseWithUnambiguousWithBestEffortRequestMatch() {
        when(mfService.lookup(any())).thenReturn(new PersonLookupResult(PersonLookupResult.Status.OK, com.google.common.base.Optional.of(Person.builder().fødselsnummer("<known pid>").build())));
        this.authState.setIdentityMatch(Arrays.asList("BEST_EFFORT"));
        IdPortenAuthnResponse authnResponse = responseCreator.create(eidasResponse, authnRequest);
        verify(auditLog).idPortenSamlAuthnResponse(any(SamlXml.class));
        ResponseWrapper response = wrap(authnResponse);
        assertThat(response.getStatusCode(), is(equalTo("urn:oasis:names:tc:SAML:2.0:status:Success")));
        assertThat(response.getOpenSAMLObject().getID(), is(notNullValue()));
        assertThat(response.getOpenSAMLObject().getIssueInstant(), is(notNullValue()));
        assertThat(response.assertions().size(), is(equalTo(1)));
        Assertion assertion = response.assertions().get(0);
        assertThat(assertion.getID(), is(notNullValue()));
        assertThat(assertion.getIssueInstant(), is(notNullValue()));
        assertThat(assertion.getAuthnStatements().size(), is(equalTo(1)));
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef(), is(equalTo(authContextClassRef)));
        assertThat(authnStatement.getSessionIndex(), is(equalTo(assertion.getID())));
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_PERSONNUMBER, "<known pid>");
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_DSF_STATUS, PersonLookupResult.Status.OK.value());
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_IDENTITY_MATCH, "UNAMBIGUOUS");
    }

    @Test
    public void testSuccessfulResponseWithUnambiguousMatch() {
        when(mfService.lookup(any())).thenReturn(new PersonLookupResult(PersonLookupResult.Status.OK, com.google.common.base.Optional.of(Person.builder().fødselsnummer("<known pid>").build())));
        this.authState.setIdentityMatch(Arrays.asList("UNAMBIGUOUS"));
        IdPortenAuthnResponse authnResponse = responseCreator.create(eidasResponse, authnRequest);
        verify(auditLog).idPortenSamlAuthnResponse(any(SamlXml.class));
        ResponseWrapper response = wrap(authnResponse);
        assertThat(response.getStatusCode(), is(equalTo("urn:oasis:names:tc:SAML:2.0:status:Success")));
        assertThat(response.getOpenSAMLObject().getID(), is(notNullValue()));
        assertThat(response.getOpenSAMLObject().getIssueInstant(), is(notNullValue()));
        assertThat(response.assertions().size(), is(equalTo(1)));
        Assertion assertion = response.assertions().get(0);
        assertThat(assertion.getID(), is(notNullValue()));
        assertThat(assertion.getIssueInstant(), is(notNullValue()));
        assertThat(assertion.getAuthnStatements().size(), is(equalTo(1)));
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef(), is(equalTo(authContextClassRef)));
        assertThat(authnStatement.getSessionIndex(), is(equalTo(assertion.getID())));
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_PERSONNUMBER, "<known pid>");
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_DSF_STATUS, PersonLookupResult.Status.OK.value());
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_IDENTITY_MATCH, "UNAMBIGUOUS");
    }

    @Test
    public void testNotFoundResponseWithUnambiguousMatch() {
        when(mfService.lookup(any())).thenReturn(new PersonLookupResult(PersonLookupResult.Status.OK, com.google.common.base.Optional.absent()));
        this.authState.setIdentityMatch(Arrays.asList("UNAMBIGUOUS"));
        IdPortenAuthnResponse authnResponse = responseCreator.create(eidasResponse, authnRequest);
        verify(auditLog).idPortenSamlAuthnResponse(any(SamlXml.class));
        ResponseWrapper response = wrap(authnResponse);
        assertThat(response.getStatusCode(), is(equalTo("urn:oasis:names:tc:SAML:2.0:status:Success")));
        assertThat(response.getOpenSAMLObject().getID(), is(notNullValue()));
        assertThat(response.getOpenSAMLObject().getIssueInstant(), is(notNullValue()));
        assertThat(response.assertions().size(), is(equalTo(1)));
        Assertion assertion = response.assertions().get(0);
        assertThat(assertion.getID(), is(notNullValue()));
        assertThat(assertion.getIssueInstant(), is(notNullValue()));
        assertThat(assertion.getAuthnStatements().size(), is(equalTo(1)));
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef(), is(equalTo(authContextClassRef)));
        assertThat(authnStatement.getSessionIndex(), is(equalTo(assertion.getID())));
        checkAttributeEmpty(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_PERSONNUMBER);
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_DSF_STATUS, PersonLookupResult.Status.MULTIPLEFOUND.value());
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_IDENTITY_MATCH, "NOT_FOUND");
    }

    @Test
    public void testSuccessfulResponseWithBestEffortFallbackUnambiguousMatch() {
        this.authState.setIdentityMatch(Arrays.asList("UNAMBIGUOUS", "BEST_EFFORT"));
        IdPortenAuthnResponse authnResponse = responseCreator.create(eidasResponse, authnRequest);
        verify(auditLog).idPortenSamlAuthnResponse(any(SamlXml.class));
        ResponseWrapper response = wrap(authnResponse);
        assertThat(response.getStatusCode(), is(equalTo("urn:oasis:names:tc:SAML:2.0:status:Success")));
        assertThat(response.getOpenSAMLObject().getID(), is(notNullValue()));
        assertThat(response.getOpenSAMLObject().getIssueInstant(), is(notNullValue()));
        assertThat(response.assertions().size(), is(equalTo(1)));
        Assertion assertion = response.assertions().get(0);
        assertThat(assertion.getID(), is(notNullValue()));
        assertThat(assertion.getIssueInstant(), is(notNullValue()));
        assertThat(assertion.getAuthnStatements().size(), is(equalTo(1)));
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef(), is(equalTo(authContextClassRef)));
        assertThat(authnStatement.getSessionIndex(), is(equalTo(assertion.getID())));
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_PERSONNUMBER, personNr);
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_DSF_STATUS, PersonLookupResult.Status.OK.value());
        checkAttribute(assertion.getAttributeStatements().get(0), IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_IDENTITY_MATCH, "BEST_EFFORT");
    }

    @Test
    public void setDsfStatusEvenIfNoSearchIsExecuted() throws Exception {
        when(eidasResponse.name()).thenReturn(Optional.empty());
        AttributeStatementBuilder builder = new AttributeStatementBuilder();
        responseCreator.addDsfAttributes(eidasResponse, builder);
        verifyZeroInteractions(dsfGateway);

        AttributeStatement attributeStatement = builder.build();
        assertEquals(2, attributeStatement.getAttributes().size());
        checkAttribute(attributeStatement, IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_DSF_STATUS, PersonLookupResult.Status.NODSFSEARCH.value());
        checkAttribute(attributeStatement, IdPortenAuthnResponseCreator.SAML_ATTRIBUTE_IDENTITY_MATCH, "ERROR");
    }

    @Test
    public void userCancelledAuthResponse() throws Exception {
        IdPortenAuthnResponse authnResponse = responseCreator.createUserCancelledResponse(authnRequest);
        ResponseWrapper response = wrap(authnResponse);
        assertThat(response.getStatusCode(), is(equalTo(IdportenSAMLConstants.SAML_RESPONSE_USER_CANCELLED_STATUS_CODE)));
        assertThat(response.getInResponseTo(), is(equalTo(id)));
    }

    @Test
    public void testConvertDsfOKWhenPersonFoundToIdentityMatchBestEffort() {
        assertEquals("BEST_EFFORT", responseCreator.convertDsfStatusToIdentityMatch(personLookupResult(PersonLookupResult.Status.OK, personNr)));
    }

    @Test
    public void testConvertDsfOKWhenPersonNotFoundToIdentityMatchNotFound() {
        assertEquals("NOT_FOUND", responseCreator.convertDsfStatusToIdentityMatch(personLookupResult(PersonLookupResult.Status.OK, null)));
    }

    @Test
    public void testConvertDsfMultipleFoundToIdentityMatchNotFound() {
        assertEquals("NOT_FOUND", responseCreator.convertDsfStatusToIdentityMatch(personLookupResult(PersonLookupResult.Status.MULTIPLEFOUND, null)));
    }

    @Test
    public void testConvertDsfErrorToIdentityMatchError() {
        assertEquals("ERROR", responseCreator.convertDsfStatusToIdentityMatch(personLookupResult(PersonLookupResult.Status.ERROR, null)));
    }

    @Test
    public void testConvertDsfNotCheckedToIdentityMatchError() {
        assertEquals("ERROR", responseCreator.convertDsfStatusToIdentityMatch(personLookupResult(PersonLookupResult.Status.NODSFSEARCH, null)));
    }

    @Test
    public void testEidasSamlResponseAsBase64EncodedSAMLAttribute() throws Exception {
        AttributeStatementBuilder attributeStatementBuilder = mock(AttributeStatementBuilder.class);
        responseCreator.addEidasSaml(eidasResponse, attributeStatementBuilder);
        verify(attributeStatementBuilder).attribute(eq("eidas-saml-response"), stringArgumentCaptor.capture());
        assertEquals(eidasResponse.eidasReponseSaml().toString(), new String(Base64.getDecoder().decode(stringArgumentCaptor.getValue())));
    }

    private ResponseWrapper wrap(IdPortenAuthnResponse authnResponse) {
        return new ResponseWrapper(
                authnResponse.toString(),
                new SamlEncrypter(
                        idPortenKeyProvider.publicKey(),
                        idPortenKeyProvider.privateKey().getPrivateKey()
                )
        );
    }

    private void checkAttributeEmpty(AttributeStatement attributeStatement, String attributeName) {
        for (Attribute attribute : attributeStatement.getAttributes()) {
            if (attribute.getName().equals(attributeName)) {
                fail(String.format("Attribute [%s] found in attribute statement", attributeName));
            }
        }
    }

    private void checkAttribute(AttributeStatement attributeStatement, String attributeName, String value) {
        for (Attribute attribute : attributeStatement.getAttributes()) {
            if (attribute.getName().equals(attributeName)) {
                assertEquals(value, ((XSStringImpl) attribute.getAttributeValues().get(0)).getValue());
                return;
            }
        }
        fail(String.format("Attribute [%s] not found in attribute statement", attributeName));
    }

    private PersonLookupResult personLookupResult(PersonLookupResult.Status status, String ssn) {
        if (ssn != null) {
            return new PersonLookupResult(status, com.google.common.base.Optional.of(Person.builder().fødselsnummer(personNr).build()));
        }
        return new PersonLookupResult(status, com.google.common.base.Optional.absent());
    }

}
