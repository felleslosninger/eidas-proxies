package no.difi.eidas.idpproxy.integrasjon.dsf;

import com.google.common.base.Function;
import no.difi.dsfgateway.DSFPersonResource;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 * Maps data between Det Sentrale Folkeregister (DSF) domain and DIFI's IDP Proxy domain.
 *
 * @author Thomas Johansen <thomas@thomasjohansen.it>
 */
public class PersonMapper implements Function<DSFPersonResource, Person> {

    @Override
    public Person apply(DSFPersonResource external) {
        return Person.builder()
                .fødselsnummer(external.getFoedselsnr())
                .fødselsdato(fødselsdato(external.getFoedselsdato()))
                .fornavn(external.getFornavn())
                .etternavn(external.getEtternavn())
                .kjønn(kjønn(external.getKjoenn()))
                .sivilstatus(sivilstatus(external.getSivilstatusKode()))
                .statsborgerskap(external.getStatsborgerskap())
                .fødeland(external.getFoedelandKode())
                .gateadresse(external.getGateadresse())
                .postnr(external.getPostnr())
                .postadresse(external.getPostadresse())
                .postadresseLand(external.getPostadresseLand())
                .postadresseLandKode(external.getPostadresseLandKode())
                .postadresseTilleggslinje(external.getPostadresseTilleggslinje())
                .build();
    }

    private LocalDate fødselsdato(String foedselsdato) {
        if (foedselsdato == null)
            return null;
        return LocalDate.parse(foedselsdato, DateTimeFormat.forPattern("yyyyMMdd"));
    }

    private Person.Sivilstatus sivilstatus(int sivilstatuskode) {
        if (sivilstatuskode == -1)
            return null;
        switch (sivilstatuskode) {
            case 1: return Person.Sivilstatus.Ugift;
            case 2: return Person.Sivilstatus.Gift;
            case 3: return Person.Sivilstatus.Enke;
            case 4: return Person.Sivilstatus.Skilt;
            case 5: return Person.Sivilstatus.Separert;
            case 6: return Person.Sivilstatus.RegistrertPartner;
            case 7: return Person.Sivilstatus.SeparertPartner;
            case 8: return Person.Sivilstatus.SkiltPartner;
            case 9: return Person.Sivilstatus.GjenlevendePartner;
            default: throw new IllegalArgumentException("Ugyldig sivilstatuskode: " + sivilstatuskode);
        }
    }

    private Person.Kjønn kjønn(String value) {
        if (value == null)
            return null;
        switch (value) {
            case "K": return Person.Kjønn.Kvinne;
            case "M": return Person.Kjønn.Mann;
            default: throw new IllegalArgumentException("Ugyldig kjønn: " + value);
        }
    }

}
