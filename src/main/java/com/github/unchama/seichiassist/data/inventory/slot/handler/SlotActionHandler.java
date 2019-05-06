package com.github.unchama.seichiassist.data.inventory.slot.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * SLotのActionを管理するクラスです.
 *
 * @author karayuu
 */
public class SlotActionHandler {
    /**
     * InventoryClickEventを受け取って,動作を行わせるかを返すFunction
     */
    @Nonnull
    private Function<InventoryClickEvent, Boolean> trigger;

    /**
     * InventoryClickEventを与えて,何かしらの動作を行わせるFunction <br>
     * {@link #trigger} がtrueを返した際に動作します. <br>
     * この時点で {@link InventoryClickEvent} の {@link InventoryClickEvent#setCancelled(boolean)} でキャンセルを行うと,
     * 動作がなかったことになります. <br>
     * なおこの時点で, {@link InventoryClickEvent#getWhoClicked()} は {@link Player} であることが保証されています.
     */
    @Nonnull
    private Consumer<InventoryClickEvent> action;

    public SlotActionHandler(@Nonnull Function<InventoryClickEvent, Boolean> trigger,
                             Consumer<InventoryClickEvent> action) {
        this.trigger = requireNonNull(trigger);
        this.action = requireNonNull(action);
    }

    /**
     * InventoryClickEventを受け取り,動作条件を満たすか判断し,満たす場合には指定された動作を行います.
     *
     * @param event InventoryClickEvent
     */
    public void invoke(@Nonnull InventoryClickEvent event) {
        if (trigger.apply(event)) {
            action.accept(event);
        }
    }
}
