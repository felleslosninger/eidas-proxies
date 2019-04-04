package no.difi.eidas.cproxy.common;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Base64;

public class FormDataHttpEntity extends HttpEntity<MultiValueMap<String, String>> {

    private FormDataHttpEntity(MultiValueMap<String, String> body, MultiValueMap<String, String> headers) {
        super(body, headers);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final HttpHeaders headers = new HttpHeaders();
        private final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        public Builder() {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }

        public Builder header(String key, String value) {
            headers.set(key, value);
            return this;
        }

        public Builder param(String key, String... value) {
            params.put(key, Arrays.asList(value));
            return this;
        }

        public FormDataHttpEntity build() {
            return new FormDataHttpEntity(params, headers);
        }

    }

    public static String basic(String username, String password) {
        byte[] auth = String.format("%s:%s", username, password).getBytes();
        return "Basic " + new String(Base64.getEncoder().encode(auth));
    }
}
