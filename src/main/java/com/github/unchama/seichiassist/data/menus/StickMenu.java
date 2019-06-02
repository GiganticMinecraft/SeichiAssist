package com.github.unchama.seichiassist.data.menus;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.descrptions.PlayerInformationDescriptions;
import com.github.unchama.seichiassist.data.itemstack.builder.SkullItemStackBuilder;
import com.github.unchama.seichiassist.data.menu.InventoryKeeper;
import com.github.unchama.seichiassist.data.slot.button.ButtonBuilder;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
import com.github.unchama.seichiassist.data.slot.handler.Trigger;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
public final class StickMenu {
    @Nonnull
    private static final InventoryKeeper stickMenu;

    private StickMenu() {
    }

    static {
        stickMenu = InventoryKeeper.from(Bukkit.createInventory(null, 4 * 9));
    }

    public void openBy(@NotNull Player player) {
        final PlayerData data = SeichiAssist.playermap.get(player.getUniqueId());
        stickMenu.setSlot(0, ButtonBuilder
            .from(
                SkullItemStackBuilder
                    .of()
                    .owner(data.getUuid())
                    .title(Text.of(data.getName() + "の統計データ", ChatColor.UNDERLINE, ChatColor.BOLD, ChatColor.YELLOW))
                    .lore(PlayerInformationDescriptions.playerInfoLore(data))
                    .build()
            )
            .appendHandler(new SlotActionHandler(
                Trigger.LEFT_CLICK,
                event -> data.toggleExpBarVisibility()
            ))
            .build()
        );

        stickMenu.openBy(player);
    }
}
