package no.difi.eidas.sproxy.integration.eidas.request;

import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnRequest;
import no.difi.eidas.sproxy.domain.country.CountryCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EidasAuthenticator {
    private final ConfigProvider configProvider;
    private final EidasAuthnRequestFactory eidasAuthnRequestFactory;

    @Autowired
    public EidasAuthenticator(
            ConfigProvider configProvider,
            EidasAuthnRequestFactory eidasAuthnRequestFactory) {
        this.configProvider = configProvider;
        this.eidasAuthnRequestFactory = eidasAuthnRequestFactory;
    }

    public EidasAuthentication authenticate(
            IdPortenAuthnRequest idPortenAuthnRequest,
            CountryCode countryCode) {
        return new EidasAuthentication(
                eidasAuthnRequestFactory.create(idPortenAuthnRequest, countryCode),
                configProvider.eidasNodeUrl()
        );
    }


}
