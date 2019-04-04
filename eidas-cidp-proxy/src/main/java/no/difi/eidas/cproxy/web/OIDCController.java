package no.difi.eidas.cproxy.web;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.claims.ACR;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import no.difi.eidas.cproxy.common.FormDataHttpEntity;
import no.difi.eidas.cproxy.common.validation.IdTokenValidatorBuilder;
import no.difi.eidas.cproxy.common.validation.PredicateValidator;
import no.difi.eidas.cproxy.config.OIDCProperties;
import no.difi.eidas.cproxy.domain.authentication.AuthenticationContext;
import no.difi.eidas.cproxy.domain.idp.IdportenSession;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.integration.oidc.OIDCNodeAttributeAssembler;
import no.difi.eidas.cproxy.integration.oidc.OIDCState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

import static no.difi.eidas.cproxy.common.FormDataHttpEntity.basic;
import static no.difi.eidas.cproxy.web.CitizenController.SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT;

@Controller
@RequestMapping("/oidc")
public class OIDCController {

    @Autowired
    private OIDCState state;

    @Autowired
    private OIDCProperties properties;

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private OIDCNodeAttributeAssembler oidcNodeAttributeAssembler;

    @RequestMapping("/start")
    public void initiate(HttpServletResponse response) throws IOException {

        AuthenticationRequest level1 = new AuthenticationRequest.Builder(
                new ResponseType(ResponseType.Value.CODE),
                Scope.parse("openid profile"),
                new ClientID(properties.getClientId()),
                URI.create(properties.getRedirectUri()))
                .acrValues(Arrays.asList(new ACR("Level1")))
                .nonce(state.getNonce())
                .state(state.getState())
                .endpointURI(properties.getMetadata().getAuthorizationEndpointURI())
                .loginHint("amr:mobileconnect").build();

        response.sendRedirect(level1.toURI().toString());

    }

    @RequestMapping("/callback")
    public String response(@RequestParam String code, HttpSession session) throws ParseException {

        ResponseEntity<Map<String, String>> tokenResponse = restTemplate.exchange(
                properties.getMetadata().getTokenEndpointURI(),
                HttpMethod.POST,
                FormDataHttpEntity.builder()
                        .header(HttpHeaders.AUTHORIZATION, basic(properties.getClientId(), properties.getClientSecret()))
                        .param("code", code)
                        .param("grant_type", "authorization_code")
                        .param("redirect_uri", properties.getRedirectUri())
                        .build(),
                new ParameterizedTypeReference<Map<String, String>>() {
                }
        );


        SignedJWT idToken = getIdToken(tokenResponse);

        AuthenticationContext context = authenticationContext(session);

        NodeAttributes assembleAttributes = oidcNodeAttributeAssembler.assembleAttributes(context, idToken);
        context.assembledAttributes(assembleAttributes);
        context.levelOfAssurance(LevelOfAssurance.LOW.getValue());
        session.setAttribute(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE, new IdportenSession(idToken.getJWTClaimsSet().getJWTID(), idToken.getJWTClaimsSet().getIssuer()));

        return "redirect:/validate";
    }


    private SignedJWT getIdToken(ResponseEntity<Map<String, String>> tokenResponse) {
        SignedJWT idToken;
        try {
            idToken = SignedJWT.parse(tokenResponse.getBody().get("id_token"));
        } catch (ParseException e) {
            throw new RuntimeException("can't parse id token");
        }

        PredicateValidator<SignedJWT> idTokenValidator;
        idTokenValidator = new IdTokenValidatorBuilder()
                .audience(properties.getClientId())
                .rsaKey((RSAKey)properties.getOIDCProviderJWKSet().getKeys().get(0))
                .issuer(properties.getMetadata().getIssuer().getValue())
                .build();

        if (!idTokenValidator.test(idToken)) {
            throw new RuntimeException("id token is invalid");
        }
        return idToken;
    }

    private AuthenticationContext authenticationContext(HttpSession session) {
        if (session.getAttribute(SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT) == null) {
            AuthenticationContext context = new AuthenticationContext();
            session.setAttribute(SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT, context);
            return context;
        }
        return (AuthenticationContext)session.getAttribute(SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT);
    }

}
