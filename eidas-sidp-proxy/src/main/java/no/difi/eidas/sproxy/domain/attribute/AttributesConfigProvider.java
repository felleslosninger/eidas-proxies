package no.difi.eidas.sproxy.domain.attribute;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.config.FileReader;
import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Attribute;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.CountriesAttributes;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Country;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AttributesConfigProvider {
    private static final Integer cacheKey = 0;
    private final LoadingCache<Integer, AttributesConfig> attributesCache;
    private final Gson gson;
    private final ConfigProvider config;

    @Autowired
    public AttributesConfigProvider(ConfigProvider config, final FileReader fileReader) {
        this.config = config;
        this.gson = new Gson();
        this.attributesCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(config.fileConfigReadPeriod(), TimeUnit.MILLISECONDS)
                .build(new CacheLoader<Integer, AttributesConfig>() {
                    @Override
                    public AttributesConfig load(Integer key) throws Exception {
                        return loadAttributesFromDisk(fileReader);
                    }
                });
    }

    @SuppressWarnings("all")
    public Set<Attribute> forCountry(CountryCode country) {
        return attributesConfig().forCountry(country);
    }

    public Optional<Country> getCountry(final CountryCode countryCode) {
        return attributesConfig()
                .countries()
                .stream()
                .filter(c -> c.countryCode().equals(countryCode))
                .findFirst();
    }

    public Set<Country> countries() {
        return attributesConfig().countries();
    }

    public boolean isAttributeCountrySpecific(final String name, CountryCode country) {
        return attributesConfig()
                .forCountryExcludingCommonDefault(country).stream()
                .filter(attribute -> name.equals(attribute.name()))
                .findFirst()
                .isPresent();
    }

    private AttributesConfig loadAttributesFromDisk(FileReader fileReader) {
        String content = fileReader.read(config.fileConfigCountriesAttributes());
        return new AttributesConfig(
                gson.fromJson(content, CountriesAttributes.class)
        );
    }

    private AttributesConfig attributesConfig() {
    try {
        return attributesCache.get(cacheKey);
    } catch (ExecutionException e) {
        throw new RuntimeException(e);
    }
    }

}
