package no.difi.eidas.sproxy.integration.eidas.response;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.engine.Correlated;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.auth.engine.xml.opensaml.CorrelatedResponse;
import no.difi.eidas.sproxy.ResourceReader;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.authentication.AuthenticationLevel;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnRequest;
import no.difi.eidas.sproxy.domain.eventlog.EventLog;
import no.difi.opensaml.wrapper.ResponseWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.DefaultBootstrap;

import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EidasResponseReceiverTest {
    private static final String ip = "192.168.11.1";
    private static final String citizenCountryCode = "NO";

    @Mock
    private ConfigProvider configProvider;
    @Mock
    private AuditLog auditLog;
    @Mock
    private EventLog eventLog;
    @Mock
    private IpProvider ipProvider;
    @Mock
    private ProtocolEngineI samlEngine;
    @InjectMocks
    private EidasResponseReceiver responseReceiver;

    @Before
    public void setUp() throws Exception {
        DefaultBootstrap.bootstrap();
        when(configProvider.instantIssueTimeSkew()).thenReturn(1000000);
        when(ipProvider.ip()).thenReturn(ip);
    }

    @Test
    public void responseValidatedEidasResponseCreated() throws Exception {
        String personIdentifier = "CE/NO/12345";
        EidasSamlResponse response = new EidasSamlResponse(EidasStringUtil.encodeToBase64(ResourceReader.eidasAuthnResponse().getBytes()));

        IAuthenticationResponse validatedResponse = mock(IAuthenticationResponse.class);
        when(validatedResponse.getStatusCode()).thenReturn(EIDASStatusCode.SUCCESS_URI.toString());
        when(validatedResponse.getLevelOfAssurance()).thenReturn(LevelOfAssurance.HIGH.stringValue());
        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();
        mapBuilder.put(EidasSpec.Definitions.PERSON_IDENTIFIER,
                EidasSpec.Definitions.PERSON_IDENTIFIER.unmarshal(personIdentifier, false));
        when(validatedResponse.getAttributes()).thenReturn(mapBuilder.build());

        CorrelatedResponse correlatedResponse = correlatedResponse();
        when(samlEngine.unmarshallResponse(any(byte[].class))).thenReturn(correlatedResponse);
        when(samlEngine.validateUnmarshalledResponse(correlatedResponse, "192.168.11.1", 1000000,1000000, null)).thenReturn(validatedResponse);

        IdPortenAuthnRequest idPortenAuthnRequest = mock(IdPortenAuthnRequest.class);
        when(idPortenAuthnRequest.authnContextClassRef()).thenReturn(AuthenticationLevel.LEVEL3.idPortenAuthnContextClassRef());

        EidasResponse eidasResponse = responseReceiver.receive(response, citizenCountryCode, idPortenAuthnRequest);
        assertEquals(AuthenticationLevel.LEVEL4.idPortenAuthnContextClassRef(), eidasResponse.authnContextClassRef());
        assertEquals(personIdentifier, eidasResponse.personIdentifier());
    }

    protected CorrelatedResponse correlatedResponse() throws Exception {
        return new CorrelatedResponse(new ResponseWrapper(ResourceReader.eidasAuthnResponse()).getOpenSAMLObject());
    }

    @Test
    public void testValidateAuthenticationLevelEqual() throws Exception {
        for (AuthenticationLevel authenticationLevel : AuthenticationLevel.list()) {
            responseReceiver.validateAuthenticationLevel(authenticationLevel.eidasLevelOfAssurance().stringValue(), authenticationLevel.idPortenAuthnContextClassRef());
        }
    }

    @Test
    public void testValidateAuthenticationLevelGreaterThan() throws Exception {
        responseReceiver.validateAuthenticationLevel(AuthenticationLevel.LEVEL4.eidasLevelOfAssurance().stringValue(), AuthenticationLevel.LEVEL3.idPortenAuthnContextClassRef());
    }

    @Test(expected = EidasErrorResponse.class)
    public void testValidateAuthenticationLevelLessThan() throws Exception {
        responseReceiver.validateAuthenticationLevel(AuthenticationLevel.LEVEL3.eidasLevelOfAssurance().stringValue(), AuthenticationLevel.LEVEL4.idPortenAuthnContextClassRef());
    }

    @Test(expected = EidasErrorResponse.class)
    public void testValidateAuthenticationLevelUnknown() throws Exception {
        responseReceiver.validateAuthenticationLevel("kvakk", AuthenticationLevel.LEVEL4.idPortenAuthnContextClassRef());
    }

    @Test
    public void testValidateStatusWhenResponseHasFailures() throws Exception {
        IAuthenticationResponse response = mock(IAuthenticationResponse.class);
        when(response.isFailure()).thenReturn(true);
        try {
            responseReceiver.validateStatus(response);
            fail();
        } catch (EidasErrorResponse e) {
            assertTrue(e.getMessage().startsWith("Invalid status code from eIDAS"));
        }
    }

    @Test
    public void testValidateStatusWhenResponseHasFailuresAndAStatusMessage() throws Exception {
        IAuthenticationResponse response = mock(IAuthenticationResponse.class);
        when(response.isFailure()).thenReturn(true);
        when(response.getStatusMessage()).thenReturn("statusMessage");
        try {
            responseReceiver.validateStatus(response);
            fail();
        } catch (EidasErrorResponse e) {
            assertEquals("statusMessage", e.getMessage());
        }
    }

    @Test(expected = EidasErrorResponse.class)
    public void testValidateUnsuccessfulStatus() throws Exception {
        IAuthenticationResponse response = mock(IAuthenticationResponse.class);
        when(response.getStatusCode()).thenReturn("foo");
        responseReceiver.validateStatus(response);
    }

    @Test
    public void testValidateSuccessStatus() throws Exception {
        IAuthenticationResponse response = mock(IAuthenticationResponse.class);
        when(response.getStatusCode()).thenReturn("urn:oasis:names:tc:SAML:2.0:status:Success");
        responseReceiver.validateStatus(response);
    }

}
