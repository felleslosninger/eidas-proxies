package no.difi.eidas.sproxy.domain.authentication;

import no.difi.eidas.sproxy.domain.saml.SamlXml;
import no.difi.opensaml.wrapper.AuthnRequestWrapper;

public class IdPortenAuthnRequest extends AuthnRequestWrapper {
    private final SamlXml xml;

    public IdPortenAuthnRequest(SamlXml xml) {
        super(xml.toString());
        this.xml = xml;
    }

    public String authnContextClassRef() {
        return getOpenSAMLObject().getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
    }

    public SamlXml xml() {
        return xml;
    }
}
