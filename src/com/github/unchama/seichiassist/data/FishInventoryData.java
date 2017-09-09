package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * 釣りシステムのインベントリデータ。MenuInventoryDataが見にくすぎるので分けた。
 * 2017/9/9
 * @author karayuu
 *
 */
public class FishInventoryData {
    static HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
    static Sql sql = SeichiAssist.sql;
    SeichiAssist plugin = SeichiAssist.plugin;

    /**
     * 釣りメニューを取得します。
     * @param player プレイヤー
     * @return 完成後のInventory
     */
    public static Inventory getFishingInv(Player player) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.BLUE + "釣りメインメニュー");

        //0マス目
        //TODO:釣りデータ作成する
        List<String> lore0 = new ArrayList<>();
        lore0.add(ChatColor.GREEN + "釣りレベル：" + "??");
        lore0.add(ChatColor.GREEN + "釣り経験値：" + "??");
        lore0.add(ChatColor.GREEN + "次のレベルまで：" + "??");
        lore0.add(ChatColor.GREEN + "放置で釣った回数：" + "??");
        lore0.add(ChatColor.GREEN + "直接釣り上げた回数：" + "??");
        ItemStack menuicon0 = Util.getMenuIcon(Material.FISHING_ROD, 1,
                ChatColor.AQUA + "釣り統計", lore0, true);
        inventory.setItem(0, menuicon0);

        //1マス目
        //クーラーボックスがあるが、未実装表記にしておく。
        List<String> lore1 = new ArrayList<>();
        lore1.add(ChatColor.DARK_GRAY + "未実装なり");
        ItemStack menuicon1 = Util.getMenuIcon(Material.LAPIS_ORE, 1,
                ChatColor.AQUA + "クーラーボックス", lore1, true);
        inventory.setItem(1, menuicon1);

        //2マス目
        //釣りスキル、未実装表記
        List<String> lore2 = lore1;
        ItemStack menuicon2 = Util.getMenuIcon(Material.ENCHANTED_BOOK, 1,
                ChatColor.AQUA + "釣りスキル", lore2, true);
        inventory.setItem(2, menuicon2);

        //4マス目
        //トグル
        List<String> lore4 = new ArrayList<>();
        lore4.add(ChatColor.WHITE + "設定：" + "??");
        lore4.add(ChatColor.RED + "クリックで切り替え");
        ItemStack menuicon4 = Util.getMenuIcon(Material.TRIPWIRE_HOOK, 1,
                ChatColor.AQUA + "ショートカット切り替え", lore4, true);
        inventory.setItem(4, menuicon4);

        return inventory;
    }
}
