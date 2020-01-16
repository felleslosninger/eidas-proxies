package no.difi.eidas.idpproxy.integrasjon.mf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MFPersonnavnResource {
    @JsonProperty("fornavn")
    private String fornavn;

    @JsonProperty("etternavn")
    private String etternavn;

    @JsonProperty("mellomnavn")
    private String mellomnavn;

    @JsonProperty("forkortetNavn")
    private String forkortetNavn;

    @JsonProperty("erGjeldende")
    private boolean erGjeldende;

    @JsonIgnore
    public boolean isGjeldende() {
        return erGjeldende;
    }
}
