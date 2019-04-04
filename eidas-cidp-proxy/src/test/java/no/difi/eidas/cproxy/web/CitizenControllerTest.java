package no.difi.eidas.cproxy.web;

import com.google.common.base.Optional;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import no.difi.eidas.cproxy.TestData;
import no.difi.eidas.cproxy.config.OIDCProperties;
import no.difi.eidas.cproxy.domain.authentication.AuthenticationContext;
import no.difi.eidas.cproxy.domain.authentication.ConsentHandler;
import no.difi.eidas.cproxy.domain.idp.IdPAuthnResponse;
import no.difi.eidas.cproxy.domain.idp.IdPortenAuthnRequestCreator;
import no.difi.eidas.cproxy.domain.idp.IdpArtifactResolver;
import no.difi.eidas.cproxy.domain.idp.IdportenSession;
import no.difi.eidas.cproxy.domain.node.NodeAttribute;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.cproxy.integration.idporten.NodeAttributeAssembler;
import no.difi.eidas.cproxy.integration.idporten.ResponseData;
import no.difi.eidas.cproxy.integration.node.NodeRequestParser;
import no.difi.eidas.cproxy.integration.node.NodeResponseGenerator;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.difi.eidas.idpproxy.integrasjon.dsf.DsfGateway;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import no.difi.eidas.idpproxy.test.SamlBootstrap;
import no.difi.opensaml.util.ConvertUtil;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml2.core.AuthnRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(MockitoJUnitRunner.class)
public class CitizenControllerTest extends TestData{

