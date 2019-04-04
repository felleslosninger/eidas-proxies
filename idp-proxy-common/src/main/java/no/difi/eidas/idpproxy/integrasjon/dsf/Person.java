package no.difi.eidas.idpproxy.integrasjon.dsf;

import com.google.common.base.Preconditions;
import org.joda.time.LocalDate;

/**
 * Represents a person from Det sentrale folkeregister (DSF).
 */
public class Person {

	public enum Sivilstatus {
		Ugift,
		Gift,
		Enke,
		Skilt,
		Separert,
		RegistrertPartner,
		SeparertPartner,
		SkiltPartner,
		GjenlevendePartner
	}

	public enum Kjønn {
		Kvinne,
		Mann
	}

    private String fødselsnummer;
	private LocalDate fødselsdato;
	private String fornavn;
    private String etternavn;
    private Kjønn kjønn;
	private String fødeland;
	private Sivilstatus sivilstatus;
	private String gateadresse;
	private String postnr;
	private String postadresse;
	private String postadresseLand;
	private String postadresseLandKode;
	private String postadresseTilleggslinje;
	private String statsborgerskap;

    private Person() {
    }

    public String fødselsnummer() {
        return fødselsnummer;
    }

	public String fornavn() {
		return fornavn;
	}

	public LocalDate fødselsdato() {
		return fødselsdato;
	}

	public String etternavn() {
		return etternavn;
	}

	public Kjønn kjønn() {
		return kjønn;
	}

	public String fødeland() {
		return fødeland;
	}

	public Sivilstatus sivilstatus() {
		return sivilstatus;
	}
	
	public String gateadresse() {
		return gateadresse;
	}
	
	public String postnr() {
		return postnr;
	}
	
	public String postadresse() {
		return postadresse;
	}
	
	public String postadresseLand() {
		return postadresseLand;
	}
	
	public String postadresseLandKode() {
		return postadresseLandKode;
	}
	public String postadresseTilleggslinje() {
		return postadresseTilleggslinje;
	}
	
	public String statsborgerskap(){
		return statsborgerskap;
	}

	public static Builder builder() {
		return new Builder(new Person());
	}

	public static class Builder {
		private Person instance;

		public Builder(Person instance) {
			this.instance = instance;
		}

		public Builder fødselsnummer(String fødselsnummer) {
			instance.fødselsnummer = fødselsnummer;
			return this;
		}

		public Builder fødselsdato(LocalDate fødselsdato) {
			instance.fødselsdato = fødselsdato;
			return this;
		}

		public Builder fornavn(String fornavn) {
			instance.fornavn = fornavn;
			return this;
		}

		public Builder etternavn(String etternavn) {
			instance.etternavn = etternavn;
			return this;
		}

		public Builder kjønn(Kjønn kjønn) {
			instance.kjønn = kjønn;
			return this;
		}

		public Builder sivilstatus(Sivilstatus sivilstatus) {
			instance.sivilstatus = sivilstatus;
			return this;
		}

		public Builder fødeland(String fødeland) {
			instance.fødeland = fødeland;
			return this;
		}
		
		public Builder gateadresse(String gateadresse) {
			instance.gateadresse = gateadresse;
			return this;
		}
		
		public Builder postnr(String postnr) {
			instance.postnr = postnr;
			return this;
		}
		
		public Builder postadresse(String postadresse) {
			instance.postadresse = postadresse;
			return this;
		}
		
		public Builder postadresseLand(String postadresseLand) {
			instance.postadresseLand = postadresseLand;
			return this;
		}
		
		public Builder postadresseLandKode(String postadresseLandKode) {
			instance.postadresseLandKode = postadresseLandKode;
			return this;
		}
		
		public Builder postadresseTilleggslinje(String postadresseTilleggslinje) {
			instance.postadresseTilleggslinje = postadresseTilleggslinje;
			return this;
		}
		
		public Builder statsborgerskap(String statsborgerskap){
			instance.statsborgerskap = statsborgerskap;
			return this;
		}

		public Person build() {
			Preconditions.checkNotNull(instance.fødselsnummer);
			return instance;
		}
	}

}
