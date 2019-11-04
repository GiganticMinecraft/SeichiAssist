package com.github.unchama.seichiassist.listener.new_year_event;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.seichiassist.util.external.ExternalPlugins;
import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import scala.collection.mutable.HashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 正月イベント・お年玉袋関連処理実装クラス。
 *
 * @author karayuu
 * @since 2017/11/29
 */
public class NewYearBagListener implements Listener {
    private static Config config = SeichiAssist.seichiAssistConfig();
    private static HashMap<UUID, PlayerData> playerMap = SeichiAssist.playermap();

    // FIXME: ここはListenerクラスですぞ
    public static ItemStack getNewYearBag() {
        ItemStack newYearBag = new ItemStack(Material.PAPER);
        ItemMeta newYearBagMeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
        newYearBagMeta.setDisplayName(ChatColor.AQUA + "お年玉袋");
        newYearBagMeta.addEnchant(Enchantment.DIG_SPEED, 100, true);
        newYearBagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "新年あけましておめでとうございます");
        lore.add(ChatColor.RESET + "新年をお祝いして" + ChatColor.RED + "" + ChatColor.UNDERLINE + "お年玉袋" + ChatColor.RESET + "をプレゼント！");
        lore.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "「各サーバースポーンワールド」の村人で");
        lore.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "様々なアイテムに交換可能です。");
        newYearBagMeta.setLore(lore);
        newYearBag.setItemMeta(newYearBagMeta);
        NBTItem nbtNewYearBag = new NBTItem(newYearBag);
        nbtNewYearBag.setString("EventItem", "NewYearEvent" + config.getNewYear());
        return nbtNewYearBag.getItem();
    }

    /**
     * プレイヤーがブロックを破壊した際に呼ばれるメソッド。
     * お年玉袋のドロップ処理に利用。
     *
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        PlayerData playerData = playerMap.apply(player.getUniqueId());

        //整地ワールドのみドロップ許可
        if (!Util.isSeichiWorld(player)) {
            return;
        }

        //自分の保護範囲外ではドロップさせない
        if (!ExternalPlugins.getWorldGuard().canBuild(player, block)) {
            return;
        }

        if (isDrop()) {
            if (Util.isPlayerInventoryFull(player)) {
                Util.dropItem(player, getNewYearBag());
                player.sendMessage(ChatColor.RED + "インベントリがいっぱいのため「お年玉袋」がドロップしました");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 3.0f, 1.0f);
            } else {
                Util.addItem(player, getNewYearBag());
                player.sendMessage(ChatColor.AQUA + "「お年玉袋」を見つけたよ！");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 3.0f, 1.0f);
            }
            playerData.newYearBagAmount_$eq(playerData.newYearBagAmount() + 1);
        }
    }

    private boolean isDrop() {
        int check = new Random().nextInt(config.getNewYearDropProbability());
        return check == 0;
    }
}
