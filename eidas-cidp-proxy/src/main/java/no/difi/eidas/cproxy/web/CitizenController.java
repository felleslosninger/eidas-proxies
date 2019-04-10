package no.difi.eidas.cproxy.web;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.config.OIDCProperties;
import no.difi.eidas.cproxy.domain.authentication.AuthenticationContext;
import no.difi.eidas.cproxy.domain.authentication.ConsentHandler;
import no.difi.eidas.cproxy.domain.idp.*;
import no.difi.eidas.cproxy.domain.node.NodeAttribute;
import no.difi.eidas.cproxy.integration.idporten.NodeAttributeAssembler;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.cproxy.integration.idporten.ResponseData;
import no.difi.eidas.cproxy.integration.mf.MFService;
import no.difi.eidas.cproxy.integration.node.NodeRequestParser;
import no.difi.eidas.cproxy.integration.node.NodeResponseGenerator;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.difi.eidas.idpproxy.integrasjon.dsf.DsfGateway;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;

@Controller
public class CitizenController {
    public static final String path 					= "/auth";
    public static final String pathIdportenResponse		= "idportenResponse";
    /*views*/
    public static final String errorModel 				= "error";
    public static final String errorRequiredAttributes = "errorRequiredAttributes";
    public static final String showAttributeConsent		= "showAttributeConsent";
    public static final String SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT = "authenticationContext";
    public static final String SAML_REQUEST = "SAMLRequest";
    private static final Logger logger = LoggerFactory.getLogger(CitizenController.class);
    private static final String samlArtifactResponse = "SAMLart";
    private final IdPortenAuthnRequestCreator idPortenAuthnRequestBuilder;
    private final NodeAttributeAssembler nodeAttributeAssembler;
    private final IdpArtifactResolver idpAuthnResponseResolver;
    private final LocaleResolver localeResolver;
    private final DsfGateway dsfService;
    private final NodeRequestParser nodeRequestParser;
    private final NodeResponseGenerator nodeResponseGenerator;
    private final LogoutHandler logoutHandler;
    private final ConsentHandler consentHandler;
    private final OIDCProperties oidcProperties;
    private final MFService mfService;
    private final ConfigProvider configProvider;

    @Autowired
    public CitizenController(
            IdPortenAuthnRequestCreator idPortenAuthnRequestCreator,
            NodeAttributeAssembler nodeAttributeAssembler,
            IdpArtifactResolver idpAuthnResponseResolver,
            LocaleResolver localeResolver,
            DsfGateway dsfService,
            NodeRequestParser nodeRequestParser,
            NodeResponseGenerator nodeResponseGenerator,
            LogoutHandler logoutHandler,
            ConsentHandler consentHandler,
            OIDCProperties oidcProperties,
            MFService mfService,
            ConfigProvider configProvider) {
        this.idPortenAuthnRequestBuilder = idPortenAuthnRequestCreator;
        this.nodeAttributeAssembler = nodeAttributeAssembler;
        this.idpAuthnResponseResolver = idpAuthnResponseResolver;
        this.localeResolver = localeResolver;
        this.dsfService = dsfService;
        this.nodeRequestParser = nodeRequestParser;
        this.nodeResponseGenerator = nodeResponseGenerator;
        this.logoutHandler = logoutHandler;
        this.consentHandler = consentHandler;
        this.oidcProperties = oidcProperties;
        this.mfService = mfService;
        this.configProvider = configProvider;
    }

    /**
     * Incoming SAML authn request from C-PEPS
     * @throws IOException 
     */
    @RequestMapping(value = path, method = RequestMethod.POST)
    public String receivePepsRequestSendToIdPorten(
            @RequestParam(value = SAML_REQUEST) String samlRequest,
            HttpServletResponse response,
            HttpSession session
    ) throws IOException, EIDASSAMLEngineException {
        clearAuthenticationContext(session);
        IAuthenticationRequest eidasAuthnRequest = nodeRequestParser.parse(samlRequest);
        NodeAuthnRequest nodeAuthnRequest = nodeRequestParser.toInternal(eidasAuthnRequest);

        AuthenticationContext context = authenticationContext(session);
        context.nodeRequest(nodeAuthnRequest);
        context.eidasRequest(eidasAuthnRequest);

        if (oidcProperties.isEnabled()
                && LevelOfAssurance.getLevel(context.nodeRequest().levelOfAssurance()) == LevelOfAssurance.LOW) {
            return "redirect:/oidc/start";
        } else {
            AuthnRequest authnRequest = idPortenAuthnRequestBuilder.create(nodeAuthnRequest);
            response.sendRedirect(idPortenAuthnRequestBuilder.buildRedirectURL(authnRequest, false, true));
            return null;
        }
    }

