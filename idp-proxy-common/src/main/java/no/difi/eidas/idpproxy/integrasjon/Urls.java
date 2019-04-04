package no.difi.eidas.idpproxy.integrasjon;

import java.net.MalformedURLException;
import java.net.URL;

public class Urls {
    public static URL url(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new UrlMalformed("URL syntax error", e);
        }
    }

    public static class UrlMalformed extends RuntimeException {
        public UrlMalformed(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
