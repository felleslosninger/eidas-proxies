package no.difi.eidas.idpproxy;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class ResourceReader {
    public static String dsfGatewayResponse() {
        return read("dsfGatewayResponse.json");
    }

    private static String read(String name) {
        URL resource = Resources.getResource(name);
        try {
            return Resources.toString(resource, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
