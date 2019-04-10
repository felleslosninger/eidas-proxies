package no.difi.eidas.cproxy.web;


import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import no.difi.eidas.cproxy.AbstractBaseTest;
import no.difi.eidas.cproxy.OIDCTestKeyProvider;
import no.difi.eidas.cproxy.config.OIDCProperties;
import no.difi.eidas.cproxy.domain.authentication.AuthenticationContext;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.cproxy.domain.node.NodeRequestedAttributes;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.time.Clock;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AbstractBaseTest.EidasCproxyTest
public class OIDCControllerTest extends AbstractBaseTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OIDCProperties properties;

    @Autowired
    private MockMvc mvc;

    private String nonce;

    @Before
    public void setup() {
        when(restTemplate.exchange(
                eq(properties.getMetadata().getTokenEndpointURI()),
                eq(HttpMethod.POST),
                any(),
                eq(new ParameterizedTypeReference<Map<String,String>>() {})
        ))
                .thenAnswer((i) -> ResponseEntity.ok(new HashMap<String, String>() {{
                    put("access_token", "tolkien");
                    put("id_token", makeJwtGrant("test"));

                }}));
    }

    @Test
    public void authorizeTest() throws Exception {
        MvcResult mvcResult = mvc.perform(get("/oidc/start"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String redirectedUrl = URLDecoder.decode(
                new URI(mvcResult.getResponse().getRedirectedUrl()).getQuery(),
                mvcResult.getResponse().getCharacterEncoding());

        MultiValueMap<String, String> parameters = queryParameters(mvcResult.getResponse().getRedirectedUrl());
        this.nonce = parameters.getFirst("nonce");
        assertTrue(parameters.containsKey("client_id"));
        assertTrue(redirectedUrl.contains("login_hint=amr:mobileconnect"));
        assertTrue(redirectedUrl.contains("acr_values=Level1"));
        assertTrue(redirectedUrl.contains("response_type=code"));
        assertTrue(redirectedUrl.contains("state="));
        assertTrue(redirectedUrl.contains("nonce="));

        AuthenticationContext context = new AuthenticationContext();
        context.nodeRequest(NodeAuthnRequest.builder()
                .requestedAttributes(NodeRequestedAttributes.builder()
                        .required(SubjectBasicAttribute.PersonIdentifier)
                        .required(SubjectBasicAttribute.CurrentFamilyName)
                        .required(SubjectBasicAttribute.CurrentGivenName)
                        .required(SubjectBasicAttribute.DateOfBirth)
                        .build())
                .build());

        mvc.perform(get("/oidc/callback")
                .param("code", "code")
                .param("state", "state")
                .sessionAttr(CitizenController.SESSION_ATTRIBUTE_AUTHENTICATION_CONTEXT, context)
        )
                .andExpect(status().is3xxRedirection());


        assertEquals(LevelOfAssurance.LOW.getValue(), context.levelOfAssurance());
        assertTrue(context.assembledAttributes().get(SubjectBasicAttribute.PersonIdentifier).isPresent());
        assertTrue(context.assembledAttributes().get(SubjectBasicAttribute.CurrentFamilyName).isPresent());
        assertTrue(context.assembledAttributes().get(SubjectBasicAttribute.CurrentGivenName).isPresent());
        assertTrue(context.assembledAttributes().get(SubjectBasicAttribute.DateOfBirth).isPresent());
    }

    private MultiValueMap<String, String> queryParameters(String redirectedUrl) throws URISyntaxException {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        String[] split = new URI(redirectedUrl)
                .getQuery().split("&");
        for (String parts : split) {
            String[] keyValue = parts.split("=");
            parameters.add(keyValue[0], keyValue[1]);
        }
        return parameters;
    }

    public String makeJwtGrant(String subject) {

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .audience(properties.getClientId())
                .subject(subject)
                .issuer(properties.getMetadata().getIssuer().getValue())
                .claim("nonce", nonce)
                .claim("scope", "openid profile")
                .claim("givenName", "givenName")
                .claim("familyName", "familyName")
                .claim("birthday", "2000-01-01")
                .claim("pid", "23079422568")
                .jwtID(UUID.randomUUID().toString())
                .issueTime(now())
                .expirationTime(exp(now()))
                .build();

        try {
            return signClaims(claims).serialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SignedJWT signClaims(JWTClaimsSet claims) throws Exception {
        JWSSigner signer = new RSASSASigner(OIDCTestKeyProvider.privateKey.getPrivateKey());
        SignedJWT signedJWT = new SignedJWT(jwtHeader(), claims);
        signedJWT.sign(signer);
        return signedJWT;
    }

    private JWSHeader jwtHeader() throws Exception {
        List<Base64> certChain = new ArrayList<>();
        certChain.add(Base64.encode(OIDCTestKeyProvider.privateKey.getCertificate().getEncoded()));

        return new JWSHeader.Builder(JWSAlgorithm.RS256).x509CertChain(certChain).build();
    }

    private Date now() {
        long now = Clock.systemUTC().millis();
        return new Date(now);
    }

    private Date exp(Date iat) {
        return new Date(iat.getTime() + (110 * 1000));
    }


}
