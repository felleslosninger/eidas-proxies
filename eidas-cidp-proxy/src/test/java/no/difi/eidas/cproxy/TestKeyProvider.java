package no.difi.eidas.cproxy;

import com.google.common.io.Resources;
import no.difi.opensaml.signature.KeyStoreReader;

import java.security.KeyStore;
import java.security.PublicKey;

public class TestKeyProvider {
    private static final String password = "changeit";
    private static final String alias = "test";
    private static final String keyStoreLocation = "idPortenKeystore.jks";

    public static final KeyStore.PrivateKeyEntry privateKey;
    public static final PublicKey publicKey;

    static {
        try {
            KeyStoreReader reader = new KeyStoreReader(Resources.toByteArray(
                    Resources.getResource(keyStoreLocation)), password, "jks");
            privateKey = reader.privateKey(alias, password);
            publicKey = reader.publicKey(alias);
        } catch(Exception e) {
            throw new RuntimeException("Can't read test keystore", e);
        }
    }
}
