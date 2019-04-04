package no.difi.eidas.sproxy.web;

import no.difi.eidas.sproxy.AbstractBaseTest;
import no.difi.eidas.sproxy.AbstractBaseTest.EidasSproxyTest;
import no.difi.eidas.sproxy.config.ConfigProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@EidasSproxyTest
public class MetadataControllerTest extends AbstractBaseTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext springContext;

    @Autowired
    private ConfigProvider configProvider;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(springContext).build();
    }

    @Test
    public void testMetadata() throws Exception {
        String entityIdAttribute = String.format("entityID=\"%s\"", configProvider.eidasMetadataUrl());
        mockMvc.perform(get("/"))
                .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE + ";charset=ISO-8859-1"))
                .andExpect(content().string(containsString(entityIdAttribute)))
                .andExpect(content().string(containsString("<md:EmailAddress>idporten@difi.no</md:EmailAddress>")));
    }

}
