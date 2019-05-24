package com.github.unchama.seichiassist.data.slot.button;

import com.github.unchama.seichiassist.data.itemstack.builder.IconItemStackBuilder;
import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
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
    private ItemStackBuilder builder = IconItemStackBuilder.EMPTY_BUILDER;

    @NotNull
    private List<@NotNull SlotActionHandler> handlers = new ArrayList<>();

    private ButtonBuilder() {

    }

    private ButtonBuilder(@NotNull ItemStackBuilder builder) {
        this.builder = builder;
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
     * {@link IconItemStackBuilder#EMPTY_BUILDER} がセットされた {@link ButtonBuilder} を生成します.
     *
     * @return {@link ButtonBuilder}
     */
    @NotNull
    public static ButtonBuilder of() {
        return new ButtonBuilder();
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
        final Button button = new Button(this.builder);
        button.addHandlers(handlers);
        return button;
    }
}
