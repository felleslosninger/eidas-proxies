package no.difi.eidas.cproxy;

import com.google.common.io.Resources;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.PasswordLookup;
import no.difi.opensaml.signature.KeyStoreReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

public class OIDCTestKeyProvider {
    private static final String password = "changeit";
    private static final String alias = "buypass";
    private static final String keyStoreLocation = "keystore.jks";

    public static final KeyStore.PrivateKeyEntry privateKey;
    public static final PublicKey publicKey;

    static {
        try {
            KeyStoreReader reader = new KeyStoreReader(Resources.toByteArray(
                    Resources.getResource(keyStoreLocation)), password, "jks");
            privateKey = reader.privateKey(alias, password);
            publicKey = reader.publicKey(alias);
        } catch (Exception e) {
            throw new RuntimeException("Can't read test keystore", e);
        }
    }

    public static String getJWSJSON() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(getFileInputStream(), password.toCharArray());
        JWKSet jwkSet = JWKSet.load(ks, new PasswordLookup() {
            @Override
            public char[] lookupPassword(final String name) {
                if ("buypass".equalsIgnoreCase(name)) return "changeit".toCharArray();
                else return "".toCharArray();
            }
        });


        return jwkSet.toJSONObject().toJSONString();
    }

    private static FileInputStream getFileInputStream() throws FileNotFoundException {
        try {
            final URL url = ClassLoader.getSystemResource(OIDCTestKeyProvider.keyStoreLocation);
            if (url != null) {
                return new FileInputStream(url.getFile());
            }
        } catch (Exception e) {
            //Ignore error.
        }
        return new FileInputStream(OIDCTestKeyProvider.keyStoreLocation);
    }
}