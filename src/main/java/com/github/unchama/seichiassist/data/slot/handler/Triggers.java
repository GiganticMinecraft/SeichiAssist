package com.github.unchama.seichiassist.data.slot.handler;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Function;

/**
 * @author karayuu
 */
public class Triggers {
    public static Function<InventoryClickEvent, Boolean> RIGHT_CLICK = InventoryClickEvent::isRightClick;
}
