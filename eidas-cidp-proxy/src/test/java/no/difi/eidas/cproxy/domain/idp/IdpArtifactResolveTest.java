package no.difi.eidas.cproxy.domain.idp;

import no.difi.eidas.cproxy.TestData;
import no.difi.eidas.idpproxy.test.SamlBootstrap;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class IdpArtifactResolveTest extends TestData{

	private IdpArtifactResolve idpArtifactResolve;
	
    @Before
    public void setUp() {
        SamlBootstrap.init();
		idpArtifactResolve = new IdpArtifactResolve(idpArtifactResolve());
    }
    
	@Test
	public void testIdpArtifactResolveSourceXml() {
		assertNotNull(idpArtifactResolve.getSourceXml());		
	}	

	@Test
	public void testArtifactResolve() {
		assertThat(
				idpArtifactResolve.artifactResolve().getIssuer().getValue(), 
				is("testsp"));
	}	
	
	@Test
	public void testArtifact() {
		assertThat(
                idpArtifactResolve.artifact().getArtifact(),
				is("AAQAAHh4rmXynxE9fFjEc3bPn8FL2jEzZ5rozFwNWX9mlgQO/1+szrz7MDE="));
	}
}
