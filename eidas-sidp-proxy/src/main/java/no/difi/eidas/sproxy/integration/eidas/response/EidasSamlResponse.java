package no.difi.eidas.sproxy.integration.eidas.response;

import eu.eidas.auth.commons.EidasStringUtil;
import no.difi.eidas.sproxy.domain.saml.SamlResponseXml;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class EidasSamlResponse {

    private final String encoded;

    public EidasSamlResponse(String encoded) {
        this.encoded = encoded;
    }

    public String encoded() {
        return encoded;
    }

    public SamlResponseXml samlXml() {
        return new SamlResponseXml(new String(EidasStringUtil.decodeBytesFromBase64(encoded), StandardCharsets.UTF_8));
    }

}
