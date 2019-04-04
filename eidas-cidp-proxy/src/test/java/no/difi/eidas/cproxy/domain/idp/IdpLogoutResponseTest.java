package no.difi.eidas.cproxy.domain.idp;

import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.idpproxy.test.SamlBootstrap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml2.metadata.provider.MetadataProvider;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by est on 07.07.2015.
 */
public class IdpLogoutResponseTest {
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
    LogoutRequest logoutRequest;

    @Before
    public void setUp() throws Exception {
        SamlBootstrap.init();
        ArrayList<SingleLogoutService> slos = new ArrayList<>();
        slos.add(singleLogoutService);
        when(idpssoDescriptor.getSingleLogoutServices()).thenReturn(slos);
        when(singleLogoutService.getLocation()).thenReturn("destination");
        when(singleLogoutService.getBinding()).thenReturn(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        //when(configProvider.spEntityID()).thenReturn("spentityid");
        when(logoutRequest.getID()).thenReturn("logoutrequestid");
    }

    @Test
    public void testGetDestination() throws Exception {
        IdpLogoutResponse idpLogoutResponse = new IdpLogoutResponse(logoutRequest);
        idpLogoutResponse.buildLogoutResponse(configProvider);
        assertTrue(idpLogoutResponse.getDestination().equals("destination"));


    }

    @Test
    public void testGetRawXML() throws Exception {
        IdpLogoutResponse idpLogoutResponse = new IdpLogoutResponse(logoutRequest);
        idpLogoutResponse.buildLogoutResponse(configProvider);
        assertTrue(idpLogoutResponse.getRawXML().contains("destination"));
        assertTrue(idpLogoutResponse.getRawXML().contains("logoutrequestid"));
    }
}