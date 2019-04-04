package no.difi.eidas.samlengine;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.KeyStoreSignatureConfigurator;
import eu.eidas.auth.engine.configuration.dom.SignatureConfiguration;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;

public class DualKeyStoreSignatureConfigurator {

    private static final String TRUST_STORE_TYPE = "trustStoreType";
    private static final String TRUST_STORE_PASSWORD = "trustStorePassword";
    private static final String TRUST_STORE_PATH = "trustStorePath";
    private static final String SECURITY_PROVIDERS = "securityProviders";

    public SignatureConfiguration getSignatureConfiguration(Map<String, String> properties) throws SamlEngineConfigurationException {
        loadCryptoProviders(properties);
        String trustStoreType = properties.getOrDefault(TRUST_STORE_TYPE, "JKS");
        String trustStorePath = properties.get(TRUST_STORE_PATH);
        String trustStorePassword = properties.get(TRUST_STORE_PASSWORD);
        KeyStore trustStore = loadKeyStore(trustStorePath, trustStoreType, trustStorePassword);
        ImmutableSet<X509Certificate> trutstedCertificates = trustedCertificates(trustStore);
        SignatureConfiguration signatureConfiguration = new KeyStoreSignatureConfigurator().getSignatureConfiguration(properties,null);
        return new SignatureConfiguration(signatureConfiguration.isCheckedValidityPeriod(),
                signatureConfiguration.isDisallowedSelfSignedCertificate(),
                signatureConfiguration.isResponseSignAssertions(),
                signatureConfiguration.getSignatureKeyAndCertificate(),
                trutstedCertificates,
                signatureConfiguration.getSignatureAlgorithm(),
                signatureConfiguration.getSignatureAlgorithmWhiteList(),
                signatureConfiguration.getMetadataSigningKeyAndCertificate());
    }

    protected ImmutableSet<X509Certificate> trustedCertificates(KeyStore trustStore) throws SamlEngineConfigurationException {
        try {
            ImmutableSet.Builder<X509Certificate> certificates = ImmutableSet.builder();
            for (String alias : Collections.list(trustStore.aliases())) {
                certificates.add((X509Certificate) trustStore.getCertificate(alias));
            }
            return certificates.build();
        } catch (KeyStoreException e) {
            throw new SamlEngineConfigurationException("Failed to read certificates from truststore", e);
        }
    }

    protected void loadCryptoProviders(Map<String, String> properties) throws SamlEngineConfigurationException {
        Map<String, String> providers = StringUtils.isNotEmpty(properties.get(SECURITY_PROVIDERS))
                ? Splitter.on(",").withKeyValueSeparator('=').split(properties.get(SECURITY_PROVIDERS))
                : Collections.emptyMap();
        for (Map.Entry<String, String> entry : providers.entrySet()) {
            if (!providerLoaded(entry.getKey()))
                loadProvider(entry.getValue());
        }
    }

    protected void loadProvider(String providerClassName) throws SamlEngineConfigurationException {
        try {
            Class<?> providerClass = Class.forName(providerClassName);
            Security.insertProviderAt((Provider) providerClass.newInstance(), Security.getProviders().length);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SamlEngineConfigurationException("Failed to load provider " + providerClassName, e);
        }
    }

    protected boolean providerLoaded(String providerName) {
        for (Provider provider : Security.getProviders()) {
            if (provider.getName().equals(providerName))
                return true;
        }
        return false;
    }

    protected KeyStore loadKeyStore(String resource, String type, String password) throws SamlEngineConfigurationException {
        try (InputStream fis = new FileInputStream(resource)) {
            if (fis.available() > 0) {
                KeyStore keyStore = KeyStore.getInstance(type != null ? type : "JKS");
                keyStore.load(fis, password != null ? password.toCharArray() : null);
                return keyStore;
            } else {
                throw new SamlEngineConfigurationException("Failed to find keystore for trust certificate from location " + resource);
            }
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new SamlEngineConfigurationException("Failed to load trusted certificates from resource " + resource, e);
        }
    }

}
