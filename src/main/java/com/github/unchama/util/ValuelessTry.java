package com.github.unchama.util;

import java.util.function.Supplier;

/**
 * @author kory
 *
 * 失敗時に何も値を記録しないような {@link Try}。
 */
public class ValuelessTry {
    private final Try<Unit> tryInstance;

    private ValuelessTry(Try<Unit> tryInstance) {
        this.tryInstance = tryInstance;
    }

    public ValuelessTry ifOkThen(Supplier<ActionStatus> action) {
        return new ValuelessTry(tryInstance.ifOkThen(Unit.instance, action));
    }

    public ActionStatus overallStatus() {
        return tryInstance.overallStatus();
    }

    public static ValuelessTry begin(Supplier<ActionStatus> action) {
        return new ValuelessTry(Try.begin(Unit.instance, action));
    }
}
