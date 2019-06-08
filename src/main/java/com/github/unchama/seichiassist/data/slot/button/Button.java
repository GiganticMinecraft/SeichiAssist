package com.github.unchama.seichiassist.data.slot.button;

import com.github.unchama.seichiassist.data.slot.base.BaseSlot;
import com.github.unchama.seichiassist.data.slot.handler.SlotAction;
import com.github.unchama.seichiassist.data.slot.handler.SlotActions;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 基本的な {@link Button} を表すクラス.
 *
 * @author karayuu
 */
public class Button extends BaseSlot {
    /**
     * この {@link Button} に付与されている {@link SlotAction} の {@link List} です.
     */
    @NotNull
    private List<SlotAction> handlers = new ArrayList<>();

    /**
     * 基本的な {@link Button} を生成します. <br>
     * {@link Inventory} において,
     * {@link SlotActions#READ_ONLY} が付与されるためReadOnlyなButtonとして働きます.
     *
     * @param itemStack  {@link Inventory} へセットする {@link ItemStack} ({@code null} は許容されません.)
     */
    public Button(@NotNull ItemStack itemStack) {
        super(itemStack);
        addHandler(SlotActions.READ_ONLY);
    }

    /**
     * {@link Button} に {@link SlotAction} を付与します.
     *
     * @param handler 付与する {@link SlotAction} ({@code null} は許容されません.)
     */
    public void addHandler(@NotNull SlotAction handler) {
        this.handlers.add(handler);
    }

    /**
     * {@link Button} に {@link SlotAction} を付与します.
     *
     * @param handlers 付与する {@link SlotAction} の {@link List} (全ての要素は {@code null} は許容されません.)
     */
    public void addHandlers(@NotNull List<SlotAction> handlers) {
        this.handlers.addAll(handlers);
    }

    @Override
    public void invoke(@NotNull InventoryClickEvent event) {
        this.handlers.forEach(handler -> handler.invoke(event));
    }
}
