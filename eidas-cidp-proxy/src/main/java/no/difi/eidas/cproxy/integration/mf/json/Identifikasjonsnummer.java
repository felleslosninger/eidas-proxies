
package no.difi.eidas.cproxy.integration.mf.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ajourholdstidspunkt",
    "erGjeldende",
    "kilde",
    "gyldighetstidspunkt",
    "status",
    "foedselsEllerDNummer",
    "identifikatortype"
})
public class Identifikasjonsnummer {

    @JsonProperty("ajourholdstidspunkt")
    private String ajourholdstidspunkt;
    @JsonProperty("erGjeldende")
    private Boolean erGjeldende;
    @JsonProperty("kilde")
    private String kilde;
    @JsonProperty("gyldighetstidspunkt")
    private String gyldighetstidspunkt;
    @JsonProperty("status")
    private String status;
    @JsonProperty("foedselsEllerDNummer")
    private String foedselsEllerDNummer;
    @JsonProperty("identifikatortype")
    private String identifikatortype;
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

    @JsonProperty("gyldighetstidspunkt")
    public String getGyldighetstidspunkt() {
        return gyldighetstidspunkt;
    }

    @JsonProperty("gyldighetstidspunkt")
    public void setGyldighetstidspunkt(String gyldighetstidspunkt) {
        this.gyldighetstidspunkt = gyldighetstidspunkt;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }


    public String getFoedselsEllerDNummer() {
        return foedselsEllerDNummer;
    }

    @JsonProperty("foedselsEllerDNummer")
    public void setFoedselsEllerDNummer(String foedselsEllerDNummer) {
        this.foedselsEllerDNummer = foedselsEllerDNummer;
    }

    @JsonProperty("identifikatortype")
    public String getIdentifikatortype() {
        return identifikatortype;
    }

    @JsonProperty("identifikatortype")
    public void setIdentifikatortype(String identifikatortype) {
        this.identifikatortype = identifikatortype;
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
