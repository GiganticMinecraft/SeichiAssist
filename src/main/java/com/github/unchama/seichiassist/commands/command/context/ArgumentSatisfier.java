package com.github.unchama.seichiassist.commands.command.context;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author unicroak
 */
public final class ArgumentSatisfier<T> {

    private final Function<String, T> transform;
    private final Predicate<T> binding;

    public ArgumentSatisfier(Function<String, T> transform, Predicate<T> binding) {
        this.transform = transform;
        this.binding = binding;
    }

    public T transform(String rawArgument) throws ClassCastException, IllegalArgumentException {
        T satisfiedArgument = transform.apply(rawArgument);

        if (satisfiedArgument == null || !binding.test(satisfiedArgument)) {
            throw new IllegalArgumentException();
        }

        return satisfiedArgument;
    }

}
