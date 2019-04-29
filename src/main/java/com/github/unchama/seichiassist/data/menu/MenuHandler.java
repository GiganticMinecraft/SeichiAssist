package com.github.unchama.seichiassist.data.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * {@link Menu} を管理するクラス.
 * Singleton で設計されています.
 *
 * @author karayuu
 */
public class MenuHandler implements Listener {
    private static MenuHandler singleton = new MenuHandler();

    /**
     * 登録された {@link Menu} の {@link List}
     */
    @Nonnull
    private List<Menu> menus = new ArrayList<>();

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
     * {@link Menu} 管理 {@link List} に {@link Menu} を追加します.
     *
     * @param menu 追加する {@link Menu}
     */
    public void addMenu(@Nonnull Menu menu) {
        menus.add(requireNonNull(menu));
    }

    /**
     * 各 {@link Menu#invokeAndReopen(InventoryClickEvent)} を呼び出します.
     * titleにて判断し, {@link InventoryClickEvent} を与えます.
     *
     * @param event {@link InventoryClickEvent}
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final String title = event.getInventory().getTitle();

        menus.stream()
             .filter(menu -> menu.getTitle().equals(title))
             .forEach(menu -> menu.invokeAndReopen(event));
    }
}
