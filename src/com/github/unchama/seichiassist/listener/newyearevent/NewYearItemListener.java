package com.github.unchama.seichiassist.listener.newyearevent;

import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.data.*;
import org.bukkit.*;
import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.*;

/**
 * Created by karayuu on 2017/12/10
 * Developer of Gigantic☆Seichi Server
 * Support at dev-basic or dev-extreme channel of Discord
 */

/**
 * 正月イベント・リンゴのListener
 */
public class NewYearItemListener implements Listener {
    private static Map<UUID, PlayerData> playerMap = SeichiAssist.playermap;
    private static Config config = SeichiAssist.config;

    @EventHandler
    public void onPlayerNewYearItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (canUseApple(event.getItem())) {
            useApple(event.getPlayer());
        }
    }

    private boolean canUseApple(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        List<String> lore = item.getItemMeta().getLore();
        List<String> checkLore = getAppleLoreList();

        return lore.containsAll(checkLore);
    }

    public static ItemStack getNewYearApple() {
        ItemStack apple = new ItemStack(Material.GOLDEN_APPLE, 1);
        ItemMeta appleMeta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
        appleMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "正月リンゴ");
        appleMeta.setLore(getAppleLoreList());
        appleMeta.addEnchant(Enchantment.DIG_SPEED, 100, true);
        appleMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        apple.setItemMeta(appleMeta);
        return apple;
    }

    private static List<String> getAppleLoreList() {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "お正月に向けて作られたりんご。");
        lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "栄養豊富で、食べるとマナが10%回復する。");
        lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "お正月パワーが含まれているため、");
        lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "賞味期限を超えると効果がなくなる。");
        lore.add("");
        lore.add(ChatColor.RESET + "" + ChatColor.DARK_GREEN + "賞味期限: " + config.getNewYearAppleEndDay());
        lore.add(ChatColor.RESET + "" + ChatColor.AQUA + "マナ回復(10%) " + ChatColor.GRAY + " (期限内) ");
        return lore;
    }

    private void useApple(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = playerMap.get(uuid);
        Mana mana = playerData.activeskilldata.mana;

        double max = mana.calcMaxManaOnly(player, playerData.level);
        mana.increaseMana(max * 0.1, player, playerData.level);
        player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f);
    }
}
