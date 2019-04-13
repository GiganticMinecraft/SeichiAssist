package com.github.unchama.seichiassist.data.slot.button;

import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.data.slot.base.BaseSlot;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandlers;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * 基本的な {@link Button} を表すクラス.
 *
 * @author karayuu
 */
public class Button extends BaseSlot {
    /**
     * この {@link Button} に付与されている {@link SlotActionHandler} の {@link List} です.
     */
    @Nonnull
    private
    List<SlotActionHandler> handlers = new ArrayList<>();

    /**
     * 基本的な {@link Button} を生成します. <br>
     * {@link Inventory} において,
     * {@link SlotActionHandlers#READ_ONLY} が付与されるためReadOnlyなButtonとして働きます.
     *
     * @param position {@link Inventory} への設置位置
     * @param builder  {@link Inventory} へセットする {@link ItemStack} を構成する {@link ItemStackBuilder}
     *                 ({@code null} は許容されません.)
     * @see Slot#getPosition()
     */
    public Button(int position, @Nonnull ItemStackBuilder builder) {
        super(position, requireNonNull(builder));
        addHandler(SlotActionHandlers.READ_ONLY);
    }

    /**
     * {@link Button} に {@link SlotActionHandler} を付与します.
     *
     * @param handler 付与する {@link SlotActionHandler} ({@code null} は許容されません.)
     */
    public void addHandler(@Nonnull SlotActionHandler handler) {
        this.handlers.add(handler);
    }

    /**
     * {@link Button} に {@link SlotActionHandler} を付与します.
     *
     * @param handlers 付与する {@link SlotActionHandler} の {@link List} (全ての要素は {@code null} であってはなりません.)
     */
    public void addHandler(@Nonnull List<SlotActionHandler> handlers) {
        this.handlers.addAll(requireNonNull(handlers));
    }
}
