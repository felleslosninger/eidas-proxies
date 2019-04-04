package no.difi.eidas.sproxy.web;

import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnRequest;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnResponseCreator;
import no.difi.eidas.sproxy.domain.authentication.InstantIssuer;
import no.difi.eidas.sproxy.domain.saml.IdportenSAMLConstants;
import no.difi.eidas.sproxy.integration.mf.MFService;
import no.difi.opensaml.util.ConvertUtil;
import no.difi.opensaml.wrapper.ResponseWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensaml.DefaultBootstrap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AuthControllerTest {

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    HttpSession httpSession;

    @Mock
    IdPortenAuthnRequest idPortenAuthnRequest;

    @Mock
    AuditLog auditLog;

    IdPortenAuthnResponseCreator idPortenAuthnResponseCreator;

    AuthController controller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        DefaultBootstrap.bootstrap();
        AuthState authState = new AuthState();
        ObjectFactory<AuthState> authStateObjectFactory = () -> authState;
        idPortenAuthnResponseCreator = new IdPortenAuthnResponseCreator(
                new ConfigProvider(),
                new InstantIssuer(),
                null,
                null,
                auditLog,
                authStateObjectFactory,
                new MFService(null, null, null, null));
        controller = new AuthController(null,
                new ConvertUtil(),
                null,
                null,
                idPortenAuthnResponseCreator,
                null,
                authStateObjectFactory);
        when(idPortenAuthnRequest.getID()).thenReturn("requestId");
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute("idPortenSamlAuthnRequest")).thenReturn(idPortenAuthnRequest);
    }


    @Test
    public void testCancelAuthentication() {
        ModelAndView mv = controller.cancelAuthentication(httpServletRequest);
        String encodedAuthnResponse = (String) mv.getModel().get("authnResponse");
        String responseXml = new ConvertUtil().decodeBase64AndUnzip(encodedAuthnResponse);
        ResponseWrapper response = new ResponseWrapper(responseXml);
        assertEquals("requestId", response.getInResponseTo());
        assertEquals(IdportenSAMLConstants.SAML_RESPONSE_USER_CANCELLED_STATUS_CODE, response.getStatusCode());
    }


}