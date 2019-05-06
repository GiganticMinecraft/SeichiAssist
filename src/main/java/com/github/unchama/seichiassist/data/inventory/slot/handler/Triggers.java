package com.github.unchama.seichiassist.data.inventory.slot.handler;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Function;

/**
 * @author karayuu
 */
public class Triggers {
    /**
     * 左クリックを表す Trigger です.
     */
    public static final Function<InventoryClickEvent, Boolean> LEFT_CLICK = InventoryClickEvent::isLeftClick;
    /**
     * 右クリックを表す Trigger です.
     */
    public static final Function<InventoryClickEvent, Boolean> RIGHT_CLICK = InventoryClickEvent::isRightClick;
}
