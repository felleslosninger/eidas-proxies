package no.difi.eidas.sproxy.integration.eidas.request;

public class EidasAuthnRequest {
    private String encodedXml;

    public EidasAuthnRequest(String encodedXml) {
        this.encodedXml = encodedXml;
    }

    @Override
    public String toString() {
        return encodedXml;
    }
}
