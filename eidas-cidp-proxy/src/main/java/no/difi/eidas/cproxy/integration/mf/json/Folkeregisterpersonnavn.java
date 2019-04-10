package no.difi.eidas.cproxy.integration.mf.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ajourholdstidspunkt",
        "erGjeldende",
        "kilde",
        "aarsak",
        "gyldighetstidspunkt",
        "fornavn",
        "etternavn"
})
public class Folkeregisterpersonnavn {
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
    @JsonProperty("fornavn")
    private String fornavn;
    @JsonProperty("mellomnavn")
    private String mellomnavn;
    @JsonProperty("etternavn")
    private String etternavn;
    @JsonProperty("forkortetNavn")
    private String forkortetNavn;
    @JsonProperty("originaltNavn")
    private String originaltNavn;
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

    @JsonProperty("fornavn")
    public String getFornavn() {
        return fornavn;
    }

    @JsonProperty("fornavn")
    public void setFornavn(String fornavn) {
        this.fornavn = fornavn;
    }

    @JsonProperty("mellomnavn")
    public String getMellomnavn() {
        return mellomnavn;
    }

    @JsonProperty("mellomnavn")
    public void setMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
    }

    @JsonProperty("etternavn")
    public String getEtternavn() {
        return etternavn;
    }

    @JsonProperty("etternavn")
    public void setEtternavn(String etternavn) {
        this.etternavn = etternavn;
    }

    @JsonProperty("forkortetNavn")
    public String getForkortetNavn() {
        return forkortetNavn;
    }

    @JsonProperty("forkortetNavn")
    public void setForkortetNavn(String forkortetNavn) {
        this.forkortetNavn = forkortetNavn;
    }

    @JsonProperty("originaltNavn")
    public String getOriginaltNavn() {
        return originaltNavn;
    }

    @JsonProperty("originaltNavn")
    public void setOriginaltNavn(String originaltNavn) {
        this.originaltNavn = originaltNavn;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
