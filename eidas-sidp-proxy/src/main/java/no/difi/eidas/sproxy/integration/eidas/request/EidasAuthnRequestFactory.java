package no.difi.eidas.sproxy.integration.eidas.request;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.attribute.AttributesConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.authentication.AuthenticationLevel;
import no.difi.eidas.sproxy.domain.authentication.IdPortenAuthnRequest;
import no.difi.eidas.sproxy.domain.country.CountryCode;
import no.difi.eidas.sproxy.integration.fileconfig.attribute.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

import static no.difi.eidas.sproxy.config.SpringConfig.EIDAS_ENGINE;


@Service
public class EidasAuthnRequestFactory {

    private final AuditLog auditLog;
    private final ConfigProvider configProvider;
    private final AttributesConfigProvider attributesConfigProvider;
    private final ProtocolEngineI engine;

    @Autowired
    public EidasAuthnRequestFactory(
            AuditLog auditLog,
            ConfigProvider configProvider,
            AttributesConfigProvider attributesConfigProvider,
            @Qualifier(EIDAS_ENGINE) ProtocolEngineI engine) {
        this.auditLog = auditLog;
        this.configProvider = configProvider;
        this.attributesConfigProvider = attributesConfigProvider;
        this.engine = engine;
    }

    public EidasAuthnRequest create(IdPortenAuthnRequest idPortenAuthnRequest, CountryCode countryCode) {
        try {
            IEidasAuthenticationRequest eidasAuthenticationRequest = authnRequest(idPortenAuthnRequest, countryCode);
            IRequestMessage binaryRequestMessage;
            binaryRequestMessage = engine.generateRequestMessage(eidasAuthenticationRequest, eidasAuthenticationRequest.getIssuer());
            byte[] token = binaryRequestMessage.getMessageBytes();
            auditLog.eidasSamlAuthnRequest(token);
            return new EidasAuthnRequest(EidasStringUtil.encodeToBase64(token));
        } catch (EIDASSAMLEngineException e) {
            throw new RuntimeException("EIDAS SAML AuthnRequest creation failed", e);
        }
    }

    private IEidasAuthenticationRequest authnRequest(IdPortenAuthnRequest idPortenAuthnRequest, CountryCode countryCode) {
        return EidasAuthenticationRequest.builder()
                .id(SAMLEngineUtils.generateNCName())
                .issuer(configProvider.eidasMetadataUrl().toString())
                .destination(configProvider.eidasNodeUrl().toString())
                .providerName("ID-porten")
                .levelOfAssuranceComparison(LevelOfAssuranceComparison.MINIMUM)
                .levelOfAssurance(levelOfAssurance(idPortenAuthnRequest))
                .nameIdFormat(SamlNameIdFormat.TRANSIENT.getNameIdFormat())
                .assertionConsumerServiceURL(configProvider.eidasProxyAuthUrl().toString())
                .serviceProviderCountryCode("NO")
                .citizenCountryCode(countryCode.toString())
                .spType(SpType.PUBLIC.getValue())
                .requestedAttributes(attributes(countryCode))
                .build();
    }

    protected LevelOfAssurance levelOfAssurance(IdPortenAuthnRequest idPortenAuthnRequest) {
        return AuthenticationLevel.convertToEidas(idPortenAuthnRequest.authnContextClassRef());
    }

    protected ImmutableAttributeMap attributes(CountryCode countryCode) {
        return attributes(attributesConfigProvider.forCountry(countryCode));
    }

    protected ImmutableAttributeMap attributes(Collection<Attribute> attributes) {
        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();
        for (Attribute attr : attributes) {
            attributeDefinition(attr).ifPresent(builder::put);
        }
        return builder.build();
    }

    protected Optional<AttributeDefinition<?>> attributeDefinition(Attribute attr) {
        Optional<AttributeDefinition<?>> attributeDefinition = engine.getProtocolProcessor().getMinimumDataSetAttributes().getByFriendlyName(attr.name()).stream().findFirst();
        if (!attributeDefinition.isPresent()) {
            attributeDefinition = engine.getProtocolProcessor().getAdditionalAttributes().getByFriendlyName(attr.name()).stream().findFirst();
        }
        if (attr.required() && ! attributeDefinition.isPresent()) {
            throw new RuntimeException(String.format("Failed to generate attribute definition for %s", attr.name()));
        }
        return attributeDefinition;
    }

}
