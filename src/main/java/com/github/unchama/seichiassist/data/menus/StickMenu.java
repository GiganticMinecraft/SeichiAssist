package com.github.unchama.seichiassist.data.menus;

import arrow.core.Either;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.descrptions.PlayerInformationDescriptions;
import com.github.unchama.seichiassist.data.itemstack.builder.SkullItemStackBuilder;
import com.github.unchama.seichiassist.data.menu.InventoryView;
import com.github.unchama.seichiassist.data.slot.button.ButtonBuilder;
import com.github.unchama.seichiassist.data.slot.handler.SlotAction;
import com.github.unchama.seichiassist.data.slot.handler.ClickEventFilter;
import com.github.unchama.seichiassist.util.ItemStackExtensionKt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.ChatColor.*;

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
public final class StickMenu {
    @NotNull
    private static final InventoryView stickMenu;

    private StickMenu() {
    }

    static {
        @SuppressWarnings("unchecked")
        final Either<Integer, InventoryType> property = new Either.Left(4 * 9);
        stickMenu = new InventoryView(property, LIGHT_PURPLE + "木の棒メニュー");
    }

    public static void openBy(@NotNull Player player) {
        final PlayerData data = SeichiAssist.playermap.get(player.getUniqueId());
        stickMenu.setSlot(0, ButtonBuilder
            .from(
                SkullItemStackBuilder
                    .of()
                    .owner(data.getUuid())
                    .title(YELLOW + "" + BOLD + "" + UNDERLINE + data.getName() + "の統計データ")
                    .lore(PlayerInformationDescriptions.playerInfoLore(data))
                    .build()
            )
            .appendAction(new SlotAction(
                ClickEventFilter.LEFT_CLICK,
                event -> {
                    data.toggleExpBarVisibility();
                    data.notifyExpBarVisibility();
                    ItemStackExtensionKt.setLoreNotNull(event.getCurrentItem(),
                        PlayerInformationDescriptions.playerInfoLore(data));
                }
            ))
            .build()
        );

        player.openInventory(stickMenu.getInventory());
    }
}
