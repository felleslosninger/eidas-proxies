package no.difi.eidas.idpproxy;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;

public enum SubjectBasicAttribute {

    EidasAdditionalAttribute("http://eidas.europa.eu/attributes/naturalperson/EidasAdditionalAttribute"),
    CurrentFamilyName(EidasSpec.Definitions.CURRENT_FAMILY_NAME),
    CurrentGivenName(EidasSpec.Definitions.CURRENT_GIVEN_NAME),
    DateOfBirth(EidasSpec.Definitions.DATE_OF_BIRTH),
    PersonIdentifier(EidasSpec.Definitions.PERSON_IDENTIFIER),
    PlaceOfBirth(EidasSpec.Definitions.PLACE_OF_BIRTH);

    private String attributeName;

    SubjectBasicAttribute(String attributeName) {
        this.attributeName = attributeName;
    }
    SubjectBasicAttribute(AttributeDefinition<?> attributeDefinition) {
        this.attributeName = attributeDefinition.getNameUri().toString();
    }

    public String attributeName() {
        return attributeName;
    }

    public static SubjectBasicAttribute fromAttributeName(String attributeName) {
        for (SubjectBasicAttribute attribute : SubjectBasicAttribute.values()) {
            if (attribute.attributeName().equals(attributeName))
                return attribute;
        }
        throw new IllegalArgumentException("Unknown attribute name \"" + attributeName + "\"");
    }

}
