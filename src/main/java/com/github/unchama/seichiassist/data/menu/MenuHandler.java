package com.github.unchama.seichiassist.data.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link InventoryKeeper} を管理するクラス.
 * Singleton で設計されています.
 *
 * @author karayuu
 */
public final class MenuHandler implements Listener {
    private static MenuHandler singleton = new MenuHandler();

    /**
     * 登録された {@link InventoryKeeper} の {@link List}
     */
    @Nonnull
    private List<InventoryKeeper> inventoryKeepers = new ArrayList<>();

    private MenuHandler() {}

    /**
     * {@link MenuHandler} のインスタンスを返します.
     *
     * @return {@link MenuHandler} のインスタンス(singleton)
     */
    public static MenuHandler getInstance() {
        return singleton;
    }


    /**
     * {@link #inventoryKeepers} に {@link InventoryKeeper} を追加します.
     *
     * @param inventoryKeeper 追加する {@link InventoryKeeper} ({@code null} は許容されません.)
     */
    public void addInventoryHolder(@Nonnull InventoryKeeper inventoryKeeper) {
        inventoryKeepers.add(inventoryKeeper);
    }

    /**
     * 各 {@link InventoryKeeper#invokeAndReload(int, InventoryClickEvent)} を呼び出します.
     * title にて判断し, {@link InventoryClickEvent} を与えます.
     *
     * @param event {@link InventoryClickEvent}
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final String title = event.getInventory().getTitle();

        inventoryKeepers.stream()
                        .filter(holder -> holder.getTitle().equals(title))
                        .forEach(holder -> holder.invokeAndReload(event.getSlot(), event));
    }
}
