package com.github.unchama.seichiassist.data.menus;

import com.github.unchama.seichiassist.data.button.PlayerDataButtons;
import com.github.unchama.seichiassist.data.menu.InventoryKeeper;
import org.bukkit.Bukkit;
import javax.annotation.Nonnull;

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
public final class StickMenu {
    @Nonnull
    public static final InventoryKeeper stickMenu;

    private StickMenu() {
    }

    static {
        stickMenu = InventoryKeeper.from(Bukkit.createInventory(null, 4 * 9));
        stickMenu.setSlot(0, PlayerDataButtons.playerInfo);
    }
}
