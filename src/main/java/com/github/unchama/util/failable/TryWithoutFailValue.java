package com.github.unchama.util.failable;

import com.github.unchama.util.ActionStatus;
import com.github.unchama.util.Unit;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collection;
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
        final Collection<Pair<Unit, Supplier<ActionStatus>>> actionsWithFailValues = Arrays
                .stream(actions).map((action) -> Pair.of(Unit.instance, action))
                .collect(Collectors.toList());

        return Try.sequence(actionsWithFailValues);
    }
}
