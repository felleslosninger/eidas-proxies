package no.difi.eidas.cproxy.domain.authentication;

import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.domain.event.EventLog;
import no.difi.eidas.cproxy.domain.idp.IdPAuthnResponse;
import no.difi.eidas.cproxy.domain.node.NodeAttribute;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.idporten.log.audit.AuditLogger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentHandlerTest {
    @Mock
    private EventLog eventLog;
    @Mock
    private AuditLogger auditLogger;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private IdPAuthnResponse authnResponse;

    @Mock
    private NodeAuthnRequest nodeAuthnRequest;

    @Mock
    NodeAttributes nodeAttributes;

    private ConsentHandler consentHandler;

    @Before
    public void setUp() {
        when(authenticationContext.nodeRequest()).thenReturn(nodeAuthnRequest);
        when(authenticationContext.assembledAttributes()).thenReturn(nodeAttributes);
        consentHandler = new ConsentHandler(eventLog, new AuditLog(auditLogger));
    }

    @Test
    public void acceptAuditLogged() {
        final String uid = "12345678901";
        NodeAttributes nodeAttributes = NodeAttributes.builder()
                .available(SubjectBasicAttribute.CurrentGivenName, "FIRST_NAME")
                .available(SubjectBasicAttribute.PersonIdentifier, "12345678901")
                .build();
        when(authenticationContext.assembledAttributes()).thenReturn(nodeAttributes);
        consentHandler.accept(authenticationContext);
        verify(auditLogger).log(eq(AuditLog.AuditMessages.responseToPepsAccepted.id()), eq(uid), matches(".*CurrentGivenName=FIRST_NAME.*"));
    }

    @Test
    public void acceptEventLogged() {
        when(nodeAttributes.get(eq(SubjectBasicAttribute.PersonIdentifier))).thenReturn(new NodeAttribute("12345678901"));
        consentHandler.accept(authenticationContext);
        verify(eventLog).attributeConsentOk(nodeAuthnRequest, nodeAttributes);
    }

    @Test
    public void rejectAuditLogged() {
        final String uid = "12345678901";
        NodeAttributes nodeAttributes = NodeAttributes.builder()
                .available(SubjectBasicAttribute.CurrentGivenName, "FIRST_NAME")
                .available(SubjectBasicAttribute.PersonIdentifier, "12345678901")
                .build();
        when(authenticationContext.assembledAttributes()).thenReturn(nodeAttributes);
        consentHandler.reject(authenticationContext);
        verify(auditLogger).log(eq(AuditLog.AuditMessages.responseToPepsRejected.id()), eq(uid), matches(".*CurrentGivenName=FIRST_NAME.*"));
    }

    @Test
    public void rejectEventLogged() {
        when(nodeAttributes.get(eq(SubjectBasicAttribute.PersonIdentifier))).thenReturn(new NodeAttribute("12345678901"));
        consentHandler.reject(authenticationContext);
        verify(eventLog).attributeConsentRejected(nodeAuthnRequest, nodeAttributes);
    }

}
