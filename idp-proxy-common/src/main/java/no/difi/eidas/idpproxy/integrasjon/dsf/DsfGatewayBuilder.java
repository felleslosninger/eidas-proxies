package no.difi.eidas.idpproxy.integrasjon.dsf;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import no.difi.dsfgateway.CustomHttpHeaderNames;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.DsfGatewayRestApi;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;

import java.net.URL;

public class DsfGatewayBuilder {
    private URL url;
    private String clientId;
    private Integer timeout;
    private Optional<Integer> retryAttempts = Optional.absent();

    public DsfGatewayBuilder(URL url, String clientId, Integer timeout) {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(clientId);
        Preconditions.checkNotNull(timeout);
        this.url = url;
        this.clientId = clientId;
        this.timeout = timeout;
    }

    public DsfGatewayBuilder retry(Integer retryAttempts) {
        this.retryAttempts = Optional.of(retryAttempts);
        return this;
    }

    public DsfGateway build() {
        return new DsfGateway(
                dsfGatewayApi()
        );
    }

    public DsfGatewayRestApi dsfGatewayApi() {
        DsfGatewayRestApi api = JAXRSClientFactory.create(
                url.toString(),
                DsfGatewayRestApi.class,
                ImmutableList.of(JacksonJsonProvider.class)

        );
        Client client = WebClient.client(api);
        clientId(client);
        retry(client);
        timeout(client);
        json(client);
        return api;
    }
    private void json(Client client) {
        client.header("Accept", "application/json");
    }


    private void clientId(Client client) {
        client.header(CustomHttpHeaderNames.CLIENT_ID, clientId);
    }

    private void retry(Client client) {
        if(retryAttempts.isPresent()) {
            client.header(CustomHttpHeaderNames.RETRY_ATTEMPTS, retryAttempts.get());
        }
    }

    private void timeout(Client client) {
        HTTPConduit httpConduit = WebClient.getConfig(client).getHttpConduit();
        httpConduit.getClient().setConnectionTimeout(timeout);
        httpConduit.getClient().setReceiveTimeout(timeout);
    }


}
