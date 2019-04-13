package com.github.unchama.seichiassist.data.menu.chest;

import com.github.unchama.seichiassist.data.menu.MenuBuilder;
import org.bukkit.event.inventory.InventoryType;

/**
 * {@link ChestMenu} を作成するためのBuilderです.
 *
 * @author karayuu
 */
public class ChestMenuBuilder extends MenuBuilder<ChestMenuBuilder> {
    private ChestMenuBuilder() {
        super(InventoryType.CHEST);
    }

    public static ChestMenuBuilder of() {
        return new ChestMenuBuilder();
    }
}
