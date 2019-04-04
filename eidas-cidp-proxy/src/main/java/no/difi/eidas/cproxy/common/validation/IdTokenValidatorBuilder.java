package no.difi.eidas.cproxy.common.validation;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.Nonce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class IdTokenValidatorBuilder {

    private final Logger log = LoggerFactory.getLogger(IdTokenValidatorBuilder.class);
    private RSAKey rsaKey;
    private Date date = Date.from(Instant.now());
    private String issuer;
    private String audience;
    private List<Predicate<JWTClaimsSet>> claimValidators = new LinkedList<>();
    private List<Predicate<SignedJWT>> validators = new LinkedList<>();

    public IdTokenValidatorBuilder rsaKey(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
        return this;
    }

    public IdTokenValidatorBuilder issuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    public IdTokenValidatorBuilder audience(String audience) {
        this.audience = audience;
        return this;
    }

    // not required
    public IdTokenValidatorBuilder nonce(Nonce nonce) {
        claimValidators.add(c -> evaluateResult("nonce doesn't match", c.getClaim("nonce").equals(nonce.getValue())));
        return this;
    }

    public PredicateValidator<SignedJWT> build() {
        claimValidators.add(c -> evaluateResult("Subject is empty", !c.getSubject().isEmpty()));
        claimValidators.add(c -> evaluateResult("Token expired", c.getExpirationTime().after(date)));
        claimValidators.add(c -> evaluateResult("Issuer doesn't match", c.getIssuer().equals(issuer)));
        claimValidators.add(c -> evaluateResult("audience doesn't match", c.getAudience().contains(audience)));
        validators.add(idToken -> {
            try {
                return evaluateResult("Token signature invalid", idToken.verify(new RSASSAVerifier(rsaKey)));
            } catch (JOSEException e) {
                throw new RuntimeException(e);
            }
        });
        validators.add(idToken -> {
            try {
                return evaluateResult("Claims invalid", new PredicateValidator<>(claimValidators).test(idToken.getJWTClaimsSet()));
            } catch (ParseException e) {
                return evaluateResult(e.getMessage(), false);
            }
        });
        return new PredicateValidator<>(validators);
    }

    protected boolean evaluateResult(String message, boolean result) {
        if (!result) {
            log.warn(message);
        }
        return result;
    }

}