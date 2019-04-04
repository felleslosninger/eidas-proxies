package no.difi.eidas.sproxy.integration.eidas;

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;

import static no.difi.eidas.sproxy.config.SpringConfig.EIDAS_ENGINE;

public class TestSamlEngineProvider {

    private static final ProtocolEngineI engine;

    static {
        engine = ProtocolEngineFactory.getDefaultProtocolEngine(EIDAS_ENGINE);
    }

    public static ProtocolEngineI engine() {
        return engine;
    }

}
