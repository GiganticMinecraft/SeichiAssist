package com.github.unchama.util.failable;

import com.github.unchama.util.ActionStatus;
import com.github.unchama.util.Unit;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author kory
 *
 * 失敗時に何も値を記録しないような {@link Try} の利用に関するヘルパー。
 */
public class TryWithoutFailValue {
    @SafeVarargs
    public static Try<Unit> sequence(Supplier<ActionStatus>... actions) {
        final List<FailableAction<Unit>> actionsWithFailValues = Arrays
                .stream(actions).map((action) -> new FailableAction<>(Unit.instance, action))
                .collect(Collectors.toList());

        return Try.sequence(actionsWithFailValues);
    }
}
