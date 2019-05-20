package com.github.unchama.seichiassist.data.menu;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author karayuu
 */
public class Menu {
    private final List<Slot> slots = new ArrayList<>();
    private final InventoryType type;
    private int column;
    private String title;

    /**
     * Menuを作成します.
     * 同時に {@link MenuHandler} に,この {@link Menu} を追加します.
     *
     * @param type この {@link Menu} の {@link InventoryType}
     * @param column この {@link Menu} の {@link Inventory} の縦列の数
     */
    public Menu(InventoryType type, int column) {
        this.type = type;
        this.column = column;
        MenuHandler.getInstance().addMenu(this);
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
    public void openBy(@Nonnull Player player) {
        final int slotsInOneRow = 9;
        final PlayerData data = SeichiAssist.playermap.get(player.getUniqueId());
        final Inventory inventory;
        if (type == InventoryType.CHEST) {
            inventory = Bukkit.createInventory(null, column * slotsInOneRow, title);
        } else {
            inventory = Bukkit.createInventory(null, type, title);
        }
        slots.forEach(slot -> setSlot(slot, inventory, data));
        player.openInventory(inventory);
    }

    /**
     * {@link InventoryClickEvent} を渡して該当 {@link Slot} に動作を行わせます.
     * その後, {@link Slot} を読み込みなおします.
     * ただし,枠外のクリック, {@link InventoryType#PLAYER} のクリックには反応しません.
     *
     * @param event {@link InventoryClickEvent} ({@code null} は許容されません)
     * @see SlotActionHandler#action
     */
    void invokeAndReloadSlot(@Nonnull InventoryClickEvent event) {
        requireNonNull(event);
        if (event.getWhoClicked().getType() != EntityType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        final int position = event.getSlot();
        final Player player = (Player) event.getWhoClicked();
        final PlayerData data = SeichiAssist.playermap.get(player.getUniqueId());

        slots.forEach(slot -> {
            if (slot.getPosition() == position) {
                slot.invoke(event);
                //非同期処理を行うとバグの原因となる
                event.getClickedInventory().setItem(slot.getPosition(), slot.getItemStack(data));
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
        Bukkit.getScheduler().runTaskAsynchronously(SeichiAssist.instance,
                () -> inventory.setItem(slot.getPosition(), slot.getItemStack(playerData)));
    }
}
