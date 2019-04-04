package no.difi.eidas.cproxy.integration.idporten;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import no.difi.eidas.cproxy.domain.authentication.AuthenticationContext;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.domain.node.NodeRequestedAttributes;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.difi.eidas.idpproxy.integrasjon.dsf.Person;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NodeAttributeAssembler {

    public static final String DATE_OF_BIRTH_FORMAT = "yyyy-MM-dd";

    public NodeAttributes assembleAttributes(AuthenticationContext context, ResponseData response)
            throws AttributeUnresolvable, AttributeUnavailable {
        NodeRequestedAttributes requestedAttributes = context.nodeRequest().requestedAttributes();
        NodeAttributes.Builder builder = NodeAttributes.builder();
        for (SubjectBasicAttribute attribute : attributesOrderedForDisplay) {
            if (!requestedAttributes.requested(attribute))
                continue;
            String value = resolveAttribute(attribute, response);
            if (value != null) {
                builder.available(attribute, value, isHidden(attribute));
            } else if (requestedAttributes.required(attribute)){
            	builder.requiredNotAvailable(attribute);
            } else {
                builder.notAvailable(attribute);
            }
        }
        return builder.build();
    }

    private boolean isHidden(SubjectBasicAttribute attribute) {
        return hiddenAttributes.contains(attribute);
    }

    private String resolveAttribute(SubjectBasicAttribute attribute, ResponseData response)
            throws AttributeUnresolvable {
        AttributeResolver resolver = resolvers.get(attribute);
        if (resolver == null)
            throw new AttributeUnresolvable(attribute);
        return resolver.apply(response);
    }



    private List<SubjectBasicAttribute> hiddenAttributes =
            ImmutableList.<SubjectBasicAttribute>builder()
                    .build();

    private List<SubjectBasicAttribute> attributesOrderedForDisplay =
            ImmutableList.<SubjectBasicAttribute>builder()
                    .add(SubjectBasicAttribute.PersonIdentifier)
                    .add(SubjectBasicAttribute.CurrentGivenName)
                    .add(SubjectBasicAttribute.CurrentFamilyName)
                    .add(SubjectBasicAttribute.DateOfBirth)
                    .add(SubjectBasicAttribute.PlaceOfBirth)
                    .build();

    private Map<SubjectBasicAttribute, AttributeResolver> resolvers =
            ImmutableMap.<SubjectBasicAttribute, AttributeResolver>builder()
                    .put(SubjectBasicAttribute.PersonIdentifier, new EIdentifierResolver())
                    .put(SubjectBasicAttribute.CurrentGivenName, new GivenNameResolver())
                    .put(SubjectBasicAttribute.CurrentFamilyName, new CurrentFamilyNameResolver())
                    .put(SubjectBasicAttribute.DateOfBirth, new DateOfBirthResolver())
                    .put(SubjectBasicAttribute.PlaceOfBirth, new NullResolver())
                    .build();

    private interface AttributeResolver extends Function<ResponseData, String> {}

    private class NullResolver implements AttributeResolver {
        @Override
        public final String apply(ResponseData input) {
        	return null;
        }
    }

    private abstract class PersonAttributeResolver implements AttributeResolver {
        @Override
        public final String apply(ResponseData input) {
            if (input.getPerson() == null)
                return null;
            return apply(input.getPerson());
        }
        protected abstract String apply(Person person);
    }

    private class GivenNameResolver extends PersonAttributeResolver {
        @Override
        protected String apply(Person person) {
            return person.fornavn();
        }
    }

    private class CurrentFamilyNameResolver extends PersonAttributeResolver {
        @Override
        protected String apply(Person person) {
            return person.etternavn();
        }
    }

    private class GenderResolver extends PersonAttributeResolver {
        @Override
        protected String apply(Person person) {
            if (person.kjønn() == null)
                return null;
            switch (person.kjønn()) {
                case Kvinne: return "F";
                case Mann: return "M";
                default: throw new IllegalArgumentException("Unknown \"kjønn\":" + person.kjønn());
            }
        }
    }

    private class DateOfBirthResolver extends PersonAttributeResolver {
        @Override
        protected String apply(Person person) {
            if (person.fødselsdato() == null)
                return null;
            return person.fødselsdato().toString(DATE_OF_BIRTH_FORMAT);
        }
    }

    private class TextResidenceAddressResolver extends PersonAttributeResolver {
        private static final String newLine = "\n";
        @Override
        protected String apply(Person person) {
            if (StringUtils.isEmpty(person.postadresse()) || StringUtils.isEmpty(person.postadresseLand()))
                return null;

            StringBuilder builder = new StringBuilder();

            if (!StringUtils.isEmpty(person.postadresseTilleggslinje())) {
                builder.append(person.postadresseTilleggslinje());
            }

            if (!StringUtils.isEmpty(person.gateadresse())) {
                if (builder.length() > 0)
                    builder.append(newLine);
                builder.append(person.gateadresse());
            }

            if (builder.length() > 0)
                builder.append(newLine);
            builder.append(person.postadresse());

            if (!StringUtils.isEmpty(person.postadresseLand())) {
                builder.append(newLine);
                builder.append(person.postadresseLand());
            }
            return builder.toString();
        }
    }

    private class EIdentifierResolver implements AttributeResolver {
        @Override
        public String apply(ResponseData input) {
            return input.getIdPAuthnResponse().uid();
        }
    }

    private class AttributeUnavailable extends RuntimeException {

        private AttributeUnavailable(SubjectBasicAttribute attribute) {
            super("Required attribute " + attribute.attributeName() + " is unavailable");
        }

    }

    private class AttributeUnresolvable extends RuntimeException {

        private AttributeUnresolvable(SubjectBasicAttribute attribute) {
            super("Attribute " + attribute.attributeName() + " is unresolvable");
        }

    }

}
