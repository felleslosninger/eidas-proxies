package no.difi.eidas.idpproxy.integrasjon.mf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFAdresseResource {
    @JsonProperty("erGjeldende")
    private boolean erGjeldende;

    @JsonProperty("adressegradering")
    private String adressegradering;

    @JsonProperty("adresselinje")
    private String[] adresselinje;

    @JsonProperty("postnummer")
    private String postnummer;

    @JsonProperty("poststed")
    private String poststed;

    @JsonProperty("landkode")
    private String landkode;
}
