package no.difi.eidas.sproxy.config;

import no.difi.eidas.idpproxy.integrasjon.Urls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    @Value("${mf.gateway.url}")
    private String mfGatewayUrl;
    @Value("${mf.gateway.username}")
    private String mfGatewayUsername;
    @Value("${mf.gateway.password}")
    private String mfGatewayPassword;
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
    @Value("${users.test:19890605.CE/NO/05061989:05068907693}")
    private String eIDASIdentifierDnumbers;

    private static final Logger log = LoggerFactory.getLogger(ConfigProvider.class);

    public Map<String, String> eIDASIdentifierDnumbers() {
        return parseTestUsers(eIDASIdentifierDnumbers);
    }

    public String getMfGatewayUrl() {
        return mfGatewayUrl;
    }

    public String getMfGatewayUsername() {
        return mfGatewayUsername;
    }

    public String getMfGatewayPassword() {
        return mfGatewayPassword;
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

    protected Map<String,String> parseTestUsers(String testUsers) {
        Map<String,String> map = new HashMap<>();
        for (String entry: testUsers.split(",")) {
            if (entry.length() == 0) {
                continue;
            }
            String[] split = entry.split(":");
            String eidasIdentifikator = getStrippedEidasIdentifikator(split[0]);
            log.debug("Testuser: key: -" + eidasIdentifikator + "- value: " + split[1]);
            map.put(eidasIdentifikator, split[1]);
        }
        return map;
    }

    private String getStrippedEidasIdentifikator(String personIdentifikator) {
        try {
            String regex = ".*\\/.*\\/(.*)";
            Matcher matcher = Pattern.compile(regex).matcher(personIdentifikator);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (PatternSyntaxException e) {
            log.warn(String.format("Regex for mock eidas identifier is invalid"), e);
        }
        return null;
    }
}
