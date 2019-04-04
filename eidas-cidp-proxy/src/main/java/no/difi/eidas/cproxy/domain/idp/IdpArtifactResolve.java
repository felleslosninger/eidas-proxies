package no.difi.eidas.cproxy.domain.idp;

import no.difi.opensaml.wrapper.AbstractOpenSAMLWrapper;
import org.opensaml.saml2.core.Artifact;
import org.opensaml.saml2.core.ArtifactResolve;

public class IdpArtifactResolve extends AbstractOpenSAMLWrapper<ArtifactResolve> {

	private Artifact artifact;
	private ArtifactResolve artifactResolve;
	
	public IdpArtifactResolve(String xml) {
		super(xml);
		this.artifact = wrappedOpenSAMLObject.getArtifact();
		this.artifactResolve = getOpenSAMLObject();
	}
		
	public ArtifactResolve artifactResolve() {
		return artifactResolve;
	}
	
	public Artifact artifact() {
		return artifact;
	}

}
