package no.difi.eidas.cproxy.integration.mf.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ajourholdstidspunkt",
        "erGjeldende",
        "kilde",
        "aarsak",
        "gyldighetstidspunkt",
        "foedselsdato",
        "foedselsaar",
        "foedested",
        "foedeland"
})
public class Foedsel {
    private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    @JsonProperty("ajourholdstidspunkt")
    private String ajourholdstidspunkt;
    @JsonProperty("erGjeldende")
    private Boolean erGjeldende;
    @JsonProperty("kilde")
    private String kilde;
    @JsonProperty("aarsak")
    private String aarsak;
    @JsonProperty("gyldighetstidspunkt")
    private String gyldighetstidspunkt;
    @JsonProperty("foedselsdato")
    private String foedselsdato;
    @JsonProperty("foedselsaar")
    private String foedselsaar;
    @JsonProperty("foedested")
    private String foedested;
    @JsonProperty("foedeland")
    private String foedeland;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("ajourholdstidspunkt")
    public String getAjourholdstidspunkt() {
        return ajourholdstidspunkt;
    }

    @JsonProperty("ajourholdstidspunkt")
    public void setAjourholdstidspunkt(String ajourholdstidspunkt) {
        this.ajourholdstidspunkt = ajourholdstidspunkt;
    }

    @JsonProperty("erGjeldende")
    public Boolean getErGjeldende() {
        return erGjeldende;
    }

    @JsonProperty("erGjeldende")
    public void setErGjeldende(Boolean erGjeldende) {
        this.erGjeldende = erGjeldende;
    }

    @JsonProperty("kilde")
    public String getKilde() {
        return kilde;
    }

    @JsonProperty("kilde")
    public void setKilde(String kilde) {
        this.kilde = kilde;
    }

    @JsonProperty("aarsak")
    public String getAarsak() {
        return aarsak;
    }

    @JsonProperty("aarsak")
    public void setAarsak(String aarsak) {
        this.aarsak = aarsak;
    }

    @JsonProperty("gyldighetstidspunkt")
    public String getGyldighetstidspunkt() {
        return gyldighetstidspunkt;
    }

    @JsonProperty("gyldighetstidspunkt")
    public void setGyldighetstidspunkt(String gyldighetstidspunkt) {
        this.gyldighetstidspunkt = gyldighetstidspunkt;
    }

    @JsonProperty("foedselsdato")
    public String getFoedselsdato() {
        return foedselsdato;
    }

    public LocalDate getFoedselsdatoAsLocalDate() {
        return LocalDate.parse(foedselsdato, dateFormatter);
    }

    @JsonProperty("foedselsdato")
    public void setFoedselsdato(String foedselsdato) {
        this.foedselsdato = foedselsdato;
    }

    @JsonProperty("foedselsaar")
    public String getFoedselsaar() {
        return foedselsaar;
    }

    @JsonProperty("foedselsaar")
    public void setFoedselsaar(String foedselsaar) {
        this.foedselsaar = foedselsaar;
    }

    @JsonProperty("foedested")
    public String getFoedested() {
        return foedested;
    }

    @JsonProperty("foedested")
    public void setFoedested(String foedested) {
        this.foedested = foedested;
    }

    @JsonProperty("foedeland")
    public String getFoedeland() {
        return foedeland;
    }

    @JsonProperty("foedeland")
    public void setFoedeland(String foedeland) {
        this.foedeland = foedeland;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
