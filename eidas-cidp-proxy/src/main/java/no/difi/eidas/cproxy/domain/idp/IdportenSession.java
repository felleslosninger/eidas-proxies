package no.difi.eidas.cproxy.domain.idp;

import java.io.Serializable;

/**
 * Created by est on 06.07.2015.
 */
public class IdportenSession implements Serializable {
public static final String IDPORTEN_SESSION_ATTRIBUTE = "IDPORTEN_SSO_SESSION";

    private String spNameId;
    private String ssoSessionIndex;

    public IdportenSession( final String spNameId, final String ssoSessionIndex) {
        this.spNameId = spNameId;
        this.ssoSessionIndex = ssoSessionIndex;
    }

    /**
     * Returns the SAML nameID for the SP used during setup of the SSO sesesion.
     *
     * @return nameID
     */
    public String getSPNameId() {
        return spNameId;
    }

    /**
     * Returns the SAML session index identifying the SSO session.
     *
     * @return ssoSessionIndex
     */
    public String getSSOSessionIndex() {
        return ssoSessionIndex;
    }
}





