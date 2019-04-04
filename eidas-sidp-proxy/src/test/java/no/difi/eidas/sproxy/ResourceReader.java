package no.difi.eidas.sproxy;

public class ResourceReader {

    public static String idPortenAuthnRequest() {
        return read("saml/idPortenAuthnRequest.xml");
    }

    public static String idPortenAuthnRequestEncoded() {
        return read("saml/idPortenAuthnRequestEncoded.txt");
    }

    public static String eidasAuthnResponse() {
        return read("saml/eidasResponse.xml");
    }

    public static String countriesAttributesJson() {
        return read("attributes/countriesAttributes.json");
    }

    public static String read(String name) {
        return no.difi.eidas.idpproxy.test.ResourceReader.read(name);
    }
}
