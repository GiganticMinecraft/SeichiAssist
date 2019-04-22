package com.github.unchama.seichiassist.data.menu.chest;

import com.github.unchama.seichiassist.data.menu.Menu;
import org.bukkit.event.inventory.InventoryType;

/**
 * Chest型のMenuを表すクラスです.
 *
 * @author karayuu
 */
public class ChestMenu extends Menu {
    /**
     * Chest型のMenuを作成します.
     */
    public ChestMenu() {
        super(InventoryType.CHEST);
    }
}
