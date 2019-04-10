
package no.difi.eidas.cproxy.integration.mf.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "identifikasjonsnummer",
        "foedsel",
        "navn"
})
public class FolkeregisterPerson {

    @JsonProperty("identifikasjonsnummer")
    private List<Identifikasjonsnummer> identifikasjonsnummer = null;
    @JsonProperty("foedsel")
    private List<Foedsel> foedsel = null;
    @JsonProperty("navn")
    private List<Folkeregisterpersonnavn> navn = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("identifikasjonsnummer")
    public List<Identifikasjonsnummer> getIdentifikasjonsnummer() {
        return identifikasjonsnummer;
    }

    @JsonProperty("identifikasjonsnummer")
    public void setIdentifikasjonsnummer(List<Identifikasjonsnummer> identifikasjonsnummer) {
        this.identifikasjonsnummer = identifikasjonsnummer;
    }

    @JsonProperty("foedsel")
    public List<Foedsel> getFoedsel() {
        return foedsel;
    }

    @JsonProperty("foedsel")
    public void setFoedsel(List<Foedsel> foedsel) {
        this.foedsel = foedsel;
    }

    @JsonProperty("navn")
    public List<Folkeregisterpersonnavn> getNavn() {
        return this.navn;
    }

    public Optional<Identifikasjonsnummer> getGjeldendeIdentifikasjonsnummer() {
        return identifikasjonsnummer.stream()
                .filter(Identifikasjonsnummer::getErGjeldende)
                .findAny();
    }

    public Optional<Folkeregisterpersonnavn> getGjeldendeNavn() {
        return navn.stream()
                .filter(Folkeregisterpersonnavn::getErGjeldende)
                .findAny();
    }

    public Optional<Foedsel> getGjeldendeFoedsel() {
        return foedsel.stream()
                .filter(Foedsel::getErGjeldende)
                .findAny();
    }


    @JsonProperty("navn")
    public void setNavn(List<Folkeregisterpersonnavn> navn) {
        this.navn = navn;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
