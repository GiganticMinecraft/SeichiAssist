package com.github.unchama.seichiassist.data.menus;

import com.github.unchama.seichiassist.data.button.PlayerDataButtons;
import com.github.unchama.seichiassist.data.menu.InventoryHolder;
import org.bukkit.Bukkit;
import javax.annotation.Nonnull;

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
public final class StickMenu {
    @Nonnull
    public static final InventoryHolder stickMenu;

    private StickMenu() {
    }

    static {
        stickMenu = InventoryHolder.from(Bukkit.createInventory(null, 4 * 9));
        stickMenu.setSlot(0, PlayerDataButtons.playerInfo);
    }
}
