package no.difi.eidas.cproxy.domain.idp;

import no.difi.eidas.cproxy.saml.SAMLUtil;
import no.difi.opensaml.signature.SamlEncrypter;
import no.difi.opensaml.wrapper.ResponseWrapper;
import org.opensaml.saml2.core.Response;

public class IdPAuthnResponse extends ResponseWrapper{

	public IdPAuthnResponse(String xml, SamlEncrypter encrypter) {
		super(xml, encrypter);
	}

	public IdPAuthnResponse(Response response, SamlEncrypter encrypter) {
		super(response, encrypter);
	}


	public String nameId() {
		return assertions().get(0).getSubject().getNameID().getValue();
	}

	public String sessionIndex() {
		return SAMLUtil.getSessionIndex(assertions().get(0));
	}

	public String culture() {
		return getAttributeValue("Culture");
	}

	public String uid() {
		return getAttributeValue("uid");
	}

	public String epostadresse() {
		return getAttributeValue("epostadresse");
	}

	public String securityLevel() {
		return getAttributeValue("SecurityLevel");
	}

}
