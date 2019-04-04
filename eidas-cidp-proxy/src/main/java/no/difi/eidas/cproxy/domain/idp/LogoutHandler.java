package no.difi.eidas.cproxy.domain.idp;

import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.saml.CIDPProxyKeyProvider;
import org.apache.commons.lang.StringUtils;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.security.SAML2HTTPRedirectDeflateSignatureRule;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.PrivateKey;

@Service
public class LogoutHandler {
    private static final String URL_PARAMETER_SAML_REQUEST = "SAMLRequest";
    private static final String URL_PARAMETER_SAML_RESPONSE = "SAMLResponse";
    private final ConfigProvider configProvider;
    private final CIDPProxyKeyProvider cidpProxyKeyProvider;

    @Autowired
    public LogoutHandler(
            ConfigProvider configProvider,
            CIDPProxyKeyProvider cidpProxyKeyProvider) {
        this.configProvider = configProvider;
        this.cidpProxyKeyProvider = cidpProxyKeyProvider;
    }

    public void handleLogout(final HttpServletRequest request, final HttpServletResponse response) throws MessageDecodingException, SecurityException {
        if (request.getParameter(URL_PARAMETER_SAML_RESPONSE) != null) {
            handleIncomingLogoutResponse(request, response);
        } else if (request.getParameter(URL_PARAMETER_SAML_REQUEST) != null) {
            handleIncomingLogoutRequest(request, response);
        } else {
            handleInitLogout(request, response);
        }
    }

    private void handleIncomingLogoutResponse(HttpServletRequest request, HttpServletResponse response) throws SecurityException, MessageDecodingException {
        final MessageContext messageContext = new BasicSAMLMessageContext();
        messageContext
                .setInboundMessageTransport(new HttpServletRequestAdapter(
                        request));
        final HTTPRedirectDeflateDecoder samlMessageDecoder = new HTTPRedirectDeflateDecoder();
        samlMessageDecoder.decode(messageContext);
        final LogoutResponse logoutResponse = (LogoutResponse) messageContext
                .getInboundMessage();

        if (!logoutResponse.getStatus().getStatusCode().getValue()
                .equals("urn:oasis:names:tc:SAML:2.0:status:Success")) {
            throw new RuntimeException("Logout not successful.");
        }

        if (!(verifyRedirectSignature(messageContext))) {
            throw new RuntimeException(
                    "Logout not successful,signature validation failed.");
        }
        destroySession(request.getSession());
    }

    private void handleIncomingLogoutRequest(HttpServletRequest request, HttpServletResponse response) throws SecurityException, MessageDecodingException {
        final BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();
        String relayState = null;
        messageContext
                .setInboundMessageTransport(new HttpServletRequestAdapter(
                        request));

        final HTTPRedirectDeflateDecoder samlMessageDecoder = new HTTPRedirectDeflateDecoder();
        samlMessageDecoder.decode(messageContext);

        final LogoutRequest logoutRequest = (LogoutRequest) messageContext
                .getInboundMessage();

        if (!(verifyRedirectSignature(messageContext))) {
            throw new RuntimeException(
                    "Logout not successful,signature validation failed.");
        }

        final IdpLogoutResponse logoutResponse = new IdpLogoutResponse(logoutRequest);
        logoutResponse.buildLogoutResponse(configProvider);
        destroySession(request.getSession());

        if (!(StringUtils.isEmpty(messageContext.getRelayState()))) {
            relayState = messageContext.getRelayState();
        }
        final PrivateKey serviceProviderPrivateKey = cidpProxyKeyProvider.privateKey();
        final StringBuilder redirectURLBuilder = new StringBuilder(512);
        redirectURLBuilder.append(configProvider.getIDPSingleLogoutService());
        redirectURLBuilder.append("?");
        redirectURLBuilder.append(IdPortenRedirectURLFactory.build(
                logoutResponse.getRawXML(), relayState,
                serviceProviderPrivateKey, true, true));
        try {
            response.sendRedirect(redirectURLBuilder.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to redirect user", e);
        }
    }

    private void handleInitLogout(HttpServletRequest request, HttpServletResponse response) {

        IdportenSession activeSession = (IdportenSession) request.getSession().getAttribute(IdportenSession.IDPORTEN_SESSION_ATTRIBUTE);
        if (activeSession == null) {
            throw new RuntimeException("User not logged in");
        }
        final IdpLogoutRequest logoutRequest = new IdpLogoutRequest(activeSession);
        logoutRequest.buildLogoutRequest(configProvider);
        final PrivateKey serviceProviderPrivateKey = cidpProxyKeyProvider.privateKey();
        final StringBuilder redirectURLBuilder = new StringBuilder(512);
        redirectURLBuilder.append(configProvider.getIDPSingleLogoutService());
        redirectURLBuilder.append("?");
        redirectURLBuilder.append(IdPortenRedirectURLFactory.build(
                logoutRequest.getRawXML(), null, serviceProviderPrivateKey,
                false, true));
        try {
            response.sendRedirect(redirectURLBuilder.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to redirect user", e);
        }

    }

    private boolean verifyRedirectSignature(
            final MessageContext context) {
        final MetadataProvider mdProvider = configProvider.getIDPMetadataProvider();
        final MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver(
                mdProvider);
        final KeyInfoCredentialResolver keyInfoCredResolver = Configuration
                .getGlobalSecurityConfiguration()
                .getDefaultKeyInfoCredentialResolver();
        final SignatureTrustEngine sigTrustEngine = new ExplicitKeySignatureTrustEngine(
                mdCredResolver, keyInfoCredResolver);
        final SAML2HTTPRedirectDeflateSignatureRule ruleGET = new SAML2HTTPRedirectDeflateSignatureRule(
                sigTrustEngine);
        try {
            ((SAMLMessageContext) context)
                    .setPeerEntityRole(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
            ruleGET.evaluate(context);
        } catch (SecurityPolicyException ex) {
            return false;
        }
        return true;
    }

    protected void destroySession(HttpSession session) {
        try {
            while(session.getAttributeNames().hasMoreElements()) {
                session.removeAttribute(session.getAttributeNames().nextElement());
            }
        } catch (Exception e) {
            // ignore
        }
        session.invalidate();
    }

}
