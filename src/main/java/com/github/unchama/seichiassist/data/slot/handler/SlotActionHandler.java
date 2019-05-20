package com.github.unchama.seichiassist.data.slot.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * SLotのActionを管理するクラスです.
 *
 * @author karayuu
 */
public class SlotActionHandler {
    /**
     * InventoryClickEventを受け取って,動作を行わせるかを決定する {@link Trigger}
     */
    @NotNull
    private Trigger trigger;

    /**
     * InventoryClickEventを与えて,何かしらの動作を行わせるFunction <br>
     * {@link #trigger} がtrueを返した際に動作します. <br>
     * この時点で {@link InventoryClickEvent} の {@link InventoryClickEvent#setCancelled(boolean)} でキャンセルを行うと,
     * 動作がなかったことになります. <br>
     * なおこの時点で, {@link InventoryClickEvent#getWhoClicked()} は {@link Player} であることが保証されています.
     */
    @NotNull
    private Consumer<InventoryClickEvent> action;

    public SlotActionHandler(@NotNull Trigger trigger,
                             @NotNull Consumer<@NotNull InventoryClickEvent> action) {
        this.trigger = trigger;
        this.action = action;
    }

    /**
     * InventoryClickEventを受け取り, {@link #trigger} で指定された条件を満たすか判断し,
     * 満たす場合には {@link #action} で指定された動作を行います.
     *
     * @param event InventoryClickEvent
     */
    public void invoke(@NotNull InventoryClickEvent event) {
        if (trigger.shouldReactTo(event)) {
            action.accept(event);
        }
    }
}
