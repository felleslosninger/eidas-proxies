package no.difi.eidas.sproxy.domain.encoding;

import no.difi.opensaml.util.ConvertUtil;
import org.junit.Before;
import org.junit.Test;

import static no.difi.eidas.sproxy.ResourceReader.idPortenAuthnRequest;
import static no.difi.eidas.sproxy.ResourceReader.idPortenAuthnRequestEncoded;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class EncoderTest {
    private ConvertUtil encoder;

    @Before
    public void setUp() {
        encoder = new ConvertUtil();
    }

    @Test
    public void encodesCorrectly() {
        assertThat(
                encoder.zipAndEncodeBase64(idPortenAuthnRequest()),
                is(equalTo(idPortenAuthnRequestEncoded()))
        );
    }

    @Test
    public void decodesCorrectly() {
        assertThat(
                encoder.decodeBase64AndUnzip(idPortenAuthnRequestEncoded()),
                is(equalTo(idPortenAuthnRequest()))
        );
    }

    @Test
    public void encodesDecodes() {
        String saml = idPortenAuthnRequest();
        assertThat(
                encoder.decodeBase64AndUnzip(encoder.zipAndEncodeBase64(saml)),
                is(equalTo(saml))
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void tomStreng() {
        encoder.decodeBase64AndUnzip("");
    }

}
