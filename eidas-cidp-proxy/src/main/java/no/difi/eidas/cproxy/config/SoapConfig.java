package no.difi.eidas.cproxy.config;

import org.apache.commons.httpclient.HttpClient;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.client.http.HttpSOAPClient;
import org.opensaml.xml.parse.BasicParserPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SoapConfig {
    @Bean
    @Scope
    HttpSOAPClient soapClient() {
        HttpClientBuilder clientBuilder = new HttpClientBuilder();
        clientBuilder.setMaxTotalConnections(100);
        clientBuilder.setMaxConnectionsPerHost(100);
        HttpClient httpClient = clientBuilder.buildClient();
        return new HttpSOAPClient(httpClient, new BasicParserPool());
    }
}

