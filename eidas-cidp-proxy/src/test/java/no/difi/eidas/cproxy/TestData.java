package no.difi.eidas.cproxy;

import no.difi.eidas.idpproxy.test.ResourceReader;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;

import javax.xml.namespace.QName;

public abstract class TestData {

	final static String mockedCPepsAuthnRequest			= "saml/MockedAuthnRequestCPEPS.xml";
	final static String mockedCPepsAuthnRequestENCODED  = "saml/MockedAuthnRequestENCODED.xml";
	final static String pepsAuthnRequestFile 			= "saml/AuthnRequestPEPS.xml";
	final static String EncryptedPepsAuthnRequestFile 	= "saml/EnctryptedAuthnRequestPeps";
	final static String pepsAuthnResponseFile 			= "saml/AuthnResponsePEPS.xml";
	final static String idpAuthnResponseFile	   		= "saml/AuthnResponseIdPorten.xml";
	final static String pepsAuthnRequestEncodedFile = "";
	final static String idpAuthnResponseEncodedFile = "";
	final static String idpAssertionResponse			= "saml/AssertionIDP.xml";
	final static String idpArtifactResponse 			= "saml/fromIDPorten/artifactResponse.xml";
	final static String idpArtifactResolve				= "saml/ArtifactResolve.xml";
    final static String mockedIdPortenAuthnRequest = "saml/MockedIdPortenAuthnRequest.xml";
    final static String mockedPepsRequest = "saml/MockedPepsRequest.xml";
    final static String mockedEidasAuthnRequestLow = "saml/MockedEidasAuthnRequestLow.xml";

    public static String encryptedPepsAuthnRequest(){
    	return ResourceReader.read(EncryptedPepsAuthnRequestFile);
    }

    public static String idpAssertionResponse() {
		return ResourceReader.read(idpAssertionResponse);
	}
    
    public static String idpArtifactResponse(){
    	return ResourceReader.read(idpArtifactResponse);
    }
    
    public static String idpArtifactResolve(){
    	return ResourceReader.read(idpArtifactResolve);
    }
    
    public static String mockedAuthnRequestCPEPS(){
    	return ResourceReader.read(mockedCPepsAuthnRequest);
    }

    public static String mockedIdPortenAuthnRequest() {
        return ResourceReader.read(mockedIdPortenAuthnRequest);
    }

    public static String mockedPepsRequest() {
        return ResourceReader.read(mockedPepsRequest);
    }
    
    public static String mockedAuthnRequestENCODED(){
    	return ResourceReader.read(mockedCPepsAuthnRequestENCODED);
    }

    public static String mockedEidasAuthnRequestLow() {
        return ResourceReader.read(mockedEidasAuthnRequestLow);
    }

    @SuppressWarnings("unchecked")
    public static <T> T buildSAMLObject(final Class<T> clazz) {
        try {
            XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
            QName defaultElementName = (QName)clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            return (T)builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new IllegalArgumentException(
                    String.format("Could not create SAML object: %s", clazz.getName()),
                    e);
        }
    }
}
