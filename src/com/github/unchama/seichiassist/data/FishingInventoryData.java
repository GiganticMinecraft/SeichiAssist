package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 放置釣りのメニューデータ
 * 2017/9/9
 * @author karayuu
 */
public class FishingInventoryData {

    public static Inventory getFishingMenuData(Player player) {
        Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.LIGHT_PURPLE + "釣りメインメニュー");

        //0マス目
        List<String> lore0 = new ArrayList<>();
        lore0.add(ChatColor.GREEN + "釣りレベル：" + "??");
        lore0.add(ChatColor.GREEN + "釣り経験値：" + "??");
        lore0.add(ChatColor.GREEN + "次のレベルまで：" + "??");
        lore0.add(ChatColor.GREEN + "放置で釣った回数：" + "??");
        lore0.add(ChatColor.GREEN + "直接釣り上げた回数：" + "??");
        ItemStack menuicon0 = Util.getMenuIcon(Material.FISHING_ROD, 1,
                ChatColor.AQUA + "釣り統計", lore0, true);
        inv.setItem(0, menuicon0);

        //4マス目
        List<String> lore4 = new ArrayList<>();
        lore4.add(ChatColor.GREEN + "釣り竿を右クリックした際に");
        lore4.add(ChatColor.GREEN + "このメニューを表示するかどうか設定可能です");
        lore4.add(ChatColor.WHITE + "設定：" + "??");
        lore4.add(ChatColor.RED + "クリックして切り替え");
        ItemStack menuicon4 = Util.getMenuIcon(Material.TRIPWIRE_HOOK, 1,
                ChatColor.AQUA + "ショートカット切り替え", lore4, true);
        inv.setItem(4, menuicon4);

        return inv;
    }
}
