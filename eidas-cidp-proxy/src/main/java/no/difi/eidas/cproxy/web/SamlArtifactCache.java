package no.difi.eidas.cproxy.web;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SamlArtifactCache {

    private final CacheManager cacheManager;

    @Autowired
    public SamlArtifactCache(@Qualifier("ehcacheManager") CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public String getSamlArtifact(String samlArtifact) {
        Cache<String, String> samlArtifactCache = cacheManager.getCache("samlArtifact", String.class, String.class);
        return samlArtifactCache.get(samlArtifact);
    }

    public void putSamlArtifact(String samlArtifact) {
        Cache<String, String> samlArtifactCache = cacheManager.getCache("samlArtifact", String.class, String.class);
        samlArtifactCache.put(samlArtifact, samlArtifact);
    }
}
