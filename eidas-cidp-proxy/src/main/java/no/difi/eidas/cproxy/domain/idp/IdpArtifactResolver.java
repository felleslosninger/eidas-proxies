package no.difi.eidas.cproxy.domain.idp;

import com.google.common.base.Preconditions;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.saml.CIDPProxyKeyProvider;
import no.difi.eidas.cproxy.saml.SAMLUtil;
import no.difi.opensaml.signature.SamlEncrypter;
import no.difi.opensaml.signature.SamlSigner;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Artifact;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.ws.soap.client.BasicSOAPMessageContext;
import org.opensaml.ws.soap.client.http.HttpSOAPClient;
import org.opensaml.ws.soap.common.SOAPException;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.ws.soap.soap11.impl.FaultImpl;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@DependsOn("engine")
@Service
public class IdpArtifactResolver {

    private final ConfigProvider configProvider;
    private final CIDPProxyKeyProvider cidpProxyKeyProvider;
    private final AuditLog auditLog;
    private final HttpSOAPClient soapClient;
    private final SamlEncrypter encrypter;
    private final Logger logger = LoggerFactory.getLogger(IdpArtifactResolver.class);

    @Autowired
    public IdpArtifactResolver(
            ConfigProvider configProvider,
            CIDPProxyKeyProvider cidpProxyKeyProvider,
            AuditLog auditLog,
            HttpSOAPClient soapClient
    ) {
        this.configProvider = configProvider;
        this.cidpProxyKeyProvider = cidpProxyKeyProvider;
        this.auditLog = auditLog;
        this.soapClient = soapClient;
        // SamlEncrypter requires OpenSAML's Configuration to be initialized, which happens upon creation of
        // the STORKSamlEngine bean. Therefore a @DependsOn("engine") on this class.
        encrypter = new SamlEncrypter(cidpProxyKeyProvider.publicKey(), cidpProxyKeyProvider.privateKey());

    }

    public IdPAuthnResponse resolve(String artifact) {
        Preconditions.checkNotNull(artifact);
        ArtifactResolve artifactResolve = buildArtifactResolve(artifact);

        SamlSigner.sign(artifactResolve,
                cidpProxyKeyProvider.privateKeyEntry());
        return resolveArtifact(artifactResolve);
    }

    private ArtifactResolve buildArtifactResolve(String artifact) {
        final ArtifactResolve artifactResolve = SAMLUtil.createXmlObject(ArtifactResolve.class);
        final Artifact assertionArtifact = SAMLUtil.createXmlObject(Artifact.class);
        assertionArtifact.setArtifact(artifact);
        artifactResolve.setArtifact(assertionArtifact);
        artifactResolve.setID(SAMLUtil.generateSecureRandomId());
        artifactResolve.setIssueInstant(new DateTime());
        artifactResolve.setIssuer(SAMLUtil.buildIssuer(configProvider.spEntityID()));
        return artifactResolve;
    }


    private IdPAuthnResponse resolveArtifact(ArtifactResolve artifactResolve) {
        Envelope soapResponse = soapResponse(artifactResolve);
        Preconditions.checkNotNull(soapResponse);
        XMLObject xmlObject = soapResponse.getBody().getUnknownXMLObjects().get(0);
        if (xmlObject instanceof FaultImpl) {
            FaultImpl fault = (FaultImpl) xmlObject;
            logger.error("Feil fra Saml: Code: " + fault.getCode()
                    + " message: " + (fault.getMessage() == null ? " - " : fault.getMessage().getValue())
                    + " actor: " + (fault.getActor() == null ? "-" : fault.getActor().toString())
                    + " details: " + (fault.getDetail() == null ? "- " : fault.getDetail().toString()));
            throw new RuntimeException("Feil i pålogging");
        } else if (xmlObject instanceof ArtifactResponse) {
            ArtifactResponse artifactResponse = (ArtifactResponse) xmlObject;
            Preconditions.checkNotNull(artifactResponse);
            return auditLog.idpResponse(
                    new IdPAuthnResponse((Response) artifactResponse.getMessage(), encrypter)
            );
        } else {
            logger.error("Unknown type of object: " + xmlObject.toString());
            throw new RuntimeException("Feil i returnert artifact");
        }
    }


    private Envelope soapResponse(ArtifactResolve artifactResolve) {
        Envelope soapEnvelope = getSOAP11Envelope(artifactResolve);
        return doSoapCall(soapEnvelope);
    }

    private Envelope getSOAP11Envelope(ArtifactResolve artifactResolve) {
        final XMLObjectBuilderFactory bf = Configuration.getBuilderFactory();
        final Envelope envelope = (Envelope) bf.getBuilder(
                Envelope.DEFAULT_ELEMENT_NAME).buildObject(
                Envelope.DEFAULT_ELEMENT_NAME);
        final Body body = (Body) bf.getBuilder(Body.DEFAULT_ELEMENT_NAME)
                .buildObject(Body.DEFAULT_ELEMENT_NAME);
        body.getUnknownXMLObjects().add(artifactResolve);
        envelope.setBody(body);
        return envelope;
    }

    private Envelope doSoapCall(final Envelope envelope) {
        try {
            final BasicSOAPMessageContext soapContext = new BasicSOAPMessageContext();
            soapContext.setOutboundMessage(envelope);
            soapClient.send(
                    configProvider.getArtifactResolutionService(),
                    soapContext
            );
            return (Envelope) soapContext.getInboundMessage();
        } catch (SOAPException | SecurityException e) {
            throw new RuntimeException(
                    "Failed sending soap call to ID-porten from c-idp-proxy. Url:" + configProvider.getArtifactResolutionService(),
                    e);
        }

    }
}
