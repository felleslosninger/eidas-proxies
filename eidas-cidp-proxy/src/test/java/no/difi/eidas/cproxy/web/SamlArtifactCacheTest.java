package no.difi.eidas.cproxy.web;

import no.difi.eidas.cproxy.AbstractBaseTest;
import no.difi.eidas.cproxy.config.CacheConfiguration;
import org.ehcache.CacheManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {AbstractBaseTest.Configuration.class, CacheConfiguration.class})
public class SamlArtifactCacheTest {
    @Autowired
    private CacheManager cacheManager;

    @Test
    public void getSamlArtifact() {
        SamlArtifactCache samlArtifactCache = new SamlArtifactCache(cacheManager);
        samlArtifactCache.putSamlArtifact("testSamlArtifact");
        String cacheHit = samlArtifactCache.getSamlArtifact("testSamlArtifact");
        assertEquals("testSamlArtifact", cacheHit);
        String noHit = samlArtifactCache.getSamlArtifact("noHitSamlArtifact");
        assertNull(noHit);
    }

}