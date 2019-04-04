package no.difi.eidas.sproxy.web;

import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.EidasMetadata;
import eu.eidas.auth.engine.metadata.MetadataConfigParams;
import eu.eidas.auth.engine.metadata.OrganizationData;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.difi.eidas.sproxy.config.ConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static no.difi.eidas.sproxy.config.SpringConfig.EIDAS_ENGINE;

@Controller
@RequestMapping(value = "/")
public class MetadataController {

    private ProtocolEngineI eidasSamlEngine;
    private ConfigProvider configProvider;

    @Autowired
    public MetadataController(@Qualifier(EIDAS_ENGINE) ProtocolEngineI eidasSamlEngine,
                              ConfigProvider configProvider) {
        this.eidasSamlEngine = eidasSamlEngine;
        this.configProvider = configProvider;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<String> generateMetadata() throws EIDASSAMLEngineException {
        EidasMetadata.Generator generator = EidasMetadata.generator();
        MetadataConfigParams.Builder mcp = MetadataConfigParams.builder();
        mcp.spEngine(eidasSamlEngine);
        mcp.entityID(configProvider.eidasMetadataUrl().toString());
        mcp.assertionConsumerUrl(configProvider.eidasProxyAuthUrl().toString());
        mcp.authnRequestsSigned(true);
        mcp.wantAssertionsSigned(true);
        mcp.digestMethods("http://www.w3.org/2001/04/xmlenc#sha256");
        mcp.signingMethods("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256");
        mcp.technicalContact(ContactData.builder().email("idporten@difi.no").build());
        mcp.supportContact(ContactData.builder().email("idporten@difi.no").build());
        mcp.organization(OrganizationData.builder().name("Difi").build());
        generator.configParams(mcp.build());
        String metadata = generator.build().getMetadata();
        return ResponseEntity.ok(metadata);
    }

}
