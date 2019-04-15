package com.github.unchama.seichiassist.data.menus;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.itemstack.builder.SkullItemStackBuilder;
import com.github.unchama.seichiassist.data.itemstack.builder.SlotItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.button.Button;
import com.github.unchama.seichiassist.data.slot.button.ButtonBuilder;
import com.github.unchama.seichiassist.data.slot.handler.SlotActionHandler;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
public class StickMenu {
    private StickMenu() {
    }

    private static Button playerInfo = ButtonBuilder.from(
            SkullItemStackBuilder.of()
                    .setPlayerSkull()
                    .title(data -> data.name + "の統計データ")
                    .lore(data -> {
                        final List<String> lore = new ArrayList<>();
                        lore.add(data.getSeichiLevelDescription());
                        if (data.level < SeichiAssist.levellist.size()) {
                            lore.add(data.getRemainLevelDescription());
                        }

                        return lore;
                    })
    ).at(0).build();
}
