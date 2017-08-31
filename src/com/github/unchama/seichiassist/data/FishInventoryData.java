package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 釣りシステムのインベントリデータ。MenuInventoryDataが見にくすぎるので分けた。
 * 2017/8/30
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
        //TODO:釣りデータ作成
        List<String> lore0 = Arrays.asList(ChatColor.GREEN + "釣りレベル：" + "??",
                ChatColor.GREEN + "釣り経験値：" + "??",
                ChatColor.GREEN + "次のレベルまで：" + "??",
                ChatColor.GREEN + "放置で釣った回数：" + "??",
                ChatColor.GREEN + "直接釣り上げた回数：" + "??");
        ItemStack menuicon0 = getMenuIcon(Material.FISHING_ROD, 1,ChatColor.AQUA + "釣り統計", lore0);
        inventory.setItem(0, menuicon0);

        //1マス目
        //クーラーボックスがあるが、未実装表記にしておく。
        List<String> lore1 = Arrays.asList(ChatColor.DARK_GRAY + "未実装なり");
        ItemStack menuicon1 = getMenuIcon(Material.LAPIS_ORE, 1, ChatColor.AQUA + "クーラーボックス", lore1);
        inventory.setItem(1, menuicon1);

        //2マス目
        //釣りスキル、未実装表記
        List<String> lore2 = lore1;
        ItemStack menuicon2 = getMenuIcon(Material.ENCHANTED_BOOK, 1, ChatColor.AQUA + "釣りスキル", lore2);
        inventory.setItem(2, menuicon2);

        //4マス目
        //トグル
        List<String> lore4 = Arrays.asList(ChatColor.WHITE + "設定：" + "??",
                ChatColor.RED + "クリックで切り替え");
        ItemStack menuicon4 = getMenuIcon(Material.TRIPWIRE_HOOK, 1, ChatColor.AQUA + "ショートカット切り替え", lore4);
        inventory.setItem(4, menuicon4);

        return inventory;
    }

    /**
     * メニューアイコン作成用。
     * @param material アイコンのマテリアル, not {@code null}
     * @param amount アイコンのアイテム個数, not 0
     * @param dispName アイコンの名前, not {@code null}
     * @param lore アイコンの説明(lore), not {@code null}
     * @throws IllegalArgumentException パラメータがどれか一つでも不正値の時
     *
     * @return (itemstack型) メニューアイコン
     */
    public static ItemStack getMenuIcon(Material material, int amount, String dispName, List<String> lore) {
        if (material == null || amount == 0 || dispName == null || lore == null) {
            throw new IllegalArgumentException("Material,DisplayName,Loreにnull,amountに0は許可されません。");
        }

        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(material);
        itemMeta.setDisplayName(dispName);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
