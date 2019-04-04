package no.difi.eidas.sproxy.domain.saml;

import no.difi.eidas.sproxy.ResourceReader;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SamlResponseXmlTest {

    @Test
    public void testExtractAttributeStatement() {
        SamlResponseXml samlXml = new SamlResponseXml(ResourceReader.eidasAuthnResponse());
        assertTrue(samlXml.attributeStatementString().startsWith("<saml2:AttributeStatement"));
        assertTrue(samlXml.attributeStatementString().endsWith("</saml2:AttributeStatement>"));
    }

}
