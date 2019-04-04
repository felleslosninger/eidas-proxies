package no.difi.eidas.cproxy.integration.oidc;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.difi.eidas.cproxy.domain.authentication.AuthenticationContext;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.domain.node.NodeRequestedAttributes;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@Service
public class OIDCNodeAttributeAssembler {

    public static final String DATE_OF_BIRTH_FORMAT = "yyyy-MM-dd";

    public NodeAttributes assembleAttributes(AuthenticationContext context, SignedJWT idToken)
            throws AttributeUnresolvable, AttributeUnavailable {
        NodeRequestedAttributes requestedAttributes = context.nodeRequest().requestedAttributes();
        NodeAttributes.Builder builder = NodeAttributes.builder();
        for (SubjectBasicAttribute attribute : attributesOrderedForDisplay) {
            if (!requestedAttributes.requested(attribute))
                continue;
            String value = resolveAttribute(attribute, idToken);
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

    private String resolveAttribute(SubjectBasicAttribute attribute, SignedJWT idToken)
            throws AttributeUnresolvable {
        AttributeResolver resolver = resolvers.get(attribute);
        if (resolver == null)
            throw new AttributeUnresolvable(attribute);
        return resolver.apply(idToken);
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

    private interface AttributeResolver extends Function<SignedJWT, String> {}

    private class NullResolver implements AttributeResolver {
        @Override
        public final String apply(SignedJWT input) {
        	return null;
        }
    }

    private abstract class ClaimAttributeResolver implements AttributeResolver {
        @Override
        public final String apply(SignedJWT input) {
            try {
                return apply(input.getJWTClaimsSet());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        protected abstract String apply(JWTClaimsSet person) throws ParseException;
    }

    private class GivenNameResolver extends ClaimAttributeResolver {
        @Override
        protected String apply(JWTClaimsSet person) throws ParseException {
            return person.getStringClaim("givenName");
        }
    }

    private class CurrentFamilyNameResolver extends ClaimAttributeResolver {
        @Override
        protected String apply(JWTClaimsSet person) throws ParseException {
            return person.getStringClaim("familyName");
        }
    }

    private class DateOfBirthResolver extends ClaimAttributeResolver {
        @Override
        protected String apply(JWTClaimsSet person) throws ParseException {
            return person.getStringClaim("birthday");
        }
    }

    private class EIdentifierResolver extends ClaimAttributeResolver {
        @Override
        protected String apply(JWTClaimsSet person) throws ParseException {
            return person.getStringClaim("pid");
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
