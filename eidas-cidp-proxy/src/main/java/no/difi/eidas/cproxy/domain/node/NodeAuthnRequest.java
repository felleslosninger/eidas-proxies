package no.difi.eidas.cproxy.domain.node;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;

/**
 * Holder of relevant values from the SAML AuthnRequest from C-PEPS.
 */
public class NodeAuthnRequest {

    private NodeRequestedAttributes requestedAttributes;
    private String spCountry;
    private String spInstitution;
    private String spApplication;
    private String correlationId;
    private boolean forceAuthn;
    private String assertionConsumerServiceAddress;
    private String issuer;
    private String levelOfAssurance;

    private NodeAuthnRequest() {
    }

    public String spCountry() {
        return spCountry;
    }

    public String spInstitution() {
        return spInstitution;
    }

    public String spApplication() {
        return spApplication;
    }

    public String correlationId() {
        return correlationId;
    }

    public boolean forceAuthn() {
        return forceAuthn;
    }

    public String assertionConsumerServiceAddress() {
        return assertionConsumerServiceAddress;
    }

    public String issuer() {
        return issuer;
    }

    public String levelOfAssurance() {
        return levelOfAssurance;
    }

    public NodeRequestedAttributes requestedAttributes() {
        return requestedAttributes;
    }

    public static Builder builder() {
        return new Builder(new NodeAuthnRequest());
    }

    public static class Builder {
        private NodeAuthnRequest instance;

        public Builder(NodeAuthnRequest instance) {
            this.instance = instance;
        }

        public Builder requestedAttributes(NodeRequestedAttributes requestedAttributes) {
            instance.requestedAttributes = requestedAttributes;
            return this;
        }

        public Builder spCountry(String spCountry) {
            instance.spCountry = spCountry;
            return this;
        }

        public Builder spInstitution(String spInstitution) {
            instance.spInstitution = spInstitution;
            return this;
        }

        public Builder spApplication(String spApplication) {
            instance.spApplication = spApplication;
            return this;
        }

        public Builder correlationId(String correlationId) {
            instance.correlationId = correlationId;
            return this;
        }

        public Builder forceAuthn(boolean forceAuthn) {
            instance.forceAuthn = forceAuthn;
            return this;
        }

        public Builder assertionConsumerServiceAddress(String assertionConsumerServiceAddress) {
            instance.assertionConsumerServiceAddress = assertionConsumerServiceAddress;
            return this;
        }

        public Builder issuer(String issuer) {
            instance.issuer = issuer;
            return this;
        }

        public Builder levelOfAssurance(String levelOfAssurance) {
            instance.levelOfAssurance = levelOfAssurance;
            return this;
        }

        public NodeAuthnRequest build() {
            return instance;
        }
    }

}
