package com.github.unchama.seichiassist.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class StaticGachaPrizeFactory {
    private static ItemStack gachaRingo;
    private static ItemStack sickleOfDeathGod;

    /**
     * @return ガチャりんごを表すItemStackを返す。
     */
    public static @NotNull ItemStack getGachaRingo() {
        if (gachaRingo == null) {
            ItemMeta meta;
            gachaRingo = new ItemStack(Material.GOLDEN_APPLE, 1);
            meta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
            meta.setDisplayName(getGachaRingoName());
            List<String> lore = getGachaRingoLore();
            meta.setLore(lore);
            gachaRingo.setItemMeta(meta);
        }
        return gachaRingo;
    }

    //がちゃりんごの名前を取得
    public static String getGachaRingoName() {
        String name = ChatColor.GOLD + "" + ChatColor.BOLD + "がちゃりんご";
        return name;
    }

    //がちゃりんごの説明を取得
    public static List<String> getGachaRingoLore() {
        List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "序盤に重宝します。"
                , ChatColor.RESET + "" + ChatColor.AQUA + "マナ回復（小）");
        return lore;
    }

    /**
     * @return 椎名林檎を表すItemStackを返す。
     */
    public static @NotNull ItemStack getMaxRingo(String name) {
        ItemStack siinaRingo = new ItemStack(Material.GOLDEN_APPLE, 1);
        siinaRingo.setDurability((short) 1);

        List<String> lore = getMaxRingoLore(name);

        ItemMeta meta = ItemMetaFactory.GOLDEN_APPLE.getValue();
        meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "椎名林檎");
        meta.setLore(lore);
        siinaRingo.setItemMeta(meta);

        return siinaRingo;
    }

    //椎名林檎の説明を取得
    private static List<String> getMaxRingoLore(String name) {
        return Arrays.asList(
                ChatColor.RESET + "" + ChatColor.GRAY + "使用するとマナが全回復します",
                ChatColor.RESET + "" + ChatColor.AQUA + "マナ完全回復",
                ChatColor.RESET + "" + ChatColor.DARK_GREEN + "所有者:" + name,
                ChatColor.RESET + "" + ChatColor.GRAY + "ガチャ景品と交換しました。"
        );
    }

    /**
     * @return 死神の鎌を表すItemStackを返す。
     */
    public static @NotNull ItemStack getMineHeadItem() {
        if (sickleOfDeathGod == null) {
            sickleOfDeathGod = new ItemStack(Material.CARROT_STICK, 1, (short) 1);
            ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.FEATHER);
            itemmeta.setDisplayName(getMineHeadItemName());
            itemmeta.setLore(getMineHeadItemLore());
            itemmeta.spigot().setUnbreakable(true);
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            sickleOfDeathGod.setItemMeta(itemmeta);
        }

        return sickleOfDeathGod;
    }

    private static String getMineHeadItemName() {
        return (ChatColor.DARK_RED + "死神の鎌");
    }

    private static List<String> getMineHeadItemLore() {
        return Arrays.asList(
                ChatColor.RED + "頭を狩り取る形をしている...",
                "",
                ChatColor.GRAY + "設置してある頭が",
                ChatColor.GRAY + "左クリックで即時に回収できます",
                ChatColor.DARK_GRAY + "インベントリに空きを作って使いましょう"
        );
    }
}
