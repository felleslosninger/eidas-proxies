package no.difi.eidas.cproxy.domain.idp;

import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.saml.CIDPProxyKeyProvider;
import no.difi.eidas.idpproxy.test.SamlBootstrap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml2.metadata.provider.MetadataProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LogoutHandlerTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    MetadataProvider idpMetadataProvider;
    @Mock
    MetadataProvider spMetadataProvider;
    @Mock
    IDPSSODescriptor idpssoDescriptor;
    @InjectMocks
    ConfigProvider configProvider;
    @Mock
    SingleLogoutService singleLogoutService;
    @Mock
    CIDPProxyKeyProvider cidpProxyKeyProvider;
    @Mock
    HttpServletResponse httpServletResponse;
    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    HttpSession httpSession;

    private static final String URL_PARAMETER_SAML_REQUEST = "SAMLRequest";
    private static final String URL_PARAMETER_SAML_RESPONSE = "SAMLResponse";
    private static final String SAMLRESPONSE = "nVI7b8IwEN75FZH3EDuBgE8EqSoLEl0KYuhSHfZBIwU7yjkV/fdNolalDAyM9/oed7dgPFc1bPzJt+GVuPaOKYou58oxDLVCtI0Dj1wyODwTQzCwfXrZQDqWUDc+eOMrMVqvCsE6l4hWozVqOskO1mRGHyapyo8mtwpzzJShmVYi2lPDpXeF6FBEtGZuae04oAtdSqppLGexnO/kDFINSr2JaEUcSodhmPoIoYYkodLGn3iKfU2O2Y9teSzHlTdYgZJS6r4BOTalreNO6uUr6T0l1WC3o3W/lne+EO+a7FynqUn1lDJ1VHoi5VRbmx/kPMtIieVo0c/DILe52tL9JSEzNb1usezENp3HuBO0SK6wfpBr2AYMLT9wgBuEZ28fu+Meq5buN/PAANvWGGLumZNb6tvcX/z/1Zbf";
    private static final String SAMLREQUEST = "nZJRb9sgFIX/isW7Y7BNjFFiqVpaLWqbaGtXqXnDBjpXNnhc3GX99cNOJ6V76ENfL5xzvnNhBaLvBn5jn+zov6tfowIfRce+M8DnozUaneFWQAvciF4B9w2/u7i94ekC88FZbxvboWi7WSNINc2ajNU1yRqal1hnUoiCaElLKZdCLVme4mXNShQ9KAetNWsUbIIaYFRbA14YH0aY0BgXMWb3hPCccVweULQJaK0Rflb99H7gSaJaGb+Ip9gOygDYhWx1u+hsIzpOMMbldEFA3LRyiAPr8U8ylUq6uS6Kdtbvzd5daK/c/7H0FFutJgWfAd3ZYj7eiwBQbiJFVcBzoVUcEFbJmdeb8S5ot5tPGEeT8tsoulB5oj/LQdGVdb3wH1tNk7A+PV/lPoihVcajin0pn3vDDvvd4erH79ev5a1Vy0v8eK3v928VTtSnCgO/UzC95dZIdfzE36kgpZLkqS4oKTCWmVYspaqRTaGlrJWidVGwTOeYnOLfJ1b/hu8+cfUX";

    @Before
    public void setUp() throws Exception {
        SamlBootstrap.init();

    }

    //Liten bug i opensaml gjere at dette fungerer utan signatur.
    @Test
    public void testHandleLogoutResponse() throws Exception {

        when(httpServletRequest.getParameter(URL_PARAMETER_SAML_RESPONSE)).thenReturn(SAMLRESPONSE);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://eid-vag-opensso.difi.local:10009/eidas-cidp-proxy/saml/logout"));
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttributeNames()).thenReturn(Collections.enumeration(Arrays.asList(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE)));
        LogoutHandler logoutHandler = new LogoutHandler(configProvider, cidpProxyKeyProvider);
        logoutHandler.handleLogout(httpServletRequest, httpServletResponse);
        verify(httpSession, times(1)).removeAttribute(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE);
        verify(httpSession).invalidate();
    }

    @Test
    public void testHandleLogoutRequest() throws Exception {

        when(httpServletRequest.getParameter(URL_PARAMETER_SAML_REQUEST)).thenReturn(SAMLREQUEST);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://eid-vag-opensso.difi.local:10009/eidas-cidp-proxy/saml/logout"));
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttributeNames()).thenReturn(Collections.enumeration(Arrays.asList(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE)));
        LogoutHandler logoutHandler = new LogoutHandler(configProvider, cidpProxyKeyProvider);
        logoutHandler.handleLogout(httpServletRequest, httpServletResponse);
        verify(httpSession, times(1)).removeAttribute(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE);
        verify(httpSession).invalidate();
    }

    @Test
    public void testInitLogout() throws Exception {

        IdportenSession idportenSession = new IdportenSession("nameid","sessionindex");
        when(httpSession.getAttribute(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE)).thenReturn(idportenSession);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        LogoutHandler logoutHandler = new LogoutHandler(configProvider, cidpProxyKeyProvider);
        logoutHandler.handleLogout(httpServletRequest, httpServletResponse);
        verify(httpServletResponse, times(1)).sendRedirect(anyString());
    }

    @Test(expected=RuntimeException.class)
    public void testInitLogoutNotLoggedIn() throws Exception {
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        LogoutHandler logoutHandler = new LogoutHandler(configProvider, cidpProxyKeyProvider);
    logoutHandler.handleLogout(httpServletRequest, httpServletResponse);
    }

}