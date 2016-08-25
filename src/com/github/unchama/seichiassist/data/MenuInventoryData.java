package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class MenuInventoryData {

	public static Inventory getMenuData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();



		int prank = Util.calcPlayerRank(player);
		int pblock = Util.calcMineBlock(player);
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.name + "の統計データ");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "整地レベル:" + playerdata.level
				, ChatColor.RESET + "" +  ChatColor.AQUA + "次のレベルまで:" + (SeichiAssist.levellist.get(playerdata.level + 1).intValue() - pblock)
				, ChatColor.RESET + "" +  ChatColor.GRAY + "パッシブスキル効果："
				, ChatColor.RESET + "" +  ChatColor.GRAY + "1ブロック破壊ごとに10%の確率で"
				, ChatColor.RESET + "" +  ChatColor.GRAY + DisplayPassiveExp(playerdata) + "の経験値を獲得します"
				, ChatColor.RESET + "" +  ChatColor.AQUA + "破壊したブロック数:" + pblock
				, ChatColor.RESET + "" +  ChatColor.GOLD + "ランキング：" + prank + "位" + ChatColor.RESET + "" +  ChatColor.GRAY + "(" + SeichiAssist.ranklist.size() +"人中)"
				));
		if(prank > 1){
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + (prank-1) + "位との差：" + (SeichiAssist.ranklist.get(prank-2).intValue() - pblock));
		}

		skullmeta.setLore(lore);
		skullmeta.setOwner(playerdata.name);
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(0,itemstack);

		//採掘速度上昇効果のトグルボタン
		itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
		itemstack.setItemMeta(DisplayEffect(playerdata));
		inventory.setItem(1,itemstack);

		// ver0.3.2 四次元ポケットOPEN
		itemstack = new ItemStack(Material.ENDER_PORTAL_FRAME,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_PORTAL_FRAME);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "四次元ポケットを開く");
		lore.clear();
		if( playerdata.level < SeichiAssist.config.getPassivePortalInventorylevel()){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "整地レベルが"+SeichiAssist.config.getPassivePortalInventorylevel()+ "以上必要です");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックすると開きます");
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(21,itemstack);

		// ver0.3.2 保護設定コマンド
		itemstack = new ItemStack(Material.GOLD_AXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_AXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護領域の申請");
		lore.clear();
		Selection selection = Util.getWorldEdit().getSelection(player);
		if (selection == null) {
			lore.addAll(Arrays.asList(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "範囲指定されてません"
					, ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "先に木の斧で2か所クリックしてネ"
					, ChatColor.DARK_GRAY + "Y座標は自動で全範囲保護されます"
					));
		}else{
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.addAll(Arrays.asList(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "範囲指定されています"
					, ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックすると保護を申請します"
					, ChatColor.DARK_GRAY + "Y座標は自動で全範囲保護されます"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "" + "A new region has been claimed"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "" + "named '" + player.getName() + "_" + playerdata.rgnum + "'."
					, ChatColor.RESET + "" +  ChatColor.GRAY + "と出れば、保護の設定が完了しています"
					, ChatColor.RESET + "" +  ChatColor.RED + "赤色で別の英文が出た場合"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "保護の設定に失敗しています"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "別の保護と被ってないか等ご確認の上"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "始めからやり直してください"
					));
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);

		/*
		 * ここまでadd.loreに変更済み
		 * 以下ボタンにadd.lore使う場合は追加行より上をすべてadd.loreに変更しないとエラー吐きます
		 */

		// 2ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "2ページ目へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowRight");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(35,itemstack);

		// ver0.3.2 homeコマンド
		itemstack = new ItemStack(Material.COMPASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームポイントにワープ");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "予め設定したホームポイントにワープできます"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとワープします"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(18,itemstack);

		// ver0.3.2 sethomeコマンド
		itemstack = new ItemStack(Material.BED,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在位置をホームポイントに設定");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると設定されます"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(19,itemstack);


		// ver0.3.2 //wandコマンド
		itemstack = new ItemStack(Material.WOOD_AXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WOOD_AXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護設定用の木の斧を召喚");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると召喚されます"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※インベントリを空けておこう"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "保護のかけ方"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "①召喚された斧を手に持ちます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "②保護したい領域の一方の角を" + ChatColor.YELLOW + "左" + ChatColor.GREEN + "クリック"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "③もう一方の対角線上の角を" + ChatColor.RED + "右" + ChatColor.GREEN + "クリック"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "④メニューの" + ChatColor.RESET + "" +  ChatColor.YELLOW + "木の斧" + ChatColor.RESET + "" +  ChatColor.GREEN + "をクリック"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(3,itemstack);

		// ver0.3.2 保護リスト表示
		itemstack = new ItemStack(Material.STONE_AXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE_AXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護リストを表示");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると表示"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "現在あなたが保護している"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "土地の一覧を表示します"
				, ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg remove 保護名"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "で保護の削除が出来ます"
				, ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg addmember 保護名 プレイヤー名"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "で該当保護にメンバーを追加出来ます"
				, ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg removemember 保護名 プレイヤー名"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "で該当保護のメンバーを削除出来ます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "その他のコマンドはWikiで確認して下さい"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(5,itemstack);


		// ver0.3.2 hubコマンド
		itemstack = new ItemStack(Material.NETHER_STAR,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ロビーサーバーへ移動");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると移動します"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(7,itemstack);

		// ver0.3.2 /spawnコマンド実行
		itemstack = new ItemStack(Material.BEACON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEACON);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スポーンワールドへワープ");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "・メインワールド"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・資源ワールド"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・整地ワールド"
				, ChatColor.RESET + "" + ChatColor.GRAY + "間を移動する時に使います"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとワープします");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8,itemstack);


		itemstack = new ItemStack(Material.COAL_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "デュアルブレイク");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "1×2マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getDualBreaklevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：1"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(10,itemstack);


		itemstack = new ItemStack(Material.IRON_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "トリアルブレイク");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×2マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル："  + SeichiAssist.config.getTrialBreaklevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：3"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);


		itemstack = new ItemStack(Material.GOLD_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エクスプロージョン");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getExplosionlevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：15"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(12,itemstack);


		itemstack = new ItemStack(Material.REDSTONE_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サンダーストーム");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3マス破壊×5"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getThunderStormlevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：30"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(13,itemstack);


		itemstack = new ItemStack(Material.LAPIS_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAPIS_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブリザード");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×5マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.5秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getBlizzardlevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：70"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(14,itemstack);


		itemstack = new ItemStack(Material.EMERALD_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "メテオ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "9*9*7マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：3秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getMeteolevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：100"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(15,itemstack);


		itemstack = new ItemStack(Material.DIAMOND_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "グラビティ");
		/*lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "16×16×256マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getGravitylevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：2000");*/

		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getGravitylevel()
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：???");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(16,itemstack);

		int gachaget = (int) playerdata.gachapoint/1000;
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ガチャ券を受け取る");
		if(gachaget != 0){
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "未獲得ガチャ券：" + gachaget + "枚"
			, ChatColor.RESET + "" +  ChatColor.AQUA + "次のガチャ券まで:" + (int)(1000 - playerdata.gachapoint%1000) + "ブロック");
		}else{
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得できるガチャ券はありません"
			, ChatColor.RESET + "" +  ChatColor.AQUA + "次のガチャ券まで:" + (int)(1000 - playerdata.gachapoint%1000) + "ブロック");
		}
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		itemstack = new ItemStack(Material.STONE_BUTTON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE_BUTTON);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ガチャ券受け取り方法");
		if(playerdata.gachaflag){
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "毎分受け取ります"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更"
					);
		}else {
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "後でまとめて受け取ります"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更"
					);
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(28,itemstack);

		return inventory;
	}

	public static Inventory getMenuData2(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//経験値変更用のクラスを設定
		ExperienceManager expman = new ExperienceManager(player);

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		// 自分の頭召喚
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "自分の頭を召喚");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "経験値10000を消費して"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "自分の頭を召喚します"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "装飾用にドウゾ！"
				));
		if(expman.hasExp(10000)){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで召喚");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "経験値が足りません");
		}
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_Villager");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(14,itemstack);

		/*
		 * ここまでadd.loreに変更済み
		 * 以下ボタンにadd.lore使う場合は追加行より上をすべてadd.loreに変更しないとエラー吐きます
		 */

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "1ページ目へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		//運営からの詫びガチャ配布ボタン
		int numofsorryforbug = (int) playerdata.numofsorryforbug;
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "運営からのガチャ券を受け取る");
		if(numofsorryforbug != 0){
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "不具合やイベント等により"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "運営チームから配布されるガチャ券は"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "このボタンから受け取れます"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "未獲得ガチャ券：" + numofsorryforbug + "枚");
		}else{
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "不具合やイベント等により"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "運営チームから配布されるガチャ券は"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "このボタンから受け取れます"
					, ChatColor.RESET + "" +  ChatColor.RED + "獲得できるガチャ券はありません");
		}
		skullmeta.setLore(lore);
		skullmeta.setOwner("whitecat_haru");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(16,itemstack);


		// ゴミ箱を開く
		itemstack = new ItemStack(Material.BUCKET,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BUCKET);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ゴミ箱を開く");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "不用品の大量処分にドウゾ！"
				, ChatColor.RESET + "" + ChatColor.RED + "復活しないので取扱注意"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(12,itemstack);

		// farmassist toggleコマンド
		itemstack = new ItemStack(Material.WHEAT,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SEEDS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "FarmAssist機能");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "ONにすると…"
				, ChatColor.RESET + "" + ChatColor.GRAY + "作物収穫時、手持ちの種や"
				, ChatColor.RESET + "" + ChatColor.GRAY + "苗を自動で植えてくれます"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでON,OFFを変更"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(9,itemstack);

		// treeassist toggleコマンド
		itemstack = new ItemStack(Material.LOG,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SAPLING);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "TreeAssist機能");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "ONにすると…"
				, ChatColor.RESET + "" + ChatColor.GRAY + "木の根元を切った時、自動で"
				, ChatColor.RESET + "" + ChatColor.GRAY + "木こり&苗木を植えてくれます"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでON,OFFを変更"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(10,itemstack);

		// ver0.3.2 wikiページ表示
		itemstack = new ItemStack(Material.BOOK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "公式Wikiにアクセス");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "鯖内の「困った」は公式Wikiで解決！"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		// ver0.3.2 運営方針とルールページを表示
		itemstack = new ItemStack(Material.PAPER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "運営方針とルールを確認");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "当鯖で遊ぶ前に確認してネ！"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);

		// ver0.3.2 鯖Mapを表示
		itemstack = new ItemStack(Material.MAP,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MAP);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "鯖Mapを見る");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "webブラウザから鯖Mapを閲覧出来ます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "他人の居場所や保護の場所を確認出来ます"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);

		// ver0.3.2 掲示板を表示
		itemstack = new ItemStack(Material.SIGN,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SIGN);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "掲示板を見る");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "管理人へのお問い合わせは"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "掲示板に書き込みをｵﾈｶﾞｲｼﾅｽ"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(3,itemstack);

		// ver0.3.2 投票ページ表示
		itemstack = new ItemStack(Material.BOOK_AND_QUILL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票ページにアクセス");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "投票すると様々な特典が！"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "1日1回投票出来ます"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);



		return inventory;
	}

	public static ItemMeta DisplayEffect(PlayerData playerdata){
		List<String> lore = new ArrayList<String>();
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "採掘速度上昇効果");
		if(playerdata.effectflag){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在ONです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "現在OFFです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックでON");
		}
		lore.addAll(
				Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "採掘速度上昇効果とは"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "接続人数と1分間の採掘量に応じて"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "採掘速度が変化するシステムです"
				, ChatColor.RESET + "" +  ChatColor.GOLD + "現在の採掘速度上昇Lv：" + (playerdata.minespeedlv+1)
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "上昇量の内訳"
				));
		for(EffectData ed : playerdata.effectdatalist){
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "" + ed.string/* + "(残" + Util.toTimeString(ed.duration/20) + ")"*/);
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}


	//パッシブスキルの獲得量表示
	private static int DisplayPassiveExp(PlayerData playerdata) {
		if(playerdata.level < 8){
			return 0;
		}else if (playerdata.level < 18){
			return SeichiAssist.config.getDropExplevel1();
		}else if (playerdata.level < 28){
			return SeichiAssist.config.getDropExplevel2();
		}else if (playerdata.level < 38){
			return SeichiAssist.config.getDropExplevel3();
		}else if (playerdata.level < 48){
			return SeichiAssist.config.getDropExplevel4();
		}else if (playerdata.level < 58){
			return SeichiAssist.config.getDropExplevel5();
		}else if (playerdata.level < 68){
			return SeichiAssist.config.getDropExplevel6();
		}else if (playerdata.level < 78){
			return SeichiAssist.config.getDropExplevel7();
		}else if (playerdata.level < 88){
			return SeichiAssist.config.getDropExplevel8();
		}else if (playerdata.level < 98){
			return SeichiAssist.config.getDropExplevel9();
		}else{
			return SeichiAssist.config.getDropExplevel10();
		}
	}
}
