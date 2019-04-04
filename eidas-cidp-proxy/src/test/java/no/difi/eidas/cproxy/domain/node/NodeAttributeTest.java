package no.difi.eidas.cproxy.domain.node;

import org.junit.Test;

import static org.junit.Assert.*;


public class NodeAttributeTest {


    @Test
    public void testValuePresent() {
        String value = "foo";
        NodeAttribute nodeAttribute = new NodeAttribute(value);
        assertTrue(nodeAttribute.isPresent());
        assertNotNull(nodeAttribute.value());
        assertTrue(nodeAttribute.value().isPresent());
        assertEquals(value, nodeAttribute.value().get());
    }

    @Test
    public void missingRequired() {
        NodeAttribute nodeAttribute = new NodeAttribute(NodeAttribute.NotPresentReason.NotAvailable, false);
        assertFalse(nodeAttribute.isRequired());
        assertFalse(nodeAttribute.isPresent());
        assertNotNull(nodeAttribute.value());
        assertFalse(nodeAttribute.value().isPresent());
    }

}
