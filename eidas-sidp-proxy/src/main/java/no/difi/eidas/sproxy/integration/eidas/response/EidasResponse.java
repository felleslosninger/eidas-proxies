package no.difi.eidas.sproxy.integration.eidas.response;

import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import no.difi.eidas.sproxy.domain.saml.SamlResponseXml;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EidasResponse {

    private String authnContextClassRef;
    private SamlResponseXml eidasReponseSaml;
    private Map<String, String> attributes = new HashMap<>();

    private EidasResponse() {}

    public String authnContextClassRef() {
        return authnContextClassRef;
    }

    public SamlResponseXml eidasReponseSaml() {
        return eidasReponseSaml;
    }

    public String personIdentifier() {
        return attributes().get(EidasSpec.Definitions.PERSON_IDENTIFIER.getFriendlyName());
    }

    public String currentFamilyName() {
        return attributes().get(EidasSpec.Definitions.CURRENT_FAMILY_NAME.getFriendlyName());
    }

    public String currentGivenName() {
        return attributes().get(EidasSpec.Definitions.CURRENT_GIVEN_NAME.getFriendlyName());
    }

    public String dateOfBirth() {
        return attributes().get(EidasSpec.Definitions.DATE_OF_BIRTH.getFriendlyName());
    }

    public Map<String, String> attributes() {
        return attributes;
    }

    public static Builder builder() {
        return new Builder(new EidasResponse());
    }


    public static class Builder {

        private EidasResponse instance;

        private Builder(EidasResponse instance) {
            this.instance = instance;
        }

        public EidasResponse build() {
            return instance;
        }

        public Builder samlXml(SamlResponseXml samlXml) {
            instance.eidasReponseSaml = samlXml;
            return this;
        }

        public Builder authnContextClassRef(String authnContextClassRef) {
            instance.authnContextClassRef = authnContextClassRef;
            return this;
        }

        public Builder personIdentifier(String personIdentifier) {
            attribute(EidasSpec.Definitions.PERSON_IDENTIFIER.getFriendlyName(), personIdentifier);
            return this;
        }

        public Builder currentFamilyName(String currentFamilyName) {
            attribute(EidasSpec.Definitions.CURRENT_FAMILY_NAME.getFriendlyName(), currentFamilyName);
            return this;
        }

        public Builder currentGivenName(String currentGivenName) {
            attribute(EidasSpec.Definitions.CURRENT_GIVEN_NAME.getFriendlyName(), currentGivenName);
            return this;
        }

        public Builder dateOfBirth(String dateOfBirth) {
            attribute(EidasSpec.Definitions.DATE_OF_BIRTH.getFriendlyName(), dateOfBirth);
            return this;
        }

        public Builder attribute(String name, String value) {
            instance.attributes.put(name, value);
            return this;
        }

        public Builder attributes(Map<String, String> attributes) {
            if (attributes != null) {
                instance.attributes.putAll(attributes);
            }
            return this;
        }


    }

    public Optional<Name> name() {
        String firstName = currentGivenName();
        String lastName = currentFamilyName();
        String birth = dateOfBirth();
        return StringUtils.hasText(firstName) && StringUtils.hasText(lastName) && StringUtils.hasText(birth) ?
                Optional.of(new Name(firstName, lastName, birth)) :
                Optional.empty();
    }

    public static class Name {
        private final String firstName;
        private final String lastName;
        private final String birth;

        public Name(String firstName, String lastName, String birth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birth = birth;
        }

        public String firstName() {
            return firstName;
        }

        public String lastName() {
            return lastName;
        }

        public String birth() {
            return birth;
        }

    }
}
