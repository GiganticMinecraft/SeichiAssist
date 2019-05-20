package com.github.unchama.seichiassist.data.slot.handler;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author karayuu
 */
public enum Trigger {
    /**
     * 左クリックを表す Trigger です.
     */
    LEFT_CLICK(InventoryClickEvent::isLeftClick),

    /**
     * 右クリックを表す Trigger です.
     */
    RIGHT_CLICK(InventoryClickEvent::isRightClick),

    ;

    @NotNull
    private Function<@NotNull InventoryClickEvent, @NotNull Boolean> trigger;

    Trigger(@NotNull Function<@NotNull InventoryClickEvent, @NotNull Boolean> trigger) {
        this.trigger = trigger;
    }

    /**
     * 与えられた {@link InventoryClickEvent} に対して動作を行うべきか返します.
     *
     * @param event {@link InventoryClickEvent} ({@code null} は許容されません.)
     * @return true: 動作を行う / false: 動作を行わない
     */
    public boolean shouldReactTo(@NotNull InventoryClickEvent event) {
        return trigger.apply(event);
    }
}
