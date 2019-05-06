package com.github.unchama.seichiassist.data.button;

import com.github.unchama.seichiassist.data.inventory.itemstack.builder.ItemStackBuilder;
import com.github.unchama.seichiassist.data.inventory.slot.button.Button;
import com.github.unchama.seichiassist.data.inventory.slot.button.ButtonBuilder;
import com.github.unchama.seichiassist.text.Templates;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karayuu on 2019/05/06
 */
public class StickMenuButtons {
    /**
     * 採掘速度上昇変更ボタンのデータ
     */
    public static Button miningSpeedButton = ButtonBuilder
        .from(
            ItemStackBuilder.of(Material.DIAMOND_PICKAXE)
                            .title(Text.of("採掘速度上昇効果", ChatColor.UNDERLINE, ChatColor.BOLD))
                            .lore(playerData -> {
                                final List<Text> textList = new ArrayList<>();
                                textList.addAll(playerData.miningSpeedData.menuDescrpition());
                                textList.addAll(Templates.miningSpeedDescription);


                                return textList;
                            })
        )
}
