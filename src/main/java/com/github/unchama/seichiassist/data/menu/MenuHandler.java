package com.github.unchama.seichiassist.data.menu;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

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
     * 各 {@link InventoryView#invoke(int, InventoryClickEvent)} を呼び出します.
     *
     * @param event {@link InventoryClickEvent}
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getType() != EntityType.PLAYER) {
            return;
        }

        final Inventory clickedInventory = event.getClickedInventory();
        final Inventory openInventory = event.getWhoClicked().getOpenInventory().getTopInventory();
        //メニュー外のクリック排除
        if (clickedInventory == null) {
            return;
        }

        //プレイヤーインベントリ内のクリック排除
        if (openInventory.getHolder() instanceof InventoryView && clickedInventory.getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        final InventoryHolder holder = clickedInventory.getHolder();
        if (holder instanceof InventoryView) {
            ((InventoryView) holder).invoke(event.getSlot(), event);
        }
    }
}
