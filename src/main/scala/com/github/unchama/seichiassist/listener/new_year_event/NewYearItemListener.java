package com.github.unchama.seichiassist.listener.new_year_event;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import scala.collection.mutable.HashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 正月イベント・リンゴのListener
 *
 * @author karayuu
 * @since 2017/12/10
 */
public class NewYearItemListener implements Listener {
    private static HashMap<UUID, PlayerData> playerMap = SeichiAssist.playermap();
    private static Config config = SeichiAssist.seichiAssistConfig();

    // FIXME: ここはListenerクラスですぞ
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

    // FIXME: ここはListenerクラスですぞ
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

    private void useApple(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = playerMap.apply(uuid);
        Mana mana = playerData.activeskilldata().mana;

        double max = mana.calcMaxManaOnly(player, playerData.level());
        mana.increase(max * 0.1, player, playerData.level());
        player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f);
    }
}
