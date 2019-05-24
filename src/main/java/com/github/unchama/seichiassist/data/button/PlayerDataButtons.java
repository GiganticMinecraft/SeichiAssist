package com.github.unchama.seichiassist.data.button;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.descrptions.PlayerInformationDescriptions;
import com.github.unchama.seichiassist.data.itemstack.builder.SkullItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.button.Button;
import com.github.unchama.seichiassist.data.slot.button.ButtonBuilder;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
import com.github.unchama.seichiassist.data.slot.handler.Trigger;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.function.Function;

/**
 * @author karayuu
 */
public final class PlayerDataButtons {
    private PlayerDataButtons() {}

    public static Function<PlayerData, Button> playerInfo = data -> ButtonBuilder
        .from(
            SkullItemStackBuilder
                .of()
                .owner(data.uuid)
                .title(Text.of(data.name + "の統計データ", ChatColor.UNDERLINE, ChatColor.BOLD, ChatColor.YELLOW))
                .lore(PlayerInformationDescriptions.playerInfoLore(data))
                .build()
        )
        .appendHandler(new SlotActionHandler(
            Trigger.LEFT_CLICK,
            event -> {
                final Player player = (Player) event.getWhoClicked();
                final PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
                playerData.toggleExpBarVisibility();
            }
        ))
        .build();
}
