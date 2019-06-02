package com.github.unchama.seichiassist.data.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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

    /**
     * 登録された {@link InventoryView} の {@link List}
     */
    @NotNull
    private List<@NotNull InventoryView> inventoryViews = new ArrayList<>();

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
     * {@link #inventoryViews} に {@link InventoryView} を追加します.
     *
     * @param inventoryView 追加する {@link InventoryView} ({@code null} は許容されません.)
     */
    public void addInventoryHolder(@Nonnull InventoryView inventoryView) {
        inventoryViews.add(inventoryView);
    }

    /**
     * 各 {@link InventoryView#invokeAndReload(int, InventoryClickEvent)} を呼び出します.
     * title にて判断し, {@link InventoryClickEvent} を与えます.
     *
     * @param event {@link InventoryClickEvent}
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final String title = event.getInventory().getTitle();

        inventoryViews.stream()
                        .filter(holder -> holder.getTitle().equals(title))
                        .forEach(holder -> holder.invokeAndReload(event.getSlot(), event));
    }
}
