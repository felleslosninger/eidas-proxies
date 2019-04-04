package no.difi.eidas.cproxy.domain.idp;

import no.difi.eidas.cproxy.TestKeyProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class IdPortenRedirectURLFactoryTest {

    @Test
    public void testBuildHTTPRedirectURLParametersIsSigned() {
        String encoded = IdPortenRedirectURLFactory.build(
                "samlRequest",
                null,
                TestKeyProvider.privateKey.getPrivateKey(),
                true,
                true
        );
        assertNotNull(encoded);
        verifyHasSignatureAndResponse(encoded);

    }

    @Test
    public void testBuildHTTPRedirectURLParametersIsNotSigned() {
        String encoded = IdPortenRedirectURLFactory.build(
                "samlRequest",
                null,
                TestKeyProvider.privateKey.getPrivateKey(),
                true,
                false
        );
        assertNotNull(encoded);
        verifyHasResponse(encoded);
        assertFalse(encoded.contains("Signature="));
        assertFalse(encoded.contains("SigAlg="));
        assertFalse(encoded.contains("rsa-sha1"));

    }

    @Test
    public void testBuildHTTPRedirectURLParametersIsRequest() {
        String encoded = IdPortenRedirectURLFactory.build(
                "samlRequest",
                null,
                TestKeyProvider.privateKey.getPrivateKey(),
                false,
                true
        );
        assertNotNull(encoded);
        assertTrue(encoded.contains("SAMLRequest="));
        verifyHasSignature(encoded);

    }

    @Test
    public void testBuildHTTPRedirectURLParametersHasRelayState() {
        String encoded = IdPortenRedirectURLFactory.build(
                "samlRequest",
                "relayState",
                TestKeyProvider.privateKey.getPrivateKey(),
                true, true
        );
        assertNotNull(encoded);
        verifyHasSignatureAndResponse(encoded);
        assertTrue(encoded.contains("RelayState="));

    }

    private void verifyHasSignatureAndResponse(String encoded) {
        verifyHasResponse(encoded);
        verifyHasSignature(encoded);
    }

    private void verifyHasResponse(String encoded) {
        assertTrue(encoded.contains("SAMLResponse="));
    }

    private void verifyHasSignature(String encoded) {
        assertTrue(encoded.contains("Signature="));
        assertTrue(encoded.contains("SigAlg="));
        assertTrue(encoded.contains("rsa-sha1"));
    }
}
