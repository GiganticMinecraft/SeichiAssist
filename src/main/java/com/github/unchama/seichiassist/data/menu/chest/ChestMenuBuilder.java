package com.github.unchama.seichiassist.data.menu.chest;

import com.github.unchama.seichiassist.data.menu.Menu;
import com.github.unchama.seichiassist.data.menu.MenuBuilder;
import org.bukkit.event.inventory.InventoryType;

/**
 * {@link InventoryType#CHEST} である {@link Menu} を作成するためのBuilderです.
 *
 * @author karayuu
 */
public class ChestMenuBuilder extends MenuBuilder<ChestMenuBuilder> {
    private ChestMenuBuilder(int column) {
        super(InventoryType.CHEST, column);
    }

    /**
     * 縦列が column 列であるような {@link InventoryType#CHEST} である {@link Menu} を作成するBuilderを生成します.
     *
     * @param column 縦列が何列であるか (1～6で指定)
     * @return {@link ChestMenuBuilder}
     */
    public static ChestMenuBuilder of(int column) {
        return new ChestMenuBuilder(column);
    }
}
