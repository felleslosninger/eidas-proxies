package no.difi.eidas.cproxy.integration.dsf;

import com.google.common.base.Optional;

import java.util.Map;

public class DSFPerson {
    
	private static final String idpFirstName = "forvavn";
    private static final String idpLastName = "etternavn";
    private static final String idpBirth = "foedselsdato";
    private static final String idpSSN	 = "personnummer";
    
    private final Map<String, String> personalAttributes;

    public DSFPerson(Map<String, String> personalAttributes) {
        this.personalAttributes = personalAttributes;
    }

    public Map<String, String> personalAttributes() {
        return personalAttributes;
    }

    public Optional<Name> name() {
        String firstName = personalAttributes.get(idpFirstName);
        String lastName = personalAttributes.get(idpLastName);
        String birth = personalAttributes.get(idpBirth);
        String ssn = personalAttributes.get(idpSSN);

        return firstName != null && lastName != null && birth != null && ssn != null ?
                Optional.of(new Name(firstName, lastName, birth, ssn)) :
                Optional.absent();
    }

    public static class Name {
        private final String firstName;
        private final String lastName;
        private final String birth;
        private final String ssn;
        
        public Name(String firstName, String lastName, String birth, String ssn) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birth = birth;
            this.ssn  = ssn;
        }

        public String firstName() {
            return firstName;
        }

        public String lastName() {
            return lastName;
        }

        public String birth() {
            return birth;
        }
        private String ssn() {
        	return ssn;
		}
    }
}
