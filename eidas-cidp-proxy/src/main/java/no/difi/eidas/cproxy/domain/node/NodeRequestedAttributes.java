package no.difi.eidas.cproxy.domain.node;

import no.difi.eidas.idpproxy.SubjectBasicAttribute;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public class NodeRequestedAttributes implements Iterable<SubjectBasicAttribute> {

    private Map<SubjectBasicAttribute, Boolean> requestedAttributes = new EnumMap<>(SubjectBasicAttribute.class);

    private NodeRequestedAttributes() {
    }

    @Override
    public Iterator<SubjectBasicAttribute> iterator() {
        return Collections.unmodifiableSet(requestedAttributes.keySet()).iterator();
    }

    public boolean requested(SubjectBasicAttribute attribute) {
        return requestedAttributes.containsKey(attribute);
    }

    public boolean required(SubjectBasicAttribute attribute) {
        if (!requested(attribute))
            throw new IllegalArgumentException("Attribute " + attribute + " is not requested");
        return requestedAttributes.get(attribute);
    }

    public static Builder builder() {
        return new Builder(new NodeRequestedAttributes());
    }

    public static class Builder {
        private NodeRequestedAttributes instance;

        public Builder(NodeRequestedAttributes instance) {
            this.instance = instance;
        }

        public Builder required(SubjectBasicAttribute attribute) {
            check(attribute);
            instance.requestedAttributes.put(attribute, Boolean.TRUE);
            return this;
        }

        public Builder optional(SubjectBasicAttribute attribute) {
            check(attribute);
            instance.requestedAttributes.put(attribute, Boolean.FALSE);
            return this;
        }

        private void check(SubjectBasicAttribute attribute) {
            if (instance.requestedAttributes.containsKey(attribute))
                throw new IllegalArgumentException("Attribute " + attribute + " is already requested");
        }

        public NodeRequestedAttributes build() {
            return instance;
        }

    }

}
