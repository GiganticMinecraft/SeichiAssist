package com.github.unchama.seichiassist.data.button;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.builder.SkullItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.button.Button;
import com.github.unchama.seichiassist.data.slot.button.ButtonBuilder;

/**
 * @author karayuu
 */
public class PlayerDataButtons {
    public static Button playerInfo = ButtonBuilder.from(
        SkullItemStackBuilder.of()
            .setPlayerSkull()
            .title(data -> data.name + "の統計データ")
            .lore(PlayerData::getPlayerInfoLore)
    ).at(0).build();
}
