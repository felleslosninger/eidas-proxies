package no.difi.eidas.cproxy.domain.idp;

import com.google.common.io.Resources;
import no.difi.eidas.cproxy.TestData;
import no.difi.eidas.idpproxy.test.SamlBootstrap;
import no.difi.opensaml.signature.KeyStoreReader;
import no.difi.opensaml.signature.SamlSigner;
import no.difi.opensaml.util.ConvertUtil;
import no.difi.opensaml.util.XMLFormatter;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.*;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VerifyRedirectBuildIT extends TestData {

    private static final String assertionConsumerURL = "http://";
    private static final String idPortenDestinationURL = "https://";
    private static final String PASSWORD_AUTHN_CTX = AuthnContext.PASSWORD_AUTHN_CTX;

    private static String issuerName = "eidas-cproxy-sp";
    private static String password = "changeit";
    private static String alias = "test";
    private static String keyStoreLocation = "idPortenKeystore.jks";

    private KeyStore.PrivateKeyEntry privateKey;
    private PublicKey publicKey;
    private KeyStore keystore;

    private ConvertUtil convertUtil;

    @Before
    public void setUp() {
        SamlBootstrap.init();
        convertUtil = new ConvertUtil();

        KeyStoreReader reader;
        try {
            reader = new KeyStoreReader(Resources.toByteArray(
                    Resources.getResource(keyStoreLocation)), password, "jks");
            privateKey = reader.privateKey(alias, password);
            publicKey = reader.publicKey(alias);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createRedirectTest1() {
        String url = buildRedirectURLKey(authnRequest(), true);
        assertThat(url.contains("https://"), is(true));
        assertThat(url.contains("rsa-sha1&Signature"), is(true));
        assertThat(url.contains("locale=nb"), is(true));
        System.out.println(url);
    }

    private AuthnRequest authnRequest() {
        AuthnRequest authnRequest = buildSAMLObject(AuthnRequest.class);
        authnRequest.setDestination(idPortenDestinationURL);
        authnRequest.setAssertionConsumerServiceURL(assertionConsumerURL);
        authnRequest.setIssuer(buildIssuer());
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setID(generateSecureRandomId());
        authnRequest.setIssuer(buildIssuer());
        authnRequest.setNameIDPolicy(buildNameIdPolicy());
        authnRequest.setForceAuthn(false);
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext());
        return authnRequest;
    }


    private String buildRedirectURLKey(final AuthnRequest authnRequest, boolean sign) {
        final StringBuilder redirectURLBuilder = new StringBuilder();
        redirectURLBuilder.append("https://eid-vag-opensso.difi.local/opensso/SSORedirect/metaAlias/norge.no/idp");
        redirectURLBuilder.append("?");
        redirectURLBuilder.append(IdPortenRedirectURLFactory.build(
                toXml(authnRequest), null,
                privateKey.getPrivateKey(), false, sign));
        redirectURLBuilder.append("&locale=");
        redirectURLBuilder.append("nb");
        return redirectURLBuilder.toString();
    }

    public String toXml(final AuthnRequest authnRequest) {
        return new XMLFormatter().format(authnRequest);
    }

    public String toSignedXml(final AuthnRequest authnRequest, KeyStore.PrivateKeyEntry privateKey) {
        SamlSigner.sign(authnRequest, privateKey);
        return new XMLFormatter().format(authnRequest);
    }

    protected String convertToEncodeInflatedXml(final AuthnRequest authnRequest) {
        ConvertUtil convertUtil = new ConvertUtil();
        return convertUtil.urlEncode(convertUtil.base64encode(convertUtil
                .zip(toSignedXml(authnRequest, privateKey))));
    }

    private Issuer buildIssuer() {
        Issuer issuer = buildSAMLObject(Issuer.class);
        issuer.setValue(issuerName);
        return issuer;
    }

    private RequestedAuthnContext buildRequestedAuthnContext() {
        RequestedAuthnContext requestedAuthnContext = buildSAMLObject(RequestedAuthnContext.class);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);

        AuthnContextClassRef passwordAuthnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
        passwordAuthnContextClassRef.setAuthnContextClassRef(PASSWORD_AUTHN_CTX);
        requestedAuthnContext.getAuthnContextClassRefs().add(passwordAuthnContextClassRef);

        return requestedAuthnContext;

    }

    private NameIDPolicy buildNameIdPolicy() {
        NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);
        nameIDPolicy.setAllowCreate(true);
        nameIDPolicy.setFormat(NameIDType.TRANSIENT);
        return nameIDPolicy;
    }

    public static String generateSecureRandomId() {
        try {
            return new SecureRandomIdentifierGenerator().generateIdentifier();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
