package no.difi.eidas.cproxy.integration.node;

import no.difi.eidas.cproxy.integration.node.SamlEngineProvider;
import org.junit.Test;

public class SamlEngineProviderIT {
	
	@Test
	public void test() {				
		new SamlEngineProvider().engine();
	}
}
