package com.github.unchama.seichiassist.data.slot.button;

import com.github.unchama.seichiassist.data.slot.base.BaseSlot;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandlers;
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
     * この {@link Button} に付与されている {@link SlotActionHandler} の {@link List} です.
     */
    @NotNull
    private List<SlotActionHandler> handlers = new ArrayList<>();

    /**
     * 基本的な {@link Button} を生成します. <br>
     * {@link Inventory} において,
     * {@link SlotActionHandlers#READ_ONLY} が付与されるためReadOnlyなButtonとして働きます.
     *
     * @param itemStack  {@link Inventory} へセットする {@link ItemStack} ({@code null} は許容されません.)
     */
    public Button(@NotNull ItemStack itemStack) {
        super(itemStack);
        addHandler(SlotActionHandlers.READ_ONLY);
    }

    /**
     * {@link Button} に {@link SlotActionHandler} を付与します.
     *
     * @param handler 付与する {@link SlotActionHandler} ({@code null} は許容されません.)
     */
    public void addHandler(@NotNull SlotActionHandler handler) {
        this.handlers.add(handler);
    }

    /**
     * {@link Button} に {@link SlotActionHandler} を付与します.
     *
     * @param handlers 付与する {@link SlotActionHandler} の {@link List} (全ての要素は {@code null} は許容されません.)
     */
    public void addHandlers(@NotNull List<SlotActionHandler> handlers) {
        this.handlers.addAll(handlers);
    }

    @Override
    public void invoke(@NotNull InventoryClickEvent event) {
        this.handlers.forEach(handler -> handler.invoke(event));
    }
}
