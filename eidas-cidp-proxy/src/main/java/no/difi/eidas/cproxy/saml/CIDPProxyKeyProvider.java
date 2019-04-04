package no.difi.eidas.cproxy.saml;

import com.google.common.io.Resources;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.opensaml.signature.KeyStoreReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

@Service
@Scope
public class CIDPProxyKeyProvider {
    private final KeyStore.PrivateKeyEntry privateKey;
    private final PublicKey publicKey;

    @Autowired
    public CIDPProxyKeyProvider(ConfigProvider config) {
        try {
            KeyStoreReader reader = new KeyStoreReader(
                    Resources.toByteArray(Resources.getResource(config.cIDPProxyKeystoreLocation())),
                    config.cIDPProxyKeystorePassword(),
                    config.cIDPProxyKeystoreType()
            );
            privateKey = reader.privateKey(
                    config.cIDPProxyKeystoreAlias(),
                    config.cIDPProxyKeystorePrivatekeyPassword()
            );
            publicKey = reader.publicKey(config.cIDPProxyKeystoreAlias());
        } catch (IOException e) {
            throw new RuntimeException("Keystore initialization failure", e);
        }
    }

    public PrivateKey privateKey(){
    	return privateKey.getPrivateKey();
    }

    public KeyStore.PrivateKeyEntry privateKeyEntry() {
        return privateKey;
    }

    public PublicKey publicKey() {
        return publicKey;
    }

}
