package no.difi.eidas.sproxy.web;

import no.difi.eidas.sproxy.domain.attribute.AttributesConfigProvider;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnRequest;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnRequestReceiver;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnResponse;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnResponseCreator;
import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.domain.saml.SamlXml;
import no.difi.eidas.sproxy.integration.eidas.request.EidasAuthentication;
import no.difi.eidas.sproxy.integration.eidas.request.EidasAuthenticator;
import no.difi.eidas.sproxy.integration.eidas.response.EidasErrorResponse;
import no.difi.eidas.sproxy.integration.eidas.response.EidasResponse;
import no.difi.eidas.sproxy.integration.eidas.response.EidasResponseReceiver;
import no.difi.eidas.sproxy.integration.eidas.response.EidasSamlResponse;
import no.difi.opensaml.util.ConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public static final String path = "/auth";
    public static final String pathRedir = "/authRedir";
    public static final String pathCancel = "/cancel";

    public static final String samlRequestParam = "SAMLRequest";
    public static final String samlResponseParam = "SAMLResponse";
    public static final String countryParam = "countrycode";
    public static final String idPortenAuthnRequestSession = "idPortenSamlAuthnRequest";
    public static final String citizenCountryParamSession = "citizenCountrycode";

    private static final String requestAuthnView = "requestAuthentication";
    private static final String sendAuthnRequestToEidasView = "forwardAuthnRequestToEidas";
    private static final String sendAuthnResponseToIdPorten = "forwardAuthnToIdporten";

    private final ConvertUtil encoder;
    private final IdPortenAuthnRequestReceiver idPortenAuthnRequestReceiver;
    private final EidasAuthenticator eidasAuthenticator;
    private final EidasResponseReceiver eidasResponseReceiver;
    private final IdPortenAuthnResponseCreator idPortenAuthnResponseCreator;
    private final AttributesConfigProvider attributesConfigProvider;
    private final ObjectFactory<AuthState> authStateObjectFactory;

    @Autowired
    public AuthController(
            IdPortenAuthnRequestReceiver idPortenAuthnRequestReceiver,
            ConvertUtil convertUtil,
            EidasAuthenticator eidasAuthenticator,
            EidasResponseReceiver eidasResponseReceiver,
            IdPortenAuthnResponseCreator idPortenAuthnResponseCreator,
            AttributesConfigProvider attributesConfigProvider,
            ObjectFactory<AuthState> authStateObjectFactory) {
        this.idPortenAuthnRequestReceiver = idPortenAuthnRequestReceiver;
        this.encoder = convertUtil;
        this.eidasAuthenticator = eidasAuthenticator;
        this.eidasResponseReceiver = eidasResponseReceiver;
        this.idPortenAuthnResponseCreator = idPortenAuthnResponseCreator;
        this.attributesConfigProvider = attributesConfigProvider;
        this.authStateObjectFactory = authStateObjectFactory;
    }

    @RequestMapping(value = AuthController.path, method = RequestMethod.GET)
    public ModelAndView requestAuthFromIdPortenChooseCountry(
            @RequestParam(value = AuthController.samlRequestParam, required = false) String samlZippedBase64,
            @RequestParam(value = "identitymatch", required = false) Optional<String> identityMatch,
            HttpServletRequest request
    ) {
        Objects.requireNonNull(samlZippedBase64, "Missing saml parameter");
        String saml = encoder.decodeBase64AndUnzip(samlZippedBase64);
        receiveAuthnRequest(new SamlXml(saml), request);

        identityMatch.ifPresent(i ->
                authStateObjectFactory.getObject().setIdentityMatch(Arrays.asList(StringUtils.delimitedListToStringArray(i, " ")))
        );

        return new ModelAndView(requestAuthnView).addObject("countries", attributesConfigProvider.countries());
    }

    @RequestMapping(value = AuthController.pathRedir, method = RequestMethod.POST)
    public ModelAndView sendAuthnRequestToEidas(
            @RequestParam(value = AuthController.countryParam, required = false) String countryCode,
            HttpServletRequest request
    ) {
        Objects.requireNonNull(countryCode, "countryCode");
        authStateObjectFactory.getObject().setCountryCode(countryCode);
        IdPortenAuthnRequest idPortenAuthRequest = getAuthnRequestFromSession(request);
        EidasAuthentication authentication = eidasAuthenticator.authenticate(
                idPortenAuthRequest,
                new CountryCode(countryCode)
        );
        putCitizenCountryCodeInSession(request, countryCode);
        return new ModelAndView(sendAuthnRequestToEidasView)
                .addObject("country", countryCode)
                .addObject("authnRequest", authentication.authnRequest().toString())
                .addObject("eidasNodeUrl", authentication.eidasNodeUrl());
    }

    @RequestMapping(value = AuthController.path, method = RequestMethod.POST)
    public ModelAndView receiveAuthnResponseFromEidasNode(
            HttpServletRequest request,
            @RequestParam(samlResponseParam) String saml) throws EidasErrorResponse {
        IdPortenAuthnRequest authnRequest = getAuthnRequestFromSession(request);
        String citizenCountryCode = getCitizenCountryCodeFromSession(request);
        EidasResponse eidasResponse = eidasResponseReceiver.receive(new EidasSamlResponse(saml), citizenCountryCode, authnRequest);
        IdPortenAuthnResponse response = idPortenAuthnResponseCreator.create(eidasResponse, authnRequest);
        encoder.zipAndEncodeBase64(response.toString());
        request.getSession().invalidate();
        return new ModelAndView(sendAuthnResponseToIdPorten)
                .addObject("authnResponse", encoder.zipAndEncodeBase64(response.toString()))
                .addObject("consumerServiceUrl", authnRequest.getAssertionConsumerServiceURL());
    }

    @RequestMapping(value = AuthController.pathCancel, method = RequestMethod.GET)
    public ModelAndView cancelAuthentication(HttpServletRequest request) {
        IdPortenAuthnRequest authnRequest = getAuthnRequestFromSession(request);
        IdPortenAuthnResponse response = idPortenAuthnResponseCreator.createUserCancelledResponse(authnRequest);
        return new ModelAndView(sendAuthnResponseToIdPorten)
                .addObject("authnResponse", encoder.zipAndEncodeBase64(response.toString()))
                .addObject("consumerServiceUrl", authnRequest.getAssertionConsumerServiceURL());
    }

    @ExceptionHandler
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ModelAndView handleEidasErrorResponse(EidasErrorResponse eidasErrorResponse) {
        if (eidasErrorResponse.getCause() != null) {
            log.error("Eidas SAML engine failed", eidasErrorResponse);
        }
        if (log.isInfoEnabled()) {
            log.info(eidasErrorResponse.getMessage());
        }
        return new ModelAndView("eidasErrorResponse")
                .addObject("eidasErrorResponse", eidasErrorResponse);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllException(Exception ex) {
        log.error("Generic error", ex);
        return new ModelAndView("error");
    }

    private void receiveAuthnRequest(SamlXml saml, HttpServletRequest request) {
        request.getSession().setAttribute(
                idPortenAuthnRequestSession,
                idPortenAuthnRequestReceiver.receive(saml)
        );
    }

    private IdPortenAuthnRequest getAuthnRequestFromSession(HttpServletRequest request) {
        IdPortenAuthnRequest authnRequest = (IdPortenAuthnRequest) request.getSession().getAttribute(idPortenAuthnRequestSession);
        Objects.requireNonNull(authnRequest, "Missing authnRequest in Session");
        return authnRequest;
    }

    private void putCitizenCountryCodeInSession(HttpServletRequest request, String citizenCountryCode) {
        Objects.requireNonNull(citizenCountryCode, "Missing citizenCountryCode");
        request.getSession().setAttribute(citizenCountryParamSession, citizenCountryCode);
    }

    private String getCitizenCountryCodeFromSession(HttpServletRequest request) {
        String citizenCountryCode = (String) request.getSession().getAttribute(citizenCountryParamSession);
        Objects.requireNonNull(citizenCountryCode, "Missing citizenCountryCode in session");
        return citizenCountryCode;
    }
}