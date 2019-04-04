package no.difi.eidas.cproxy.domain.idp;

import com.google.common.io.Resources;
import no.difi.eidas.cproxy.TestData;
import no.difi.eidas.cproxy.saml.CIDPProxyKeyProvider;
import no.difi.eidas.idpproxy.test.ResourceReader;
import no.difi.eidas.idpproxy.test.SamlBootstrap;
import no.difi.opensaml.signature.KeyStoreReader;
import no.difi.opensaml.signature.SamlEncrypter;
import no.difi.opensaml.util.ConvertUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml2.core.Assertion;

import java.io.IOException;
import java.security.KeyStore;
import java.security.PublicKey;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IdPortenSamlResponseTest extends TestData{
	
	private static final String password  		= "changeit";
	private static final String alias    			= "test";
	private static final String keyStoreLocation  = "idPortenKeystore.jks";
	
	private KeyStore.PrivateKeyEntry privateKey;
	private PublicKey publicKey;

	@Mock
	private CIDPProxyKeyProvider cidpProxyKeyProvider;
    @Before
    public void setUp() {
        SamlBootstrap.init();
		KeyStoreReader reader;
		try {		
			reader = new KeyStoreReader(Resources.toByteArray(
					Resources.getResource(keyStoreLocation)), password, "jks");
			privateKey = reader.privateKey(alias, password);
			publicKey = reader.publicKey(alias);

		} catch (IOException e) {
			e.printStackTrace();
		}
    }    
    
    /**
     * verify decoding authnreponse
     */
    @Test
    public void testDecodeIdPortenResponse(){
    	String saml = ResourceReader.read("saml/IdPortenSamlResponseEncoded.xml");
		SamlEncrypter encrypter = new SamlEncrypter(publicKey, privateKey.getPrivateKey());
    	new IdPAuthnResponse(new ConvertUtil().decodeBase64(saml), encrypter);
    }
    
    /**
     * verify decryption of encoded assertion
     */
    @Test
    public void testDecodeIdPortenEncryptedAssertion(){
		SamlEncrypter encrypter = new SamlEncrypter(publicKey, privateKey.getPrivateKey());

    	String encodedSaml = ResourceReader.read("saml/IdPortenSamlResponseEncoded.xml");
    	IdPAuthnResponse authnResponse = new IdPAuthnResponse(new ConvertUtil().decodeBase64(encodedSaml), encrypter);

    	Assertion decrypted = authnResponse.assertions().get(0);
    	assertThat(decrypted.getID().contains("s21cabc2cfead2817c311934e36eed1f745465fe59"), is(true));
    	assertThat(decrypted.getIssuer().getValue().equals("vagrant-idp"), is(true));
    }
}
