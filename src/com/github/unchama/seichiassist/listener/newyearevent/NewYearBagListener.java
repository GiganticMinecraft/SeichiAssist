package com.github.unchama.seichiassist.listener.newyearevent;

import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.data.*;
import com.github.unchama.seichiassist.util.*;
import de.tr7zw.itemnbtapi.*;
import org.bukkit.*;
import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.*;

/**
 * Created by karayuu on 2017/11/29
 * Developer of Gigantic☆Seichi Server
 * Support at dev-basic or dev-extreme channel of Discord
 */

/**
 * 正月イベント・お年玉袋関連処理実装クラス。
 */
public class NewYearBagListener implements Listener {
    private static Config config = SeichiAssist.config;
    private static Map<UUID, PlayerData> playerMap = SeichiAssist.playermap;
    /**
     * プレイヤーがブロックを破壊した際に呼ばれるメソッド。
     * お年玉袋のドロップ処理に利用。
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = playerMap.get(player.getUniqueId());

        //整地ワールドのみドロップ許可
        if (!Util.isSeichiWorld(player)) {
            return;
        }

        if (isDrop()) {
            if (Util.isPlayerInventryFill(player)) {
                Util.dropItem(player, getNewYearBag());
                player.sendMessage(ChatColor.RED + "インベントリがいっぱいのため「お年玉袋」がドロップしました");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 3f, 1f);
            } else {
                Util.addItem(player, getNewYearBag());
                player.sendMessage(ChatColor.AQUA + "「お年玉袋」を見つけたよ！");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 3f, 1f);
            }
            playerData.newYearBagAmount += 1;
        }
    }

    private boolean isDrop() {
        int check = new Random().nextInt(config.getNewYearDropProbability());
        return check == 0;
    }

    public static ItemStack getNewYearBag() {
        ItemStack newYearBag = new ItemStack(Material.PAPER);
        ItemMeta newYearBagMeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
        newYearBagMeta.setDisplayName(ChatColor.AQUA + "お年玉袋");
        newYearBagMeta.addEnchant(Enchantment.DIG_SPEED, 100, true);
        newYearBagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "新年あけましておめでとうございます");
        lore.add(ChatColor.RESET + "新年をお祝いして" + ChatColor.RED + "" + ChatColor.UNDERLINE + "お年玉袋" + ChatColor.RESET + "をプレゼント！");
        lore.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "「各サーバスポーンワールド」の村人で");
        lore.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "様々なアイテムに交換可能です。");
        newYearBagMeta.setLore(lore);
        newYearBag.setItemMeta(newYearBagMeta);
        NBTItem nbtNewYearBag = new NBTItem(newYearBag);
        nbtNewYearBag.setString("EventItem", "NewYearEvent" + config.getNewYear());
        return nbtNewYearBag.getItem();
    }
}
