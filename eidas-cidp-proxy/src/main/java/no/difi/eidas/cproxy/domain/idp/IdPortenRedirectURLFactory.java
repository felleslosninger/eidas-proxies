package no.difi.eidas.cproxy.domain.idp;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.*;
import java.util.zip.Deflater;

public class IdPortenRedirectURLFactory {
    private static final String URL_PARAMETER_SAML_REQUEST = "SAMLRequest";
    private static final String URL_PARAMETER_SAML_RESPONSE = "SAMLResponse";
    private static final String URL_PARAMETER_SAML_SIGNATURE_ALGORITHM = "SigAlg";
    private static final String URL_PARAMETER_SAML_SIGNATURE = "Signature";
    private static final String SIGNATURE_ALGORITHM_URI = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    private static final String SIGNATURE_ALGORITHM_NAME = "SHA1withRSA";
    private static final String URL_CHARACTER_ENCODING = "UTF-8";

    /**
     * Builds parameters for redirect URL with SAML authentication request
     * according to the SAML HTTP redirect binding. NB: the SAML request is sent
     * as a URL parameter in the SAML HTTP redirect binding. NB: this step also
     * signs and encrypts the SAML request if signing and encryption is enabled.
     *
     * @param SAMLRequest
     * @param serviceProviderSignerKey
     * @return
     */
    public static String build(
            final String SAMLRequest, final String relayState,
            final PrivateKey serviceProviderSignerKey,
            final boolean isResponse, final boolean sign) {
        // Appends the appropriate HTTP parameter name for either SAML response
        // or SAML request.
        final StringBuilder samlQueryStringBuilder = new StringBuilder(512);
        if (isResponse) {
            samlQueryStringBuilder.append(URL_PARAMETER_SAML_RESPONSE);
        } else {
            samlQueryStringBuilder.append(URL_PARAMETER_SAML_REQUEST);
        }
        samlQueryStringBuilder.append("=");

        // Appends the SAML message encoded for HTTP redirect (xml string
        // encoded using base64 and gzip).
        samlQueryStringBuilder
                .append(getEncodedXMLForHTTPRedirect(SAMLRequest));
        if (!(StringUtils.isEmpty(relayState))) {
            samlQueryStringBuilder.append("&RelayState=");
            samlQueryStringBuilder.append(relayState);
        }
        // Appends SAML signature if service provider is configured for signing.
        if ((serviceProviderSignerKey != null) && sign) {
            appendSignatureToHTTPRedirectURLParameters(samlQueryStringBuilder,
                    serviceProviderSignerKey);
        }
        // Returns the complete SAML query string (more URL parameters can be
        // appended)
        return samlQueryStringBuilder.toString();
    }

    /**
     * Encodes a SAML XML message for use with SAML services that uses
     * HTTP-Artifact as transport binding.
     * <p/>
     * Messages are zipped, base64-encoded and finally made URL-safe
     *
     * @param rawXML String representation of the SAML XML message
     * @return the encoded message
     */
    private static String getEncodedXMLForHTTPRedirect(final String rawXML) {
        final int n = rawXML.length();
        byte[] input = null;
        try {
            input = rawXML.getBytes(URL_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
        final byte[] output = new byte[n];

        final Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION,
                true);
        deflater.setInput(input);
        deflater.finish();
        final int len = deflater.deflate(output);
        deflater.end();

        final byte[] exact = new byte[len];

        System.arraycopy(output, 0, exact, 0, len);

        final String base64Str = Base64.encodeBase64String(exact);

        String encoded;
        try {
            encoded = URLEncoder.encode(base64Str, URL_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to base64 encode request", e);
        }
        return encoded;
    }

    /**
     * Signs the given SAML query string and appends singature and signature
     * algorithm to the URL querystring according to the SAML HTTP redirect
     * binding.
     *
     * @param samlQueryStringBuilder
     * @param serviceProviderSignerKey
     */
    private static void appendSignatureToHTTPRedirectURLParameters(
            StringBuilder samlQueryStringBuilder,
            PrivateKey serviceProviderSignerKey) {
        samlQueryStringBuilder.append("&");
        samlQueryStringBuilder.append(URL_PARAMETER_SAML_SIGNATURE_ALGORITHM);
        samlQueryStringBuilder.append("=");
        samlQueryStringBuilder
                .append(getEncodedSignatureAlgorithmURIForHTTPRedirect(SIGNATURE_ALGORITHM_URI));
        byte[] bytesToSign;
        try {
            bytesToSign = samlQueryStringBuilder.toString().getBytes(
                    URL_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        // Signs the SAML query string
        Signature signature = null;
        try {
            signature = Signature.getInstance(SIGNATURE_ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            signature.initSign(serviceProviderSignerKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        try {
            signature.update(bytesToSign);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
        byte[] signatureBytes = null;
        try {
            signatureBytes = signature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }

        // Appends the signature to the SAML query string
        samlQueryStringBuilder.append("&");
        samlQueryStringBuilder.append(URL_PARAMETER_SAML_SIGNATURE);
        samlQueryStringBuilder.append("=");
        samlQueryStringBuilder
                .append(getEncodedSignatureForHTTPRedirect(signatureBytes));
    }

    /**
     * Encodes a signature algorithm URI for use with SAML services that uses
     * HTTP-Artifact as transport binding.
     *
     * @param signatureAlgorithmURI
     * @return
     */
    private static String getEncodedSignatureAlgorithmURIForHTTPRedirect(
            final String signatureAlgorithmURI) {
        String urlEncodedSignatureAlgorithmURI;
        try {
            urlEncodedSignatureAlgorithmURI = URLEncoder.encode(
                    signatureAlgorithmURI, URL_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return urlEncodedSignatureAlgorithmURI;
    }

    /**
     * Encodes signature for use with SAML services that uses HTTP-Artifact as
     * transport binding.
     *
     * @param signatureBytes
     * @return
     */
    private static String getEncodedSignatureForHTTPRedirect(
            final byte[] signatureBytes) {
        final String base64EncodedSignature = Base64
                .encodeBase64String(signatureBytes);
        String urlAndBase64EncodedSignature;
        try {
            urlAndBase64EncodedSignature = URLEncoder.encode(
                    base64EncodedSignature, URL_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return urlAndBase64EncodedSignature;
    }
}
