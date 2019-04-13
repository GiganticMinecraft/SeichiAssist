package com.github.unchama.seichiassist.data.slot;

import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;

import javax.annotation.Nonnull;

/**
 * @author karayuu
 */
public abstract class AbstractSlotBuilder<T extends AbstractSlotBuilder<T>> implements Slot.Builder {
    protected int position = -1;
    @Nonnull
    protected ItemStackBuilder builder;

    protected AbstractSlotBuilder(@Nonnull ItemStackBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public T at(int position) {
        this.position = position;
        return (T) this;
    }
}
