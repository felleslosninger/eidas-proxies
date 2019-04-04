package no.difi.eidas.cproxy.integration.node;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.domain.audit.AuditLog;
import no.difi.eidas.cproxy.domain.node.NodeAttribute;
import no.difi.eidas.cproxy.domain.node.NodeAttributes;
import no.difi.eidas.cproxy.domain.node.NodeAuthnRequest;
import no.difi.eidas.idpproxy.SubjectBasicAttribute;
import no.difi.opensaml.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * Service for handling eIDAS integration.
 */
@Service
public class NodeResponseGenerator {

    private final ProtocolEngineI engine;
    private final AuditLog auditLog;
    private final ConfigProvider configProvider;
    private ConvertUtil convertUtil = new ConvertUtil();

    @Autowired
    public NodeResponseGenerator(
        ProtocolEngineI engine,
        AuditLog auditLog,
        ConfigProvider configProvider
    ) {
        this.engine = engine;
        this.auditLog = auditLog;
        this.configProvider = configProvider;
    }

    /**
     * Generate a SAML authentication response for C-PEPS.
     *
     * @param nodeAuthnRequest EidasNode authn request
     * @param levelOfAssurance Response level of assurance.
     * @param subjectLocality  Locality (IP address) of the subject.
     * @return BASE64 encoded SAML response.
     */
    public String generate(
            IAuthenticationRequest eidasAuthnRequest,
            NodeAuthnRequest nodeAuthnRequest,
            NodeAttributes assembledAttributes,
            String levelOfAssurance,
            String subjectLocality
    ) throws EIDASSAMLEngineException, AttributeValueMarshallingException {
            ImmutableAttributeMap eidasPopulatedAttributes = createAssembledExternalAttributes(assembledAttributes);

            AuthenticationResponse eidasAuthnResponse = createEidasAuthnResponse(
                    eidasPopulatedAttributes,
                    levelOfAssurance,
                    eidasAuthnRequest.getId()
            );

            IResponseMessage responseMessage = engine.generateResponseMessage(
                    eidasAuthnRequest,
                    eidasAuthnResponse,
                    false, // Toggle for SHA-512 hashing of attribute values (as of PEPS 1.3.0)
                    subjectLocality
            );

            byte[] messageBytes = responseMessage.getMessageBytes();
            auditLog.responseToPepsOk(messageBytes);
            return convertUtil.base64encode(messageBytes);
    }

    public AuthenticationResponse createEidasAuthnResponse(
            ImmutableAttributeMap eidasAttributes,
            String levelOfAssurance,
            String inResponseTo
    ) {
        AuthenticationResponse.Builder authenticationResponseBuilder = AuthenticationResponse.builder();
        authenticationResponseBuilder.attributes(eidasAttributes);
        authenticationResponseBuilder.statusCode(EIDASStatusCode.SUCCESS_URI.toString());
        authenticationResponseBuilder.id(SAMLEngineUtils.generateNCName());
        authenticationResponseBuilder.levelOfAssurance(levelOfAssurance);
        authenticationResponseBuilder.inResponseTo(inResponseTo);
        authenticationResponseBuilder.issuer(configProvider.cidpProxyMetadataUrl().toString());
        return authenticationResponseBuilder.build();
    }

    
    public ImmutableAttributeMap createAssembledExternalAttributes(NodeAttributes assembledAttributes) throws AttributeValueMarshallingException {
        ImmutableAttributeMap.Builder externalAssembledAttributes = new ImmutableAttributeMap.Builder();
        Iterator<SubjectBasicAttribute> iterator = assembledAttributes.iterator();
        SubjectBasicAttribute internalSubject;
        NodeAttribute internalAttribute;
        AttributeDefinition<?> externalAttributeDef;
        AttributeValue externalAttributeVal;
        while (iterator.hasNext()) {
            internalSubject = iterator.next();
            internalAttribute = assembledAttributes.get(internalSubject);

            externalAttributeDef = EidasSpec.REGISTRY.getByName(internalSubject.attributeName());
            externalAttributeVal = externalAttributeDef.getAttributeValueMarshaller().unmarshal(internalAttribute.value().get(), false);
            externalAssembledAttributes.put(externalAttributeDef, externalAttributeVal);
        }

        return externalAssembledAttributes.build();
    }

    private String toExternal(NodeAttribute.NotPresentReason notPresentReason) {
        switch (notPresentReason) {
            case NotAvailable:
                return "";
                //return STORKStatusCode.STATUS_NOT_AVAILABLE.toString();
            case Withheld:
                return "";
                //return STORKStatusCode.STATUS_WITHHELD.toString();
            default:
                throw new IllegalArgumentException("Unknown notPresentReason " + notPresentReason);
        }
    }

}
