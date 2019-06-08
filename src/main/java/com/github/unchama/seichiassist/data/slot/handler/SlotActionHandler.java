package com.github.unchama.seichiassist.data.slot.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * SLotのActionを管理するクラスです.
 *
 * @author karayuu
 */
public class SlotActionHandler {
    /**
     * InventoryClickEventを受け取って,動作を行わせるかを決定する {@link ClickEventFilter}
     */
    @NotNull
    private ClickEventFilter clickEventFilter;

    /**
     * InventoryClickEventを与えて,何かしらの動作を行わせるFunction
     */
    @NotNull
    private Consumer<InventoryClickEvent> action;

    /**
     * Slotの動作を決定する {@link SlotActionHandler} を生成します.
     *
     * @param clickEventFilter InventoryClickEventを受け取って,動作を行わせるかを決定する {@link ClickEventFilter}
     * @param action  InventoryClickEventを与えて,何かしらの動作を行わせるFunction <br>
     *                {@link #clickEventFilter} がtrueを返した際に動作します. <br>
     *                なお {@link #action} 呼び出し時点で, {@link InventoryClickEvent#getWhoClicked()} は {@link Player} であることが保証されています.
     */
    public SlotActionHandler(@NotNull ClickEventFilter clickEventFilter,
                             @NotNull Consumer<@NotNull InventoryClickEvent> action) {
        this.clickEventFilter = clickEventFilter;
        this.action = action;
    }

    /**
     * InventoryClickEventを受け取り, {@link #clickEventFilter} で指定された条件を満たすか判断し,
     * 満たす場合には {@link #action} で指定された動作を行います.
     *
     * @param event InventoryClickEvent
     */
    public void invoke(@NotNull InventoryClickEvent event) {
        if (clickEventFilter.shouldReactTo(event)) {
            action.accept(event);
        }
    }
}
