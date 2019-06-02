package com.github.unchama.seichiassist.data.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link InventoryManipulator} を管理するクラス.
 * Singleton で設計されています.
 *
 * @author karayuu
 */
public final class MenuHandler implements Listener {
    private static MenuHandler singleton = new MenuHandler();

    /**
     * 登録された {@link InventoryManipulator} の {@link List}
     */
    @NotNull
    private List<@NotNull InventoryManipulator> inventoryManipulators = new ArrayList<>();

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
     * {@link #inventoryManipulators} に {@link InventoryManipulator} を追加します.
     *
     * @param inventoryManipulator 追加する {@link InventoryManipulator} ({@code null} は許容されません.)
     */
    public void addInventoryHolder(@Nonnull InventoryManipulator inventoryManipulator) {
        inventoryManipulators.add(inventoryManipulator);
    }

    /**
     * 各 {@link InventoryManipulator#invokeAndReload(int, InventoryClickEvent)} を呼び出します.
     * title にて判断し, {@link InventoryClickEvent} を与えます.
     *
     * @param event {@link InventoryClickEvent}
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final String title = event.getInventory().getTitle();

        inventoryManipulators.stream()
                        .filter(holder -> holder.getTitle().equals(title))
                        .forEach(holder -> holder.invokeAndReload(event.getSlot(), event));
    }
}