    @RequestMapping(value = pathIdportenResponse, method = RequestMethod.GET)
    public ModelAndView receiveIdpAuthnResponseSendResponseToPeps(
            @RequestParam(samlArtifactResponse) String samlArtifact,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session
    ) throws IOException {
        IdPAuthnResponse idPAuthnResponse = idpAuthnResponseResolver.resolve(samlArtifact);
        setLocale(request, response, idPAuthnResponse);

        session.setAttribute(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE, new IdportenSession(idPAuthnResponse.nameId(), idPAuthnResponse.sessionIndex()));
        AuthenticationContext context = authenticationContext(session);
        Person person = findPerson(idPAuthnResponse.uid()).orNull();
        context.assembledAttributes(nodeAttributeAssembler.assembleAttributes(context, new ResponseData(idPAuthnResponse, person)));
        context.levelOfAssurance(levelOfAssurance(idPAuthnResponse.securityLevel()));

        return validateAttributes(session);
    }

    @RequestMapping("/validate")
    private ModelAndView validateAttributes(HttpSession session) {
        AuthenticationContext context = authenticationContext(session);
        if(context.assembledAttributes().hasMissingRequiredAttributes()) {
            return errorRequiredAttributes(session, context);
        }

        return showAttributeConsent(session);
    }

    private ModelAndView errorRequiredAttributes(HttpSession session, AuthenticationContext context) {
        List<String> attributeMap = from(context.assembledAttributes().missingRequiredAttributes()).transform(
                attr -> "eidas-cidp-proxy.attributes.attribute." + attr.name().toLowerCase()
        ).toList();
        clearAuthenticationContext(session);
        return new ModelAndView(errorRequiredAttributes).addObject("attributeMap", attributeMap);
    }

    private Optional<Person> findPerson(String uid) {
        if (configProvider.isMfGatewayEnabled()) {
            return findMFPerson(uid);
        } else {
            return findDSFPerson(uid);
        }
    }

    private Optional<Person> findDSFPerson(String uid) {
        PersonLookupResult result = dsfService.bySsn(uid);
        if (result.status().equals(PersonLookupResult.Status.OK)) {
            return result.person();
        } else {
            return Optional.absent();
        }
    }

    private Optional<Person> findMFPerson(String uid) {
        PersonLookupResult result = mfService.lookup(uid);
        if (result.status().equals(PersonLookupResult.Status.OK)) {
            return result.person();
        } else {
            return Optional.absent();
        }
    }

    private void setLocale(HttpServletRequest request, HttpServletResponse response, IdPAuthnResponse idPAuthnResponse) {
        String culture = idPAuthnResponse.culture();
        if (culture != null && culture.matches("nb|nn|en|se")) {
            localeResolver.setLocale(request, response, new Locale(culture));
        }
    }

    @RequestMapping(value = "/rejectConsent", method = RequestMethod.GET)
    public ModelAndView rejectConsent(HttpSession session) {
        checkAuthentication(session);
        consentHandler.reject(authenticationContext(session));       
        return new ModelAndView("redirect:/saml/logout");
    }

