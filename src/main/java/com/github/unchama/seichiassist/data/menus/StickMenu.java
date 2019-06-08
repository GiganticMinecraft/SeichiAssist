package com.github.unchama.seichiassist.data.menus;

import arrow.core.Either;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.descrptions.PlayerInformationDescriptions;
import com.github.unchama.seichiassist.data.itemstack.builder.SkullItemStackBuilder;
import com.github.unchama.seichiassist.data.menu.InventoryView;
import com.github.unchama.seichiassist.data.slot.button.ButtonBuilder;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
import com.github.unchama.seichiassist.data.slot.handler.Trigger;
import com.github.unchama.seichiassist.util.ItemStackExtensionKt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static org.bukkit.ChatColor.*;

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
public final class StickMenu {
    @Nonnull
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
            .appendHandler(new SlotActionHandler(
                Trigger.LEFT_CLICK,
                event -> {
                    data.toggleExpBarVisibility();
                    data.notifyExpBarVisibility();
                    ItemStackExtensionKt.setLore(event.getCurrentItem(),
                        PlayerInformationDescriptions.playerInfoLore(data));
                }
            ))
            .build()
        );

        player.openInventory(stickMenu.getInventory());
    }
}
