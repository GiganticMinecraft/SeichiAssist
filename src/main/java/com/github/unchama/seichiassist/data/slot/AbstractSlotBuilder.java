package com.github.unchama.seichiassist.data.slot;

import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author karayuu
 */
public abstract class AbstractSlotBuilder {
    @Nullable
    protected Integer position = null;

    @NotNull
    protected ItemStackBuilder builder;

    protected AbstractSlotBuilder(@NotNull ItemStackBuilder builder) {
        this.builder = builder;
    }
}
