package no.difi.eidas.cproxy.integration.node;

import eu.eidas.auth.engine.metadata.impl.BaseMetadataFetcher;
import org.springframework.stereotype.Service;

@Service
public class IdpMetadataFetcher extends BaseMetadataFetcher{
    @Override
    protected boolean mustUseHttps() {
        return false;
    }

    @Override
    protected String[] getTlsEnabledProtocols() {
        return getTlsEnabledProtocols("");
    }

    @Override
    protected boolean mustValidateSignature(String url) {
        return false;
    }
}
