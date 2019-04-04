package no.difi.eidas.sproxy.integration.eidas.response;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.xml.opensaml.CorrelatedResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.authentication.AuthenticationLevel;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnRequest;
import no.difi.eidas.sproxy.domain.eventlog.EventLog;
import no.difi.eidas.sproxy.domain.saml.SamlResponseXml;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static no.difi.eidas.sproxy.config.SpringConfig.EIDAS_ENGINE;

@Service
public class EidasResponseReceiver {
    private final AuditLog auditLog;
    private final EventLog eventLog;
    private final ConfigProvider configProvider;
    private final IpProvider ipProvider;
    private final ProtocolEngineI engine;

    @Autowired
    public EidasResponseReceiver(
            AuditLog auditLog,
            EventLog eventLog,
            ConfigProvider configProvider,
            IpProvider ipProvider,
            @Qualifier(EIDAS_ENGINE) ProtocolEngineI engine) {
        this.auditLog = auditLog;
        this.eventLog = eventLog;
        this.configProvider = configProvider;
        this.ipProvider = ipProvider;
        this.engine = engine;
    }

    public EidasResponse receive(EidasSamlResponse eidasSamlResponse, String citizenCountryCode, IdPortenAuthnRequest idPortenAuthnRequest) throws EidasErrorResponse {
        Objects.requireNonNull(eidasSamlResponse, "eidasSamlResponse");
        Objects.requireNonNull(citizenCountryCode, "citizenCountryCode");
        Objects.requireNonNull(idPortenAuthnRequest, "idPortenSamlAuthnRequest");
        try {
            CorrelatedResponse response = (CorrelatedResponse) engine.unmarshallResponse(eidasSamlResponse.samlXml().toString().getBytes());
            auditLog.eidasSamlResponse(response.getResponse());
            IAuthenticationResponse authenticationResponse = engine.validateUnmarshalledResponse(response,
                            ipProvider.ip(),
                            configProvider.instantIssueTimeSkew(),
                            configProvider.instantIssueTimeSkew(),null);
            validateStatus(authenticationResponse);
            validateAuthenticationLevel(authenticationResponse.getLevelOfAssurance(), idPortenAuthnRequest.authnContextClassRef());
            EidasResponse eidasResponse = assertionData(authenticationResponse, response);
            eventLog.eidasResponse(eidasResponse, citizenCountryCode);
            return eidasResponse;
        } catch (EIDASSAMLEngineException e) {
            throw new RuntimeException(e);
        }
    }

    protected void validateStatus(IAuthenticationResponse response) throws EidasErrorResponse {
        if (response.isFailure() || !"urn:oasis:names:tc:SAML:2.0:status:Success".equals(response.getStatusCode())) {
            if (StringUtils.hasText(response.getStatusMessage())) {
                throw new EidasErrorResponse(response.getStatusMessage());
            }
            throw new EidasErrorResponse(String.format("Invalid status code from eIDAS [%s].", response.getStatusCode()));
        }
    }

    protected void validateAuthenticationLevel(String responseLevelOfAssurance, String requestedAuthnContextClassRef) throws EidasErrorResponse {
        LevelOfAssurance levelOfAssurance = LevelOfAssurance.fromString(responseLevelOfAssurance);
        if (levelOfAssurance == null) {
            throw new EidasErrorResponse(String.format("Invalid level of assurance from eIDAS [%s]", responseLevelOfAssurance));
        }
        LevelOfAssurance requestedLevel = AuthenticationLevel.convertToEidas(requestedAuthnContextClassRef);
        if (requestedLevel.numericValue() > levelOfAssurance.numericValue()) {
            throw new EidasErrorResponse(String.format(
                    "Invalid level of assurance from eIDAS, expected minimum [%s] and got [%s]",
                    requestedAuthnContextClassRef,
                    levelOfAssurance.stringValue()));
        }
    }

    protected EidasResponse assertionData(final IAuthenticationResponse response, CorrelatedResponse correlatedResponse) {
        return EidasResponse.builder()
                .samlXml(extractSamlResponse(correlatedResponse))
                .authnContextClassRef(AuthenticationLevel.convertToIdPorten(response.getLevelOfAssurance()))
                .attributes(extractAttributes(response))
                .build();
    }

    protected Map<String, String> extractAttributes(IAuthenticationResponse response) {
        Map<String, String> attributeMap = new HashMap<>();
        for (AttributeDefinition<?> attributeDefinition : response.getAttributes().getDefinitions()) {
            attributeMap.put(
                    attributeDefinition.getFriendlyName(),
                    response.getAttributes().getFirstAttributeValue(attributeDefinition).toString());
        }
        return attributeMap;
    }

    protected SamlResponseXml extractSamlResponse(CorrelatedResponse correlatedResponse) {
        try {
            return new SamlResponseXml(XMLHelper.nodeToString(new ResponseMarshaller().marshall(correlatedResponse.getResponse())));
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        }
    }

}
