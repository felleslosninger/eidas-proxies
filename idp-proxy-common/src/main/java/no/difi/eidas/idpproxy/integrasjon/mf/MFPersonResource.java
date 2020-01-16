package no.difi.eidas.idpproxy.integrasjon.mf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MFPersonResource {
    @JsonProperty("navn")
    private MFPersonnavnResource navn;

    @JsonProperty("postadresse")
    private MFAdresseResource adresse;

    @JsonProperty("personIdentifikator")
    private String personIdentifikator;

    @JsonProperty("foedselsdato")
    private String foedselsdato;

    @JsonProperty("foedested")
    private String foedested;
}
