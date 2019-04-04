package no.difi.eidas.sproxy.integration.eidas.response;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class IpProvider {
    public String ip() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.toString();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't look up servers IP", e);
        }
    }
}
