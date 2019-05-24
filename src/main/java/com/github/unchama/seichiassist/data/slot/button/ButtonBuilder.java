package com.github.unchama.seichiassist.data.slot.button;

import com.github.unchama.seichiassist.data.itemstack.builder.IconItemStackBuilder;
import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
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
    private final List<@NotNull SlotActionHandler> handlers = new ArrayList<>();

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
     * 与えられた {@link SlotActionHandler} を {@link Button} に付与します.
     *
     * @param handler {@link Button} に付与する {@link SlotActionHandler} ({@code null} は許容されません.)
     * @return {@link ButtonBuilder}
     */
    public ButtonBuilder appendHandler(@NotNull SlotActionHandler handler) {
        this.handlers.add(handler);
        return this;
    }

    /**
     * 与えられた {@link SlotActionHandler} を {@link Button} に付与します.
     *
     * @param handlers {@link Button} に付与する {@link SlotActionHandler} の {@link List} ({@code null} は許容されません.)
     * @return {@link ButtonBuilder}
     */
    public ButtonBuilder appendHandlers(@NotNull List<@NotNull SlotActionHandler> handlers) {
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
