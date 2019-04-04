package no.difi.eidas.idpproxy;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SubjectBasicAttributeTest {

	private static String prefix = "http://eidas.europa.eu/attributes/naturalperson/";

	@Test
	public void testFromAttributeName() throws Exception {
		for(String attribute: attributes()){
		  	assertThat(SubjectBasicAttribute.fromAttributeName(prefix.concat(attribute)).toString() == attribute, 
        			is(true));   
		}
	}

	private List<String> attributes(){
		return Arrays.asList(
				"CurrentGivenName",
				"CurrentFamilyName",
				"DateOfBirth",
				"PersonIdentifier",
				"PlaceOfBirth");
	}	
}
