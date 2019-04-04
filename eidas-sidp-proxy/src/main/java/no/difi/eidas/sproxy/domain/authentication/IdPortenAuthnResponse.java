package no.difi.eidas.sproxy.domain.authentication;

import no.difi.eidas.sproxy.domain.saml.SamlXml;

public class IdPortenAuthnResponse extends SamlXml {
    public IdPortenAuthnResponse(String samlXml) {
        super(samlXml);
    }
}
