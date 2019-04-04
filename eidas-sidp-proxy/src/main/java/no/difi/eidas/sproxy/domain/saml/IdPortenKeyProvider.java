package no.difi.eidas.sproxy.domain.saml;

import com.google.common.io.Resources;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.opensaml.signature.KeyStoreReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.PublicKey;

@Service
@Scope
public class IdPortenKeyProvider {
    private final KeyStore.PrivateKeyEntry privateKey;
    private final PublicKey publicKey;

    @Autowired
    public IdPortenKeyProvider(ConfigProvider config) {
        try {
            KeyStoreReader reader = new KeyStoreReader(
                    Resources.toByteArray(Resources.getResource(config.idPortenKeystoreLocation())),
                    config.idPortenKeystorePassword(),
                    config.idPortenKeystoreType()
            );
            privateKey = reader.privateKey(
                    config.idPortenKeystoreAlias(),
                    config.idPortenKeystorePrivatekeyPassword()
            );
            publicKey = reader.publicKey(config.idPortenKeystoreAlias());
        } catch (IOException e) {
            throw new RuntimeException("Keystore initialization failure", e);
        }
    }

    public KeyStore.PrivateKeyEntry privateKey() {
        return privateKey;
    }

    public PublicKey publicKey() {
        return publicKey;
    }

}
