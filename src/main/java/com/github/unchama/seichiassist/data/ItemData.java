package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.util.collection.ImmutableListFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

/**
 * Created by karayuu on 2018/04/20
 */
public class ItemData {
	public static ItemStack getSuperPickaxe(int amount) {
		ItemStack itemstack = new ItemStack(Material.DIAMOND_PICKAXE, amount);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Thanks for Voting!");
		List<String> lore = ImmutableListFactory.of("投票ありがとナス♡");
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 3, true);
		itemmeta.addEnchant(Enchantment.DURABILITY, 3, true);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);

		return itemstack;
	}

	public static ItemStack getForLevelUpskull(String name, int amount) {
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, amount);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" + ChatColor.DARK_GREEN + "所有者：" + name
				, ChatColor.RESET + "" + ChatColor.DARK_RED + "レベルアップ記念です");
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}

	public static ItemStack getGachaApple(int amount) {
		ItemStack gachaimo;
		ItemMeta meta;
		gachaimo = new ItemStack(Material.GOLDEN_APPLE, amount);
		meta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
		meta.setDisplayName(Util.getGachaRingoName());
		List<String> lore = Util.getGachaRingoLore();
		meta.setLore(lore);
		gachaimo.setItemMeta(meta);
		return gachaimo;
	}

	public static ItemStack getElsa(int amount) {
		ItemStack elsa = new ItemStack(Material.DIAMOND_BOOTS, amount);
		ItemMeta elsaMeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BOOTS);
		elsaMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "エルサ");
		List<String> lore = ImmutableListFactory.of("",
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
		itemMeta.setDisplayName(ChatColor.AQUA  + "投票ギフト券");
		List<String> lore = ImmutableListFactory.of("",
				ChatColor.WHITE + "公共施設鯖にある" ,
				ChatColor.WHITE + "デパートで買い物ができます");
		itemMeta.setLore(lore);
		gift.setItemMeta(itemMeta);
		return gift;
	}
}
