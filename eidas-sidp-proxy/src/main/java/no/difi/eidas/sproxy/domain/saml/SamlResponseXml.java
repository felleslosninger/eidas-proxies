package no.difi.eidas.sproxy.domain.saml;

import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

public class SamlResponseXml extends SamlXml {

    public SamlResponseXml(String samlXml) {
        super(samlXml);
    }

    public String attributeStatementString() {
        InputSource source = new InputSource(new StringReader(toString()));
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            Node nodeList = (Node) xpath.evaluate("//*[local-name()='Response']//*[local-name()='Assertion']//*[local-name()='AttributeStatement']", source, XPathConstants.NODE);
            return DocumentBuilderFactoryUtil.toString(nodeList);
        } catch (TransformerException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

}
