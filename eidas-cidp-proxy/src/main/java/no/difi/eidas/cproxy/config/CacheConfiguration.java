package no.difi.eidas.cproxy.config;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnExpression("'${spring.cache.type}'!='none'")
public class CacheConfiguration {
    @Value("${cache.ttl-in-minutes:60}")
    private int ttl;

    @Bean(name = "ehcacheManager")
    public CacheManager cacheManager() {
        return CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("samlArtifact",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(ttl)))
                                .build())
                .build(true);
    }

}
