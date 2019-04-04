package no.difi.eidas.sproxy.domain.country;

import java.util.Objects;

public class CountryCode {

    private final String code;

    public CountryCode(String code) {
        Objects.requireNonNull(code);
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CountryCode that = (CountryCode) o;

        return code.equals(that.code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
