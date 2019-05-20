package com.github.unchama.seichiassist.data.slot.base;

import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.AbstractSlotBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * {@link BaseSlot} を構成する {@link Slot.Builder} です.
 *
 * @author karayuu
 * @see BaseSlot
 */
public class BaseSlotBuilder extends AbstractSlotBuilder<BaseSlotBuilder> {
    private BaseSlotBuilder(@NotNull ItemStackBuilder builder) {
        super(builder);
    }

    /**
     * 与えられた {@link ItemStack} から {@link BaseSlotBuilder} を生成します.
     *
     * @param builder {@link Slot} の生成に用いられる {@link ItemStackBuilder}
     * @return {@link BaseSlotBuilder}
     */
    @NotNull
    public static BaseSlotBuilder from(@NotNull ItemStackBuilder builder) {
        return new BaseSlotBuilder(builder);
    }

    @NotNull
    public BaseSlotBuilder at(int position) {
        this.position = position;
        return this;
    }

    /**
     * {@link BaseSlot} を生成します.
     *
     * @return 生成された {@link BaseSlot}
     */
    @NotNull
    public BaseSlot build() {
        if (this.position == -1) {
            throw new IllegalArgumentException("Slot.Builderにおいては,Slotの設置位置をpositionにて設定する必要があります.");
        }
        return new BaseSlot(position, builder);
    }
}
