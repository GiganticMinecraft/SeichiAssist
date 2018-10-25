package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;

public class ActiveSkillInventoryData {
	static HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	static Sql sql = SeichiAssist.sql;
	SeichiAssist plugin = SeichiAssist.plugin;

	//アクティブスキルメニュー
	public static Inventory getActiveSkillMenuData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,5*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキル選択");
		ItemStack itemstack;
		ItemMeta itemmeta;
		PotionMeta potionmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(36,itemstack);

		//1行目

		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.name + "のアクティブスキルデータ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "現在選択しているスキル：" + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype,playerdata.activeskilldata.skillnum)
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "使えるアクティブスキルポイント：" + playerdata.activeskilldata.skillpoint);
		skullmeta.setLore(lore);
		skullmeta.setOwner(playerdata.name);
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(0,itemstack);


		itemstack = new ItemStack(Material.GLASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スキルを使用しない");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);

		itemstack = new ItemStack(Material.BOOKSHELF,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOKSHELF);
		itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "演出効果設定");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "" + "スキル使用時の演出を選択できるゾ"
					,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで演出一覧を開く");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);

		if(SeichiAssist.DEBUG){
		itemstack = new ItemStack(Material.STONE_BUTTON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE_BUTTON);
		itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リセットボタン");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "" + "全スキル・全エフェクトの振り直しができます。"
					,ChatColor.RESET + "" +  ChatColor.RED + "必要経験値：10000"
					,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでリセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(9,itemstack);
		}
		if(playerdata.activeskilldata.arrowskill >= 4){
			itemstack = new ItemStack(Material.TIPPED_ARROW,1);
			potionmeta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.TIPPED_ARROW);
			potionmeta.setBasePotionData(new PotionData(PotionType.REGEN));
			potionmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			potionmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			potionmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エビフライ・ドライブ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠3×3×3ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0.2秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：18"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			potionmeta.setLore(lore);
			itemstack.setItemMeta(potionmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エビフライ・ドライブ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠3×3×3ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0.2秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：18"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：40"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：エクスプロージョン"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(3,itemstack);

		if(playerdata.activeskilldata.arrowskill >= 5){
			itemstack = new ItemStack(Material.TIPPED_ARROW,1);
			potionmeta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.TIPPED_ARROW);
			potionmeta.setBasePotionData(new PotionData(PotionType.FIRE_RESISTANCE));
			potionmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			potionmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			potionmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホーリー・ショット");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠5×5×3ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.3秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：35"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			potionmeta.setLore(lore);
			itemstack.setItemMeta(potionmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホーリー・ショット");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠5×5×3ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.3秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：35"
											, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：50"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：エビフライ・ドライブ"
											, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(4,itemstack);

		if(playerdata.activeskilldata.arrowskill >= 6){
			itemstack = new ItemStack(Material.TIPPED_ARROW,1);
			potionmeta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.TIPPED_ARROW);
			potionmeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
			potionmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			potionmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			potionmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ツァーリ・ボンバ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠7×7×5ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.6秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：80"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			potionmeta.setLore(lore);
			itemstack.setItemMeta(potionmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ツァーリ・ボンバ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠7×7×5ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.6秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：80"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：60"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ホーリー・ショット"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(5,itemstack);

		if(playerdata.activeskilldata.arrowskill >= 7){
			itemstack = new ItemStack(Material.TIPPED_ARROW,1);
			potionmeta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.TIPPED_ARROW);
			potionmeta.setBasePotionData(new PotionData(PotionType.NIGHT_VISION));
			potionmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			potionmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			potionmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アーク・ブラスト");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠9×9×7ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.7秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：110"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			potionmeta.setLore(lore);
			itemstack.setItemMeta(potionmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アーク・ブラスト");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠9×9×7ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.7秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：110"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：70"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ツァーリ・ボンバ"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(6,itemstack);

		if(playerdata.activeskilldata.arrowskill >= 8){
			itemstack = new ItemStack(Material.TIPPED_ARROW,1);
			potionmeta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.TIPPED_ARROW);
			potionmeta.setBasePotionData(new PotionData(PotionType.SPEED));
			potionmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			potionmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			potionmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ファンタズム・レイ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠11×11×9ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：3.8秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：220"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			potionmeta.setLore(lore);
			itemstack.setItemMeta(potionmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ファンタズム・レイ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠11×11×9ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：3.8秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：220"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：80"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：アーク・ブラスト"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(7,itemstack);

		if(playerdata.activeskilldata.arrowskill >= 9){
			itemstack = new ItemStack(Material.TIPPED_ARROW,1);
			potionmeta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.TIPPED_ARROW);
			potionmeta.setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE));
			potionmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			potionmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			potionmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スーパー・ノヴァ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠13×13×11ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：5.5秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：380"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			potionmeta.setLore(lore);
			itemstack.setItemMeta(potionmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スーパー・ノヴァ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "遠13×13×11ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：5.5秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：380"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：90"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ファンタズム・レイ"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(8,itemstack);

		//2列目
		if(playerdata.activeskilldata.multiskill >= 4){
			itemstack = new ItemStack(Material.SADDLE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SADDLE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "トム・ボウイ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3ブロック破壊 ×3"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0.6秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：28"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "トム・ボウイ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3ブロック破壊 ×3"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0.6秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：28"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：40"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：エクスプロージョン"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(12,itemstack);


		if(playerdata.activeskilldata.multiskill >= 5){
			itemstack = new ItemStack(Material.MINECART,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MINECART);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サンダー・ストーム");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3ブロック破壊 ×7"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.4秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：65"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サンダー・ストーム");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3ブロック破壊 ×7"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.4秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：65"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：50"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：トム・ボウイ"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(13,itemstack);

		if(playerdata.activeskilldata.multiskill >= 6){
			itemstack = new ItemStack(Material.STORAGE_MINECART,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STORAGE_MINECART);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スターライト・ブレイカー");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "5×5×5ブロック破壊 ×3"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.4秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：90"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スターライト・ブレイカー");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "5×5×5ブロック破壊 ×3"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.4秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：90"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：60"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：サンダー・ストーム"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(14,itemstack);


		if(playerdata.activeskilldata.multiskill >= 7){
			itemstack = new ItemStack(Material.POWERED_MINECART,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.POWERED_MINECART);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アース・ディバイド");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "5×5×5ブロック破壊 ×5"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：3.4秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：185"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アース・ディバイド");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "5×5×5ブロック破壊 ×5"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：3.4秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：185"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：70"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：スターライト・ブレイカー"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(15,itemstack);


		if(playerdata.activeskilldata.multiskill >= 8){
			itemstack = new ItemStack(Material.EXPLOSIVE_MINECART,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EXPLOSIVE_MINECART);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ヘヴン・ゲイボルグ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×7ブロック破壊 ×3"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：4.8秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：330"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ヘヴン・ゲイボルグ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×7ブロック破壊 ×3"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：4.8秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：330"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：80"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：アース・ディバイド"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(16,itemstack);


		if(playerdata.activeskilldata.multiskill >= 9){
			itemstack = new ItemStack(Material.HOPPER_MINECART,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.HOPPER_MINECART);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ディシジョン");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×7ブロック破壊 ×7"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：6.8秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：480"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ディシジョン");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×7ブロック破壊 ×7"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：6.8秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：480"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：90"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ヘヴン・ゲイボルグ"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(17,itemstack);


		//３列目
		if(playerdata.activeskilldata.breakskill >= 1){
			itemstack = new ItemStack(Material.GRASS,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "デュアル・ブレイク");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "1×2ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：1"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "デュアル・ブレイク");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "1×2ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：1"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：10"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：なし"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(18,itemstack);


		if(playerdata.activeskilldata.breakskill >= 2){
			itemstack = new ItemStack(Material.STONE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "トリアル・ブレイク");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×2ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：3"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "トリアル・ブレイク");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×2ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：3"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：20"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：デュアルブレイク"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(19,itemstack);

		if(playerdata.activeskilldata.breakskill >= 3){
			itemstack = new ItemStack(Material.COAL_ORE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エクスプロージョン");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：12"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エクスプロージョン");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：12"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：30"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：トリアルブレイク"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(20,itemstack);

		if(playerdata.activeskilldata.breakskill >= 4){
			itemstack = new ItemStack(Material.IRON_ORE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_ORE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ミラージュ・フレア");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "5×5×3ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0.7秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：30"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ミラージュ・フレア");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "5×5×3ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0.7秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：30"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：40"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：エクスプロージョン"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(21,itemstack);

		if(playerdata.activeskilldata.breakskill >= 5){
			itemstack = new ItemStack(Material.GOLD_ORE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_ORE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.GRAY + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ドッ・カーン");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×5ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.5秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：70"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.GRAY + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ドッ・カーン");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×5ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.5秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：70"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：50"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ミラージュ・フレア"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(22,itemstack);

		if(playerdata.activeskilldata.breakskill >= 6){
			itemstack = new ItemStack(Material.REDSTONE_ORE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_ORE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ギガンティック・ボム");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "9×9×7ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.5秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：100"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ギガンティック・ボム");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "9×9×7ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.5秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：100"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：60"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ドッ・カーン"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(23,itemstack);


		if(playerdata.activeskilldata.breakskill >= 7){
			itemstack = new ItemStack(Material.LAPIS_ORE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAPIS_ORE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブリリアント・デトネーション");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "11×11×9ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：3.5秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：200"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブリリアント・デトネーション");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "11×11×9ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：3.5秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：200"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：70"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ギガンティック・ボム"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(24,itemstack);

		if(playerdata.activeskilldata.breakskill >= 8){
			itemstack = new ItemStack(Material.EMERALD_ORE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "レムリア・インパクト");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "13×13×11ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：5秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：350"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "レムリア・インパクト");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "13×13×11ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：5秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：350"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：80"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ブリリアント・デトネーション"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(25,itemstack);

		if(playerdata.activeskilldata.breakskill >= 9){
			itemstack = new ItemStack(Material.DIAMOND_ORE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_ORE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エターナル・ヴァイス");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "15×15×13ブロック破壊"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：7秒"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：500"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エターナル・ヴァイス");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "15×15×13ブロック破壊"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：7秒"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：500"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：90"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：レムリア・インパクト"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(26,itemstack);

		//4列目
		if(playerdata.activeskilldata.watercondenskill >= 7){
			itemstack = new ItemStack(Material.SNOW_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SNOW_BLOCK);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホワイト・ブレス");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水7×7×7ブロックを凍らせます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：30"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホワイト・ブレス");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水7×7×7ブロックを凍らせます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：30"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：70"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：エクスプロージョン"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(30,itemstack);


		if(playerdata.activeskilldata.watercondenskill >= 8){
			itemstack = new ItemStack(Material.ICE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ICE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アブソリュート・ゼロ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水9×9×9ブロックを凍らせます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：80"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アブソリュート・ゼロ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水9×9×9ブロックを凍らせます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：80"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：80"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ホワイト・ブレス"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(31,itemstack);


		if(playerdata.activeskilldata.watercondenskill >= 9){
			itemstack = new ItemStack(Material.PACKED_ICE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PACKED_ICE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ダイアモンド・ダスト");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水11×11×11ブロックを凍らせます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：160"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ダイアモンド・ダスト");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水11×11×11ブロックを凍らせます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：160"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：90"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：アブソリュート・ゼロ"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(32,itemstack);


		//五行目
		if(playerdata.activeskilldata.lavacondenskill >= 7){
			itemstack = new ItemStack(Material.NETHERRACK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHERRACK);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ラヴァ・コンデンセーション");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩7×7×7ブロックを固めます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：20"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ラヴァ・コンデンセーション");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩7×7×7ブロックを固めます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：20"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：70"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：エクスプロージョン"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(39,itemstack);


		if(playerdata.activeskilldata.lavacondenskill >= 8){
			itemstack = new ItemStack(Material.NETHER_BRICK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "モエラキ・ボールダーズ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩9×9×9ブロックを固めます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：60"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "モエラキ・ボールダーズ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩9×9×9ブロックを固めます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：200"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：60"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ラヴァ・コンデンセーション"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(40,itemstack);


		if(playerdata.activeskilldata.lavacondenskill >= 9){
			itemstack = new ItemStack(Material.MAGMA,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MAGMA);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エルト・フェットル");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩11×11×11ブロックを固めます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：150"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エルト・フェットル");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩11×11×11ブロックを固めます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：150"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：90"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：モエラキ・ボールダーズ"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(41,itemstack);

		if(playerdata.activeskilldata.multiskill >= 9 && playerdata.activeskilldata.breakskill >= 9 && playerdata.activeskilldata.arrowskill >= 9 && playerdata.activeskilldata.watercondenskill >= 9 && playerdata.activeskilldata.lavacondenskill >= 9){
			itemstack = new ItemStack(Material.DIAMOND_CHESTPLATE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_CHESTPLATE);
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アサルト・アーマー");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲のブロック11×11×11を破壊します"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：600"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アサルト・アーマー");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲のブロック11×11×11を破壊します"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：600"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "全てのスキルを獲得すると解除されます");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(10,itemstack);

		if(playerdata.activeskilldata.watercondenskill >= 9 && playerdata.activeskilldata.lavacondenskill >= 9){
			if(playerdata.activeskilldata.fluidcondenskill == 10){
				itemstack = new ItemStack(Material.NETHER_STAR,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ヴェンダー・ブリザード");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水/熔岩11×11×11ブロックを固めます"
												, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
												, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：170"
												, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
			}else {
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ヴェンダー・ブリザード");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水/溶岩11×11×11ブロックを固めます"
						, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
						, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：170"
						, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：110"
						, ChatColor.RESET + "" +  ChatColor.DARK_RED + "水凝固/熔岩凝固の双方を扱える者にのみ発現する上位凝固スキル"
						, ChatColor.RESET + "" +  ChatColor.DARK_RED + "アサルト・アーマーの発現には影響しない"
						, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
			}
			inventory.setItem(28,itemstack);
		}


		return inventory;
	}

}
