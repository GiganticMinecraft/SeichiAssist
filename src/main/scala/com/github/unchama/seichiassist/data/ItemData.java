package com.github.unchama.seichiassist.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Created by karayuu on 2018/04/20
 */
public class ItemData {
    public static ItemStack getSuperPickaxe(int amount) {
        ItemStack itemstack = new ItemStack(Material.DIAMOND_PICKAXE, amount);
        ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Thanks for Voting!");
        List<String> lore = Arrays.asList("投票ありがとナス♡");
        itemmeta.addEnchant(Enchantment.DIG_SPEED, 3, true);
        itemmeta.addEnchant(Enchantment.DURABILITY, 3, true);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);

        return itemstack;
    }

    public static ItemStack getElsa(int amount) {
        ItemStack elsa = new ItemStack(Material.DIAMOND_BOOTS, amount);
        ItemMeta elsaMeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BOOTS);
        elsaMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "エルサ");
        List<String> lore = Arrays.asList("",
                ChatColor.GREEN + "装備中の移動速度" + ChatColor.YELLOW + "(中)" + ChatColor.GREEN,
                "",
                ChatColor.YELLOW + "金床" + ChatColor.RED + "不可",
                ChatColor.YELLOW + "修繕エンチャント" + ChatColor.AQUA + "可");
        elsaMeta.setLore(lore);
        elsaMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, false);
        elsaMeta.addEnchant(Enchantment.FROST_WALKER, 2, false);
        elsaMeta.addEnchant(Enchantment.DURABILITY, 4, false);
        elsaMeta.addEnchant(Enchantment.PROTECTION_FALL, 6, true);
        elsaMeta.addEnchant(Enchantment.MENDING, 1, false);
        elsa.setItemMeta(elsaMeta);
        return elsa;
    }

    public static ItemStack getVotingGift(int amount) {
        ItemStack gift = new ItemStack(Material.PAPER, amount);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
        itemMeta.setDisplayName(ChatColor.AQUA + "投票ギフト券");
        List<String> lore = Arrays.asList("",
                ChatColor.WHITE + "公共施設鯖にある",
                ChatColor.WHITE + "デパートで買い物ができます");
        itemMeta.setLore(lore);
        gift.setItemMeta(itemMeta);
        return gift;
    }
}