    @RequestMapping(value = "/saml/logout", method = RequestMethod.GET)
    public ModelAndView processLogout(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws org.opensaml.xml.security.SecurityException, MessageDecodingException {
        clearAuthenticationContext(session);
        logoutHandler.handleLogout(request, response);
        return logoutSuccess();
    }

    @RequestMapping(value = "logoutSuccess", method = RequestMethod.GET)
    public ModelAndView logoutSuccess() {
        return new ModelAndView("logoutSuccess");
    }

    @RequestMapping(value = "showAttributeHelp", method = RequestMethod.GET)
    public ModelAndView showAtributeHelp(HttpSession session) {
        checkAuthentication(session);
        return new ModelAndView("showAttributeHelp");
    }

    @RequestMapping(value = showAttributeConsent, method = RequestMethod.GET)
    public ModelAndView showAttributeConsent(HttpSession session) {
        checkAuthentication(session);
        Preconditions.checkNotNull(authenticationContext(session).assembledAttributes());
        AuthenticationContext context = authenticationContext(session);

        List<String> attributes = from(context.assembledAttributes()).transform(
                attr -> "eidas-cidp-proxy.attributes.attribute." + attr.name().toLowerCase()
        ).toList();

        return new ModelAndView(showAttributeConsent).addObject("attributes", attributes);
    }

    @RequestMapping(value = "showAttributeDetails", method = RequestMethod.GET)
    public ModelAndView showAttributeDetails(HttpSession session) {
        checkAuthentication(session);
        AuthenticationContext context = authenticationContext(session);
        Preconditions.checkNotNull(context.assembledAttributes());

        Map<String, String> attributes = new LinkedHashMap<>(); // LinkedHashMap maintains attribute order for the view

        for (SubjectBasicAttribute entry : context.assembledAttributes()){
            NodeAttribute nodeAttribute = context.assembledAttributes().get(entry);
            if (nodeAttribute.isPresent() && !nodeAttribute.isHidden()) {
                String resourceAttribute = "eidas-cidp-proxy.attributes.attribute." + entry.name().toLowerCase();
                attributes.put(resourceAttribute, nodeAttribute.value().get());
            }
        }           	           	
        return new ModelAndView("showAttributeDetails").addObject("attributeMap", attributes);
    }

    /**
     * Samtykke er gitt, send assertion redirect til C-PEPS.
     */
    @RequestMapping(value = "acceptConsent", method = RequestMethod.GET)
    public ModelAndView acceptConsent(HttpSession session, HttpServletRequest request) throws EIDASSAMLEngineException, AttributeValueMarshallingException {
        checkAuthentication(session);
        AuthenticationContext context = authenticationContext(session);
        consentHandler.accept(context);
        String response = nodeResponseGenerator.generate(
                context.eidasRequest(),
                context.nodeRequest(),
                context.assembledAttributes(),
                context.levelOfAssurance(),
                request.getRemoteAddr()
        );
        clearAuthenticationContext(session);
        return new ModelAndView("forwardAuthnResponseToEidas")
                .addObject("assertionConsumerUrl", context.nodeRequest().assertionConsumerServiceAddress())
                .addObject("SAMLResponseToEidas", response)
                .addObject("userName", context.assembledAttributes().get(SubjectBasicAttribute.PersonIdentifier).value().get());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllException(Exception ex) {
        logger.error("Generic error", ex);
        return new ModelAndView(errorModel);
    }

    private AuthenticationContext authenticationContext(HttpSession session) {
        if (session.getAttribute(SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT) == null) {
            AuthenticationContext context = new AuthenticationContext();
            session.setAttribute(SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT, context);
            return context;
        }
        return (AuthenticationContext)session.getAttribute(SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT);
    }

    protected void clearAuthenticationContext(HttpSession session) {
        session.removeAttribute(SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT);
    }

    protected void checkAuthentication(HttpSession session) {
        IdportenSession activeSession = (IdportenSession) session.getAttribute(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE);
        if (activeSession == null) {
            clearAuthenticationContext(session);
            throw new IllegalStateException("Unauthenticatd request in session [" + session.getId() + "]");
        }
    }

    protected String levelOfAssurance(String securityLevel) {
        switch(securityLevel) {
            case "3":
                return LevelOfAssurance.SUBSTANTIAL.getValue();
            case "4":
                return LevelOfAssurance.HIGH.getValue();
            default:
                throw new IllegalArgumentException("Illegal argument security level for LevelOfAssurance mapper");
        }
    }

}
