package com.github.unchama.util.failable;

import com.github.unchama.util.ActionStatus;

import java.util.function.Supplier;

public final class FailableAction<F> {
    final F failValue;
    final Supplier<ActionStatus> action;

    public FailableAction(F failValue, Supplier<ActionStatus> action) {
        this.failValue = failValue;
        this.action = action;
    }
}
