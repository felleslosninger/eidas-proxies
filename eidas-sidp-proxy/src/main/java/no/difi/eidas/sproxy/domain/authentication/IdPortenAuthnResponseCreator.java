package no.difi.eidas.sproxy.domain.authentication;

import no.difi.eidas.idpproxy.integrasjon.dsf.DsfGateway;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import no.difi.eidas.sproxy.config.ConfigProvider;
import no.difi.eidas.sproxy.domain.audit.AuditLog;
import no.difi.eidas.sproxy.domain.saml.IdPortenKeyProvider;
import no.difi.eidas.sproxy.domain.saml.IdportenSAMLConstants;
import no.difi.eidas.sproxy.integration.eidas.response.EidasResponse;
import no.difi.eidas.sproxy.integration.mf.MFService;
import no.difi.eidas.sproxy.web.AuthState;
import no.difi.opensaml.builder.AssertionBuilder;
import no.difi.opensaml.builder.AttributeStatementBuilder;
import no.difi.opensaml.builder.ResponseBuilder;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.StatusCode;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IdPortenAuthnResponseCreator {

    private static final DateTimeFormatter eidasFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dsfFormat = DateTimeFormat.forPattern("ddMMyy");

    protected static final String SAML_ATTRIBUTE_PERSONNUMBER = "personnumber";
    protected static final String SAML_ATTRIBUTE_DSF_STATUS = "identity-match-dsf";
    protected static final String SAML_ATTRIBUTE_IDENTITY_MATCH = "identity-match";

    protected static final String IDENTITY_MATCH_STATUS_UNAMBIGUOUS = "UNAMBIGUOUS";
    protected static final String IDENTITY_MATCH_STATUS_BEST_EFFORT = "BEST_EFFORT";
    protected static final String IDENTITY_MATCH_STATUS_NOT_FOUND = "NOT_FOUND";
    protected static final String IDENTITY_MATCH_STATUS_ERROR = "ERROR";

    private final ConfigProvider configProvider;
    private final InstantIssuer instantIssuer;
    private final DsfGateway dsfGateway;
    private final IdPortenKeyProvider idPortenKeyProvider;
    private final AuditLog auditLog;
    private final ObjectFactory<AuthState> authStateObjectFactory;
    private final MFService mfService;

    @Autowired
    public IdPortenAuthnResponseCreator(ConfigProvider configProvider, InstantIssuer instantIssuer, DsfGateway dsfGateway, IdPortenKeyProvider idPortenKeyProvider, AuditLog auditLog, ObjectFactory<AuthState> authStateObjectFactory, MFService mfService) {
        this.configProvider = configProvider;
        this.instantIssuer = instantIssuer;
        this.dsfGateway = dsfGateway;
        this.idPortenKeyProvider = idPortenKeyProvider;
        this.auditLog = auditLog;
        this.authStateObjectFactory = authStateObjectFactory;
        this.mfService = mfService;
    }

    public IdPortenAuthnResponse create(EidasResponse eidasResponse, IdPortenAuthnRequest authnRequest) {
        IdPortenAuthnResponse response = new IdPortenAuthnResponse(
                buildSAMLResponse(eidasResponse, authnRequest)
        );
        auditLog.idPortenSamlAuthnResponse(response);
        return response;
    }

    public IdPortenAuthnResponse createUserCancelledResponse(IdPortenAuthnRequest authnRequest) {
        return new IdPortenAuthnResponse(buildUserCancelledSAMLResponse(authnRequest));
    }

    private String buildSAMLResponse(EidasResponse eidasResponse, IdPortenAuthnRequest authnRequest) {
        AssertionBuilder assertionBuilder = new AssertionBuilder();
        assertionBuilder
                .withDefaults()
                .issueInstant(instantIssuer.now())
                .issuer(configProvider.idpEntityName())
                .attributeStatement(buildAttributeStatement(eidasResponse))
                .authnStatement(instantIssuer.now(), eidasResponse.authnContextClassRef())
                .addAudienceRestriction(authnRequest.getIssuer());
        return new ResponseBuilder()
                .withDefaults()
                .issueInstant(instantIssuer.now())
                .inResponseTo(authnRequest.getID())
                .status(StatusCode.SUCCESS_URI)
                .addSignedEncryptedAssertion(assertionBuilder, idPortenKeyProvider.publicKey(), idPortenKeyProvider.privateKey())
                .buildXml();
    }

    private String buildUserCancelledSAMLResponse(IdPortenAuthnRequest authnRequest) {
        return new ResponseBuilder()
                .withDefaults()
                .issueInstant(instantIssuer.now())
                .inResponseTo(authnRequest.getID())
                .status(IdportenSAMLConstants.SAML_RESPONSE_USER_CANCELLED_STATUS_CODE)
                .buildXml();
    }

    private AttributeStatement buildAttributeStatement(EidasResponse eidasResponse) {
        AttributeStatementBuilder builder = new AttributeStatementBuilder();
        addEidasAttributes(eidasResponse, builder);
        addDsfAttributes(eidasResponse, builder);
        addEidasSaml(eidasResponse, builder);
        return builder.build();
    }

    protected void addDsfAttributes(EidasResponse eidasResponse, AttributeStatementBuilder builder) {
        List<String> identityMatch = authStateObjectFactory.getObject().getIdentityMatch();
        if (identityMatch.contains(IDENTITY_MATCH_STATUS_UNAMBIGUOUS)
                || identityMatch.contains(IDENTITY_MATCH_STATUS_BEST_EFFORT)) {
            PersonLookupResult lookup = mfService.lookup(eidasResponse);
            if (lookup.person().isPresent()) {
                builder.attribute(SAML_ATTRIBUTE_PERSONNUMBER, lookup.person().get().fødselsnummer());
                builder.attribute(SAML_ATTRIBUTE_DSF_STATUS, PersonLookupResult.Status.OK.value());
                builder.attribute(SAML_ATTRIBUTE_IDENTITY_MATCH, IDENTITY_MATCH_STATUS_UNAMBIGUOUS);
                return;
            }
        }

        if (identityMatch.contains(IDENTITY_MATCH_STATUS_BEST_EFFORT)) {
            PersonLookupResult result = dsfLookup(eidasResponse.name());
            if (result.person().isPresent()) {
                builder.attribute(SAML_ATTRIBUTE_PERSONNUMBER, result.person().get().fødselsnummer());
            }
            builder.attribute(SAML_ATTRIBUTE_DSF_STATUS, result.status().value());
            builder.attribute(SAML_ATTRIBUTE_IDENTITY_MATCH, convertDsfStatusToIdentityMatch(result));
            return;
        }

        builder.attribute(SAML_ATTRIBUTE_DSF_STATUS, PersonLookupResult.Status.MULTIPLEFOUND.value());
        builder.attribute(SAML_ATTRIBUTE_IDENTITY_MATCH, IDENTITY_MATCH_STATUS_NOT_FOUND);
    }

    private PersonLookupResult dsfLookup(Optional<EidasResponse.Name> eidasName) {
        if (! eidasName.isPresent()) {
            return new PersonLookupResult(PersonLookupResult.Status.NODSFSEARCH, com.google.common.base.Optional.absent());
        }
        return dsfGateway.byNameAndBirth(
                eidasName.get().firstName(),
                eidasName.get().lastName(),
                birth(eidasName.get().birth()));
    }

    protected String convertDsfStatusToIdentityMatch(PersonLookupResult result) {
        switch (result.status()) {
            case OK:
                return result.person().isPresent() ? IDENTITY_MATCH_STATUS_BEST_EFFORT : IDENTITY_MATCH_STATUS_NOT_FOUND;
            case MULTIPLEFOUND:
                return IDENTITY_MATCH_STATUS_NOT_FOUND;
            default:
                return IDENTITY_MATCH_STATUS_ERROR;
        }
    }

    private void addEidasAttributes(EidasResponse eidasResponse, AttributeStatementBuilder builder) {
        for (Map.Entry<String, String> entry : eidasResponse.attributes().entrySet()) {
            builder.attribute(eidasPrefix(entry.getKey()), entry.getValue());
        }
    }

    protected String eidasPrefix(String attributeName) {
        return String.format("eidas-%s", attributeName);
    }

    protected void addEidasSaml(EidasResponse eidasResponse, AttributeStatementBuilder builder) {
            builder.attribute(eidasPrefix("saml-response"), Base64.getEncoder().encodeToString(eidasResponse.eidasReponseSaml().toString().getBytes()));
    }

    private String birth(String eidasBirth) {
        LocalDateTime dateTime = LocalDateTime.parse(eidasBirth, eidasFormat);
        return dateTime.toString(dsfFormat);
    }

}
