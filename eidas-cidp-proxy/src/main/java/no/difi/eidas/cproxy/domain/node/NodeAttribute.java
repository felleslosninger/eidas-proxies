package no.difi.eidas.cproxy.domain.node;

import com.google.common.base.Optional;

public class NodeAttribute {
    public enum NotPresentReason {NotAvailable, Withheld}

    private Optional<NotPresentReason> notPresentReason;
    private Optional<String> value;
    private boolean required;
    private boolean hidden;

    public NodeAttribute(String value) {
        this(value, false);
    }

    public NodeAttribute(String value, boolean hidden) {
        this.value = Optional.of(value);
        this.notPresentReason = Optional.absent();
        this.hidden = hidden;
    }

    public NodeAttribute(NotPresentReason notPresentReason, boolean required) {
        this.value = Optional.absent();
        this.notPresentReason = Optional.of(notPresentReason);
        this.required = required;
    }

    public Optional<String> value() {
        return value;
    }

    public Optional<NotPresentReason> notPresentReason() {
        return notPresentReason;
    }
    
    public boolean isRequired() {
		return required;
	}

    public boolean isPresent(){
    	return value.isPresent();
    }

    public boolean isHidden() {
        return hidden;
    }

}
