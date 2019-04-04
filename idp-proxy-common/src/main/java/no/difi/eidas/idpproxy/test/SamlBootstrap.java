package no.difi.eidas.idpproxy.test;

import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;

public class SamlBootstrap {
    static {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        // let the static block run once
    }
}
