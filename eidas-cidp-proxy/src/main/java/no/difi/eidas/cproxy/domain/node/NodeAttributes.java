package no.difi.eidas.cproxy.domain.node;

import no.difi.eidas.idpproxy.SubjectBasicAttribute;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A structure representing attributes that will be used for building the AttributeStatement sent in the
 * SAML assertion to C-PEPS. Attributes are optional and each will be present if and only if it was requested
 * in the authorization request.
 */
public class NodeAttributes implements Iterable<SubjectBasicAttribute> {

    private final Map<SubjectBasicAttribute, NodeAttribute> attributes;
    private final Map<SubjectBasicAttribute, NodeAttribute> missingRequiredAttributes;

    private NodeAttributes(Map<SubjectBasicAttribute, NodeAttribute> attributes, Map<SubjectBasicAttribute, NodeAttribute> missingRequiredAttributes) {
        this.attributes = attributes;
        this.missingRequiredAttributes = missingRequiredAttributes;
    }

    @Override
    public Iterator<SubjectBasicAttribute> iterator() {
        return attributes.keySet().iterator();
    }
    
    public Set<SubjectBasicAttribute> missingRequiredAttributes() {
        return missingRequiredAttributes.keySet();
    }
    public boolean hasMissingRequiredAttributes(){
    	return !missingRequiredAttributes.isEmpty();
    }

    public NodeAttribute get(SubjectBasicAttribute attribute) {
        if (!attributes.containsKey(attribute))
            throw new IllegalArgumentException("Attribute " + attribute + " is not available");
        return attributes.get(attribute);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NodeAttributes: [");
        NodeAttribute pepsAttribute;
        for (SubjectBasicAttribute subjectBasicAttribute : attributes.keySet()) {
            pepsAttribute = attributes.get(subjectBasicAttribute);
            sb.append(subjectBasicAttribute.attributeName());
            sb.append("=");
            if (pepsAttribute.isPresent()) {
                sb.append(pepsAttribute.value().get());
            } else {
                sb.append("<missing>");
            }
            sb.append(",");
        }

        sb.append("]");
        return sb.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<SubjectBasicAttribute, NodeAttribute> attributes = new LinkedHashMap<>();
        private Map<SubjectBasicAttribute, NodeAttribute> missingRequiredAttributes = new LinkedHashMap<>();
        private boolean done;

        private Builder() {
        }

        public Builder available(SubjectBasicAttribute attribute, String value) {
            return available(attribute, value, false);
        }

        public Builder available(SubjectBasicAttribute attribute, String value, boolean hidden) {
            check(attribute);
            attributes.put(attribute, new NodeAttribute(value, hidden));
            return this;
        }

        public Builder requiredNotAvailable(SubjectBasicAttribute attribute) {
            check(attribute);
            missingRequiredAttributes.put(attribute, new NodeAttribute(NodeAttribute.NotPresentReason.NotAvailable, true));
            return this;			
		}
        
        public Builder notAvailable(SubjectBasicAttribute attribute) {
            check(attribute);
            attributes.put(attribute, new NodeAttribute(NodeAttribute.NotPresentReason.NotAvailable, false));
            return this;
        }

        private void check(SubjectBasicAttribute attribute) {
            if (attributes.containsKey(attribute))
                throw new IllegalArgumentException("Attribute " + attribute + " is already assembled");
        }

        public NodeAttributes build() {
            if (done) throw new IllegalStateException("This builder has already built");
            done = true;
            return new NodeAttributes(
                    attributes,
                    missingRequiredAttributes
            );
        }
    }

}
