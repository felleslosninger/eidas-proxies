package no.difi.eidas.cproxy.domain.node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import no.difi.eidas.cproxy.domain.authentication.AuthenticationContext;
import no.difi.eidas.cproxy.domain.idp.IdPAuthnResponse;
import no.difi.eidas.cproxy.integration.idporten.NodeAttributeAssembler;
import no.difi.eidas.cproxy.integration.idporten.ResponseData;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Iterator;
import java.util.Set;

import static no.difi.eidas.idpproxy.SubjectBasicAttribute.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class NodeAttributeAssemblerTest {

    @Mock
    private IdPAuthnResponse authnResponse;
    @Mock
    private NodeAuthnRequest nodeRequest;
    @InjectMocks
    private NodeAttributeAssembler nodeAttributeAssembler;

    @Test
    public void testAssembleAttributes() {
        String uid = "12046745114";
        String securityLevel = "4";
        when(authnResponse.uid()).thenReturn(uid);
        AuthenticationContext context = new AuthenticationContext();
        context.nodeRequest(createNodeRequest());
        NodeAttributes attributes = nodeAttributeAssembler.assembleAttributes(context, new ResponseData(authnResponse, null));
        assertEquals(uid, attributes.get(PersonIdentifier).value().get());
    }

    @Test
    public void whenAssembledThenAttributesAreInPredefinedOrder() {
        // given
        String uid = "12046745114";
        String securityLevel = "4";
        when(authnResponse.uid()).thenReturn(uid);
        AuthenticationContext context = new AuthenticationContext();
        context.nodeRequest(createNodeRequest(NodeRequestedAttributes.builder()
            .optional(PersonIdentifier)
            .optional(DateOfBirth)
            .optional(CurrentFamilyName)
            .optional(CurrentGivenName)
            .build()
        ));

        // when
        NodeAttributes attributes = nodeAttributeAssembler.assembleAttributes(context, new ResponseData(authnResponse, null));

        // then order should be as expected here
        Iterator<SubjectBasicAttribute> iterator = attributes.iterator();
        assertEquals(SubjectBasicAttribute.PersonIdentifier, iterator.next());
        assertEquals(SubjectBasicAttribute.CurrentGivenName, iterator.next());
        assertEquals(SubjectBasicAttribute.CurrentFamilyName, iterator.next());
        assertEquals(SubjectBasicAttribute.DateOfBirth, iterator.next());
    }

    @Test
    public void testFornavn() {
        final String fornavn = "Ingelin";
        Person person = personBuilder().fornavn(fornavn).build();
        assertEquals(fornavn, map(person).get(CurrentGivenName).value().get());
    }

    @Test
    public void testEtternavn() {
        final String etternavn = "Killengren";
        Person person = personBuilder().etternavn(etternavn).build();
        assertEquals(etternavn, map(person).get(CurrentFamilyName).value().get());
    }

    @Test
    public void testValidFødselsdato() {
        testFødselsdato("1985-10-07", "1985-10-07");
        testFødselsdato("2003-01-13", "2003-01-13");
        testFødselsdato("2089-12-13", "2089-12-13");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFødselsdatoInvalidMonth() {
        testFødselsdato("1985-17-07", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFødselsdatoInvalidYear() {
        testFødselsdato("abcd-11-07", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFødselsdatoInvalidDay() {
        testFødselsdato("1985-10-32", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFødselsdatoInvalidDay0() {
        testFødselsdato("1985-10-32", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFødselsdatoInvalidDay32() {
        testFødselsdato("1985-10-32", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFødselsdatoInvalidDay31() {
        testFødselsdato("1985-02-31", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFødselsdatoInvalidDay30() {
        testFødselsdato("1985-02-30", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFødselsdatoInvalidDay29() {
        testFødselsdato("1985-02-29", null);
    }

    private void testFødselsdato(String fødselsdato, String dateOfBirth) {
        Person person = personBuilder().fødselsdato(
                LocalDate.parse(fødselsdato, DateTimeFormat.forPattern(NodeAttributeAssembler.DATE_OF_BIRTH_FORMAT))
        ).build();
        assertEquals(dateOfBirth, map(person).get(SubjectBasicAttribute.DateOfBirth).value().get());
    }

    @Test
    public void testMissingAssembleAttributes() {

        NodeRequestedAttributes attributes = requiredAttributes();
        when(nodeRequest.requestedAttributes()).thenReturn(attributes);

        final AuthenticationContext context = new AuthenticationContext();
        context.nodeRequest(nodeRequest);
        context.assembledAttributes(nodeAttributeAssembler.assembleAttributes(context, new ResponseData(authnResponse, null)));

        Set<SubjectBasicAttribute> missingAttributes = context.assembledAttributes().missingRequiredAttributes();
        assertThat(Iterables.size(missingAttributes), is(4));
        assertThat(
                Iterables.all(missingAttributes, new Predicate<SubjectBasicAttribute>() {
                    @Override
                    public boolean apply(SubjectBasicAttribute subjectBasicAttribute) {
                        return context.nodeRequest().requestedAttributes().required(subjectBasicAttribute);
                    }
                }), is(true));
    }
    
    private Person.Builder personBuilder() {
        return Person.builder().fødselsnummer("12046745114");
    }

    private NodeAttributes map(Person person) {
        AuthenticationContext context = new AuthenticationContext();
        context.nodeRequest(createNodeRequest());
        return nodeAttributeAssembler.assembleAttributes(context, new ResponseData(authnResponse, person));
    }

    private NodeAuthnRequest createNodeRequest(NodeRequestedAttributes requestedAttributes) {
        return NodeAuthnRequest.builder()
                .requestedAttributes(requestedAttributes)
                .build();
    }

    private NodeAuthnRequest createNodeRequest() {
        return createNodeRequest(requestedAttributes());
    }

    private NodeRequestedAttributes requestedAttributes() {
        return NodeRequestedAttributes.builder()
                .optional(PersonIdentifier)
                .optional(CurrentGivenName)
                .optional(CurrentFamilyName)
                .optional(DateOfBirth)
                .build();
    }

    private NodeRequestedAttributes requiredAttributes() {
        return NodeRequestedAttributes.builder()
                .required(PersonIdentifier)
                .required(CurrentGivenName)
                .required(CurrentFamilyName)
                .required(DateOfBirth)
                .build();

    }

}
