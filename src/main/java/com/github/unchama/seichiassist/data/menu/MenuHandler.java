package com.github.unchama.seichiassist.data.menu;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link InventoryView} を管理するクラス.
 * Singleton で設計されています.
 *
 * @author karayuu
 */
public final class MenuHandler implements Listener {
    private static MenuHandler singleton = new MenuHandler();

    private MenuHandler() {
    }

    /**
     * {@link MenuHandler} のインスタンスを返します.
     *
     * @return {@link MenuHandler} のインスタンス(singleton)
     */
    public static MenuHandler getInstance() {
        return singleton;
    }

    /**
     * 各 {@link InventoryView#invokeAndReload(int, InventoryClickEvent)} を呼び出します.
     *
     * @param event {@link InventoryClickEvent}
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getType() != EntityType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        final InventoryHolder holder = event.getClickedInventory().getHolder();
        if (holder instanceof InventoryView) {
            ((InventoryView) holder).invokeAndReload(event.getSlot(), event);
        }
    }
}
