package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import scala.collection.immutable.List;
import scala.collection.mutable.HashMap;
import scala.jdk.CollectionConverters;

import java.util.UUID;

public class GachaItemListener implements Listener {
    private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();

    //private SeichiAssist instance = SeichiAssist.instance;
    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        PlayerData playerdata = playermap.apply(player.getUniqueId());
        //念のためエラー分岐
        if (playerdata == null) {
            Util.sendPlayerDataNullMessage(player);
            Bukkit.getLogger().warning(player.getName() + " -> PlayerData not found.");
            Bukkit.getLogger().warning("GachaItemListener.onPlayerItemConsumeEvent");
            return;
        }
        int level = playerdata.level();
        Mana mana = playerdata.activeskilldata().mana;
        ItemStack i = e.getItem();
        //Material m = i.getType();
        ItemMeta itemmeta = i.getItemMeta();

        //これ以降は説明文あり
        if (!itemmeta.hasLore()) return;
        List<String> lore = CollectionConverters.ListHasAsScala(itemmeta.getLore()).asScala().toList();

        if (Util.loreIndexOf(lore, "マナ完全回復") > 0) {
            mana.setFull(player, level);
            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F);
        }

        if (Util.loreIndexOf(lore, "マナ回復（小）") > 0) {
            mana.increase(300, player, level);
            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F);
        }

        if (Util.loreIndexOf(lore, "マナ回復（中）") > 0) {
            mana.increase(1500, player, level);
            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F);
        }

        if (Util.loreIndexOf(lore, "マナ回復（大）") > 0) {
            mana.increase(10000, player, level);
            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F);
        }

        if (Util.loreIndexOf(lore, "マナ回復（極）") > 0) {
            mana.increase(100000, player, level);
            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F);
        }
    }

}
