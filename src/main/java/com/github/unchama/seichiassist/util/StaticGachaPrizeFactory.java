package com.github.unchama.seichiassist.util;

import com.github.unchama.util.collection.ImmutableListFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class StaticGachaPrizeFactory {

    //がちゃりんごの取得
    public static ItemStack getGachaRingo() {
        ItemStack gachaimo;
        ItemMeta meta;
        gachaimo = new ItemStack(Material.GOLDEN_APPLE,1);
        meta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
        meta.setDisplayName(getGachaRingoName());
        List<String> lore = getGachaRingoLore();
        meta.setLore(lore);
        gachaimo.setItemMeta(meta);
        return gachaimo;
    }

    //がちゃりんごの名前を取得
    public static String getGachaRingoName(){
        String name = ChatColor.GOLD + "" + ChatColor.BOLD + "がちゃりんご";
        return name;
    }

    //がちゃりんごの説明を取得
    public static List<String> getGachaRingoLore(){
        List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.GRAY + "序盤に重宝します。"
                , ChatColor.RESET + "" +  ChatColor.AQUA + "マナ回復（小）");
        return lore;
    }

    //椎名林檎の取得
    public static ItemStack getMaxRingo(String name) {
        ItemStack maxringo;
        ItemMeta meta;
        maxringo = new ItemStack(Material.GOLDEN_APPLE,1);
        maxringo.setDurability((short) 1);
        meta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
        meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "椎名林檎");
        List<String> lore = getMaxRingoLore(name);
        meta.setLore(lore);
        maxringo.setItemMeta(meta);
        return maxringo;
    }

    //椎名林檎の説明を取得
    public static List<String> getMaxRingoLore(String name) {
        List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.GRAY + "使用するとマナが全回復します"
                , ChatColor.RESET + "" +  ChatColor.AQUA + "マナ完全回復"
                , ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者:" + name
                , ChatColor.RESET + "" +  ChatColor.GRAY + "ガチャ景品と交換しました。");
        return lore;
    }

    public static ItemStack getMineHeadItem() {
        ItemStack itemstack = new ItemStack(Material.CARROT_STICK,1,(short) 1);
        ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.FEATHER);
        itemmeta.setDisplayName(getMineHeadItemName());
        itemmeta.setLore(getMineHeadItemLore());
        itemmeta.spigot().setUnbreakable(true);
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }

    private static String getMineHeadItemName() {
        return (ChatColor.DARK_RED + "死神の鎌");
    }

    private static List<String> getMineHeadItemLore() {
        return ImmutableListFactory.of(
                ChatColor.RED + "頭を狩り取る形をしている..."
                ,""
                ,ChatColor.GRAY + "設置してある頭が"
                ,ChatColor.GRAY + "左クリックで即時に回収できます"
                ,ChatColor.DARK_GRAY + "インベントリに空きを作って使いましょう"
                );
    }
}
