package no.difi.eidas.cproxy.domain.idp;

import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import no.difi.eidas.cproxy.TestData;
import no.difi.eidas.cproxy.TestKeyProvider;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.cproxy.saml.CIDPProxyKeyProvider;
import no.difi.eidas.idpproxy.test.SamlBootstrap;
import no.idporten.log.audit.AuditLogger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdPortenAuthnRequestCreatorTest {
    private static final DateTime instant = new DateTime(2015, 7, 24, 12, 26, 0, DateTimeZone.UTC);
    private static final String correlationId = "someId";
    @Mock
    private NodeAuthnRequest nodeAuthnRequest;

    @Mock
    private ConfigProvider configProvider;

    @Mock
    private CIDPProxyKeyProvider cidpProxyKeyProvider;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private InstantIssuer instantIssuer;

    @Captor
    private ArgumentCaptor<String> messageId;

    @Captor
    private ArgumentCaptor<String> authnRequestXml;

    @Captor
    private ArgumentCaptor<String> ssn;

    private IdPortenAuthnRequestCreator idPortenAuthnRequestCreator;

    @Before
    public void setUp() {
        SamlBootstrap.init();
        when(nodeAuthnRequest.correlationId()).thenReturn(correlationId);
        when(nodeAuthnRequest.forceAuthn()).thenReturn(true);
        when(nodeAuthnRequest.levelOfAssurance()).thenReturn(LevelOfAssurance.SUBSTANTIAL.getValue());
        when(instantIssuer.now()).thenReturn(instant);
        idPortenAuthnRequestCreator = new IdPortenAuthnRequestCreator(
                configProvider,
                cidpProxyKeyProvider,
                new AuditLog(auditLogger),
                instantIssuer
        );
    }

    @Test
    public void idPortenRequestCreated() throws IOException {
        AuthnRequest authnRequest = idPortenAuthnRequestCreator.create(nodeAuthnRequest);
        assertThat(authnRequest.isForceAuthn(), is(true));
        assertThat(authnRequest.getID(), is(equalTo(correlationId)));
    }

    @Test
    public void auditLogged() {
        idPortenAuthnRequestCreator.create(nodeAuthnRequest);
        verify(auditLogger).log(messageId.capture(), ssn.capture(), authnRequestXml.capture());
        assertThat(messageId.getValue(), is(equalTo(AuditLog.AuditMessages.requestToIdPorten.id())));
        assertThat(ssn.getValue(), is(equalTo(AuditLog.ingenSsn)));
        assertThat(authnRequestXml.getValue(), is(equalTo(TestData.mockedIdPortenAuthnRequest())));
    }

    @Test
    public void testBuildIdportenRequestWithSecurityLevel4 () {
        when(nodeAuthnRequest.levelOfAssurance()).thenReturn(LevelOfAssurance.HIGH.getValue());
        AuthnRequest authnRequest = idPortenAuthnRequestCreator.create(nodeAuthnRequest);
        assertThat(authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().size(), is(1));
        assertThat(
                authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef(),
                is(equalTo(AuthnContext.SMARTCARD_PKI_AUTHN_CTX))
        );
    }

    @Test
    public void testBuildIdportenRequestWithSecurityLevel3 () {
        when(nodeAuthnRequest.levelOfAssurance()).thenReturn(LevelOfAssurance.SUBSTANTIAL.getValue());
        AuthnRequest authnRequest = idPortenAuthnRequestCreator.create(nodeAuthnRequest);
        assertThat(authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().size(), is(1));
        assertThat(
                authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef(),
                is(equalTo(AuthnContext.PPT_AUTHN_CTX))
        );

        assertThat(
                authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef(),
                is(equalTo(AuthnContext.PPT_AUTHN_CTX))
        );
    }

    @Test(expected = NullPointerException.class)
    public void testBuildIdportenRequestWithInvalidLevelOfAssurance () {
        when(nodeAuthnRequest.levelOfAssurance()).thenReturn("INVALID");
        AuthnRequest authnRequest = idPortenAuthnRequestCreator.create(nodeAuthnRequest);
    }
}
