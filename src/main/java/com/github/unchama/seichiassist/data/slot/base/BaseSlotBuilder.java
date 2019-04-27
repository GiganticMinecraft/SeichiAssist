package com.github.unchama.seichiassist.data.slot.base;

import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.AbstractSlotBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * {@link BaseSlot} を構成する {@link Slot.Builder} です.
 *
 * @author karayuu
 * @see BaseSlot
 */
public class BaseSlotBuilder extends AbstractSlotBuilder<BaseSlotBuilder> {
    private BaseSlotBuilder(@Nonnull ItemStackBuilder builder) {
        super(requireNonNull(builder));
    }

    /**
     * 与えられた {@link ItemStack} から {@link BaseSlotBuilder} を生成します.
     *
     * @param builder {@link Slot} の生成に用いられる {@link ItemStackBuilder}
     * @return {@link BaseSlotBuilder}
     */
    @Nonnull
    public static BaseSlotBuilder from(@Nonnull ItemStackBuilder builder) {
        return new BaseSlotBuilder(requireNonNull(builder));
    }

    @Nonnull
    @Override
    public BaseSlotBuilder at(int position) {
        this.position = position;
        return this;
    }

    /**
     * {@link BaseSlot} を生成します.
     *
     * @return 生成された {@link BaseSlot}
     */
    @Nonnull
    public BaseSlot build() {
        if (this.position == -1) {
            throw new IllegalArgumentException("Slot.Builderにおいては,Slotの設置位置をpositionにて設定する必要があります.");
        }
        return new BaseSlot(position, builder);
    }
}
