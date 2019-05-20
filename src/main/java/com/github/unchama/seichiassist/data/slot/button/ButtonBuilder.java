package com.github.unchama.seichiassist.data.slot.button;

import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.AbstractSlotBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Button} を構成する {@link Slot.Builder} です.
 *
 * @author karayuu
 * @see Button
 */
public class ButtonBuilder extends AbstractSlotBuilder<ButtonBuilder> {
    private List<@NotNull SlotActionHandler> handlers = new ArrayList<>();

    private ButtonBuilder(@NotNull ItemStackBuilder builder) {
        super(builder);
    }

    /**
     * 与えられた {@link ItemStackBuilder} から {@link ButtonBuilder} を生成します.
     *
     * @param builder {@link Slot} の生成に用いられる {@link ItemStackBuilder} ({@code null} は許容されません.)
     * @return {@link ButtonBuilder}
     */
    @NotNull
    public static ButtonBuilder from(@NotNull ItemStackBuilder builder) {
        return new ButtonBuilder(builder);
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
        if (this.position == null) {
            throw new IllegalArgumentException("Slot.Builderにおいては,Slotの設置位置をpositionにて設定する必要があります.");
        }
        Button button = new Button(this.position, this.builder);
        button.addHandlers(handlers);
        return button;
    }
}
