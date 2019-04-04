package no.difi.eidas.sproxy.integration.fileconfig.attribute;

public class Attribute {
    private final String name;
    private final Boolean required;

    public Attribute(String name, Boolean required) {
        this.name = name;
        this.required = required;
    }

    public String name() {
        return name;
    }

    public Boolean required() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attribute attribute = (Attribute) o;

        if (name != null ? !name.equals(attribute.name) : attribute.name != null) return false;
        return !(required != null ? !required.equals(attribute.required) : attribute.required != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (required != null ? required.hashCode() : 0);
        return result;
    }
}
