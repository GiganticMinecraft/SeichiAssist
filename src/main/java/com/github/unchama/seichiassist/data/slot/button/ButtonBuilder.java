package com.github.unchama.seichiassist.data.slot.button;

import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.data.slot.handler.SlotAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Button} を構成する Builder です.
 *
 * @author karayuu
 * @see Button
 */
public class ButtonBuilder {
    @NotNull
    private final ItemStack itemStack;

    @NotNull
    private final List<@NotNull SlotAction> handlers = new ArrayList<>();

    private ButtonBuilder(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * 与えられた {@link ItemStackBuilder} から {@link ButtonBuilder} を生成します.
     *
     * @param itemStack {@link Slot} の生成に用いられる {@link ItemStack} ({@code null} は許容されません.)
     * @return {@link ButtonBuilder}
     */
    @NotNull
    public static ButtonBuilder from(@NotNull ItemStack itemStack) {
        return new ButtonBuilder(itemStack);
    }

    /**
     * 与えられた {@link SlotAction} を {@link Button} に付与します.
     *
     * @param handler {@link Button} に付与する {@link SlotAction} ({@code null} は許容されません.)
     * @return {@link ButtonBuilder}
     */
    @NotNull
    public ButtonBuilder appendAction(@NotNull SlotAction handler) {
        this.handlers.add(handler);
        return this;
    }

    /**
     * 与えられた {@link SlotAction} を {@link Button} に付与します.
     *
     * @param handlers {@link Button} に付与する {@link SlotAction} の {@link List} ({@code null} は許容されません.)
     * @return {@link ButtonBuilder}
     */
    @NotNull
    public ButtonBuilder appendActions(@NotNull List<@NotNull SlotAction> handlers) {
        this.handlers.addAll(handlers);
        return this;
    }

    /**
     * {@link Button} を生成します.
     *
     * @return 生成された {@link Button}
     */
    @NotNull
    public Button build() {
        final Button button = new Button(this.itemStack);
        button.addHandlers(handlers);
        return button;
    }
}
