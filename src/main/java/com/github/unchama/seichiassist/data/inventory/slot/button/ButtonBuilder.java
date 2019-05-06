package com.github.unchama.seichiassist.data.inventory.slot.button;

import com.github.unchama.seichiassist.data.inventory.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.inventory.slot.AbstractSlotBuilder;
import com.github.unchama.seichiassist.data.inventory.slot.Slot;
import com.github.unchama.seichiassist.data.inventory.slot.handler.SlotActionHandler;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * {@link Button} を構成する {@link Slot.Builder} です.
 *
 * @author karayuu
 * @see Button
 */
public class ButtonBuilder extends AbstractSlotBuilder<ButtonBuilder> {
    private List<SlotActionHandler> handlers = new ArrayList<>();

    private ButtonBuilder(@Nonnull ItemStackBuilder builder) {
        super(builder);
    }

    /**
     * 与えられた {@link ItemStack} から {@link ButtonBuilder} を生成します.
     *
     * @param builder {@link Slot} の生成に用いられる {@link ItemStackBuilder}
     * @return {@link ButtonBuilder}
     */
    public static ButtonBuilder from(@Nonnull ItemStackBuilder builder) {
        return new ButtonBuilder(requireNonNull(builder));
    }

    /**
     * 与えられた {@link SlotActionHandler} を {@link Button} に付与します.
     *
     * @param handler {@link Button} に付与する {@link SlotActionHandler}
     * @return {@link ButtonBuilder}
     */
    public ButtonBuilder handler(@Nonnull SlotActionHandler handler) {
        this.handlers.add(requireNonNull(handler));
        return this;
    }

    public ButtonBuilder handler(@Nonnull List<SlotActionHandler> handlers) {
        requireNonNull(handlers);
        this.handlers.addAll(handlers);
        return this;
    }

    /**
     * {@link Button} を生成します.
     *
     * @return 生成された {@link Button}
     */
    @Nonnull
    public Button build() {
        if (this.position == -1) {
            throw new IllegalArgumentException("Slot.Builderにおいては,Slotの設置位置をpositionにて設定する必要があります.");
        }
        Button button = new Button(this.position, this.builder);
        button.addHandler(handlers);
        return button;
    }
}
