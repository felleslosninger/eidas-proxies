package no.difi.eidas.cproxy.common.validation;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class PredicateValidator<T> implements Predicate<T> {

    private final List<Predicate<T>> validators = new LinkedList<>();

    public PredicateValidator(List<Predicate<T>> validators) {
        this.validators.addAll(validators);
    }

    @Override
    public boolean test(final T t) {
        return validators.parallelStream().allMatch(predicate -> predicate.test(t));
    }

}
