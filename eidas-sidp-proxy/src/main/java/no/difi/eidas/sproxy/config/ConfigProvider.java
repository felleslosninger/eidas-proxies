package no.difi.eidas.sproxy.config;

import no.difi.eidas.idpproxy.integrasjon.Urls;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigProvider {

    public ConfigProvider() {
    }

    @Value("${saml.instantIssueTimeToLive}")
    private int instantIssueTimeToLive;
    @Value("${saml.instantIssueTimeSkew}")
    private int instantIssueTimeSkew;
    @Value("${saml.idp.entityName}")
    private String idpEntityName;
    @Value("${auditlog.dir}")
    private String auditLogDir;
    @Value("${auditlog.file}")
    private String auditLogFile;
    @Value("${auditlog.dataSeparator}")
    private String auditLogDataSeparator;
    @Value("${fileconfig.readPeriod}")
    private long fileConfigReadPeriod;
    @Value("${fileconfig.countriesAttributes}")
    private String fileConfigCountriesAttributes;
    @Value("${eidas-node.url}")
    private String eidasNodeUrl;
    @Value("${sidp-proxy.url}")
    private String sidpProxyUrl;
    @Value("${dsf.gateway.url}")
    private String dsfGatewayUrl;
    @Value("${dsf.gateway.timeout}")
    private int dsfGatewayTimeout;
    @Value("${dsf.gateway.retryCount}")
    private int dsfRetryCount;
    @Value("${mf.gateway.url}")
    private String mfGatewayUrl;
    @Value("${mf.gateway.timeout}")
    private int mfGatewayTimeout;
    @Value("${mf.gateway.retryCount}")
    private int mfRetryCount;
    @Value("${idporten.keystore.type}")
    private String idPortenKeystoreType;
    @Value("${idporten.keystore.alias}")
    private String idPortenKeystoreAlias;
    @Value("${idporten.keystore.location}")
    private String idPortenKeystoreLocation;
    @Value("${idporten.keystore.password}")
    private String idPortenKeystorePassword;
    @Value("${idporten.keystore.privatekeyPassword}")
    private String idPortenKeystorePrivatekeyPassword;
    @Value("${eventlog.jms.queuename}")
    private String jmsQueueName;
    @Value("${eventlog.jms.url}")
    private String jmsUrl;
    @Value("${users.test:CE/NO/05061989.UTENLANDSK_IDENTIFIKASJONS_NUMMER:05068907693}")
    private String eIDASIdentifierDnumbers;

    public Map<String, String> eIDASIdentifierDnumbers() {
        return parseTestUsers(eIDASIdentifierDnumbers);
    }

    public String getMfGatewayUrl() {
        return mfGatewayUrl;
    }

    public int instantIssueTimeToLive() {
        return instantIssueTimeToLive;
    }
    
    public int instantIssueTimeSkew() {
        return instantIssueTimeSkew;
    }

    public String idpEntityName() {
    	return idpEntityName;
    }

    public String auditLogDir() {
        return auditLogDir;
    }

    public String auditLogFile() {
        return auditLogFile;
    }

    public String auditLogDataSeparator() {
        return auditLogDataSeparator;
    }

    public URL eidasNodeUrl() {
        return url(eidasNodeUrl, "eidasNodeUrl");
    }

    public URL eidasProxyAuthUrl() {
        return endpointUrl(sidpProxyUrl, "/auth", "eidasProxyAuthUrl");
    }

    public URL eidasMetadataUrl() {
        return endpointUrl(sidpProxyUrl, "/", "eidasMetadatUrl");
    }

    public long fileConfigReadPeriod() {
        return fileConfigReadPeriod;
    }

    public File fileConfigCountriesAttributes() {
        return file(fileConfigCountriesAttributes);
    }

    public URL dsfGatewayUrl() {
        return url(dsfGatewayUrl, "dsfGatewayUrl");
    }

    public Integer dsfGatewayTimeout() {
        return dsfGatewayTimeout;
    }

    public int dsfRetryCount() {
        return dsfRetryCount;
    }

    public String idPortenKeystoreLocation() {
        return idPortenKeystoreLocation;
    }

    public String idPortenKeystorePassword() {
        return idPortenKeystorePassword;
    }

    public String idPortenKeystorePrivatekeyPassword() {
        return idPortenKeystorePrivatekeyPassword;
    }

    public String idPortenKeystoreAlias() {
        return idPortenKeystoreAlias;
    }

    public String idPortenKeystoreType() {
        return idPortenKeystoreType;
    }

    public String jmsQueueName() {
        return jmsQueueName;
    }

    public String jmsUrl() {
        return jmsUrl;
    }

    private URL url(String value, String description) {
        try {
            return Urls.url(value);
        } catch (Urls.UrlMalformed e) {
            throw new RuntimeException(String.format("Wrong format for URL: %s for %s", value, description), e);
        }
    }

    protected String concatUrl(String base, String resource) {
        return base.replaceAll("/\\z", "") + "/" + resource.replaceAll("\\A/", "");
    }

    protected URL endpointUrl(String base, String endpoint, String description) {
        return url(concatUrl(base, endpoint), description);
    }

    private File file(String name) {
        return new File(name);
    }

    private Map<String,String> parseTestUsers(String testUsers) {
        Map<String,String> map = new HashMap<>();
        for (String entry: testUsers.split(",")) {
            if (entry.length() == 0) {
                continue;
            }
            String[] split = entry.split(":");
            map.put(split[0], split[1]);
        }
        return map;
    }
}
