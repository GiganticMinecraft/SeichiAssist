package com.github.unchama.seichiassist.data.button;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.descrptions.PlayerInfomationDescriptions;
import com.github.unchama.seichiassist.data.inventory.itemstack.builder.SkullItemStackBuilder;
import com.github.unchama.seichiassist.data.inventory.slot.button.Button;
import com.github.unchama.seichiassist.data.inventory.slot.button.ButtonBuilder;
import com.github.unchama.seichiassist.data.inventory.slot.handler.SlotActionHandler;
import com.github.unchama.seichiassist.data.inventory.slot.handler.Triggers;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author karayuu
 */
public class PlayerDataButtons {
    public static Button playerInfo = ButtonBuilder
        .from(
            SkullItemStackBuilder
                .of()
                .playerSkull()
                .title(data -> Text.of(data.name + "の統計データ", ChatColor.UNDERLINE, ChatColor.BOLD, ChatColor.YELLOW))
                .lore(PlayerInfomationDescriptions.playerInfoLore)
        )
        .at(0)
        .handler(new SlotActionHandler(
            Triggers.LEFT_CLICK,
            event -> {
                final Player player = (Player) event.getWhoClicked();
                final PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
                playerData.toggleExpBarVisibility();
            }
        ))
        .build();
}
