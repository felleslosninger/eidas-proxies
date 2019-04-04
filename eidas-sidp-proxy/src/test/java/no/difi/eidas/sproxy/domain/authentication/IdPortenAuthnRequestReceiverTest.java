package no.difi.eidas.sproxy.domain.authentication;

import no.difi.eidas.idpproxy.test.SamlBootstrap;
import no.difi.eidas.sproxy.IdportenTestKey;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.saml.IdPortenKeyProvider;
import no.difi.eidas.sproxy.domain.saml.SamlXml;
import no.difi.opensaml.signature.SamlSigner;
import no.difi.opensaml.util.SAMLUtil;
import no.difi.opensaml.util.XMLFormatter;
import no.difi.opensaml.wrapper.AuthnRequestWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.validation.ValidationException;

import java.io.IOException;
import java.time.ZonedDateTime;

import static no.difi.eidas.sproxy.ResourceReader.idPortenAuthnRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdPortenAuthnRequestReceiverTest {

    private IdPortenAuthnRequestReceiver reader;

    @Mock
    private AuditLog auditLog;

    @Before
    public void setUp() {
        SamlBootstrap.init();
        reader = reader(auditLog);
    }

    @Test
    public void receivedAuthnRequestValidatesAndLogsAuditTrail() throws ConfigurationException, ValidationException {
        SamlXml samlXml = new SamlXml(idPortenAuthnRequest());
        IdPortenAuthnRequest authRequest = reader.receive(samlXml);
        assertThat(authRequest.getID(), is(equalTo("_de1ec29db56a6d3fedd5840cc44a1769")));
        assertThat(authRequest.authnContextClassRef(), is(equalTo("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport")));
        verify(auditLog).idPortenSamlAuthnRequest(samlXml);
    }

    @Test
    public void sign() throws IOException {
        IdPortenKeyProvider idPortenKeyProvider = mock(IdPortenKeyProvider.class);
        when(idPortenKeyProvider.privateKey()).thenReturn(IdportenTestKey.privateKey());
        AuthnRequestWrapper wrapper = new AuthnRequestWrapper(idPortenAuthnRequest());
        AuthnRequest request = wrapper.getOpenSAMLObject();
        SamlSigner.sign(request, idPortenKeyProvider.privateKey());
        XMLFormatter formatter = new XMLFormatter();
        String xml = formatter.format(request);
        SamlXml samlXml = new SamlXml(xml);
        AuthnRequestWrapper authRequest = reader.receive(samlXml);
    }

    @Test
    public void foo() {
        IdportenTestKey.publicKey();
        IdportenTestKey.privateKey();
    }

    private static IdPortenAuthnRequestReceiver reader() {
        return reader(mock(AuditLog.class));
    }

    private static IdPortenAuthnRequestReceiver reader(AuditLog auditLog) {
        ConfigProvider configProvider = mock(ConfigProvider.class);
        SAMLUtil samlUtil = mock(SAMLUtil.class);
        when(samlUtil.validateIssueInstant(any(ZonedDateTime.class), any(Integer.class), any(Integer.class))).thenReturn(true);
        return new IdPortenAuthnRequestReceiver(configProvider, samlUtil, auditLog, idPortenKeyProvider());
    }

    public static IdPortenKeyProvider idPortenKeyProvider() {
        IdPortenKeyProvider idPortenKeyProvider = mock(IdPortenKeyProvider.class);
        when(idPortenKeyProvider.publicKey()).thenReturn(IdportenTestKey.publicKey());
        return idPortenKeyProvider;
    }

    // authn request eksponert til andre testene
    public static IdPortenAuthnRequest idPortenSamlAuthnRequest() {
        SamlXml samlXml = new SamlXml(idPortenAuthnRequest());
        return reader().receive(samlXml);
    }


}
