package com.github.unchama.seichiassist.data.menu;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Menuのベースとなるクラスです.<br>
 * メニューはこのクラスを継承して作成するのをお勧めします.
 *
 * @author karayuu
 */
public class Menu {
    private final List<Slot> slots = new ArrayList<>();
    private final InventoryType type;
    private int size;
    private String title;

    /**
     * Menuを作成します.
     *
     * @param type このMenuの {@link InventoryType}
     */
    public Menu(InventoryType type) {
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

    /**
     * {@link Inventory} の {@link InventoryType} が {@link InventoryType#CHEST} の場合のみ使用可能です.
     * {@link Inventory} の大きさを指定します.
     * 必ず9の倍数である必要があります.
     *
     * @param size
     */
    public void setSize(int size) {
        if (size % 9 != 0) {
            throw new IllegalArgumentException("Menu#setSizeは9の倍数である必要があります.");
        }
        if (type != InventoryType.CHEST) {
            throw new IllegalArgumentException("InventoryTypeがChestではないため,sizeをセットすることはできません.");
        }
        this.size = size;
    }

    /**
     * このMenuのtitleを設定します.
     *
     * @param title このMenuのtitle ({@code null} は許容されません.)
     */
    public void setTitle(@Nonnull String title) {
        this.title = requireNonNull(title);
    }

    /**
     * このMenuのtitleを取得します.
     *
     * @return Menuのtitle
     */
    @Nonnull
    public String getTitle() {
        return this.title;
    }

    /**
     * 与えられた {@link Player} にこのMenuを開かせます.
     *
     * @param player Menuを開く {@link Player}
     */
    public void open(@Nonnull Player player) {
        final PlayerData data = SeichiAssist.playermap.get(player.getUniqueId());
        final Inventory inventory;
        if (type == InventoryType.CHEST) {
            inventory = Bukkit.createInventory(null, size, title);
        } else {
            inventory = Bukkit.createInventory(null, type, title);
        }
        slots.forEach(slot -> setSlot(slot, inventory, data));
    }

    /**
     * {@link InventoryClickEvent} を渡して該当 {@link Slot} に動作を行わせます.
     *
     * @param event {@link InventoryClickEvent} ({@code null} は許容されません)
     */
    public void invoke(@Nonnull InventoryClickEvent event) {
        requireNonNull(event);
        final int position = event.getSlot();
        slots.forEach(slot -> {
            if (slot.getPosition() == position) {
                slot.invoke(event);
            }
        });
    }

    /**
     * 非同期で {@link Inventory} に {@link Slot} をセットします.
     *
     * @param slot       {@link Slot}
     * @param inventory  {@link Inventory}
     * @param playerData {@link PlayerData}
     */
    private void setSlot(@Nonnull Slot slot, @Nonnull Inventory inventory, @Nonnull PlayerData playerData) {
        requireNonNull(slot);
        requireNonNull(inventory);
        requireNonNull(playerData);
        Bukkit.getScheduler().runTaskAsynchronously(SeichiAssist.plugin,
                () -> inventory.setItem(slot.getPosition(), slot.getItemStack(playerData)));
    }
}
