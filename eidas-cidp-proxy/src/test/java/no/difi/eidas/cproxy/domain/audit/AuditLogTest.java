package no.difi.eidas.cproxy.domain.audit;

import no.difi.eidas.cproxy.domain.idp.IdPAuthnResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditLogTest {

    @Mock
    private IdPAuthnResponse idPAuthnResponse;

    @InjectMocks
    private AuditLog auditLog;

    @Test
    public void testSsnNoResponse() {
        assertEquals("", auditLog.ssn(idPAuthnResponse));
    }

    @Test
    public void testSsnNoUid() {
        assertEquals("", auditLog.ssn(idPAuthnResponse));
    }

    @Test
    public void testSsn() {
        final String uid = "uid";
        when(idPAuthnResponse.uid()).thenReturn(uid);
        assertEquals(uid, auditLog.ssn(idPAuthnResponse));
    }

    @Test
    public void testSkipRemoveWhitespaceForNullString() {
        assertNull(auditLog.withoutLineEndings(null));
    }

    @Test
    public void testWithoutLineEndings() {
        assertEquals("foobarbazxxx", auditLog.withoutLineEndings("foo\n\rbar\nbaz\rxxx\n"));
    }

}