    @Mock
    private OIDCProperties oidcProperties;
    @Mock
    private IdpArtifactResolver artifactResolver;
    @Mock
    private NodeAttributeAssembler nodeAttributeAssembler;
    @Spy
    private ConvertUtil convertUtil = new ConvertUtil();
    @Mock
    private IdPortenAuthnRequestCreator authnRequestCreator;
    @Mock
    private NodeRequestParser nodeRequestParser;
    @Mock
    private DsfGateway dsfGateway;
    @Mock
    private NodeResponseGenerator nodeResponseGenerator;
    @Mock
    private LocaleResolver localeResolver;
    @Mock
    private ConsentHandler consentHandler;
    @InjectMocks
    private CitizenController controller;
    @Mock
    private IEidasAuthenticationRequest eidasRequestMock;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".jsp");

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
        SamlBootstrap.init();
    }

    @Test
    public void verifyViewConfirmRequest() throws Exception {
        String redirectURL = "RedirectURL";
        when(authnRequestCreator.buildRedirectURL(null, false, true))
                .thenReturn(redirectURL);
        when(nodeRequestParser.toInternal(any()))
                .thenReturn(NodeAuthnRequest.builder().levelOfAssurance("http://eidas.europa.eu/LoA/high").build());
        mockMvc.perform(post("/auth")
                .param(CitizenController.SAML_REQUEST, mockedAuthnRequestENCODED())
                .accept(MediaType.ALL))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(redirectURL));
    }

    @Test
    public void verifyRedirectToOIDCWhenEnabled() throws Exception {
        when(oidcProperties.isEnabled()).thenReturn(true);
        when(nodeRequestParser.toInternal(any()))
                .thenReturn(NodeAuthnRequest.builder().levelOfAssurance("http://eidas.europa.eu/LoA/low").build());
        mockMvc.perform(post("/auth")
                .param(CitizenController.SAML_REQUEST, mockedEidasAuthnRequestLow())
                .accept(MediaType.ALL))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oidc/start"));
    }

    @Test
    public void verifyNoOIDCRedirectWhenDisabled() throws Exception {
        when(nodeRequestParser.toInternal(any()))
                .thenReturn(NodeAuthnRequest.builder().levelOfAssurance("http://eidas.europa.eu/LoA/low").build());
        MvcResult mvcResult = mockMvc.perform(post("/auth")
                .param(CitizenController.SAML_REQUEST, mockedEidasAuthnRequestLow())
                .accept(MediaType.ALL))
                .andReturn();
        String redirectedUrl = mvcResult.getResponse().getRedirectedUrl();
        if (redirectedUrl != null) {
            assertNotEquals("/oidc/start", redirectedUrl);
        }
    }


    @Test
    public void verifyViewRejectConsent() throws Exception {
        AuthenticationContext context = new AuthenticationContext();

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CitizenController.SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT, context);

        RequestBuilder requestBuilder = get("/rejectConsent")
                .accept(MediaType.ALL)
                .session(session)
                .sessionAttr(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE, new IdportenSession("test", "test"));
        // when calling acceptConsent
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/saml/logout"));
    }

    @Test
    public void whenHandlingIdPortenResponseThenNodeAttributesArePutOnSession() throws Exception {
        // Given
        NodeAttributes expectedAttributes = NodeAttributes.builder().build();
        IdPAuthnResponse mockAuthnResponse = Mockito.mock(IdPAuthnResponse.class);
        when(artifactResolver.resolve(anyString())).thenReturn(mockAuthnResponse);
        when(nodeAttributeAssembler.assembleAttributes(any(AuthenticationContext.class), any(ResponseData.class))).thenReturn(expectedAttributes);
        String uid = "uid";
        when(mockAuthnResponse.uid()).thenReturn(uid);
        when(dsfGateway.bySsn(uid)).thenReturn(new PersonLookupResult(PersonLookupResult.Status.OK, Optional.absent()));
        when(mockAuthnResponse.securityLevel()).thenReturn("3");

        // When
        ResultActions resultActions = mockMvc.perform(
                get("/idportenResponse")
                        .param("SAMLart", "dummySAMLArtifact")
                        .accept(MediaType.ALL)
        );

        // Then
        HttpSession session = resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("showAttributeConsent"))
                .andReturn().getRequest().getSession();
        AuthenticationContext authenticationContext = (AuthenticationContext)
                session.getAttribute("authenticationContext");
        assertEquals(
                expectedAttributes,
                authenticationContext.assembledAttributes()
        );
        assertNotNull(session.getAttribute(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE));
    }

    @Test
    @Ignore
    public void whenHandlingIdPortenResponseWithKnownCultureThenLocaleIsSetAccordingly() throws Exception {
        String[] languages = {"nb", "nn", "en", "se"};
        for (String language : languages) {
            handleIdPortenResponseWithCulture(language);
            verify(localeResolver).setLocale(
                    any(HttpServletRequest.class),
                    any(HttpServletResponse.class),
                    eq(new Locale(language))
            );
        }
    }

    @Test
    @Ignore
    public void whenHandlingIdPortenResponseWithUnknownCultureThenLocaleIsNotChanged() throws Exception {
        String[] languages = {null, "", "ty", "sk", "dk", "xxx"};
        for (String language : languages) {
            handleIdPortenResponseWithCulture(language);
        }
        verifyZeroInteractions(localeResolver);
    }

    @Test
    public void whenAcceptConsentThenCitizenAddressIsProvidedToNodeResponseGenerator() throws Exception {
        // given a valid session state and request with specific remote address
        final String remoteAddress = "testRemoteAddress";
        AuthenticationContext context = new AuthenticationContext();
        context.eidasRequest(eidasRequestMock);
        context.nodeRequest(NodeAuthnRequest.builder().build());
        context.assembledAttributes(NodeAttributes.builder()
                .available(SubjectBasicAttribute.PersonIdentifier, "TestEIdentifier")
                .build());
//        when(idpAuthnResponse.securityLevel()).thenReturn("3");
//        context.idPortenResponse(idpAuthnResponse);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CitizenController.SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT, context);
        RequestBuilder requestBuilder = get("/acceptConsent")
                .with(new RequestPostProcessor() {
                    @Override
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setRemoteAddr(remoteAddress);
                        return request;
                    }
                })
                .accept(MediaType.ALL)
                .session(session)
                .sessionAttr(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE, new IdportenSession("sp1", "index2"));
        // when calling acceptConsent
        ResultActions resultActions = mockMvc.perform(requestBuilder);
        // then NodeResponseGenerator is called with citizenAddress equal to the specific remote address
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("forwardAuthnResponseToEidas"));
        verify(nodeResponseGenerator).generate(
                eidasRequestMock,
                context.nodeRequest(),
                context.assembledAttributes(),
                null,
                "testRemoteAddress");
        assertNull(session.getAttribute(CitizenController.SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT));
    }

    @Test
    public void whenRequiredAttributesIsMissingShowErrorPage() throws Exception {
        // Given
        Map<SubjectBasicAttribute, NodeAttribute> attributes = new EnumMap<>(SubjectBasicAttribute.class);
        attributes.put(SubjectBasicAttribute.PersonIdentifier, new NodeAttribute(NodeAttribute.NotPresentReason.NotAvailable, true));

        MockHttpSession session = new MockHttpSession();
        NodeAttributes expectedAttributes = Mockito.mock(NodeAttributes.class);
        IdPAuthnResponse mockAuthnResponse = Mockito.mock(IdPAuthnResponse.class);
        when(expectedAttributes.missingRequiredAttributes()).thenReturn(attributes.keySet());
        when(expectedAttributes.hasMissingRequiredAttributes()).thenReturn(true);
        String uid = "uid";
        when(mockAuthnResponse.uid()).thenReturn(uid);
        when(mockAuthnResponse.securityLevel()).thenReturn("3");
        when(artifactResolver.resolve(anyString())).thenReturn(mockAuthnResponse);
        when(nodeAttributeAssembler.assembleAttributes(any(AuthenticationContext.class), any(ResponseData.class))).thenReturn(expectedAttributes);
        when(dsfGateway.bySsn(uid)).thenReturn(new PersonLookupResult(PersonLookupResult.Status.OK, Optional.absent()));

        // When
        ResultActions resultActions = mockMvc.perform(
                get("/idportenResponse")
                        .param("SAMLart", "dummySAMLArtifact")
                        .session(session)
                        .accept(MediaType.ALL)
        );
        // Then
        ModelAndView view = resultActions.andReturn().getModelAndView();
        assertEquals(
                StringUtils.containsIgnoreCase(view.getModelMap().get("attributeMap").toString(), ("eidas-cidp-proxy.attributes.attribute.personidentifier")),
                true);
        assertNull(session.getAttribute(CitizenController.SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT));
    }

    @Test
    public void whenShowAttributeDetailsThenOrderIsCorrect() throws Exception {
        // given
        NodeAttributes attributes = NodeAttributes.builder()
                .available(SubjectBasicAttribute.CurrentFamilyName, "XYZ")
                .available(SubjectBasicAttribute.PersonIdentifier, "eIdentifier")
                .build();
        AuthenticationContext context = new AuthenticationContext();
        context.assembledAttributes(attributes);

        //when preparing view for showing attributes
        ResultActions results = mockMvc.perform(
                get("/showAttributeDetails")
                        .accept(MediaType.ALL)
                        .sessionAttr("authenticationContext", context)
                        .sessionAttr(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE, new IdportenSession("spTest", "index1"))
        );

        // then order of attributes is same as the order attributes were added to NodeAttributes
        ModelAndView view = results.andReturn().getModelAndView();
        Map<String, String> attributeMap = (Map<String, String>) view.getModelMap().get("attributeMap");
        Iterator<String> attributeIterator = attributeMap.keySet().iterator();
        assertEquals("eidas-cidp-proxy.attributes.attribute.currentfamilyname", attributeIterator.next());
        assertEquals("eidas-cidp-proxy.attributes.attribute.personidentifier", attributeIterator.next());
    }

    @Test
    public void whenShowingAttributesListThroughPropertyMapping() throws Exception {
        // given
        NodeAttributes attributes = NodeAttributes.builder()
                .available(SubjectBasicAttribute.CurrentFamilyName, "XYZ")
                .build();

        AuthenticationContext context = Mockito.mock(AuthenticationContext.class);
        when(context.assembledAttributes()).thenReturn(attributes);

        //when
        ResultActions results = mockMvc.perform(
                get("/showAttributeDetails")
                        .accept(MediaType.ALL)
                        .sessionAttr("authenticationContext", context)
                        .sessionAttr(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE, new IdportenSession("spTest", "index1"))
        );

        // then
        ModelAndView view = results.andReturn().getModelAndView();
        Map<String, String> attributeMap = (Map<String, String>) view.getModelMap().get("attributeMap");

        assertEquals(attributeMap.keySet().size(), 1);

        assertEquals(attributeMap.get("eidas-cidp-proxy.attributes.attribute.currentfamilyname"), "XYZ");

    }



    @Test
    public void testSecurityLevelToLevelOfAssurance() {
        assertEquals(LevelOfAssurance.SUBSTANTIAL.getValue(), controller.levelOfAssurance("3"));
        assertEquals(LevelOfAssurance.HIGH.getValue(), controller.levelOfAssurance("4"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSecurityLevelToLevelOfAssurance2(){
        controller.levelOfAssurance("2");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSecurityLevelToLevelOfAssurance5(){
        controller.levelOfAssurance("5");
    }

    private void handleIdPortenResponseWithCulture(String culture) throws Exception {
        IdPAuthnResponse mockAuthnResponse = Mockito.mock(IdPAuthnResponse.class);
        String uid = "uid";
        when(mockAuthnResponse.uid()).thenReturn(uid);
        when(mockAuthnResponse.culture()).thenReturn(culture);
        when(artifactResolver.resolve(anyString())).thenReturn(mockAuthnResponse);
        when(dsfGateway.bySsn(uid)).thenReturn(new PersonLookupResult(PersonLookupResult.Status.OK, Optional.absent()));

        mockMvc.perform(
                get("/idportenResponse")
                        .param("SAMLart", "dummySAMLArtifact")
                        .accept(MediaType.ALL)
        );
    }

}
