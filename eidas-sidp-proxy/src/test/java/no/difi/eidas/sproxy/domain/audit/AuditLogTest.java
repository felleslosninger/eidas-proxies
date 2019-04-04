package no.difi.eidas.sproxy.domain.audit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuditLogTest {

    @InjectMocks
    private AuditLog auditLog;

    @Test
    public void testWithoutLineEndings() {
        Assert.assertEquals("foobarbazxxx", auditLog.withoutLineEndings("foo\n\rbar\nbaz\rxxx\n"));
    }

}
