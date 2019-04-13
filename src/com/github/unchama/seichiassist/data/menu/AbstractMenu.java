package com.github.unchama.seichiassist.data.menu;

import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Menuのベースとなる抽象クラスです.<br>
 * メニューはこのクラスを継承して作成するのをお勧めします.
 *
 * @author karayuu
 */
public abstract class AbstractMenu {
    private final List<Slot> slots = new ArrayList<>();
    private final InventoryType type;

    /**
     * Menuを作成します.
     *
     * @param type このMenuの {@link InventoryType}
     */
    public AbstractMenu(InventoryType type) {
        this.type = type;
    }
    /**
     * {@link Inventory} に {@link Slot} を追加します.
     *
     * @param slots 追加する {@link Slot} の {@link List}
     */
    public void addSlots(List<Slot> slots) {
        this.slots.addAll(slots);
    }
}
