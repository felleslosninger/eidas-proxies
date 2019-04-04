package no.difi.eidas.sproxy;

import com.google.common.io.Resources;
import no.difi.opensaml.signature.KeyStoreReader;

import java.io.IOException;
import java.security.KeyStore;
import java.security.PublicKey;

public class IdportenTestKey {
    public static final String alias = "selfsigned";
    public static final String password = "password";
    private static final KeyStoreReader keyStoreReader;

    static {
        try {
            keyStoreReader = new KeyStoreReader(Resources.toByteArray(Resources.getResource("idPortenKeystore.jks")), "password", "jks");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyStoreReader keyStoreReader() {
        return keyStoreReader;
    }

    public static PublicKey publicKey() {
        return keyStoreReader.publicKey(alias);
    }

    public static KeyStore.PrivateKeyEntry privateKey() {
        return keyStoreReader.privateKey(alias, password);
    }
}
