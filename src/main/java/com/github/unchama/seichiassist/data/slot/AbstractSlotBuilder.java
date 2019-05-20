package com.github.unchama.seichiassist.data.slot;

import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author karayuu
 */
public abstract class AbstractSlotBuilder<T extends AbstractSlotBuilder<T>> implements Slot.Builder {
    @Nullable
    protected Integer position = null;

    @NotNull
    protected ItemStackBuilder builder;

    protected AbstractSlotBuilder(@NotNull ItemStackBuilder builder) {
        this.builder = builder;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public T at(int position) {
        this.position = position;
        return (T) this;
    }
}
