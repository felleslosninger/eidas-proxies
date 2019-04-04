package no.difi.eidas.cproxy.domain.authentication;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;

/**
 * State holder through the authentication process.
 */
public class AuthenticationContext {

    private IAuthenticationRequest eidasRequest;
    private NodeAuthnRequest nodeRequest;
    private NodeAttributes assembledAttributes;
    private String levelOfAssurance;

    public void nodeRequest(NodeAuthnRequest nodeRequest) {
        this.nodeRequest = nodeRequest;
    }

    public NodeAuthnRequest nodeRequest() {
        return nodeRequest;
    }

    public void assembledAttributes(NodeAttributes assembledAttributes) {
        this.assembledAttributes = assembledAttributes;
    }

    public NodeAttributes assembledAttributes() {
        return assembledAttributes;
    }

    public void eidasRequest(IAuthenticationRequest eidasAuthnRequest) {
        this.eidasRequest = eidasAuthnRequest;
    }

    public IAuthenticationRequest eidasRequest() {
        return eidasRequest;
    }

    public String levelOfAssurance() {
        return levelOfAssurance;
    }

    public void levelOfAssurance(String levelOfAssurance) {
        this.levelOfAssurance = levelOfAssurance;
    }
}
