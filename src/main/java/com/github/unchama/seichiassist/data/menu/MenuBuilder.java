package com.github.unchama.seichiassist.data.menu;

import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Menuを作成する抽象Builderクラスです.
 * @author karayuu
 */
@SuppressWarnings("unchecked")
public abstract class MenuBuilder<T extends MenuBuilder<T>> {
    @NotNull
    private InventoryType type;

    @NotNull
    private List<@NotNull Slot> slots = new ArrayList<>();

    private int column;

    @NotNull
    private Text title = Text.of();

    protected MenuBuilder(@NotNull InventoryType type, int column) {
        this.type = type;
        this.column = column;
    }

    /**
     * Menuに {@link List<Slot>} を追加します.
     *
     * @param slots {@link List<Slot>}
     * @return Builder
     */
    @NotNull
    public T slots(@NotNull List<@NotNull Slot> slots) {
        this.slots.addAll(slots);
        return (T) this;
    }

    @NotNull
    public T slots(Slot... slots) {
        slots(Arrays.asList(slots));
        return (T) this;
    }

    @NotNull
    public T slots(Slot slot) {
        this.slots.add(slot);
        return (T) this;
    }

    @NotNull
    public T title(@NotNull Text title) {
        this.title = title;
        return (T) this;
    }

    @NotNull
    public Menu build() {
        Menu menu = new Menu(type, column);
        menu.addSlots(slots);
        menu.setTitle(title.stringValue());

        return menu;
    }
}
