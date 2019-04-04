package no.difi.eidas.sproxy.domain.authentication;

import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;

import java.util.Arrays;
import java.util.List;

/**
 * Converting ID-porten authentication and eIDAS authentication levels.
 */
public enum AuthenticationLevel {

    LEVEL3("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", LevelOfAssurance.SUBSTANTIAL),
    LEVEL4("urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI", LevelOfAssurance.HIGH);

    private String authnContextClassRef;
    private LevelOfAssurance levelOfAssurance;

    AuthenticationLevel(String authnContextClassRef, LevelOfAssurance levelOfAssurance) {
        this.authnContextClassRef = authnContextClassRef;
        this.levelOfAssurance = levelOfAssurance;
    }

    public String idPortenAuthnContextClassRef() {
        return authnContextClassRef;
    }

    public LevelOfAssurance eidasLevelOfAssurance() {
        return levelOfAssurance;
    }

    public static String convertToIdPorten(String levelOfAssurance) {
        return convertToIdPorten(LevelOfAssurance.fromString(levelOfAssurance));
    }

    public static String convertToIdPorten(LevelOfAssurance levelOfAssurance) {
        return list()
                .stream()
                .filter(authenticationLevel -> authenticationLevel.levelOfAssurance.equals(levelOfAssurance))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown levelOfAssurance " + levelOfAssurance))
                .authnContextClassRef;
    }

    public static LevelOfAssurance convertToEidas(String authnContextClassRef) {
        return list()
                .stream()
                .filter(authenticationLevel -> authenticationLevel.authnContextClassRef.equals(authnContextClassRef))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown authnContextClassRef " + authnContextClassRef))
                .levelOfAssurance;
    }

    public static List<AuthenticationLevel> list() {
        return Arrays.asList(values());
    }

}
