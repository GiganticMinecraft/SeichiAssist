package com.github.unchama.seichiassist.data.inventory.menu;

import com.github.unchama.seichiassist.data.inventory.slot.Slot;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.event.inventory.InventoryType;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Menuを作成する抽象Builderクラスです.
 * @author karayuu
 */
@SuppressWarnings("unchecked")
public abstract class MenuBuilder<T extends MenuBuilder<T>> {
    @Nonnull
    private InventoryType type;

    @Nonnull
    private List<Slot> slots = new ArrayList<>();

    private int size;

    @Nonnull
    private Text title = Text.of();

    protected MenuBuilder(@Nonnull InventoryType type) {
        this.type = requireNonNull(type);
    }

    /**
     * Menuに {@link List<Slot>} を追加します.
     *
     * @param slots {@link List<Slot>}
     * @return Builder
     */
    public T slots(@Nonnull List<Slot> slots) {
        this.slots.addAll(requireNonNull(slots));
        return (T) this;
    }

    public T slots(Slot... slots) {
        slots(Arrays.asList(slots));
        return (T) this;
    }

    public T slots(Slot slot) {
        this.slots.add(slot);
        return (T) this;
    }

    public T size(int size) {
        this.size = size;
        return (T) this;
    }

    public T title(@Nonnull Text title) {
        this.title = requireNonNull(title);
        return (T) this;
    }

    public Menu build() {
        Menu menu = new Menu(type);
        menu.addSlots(slots);
        menu.setSize(size);
        menu.setTitle(title.stringValue());

        return menu;
    }
}
