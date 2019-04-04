package no.difi.eidas.samlengine;

import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.core.impl.AbstractProtocolSigner;

import java.util.Map;

/**
 * A ProtocolSigner that keeps private key for signing and trusted certificates in separate KeyStores.
 */
public class DualKeyStoreProtocolSigner extends AbstractProtocolSigner {

    public DualKeyStoreProtocolSigner(Map<String,String> properties) throws SamlEngineConfigurationException {
        super(new DualKeyStoreSignatureConfigurator().getSignatureConfiguration(properties));
    }


}
