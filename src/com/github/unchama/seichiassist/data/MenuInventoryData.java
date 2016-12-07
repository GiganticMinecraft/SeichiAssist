package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class MenuInventoryData {
	static HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	static Sql sql = SeichiAssist.sql;
	SeichiAssist plugin = SeichiAssist.plugin;
	//1ページ目メニュー
	public static Inventory getMenuData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			Util.sendPlayerDataNullMessage(p);
			Bukkit.getLogger().warning(p.getName() + " -> PlayerData not found.");
			Bukkit.getLogger().warning("MenuInventoryData.getMenuData");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();



		int prank = playerdata.calcPlayerRank(player);
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.name + "の統計データ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "整地レベル:" + playerdata.level);
		if(playerdata.level < SeichiAssist.levellist.size()){
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "次のレベルまで:" + (SeichiAssist.levellist.get(playerdata.level).intValue() - playerdata.totalbreaknum));
		}
		//整地ワールド外では整地数が反映されない
		if(!Util.isGainSeichiExp(p)){
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "整地ワールド以外では");
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "整地量とガチャ券は増えません");
		}
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "パッシブスキル効果："
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "1ブロック整地ごとに"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "10%の確率で"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + playerdata.dispPassiveExp() + "のマナを獲得"
				, ChatColor.RESET + "" +  ChatColor.AQUA + "総整地量:" + playerdata.totalbreaknum
				, ChatColor.RESET + "" +  ChatColor.GOLD + "ランキング：" + prank + "位" + ChatColor.RESET + "" +  ChatColor.GRAY + "(" + SeichiAssist.ranklist.size() +"人中)"
				));
		if(prank > 1){
			RankData rankdata = SeichiAssist.ranklist.get(prank-2);
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + (prank-1) + "位("+ rankdata.name +")との差：" + (rankdata.totalbreaknum - playerdata.totalbreaknum));
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "総ログイン時間：" + Util.toTimeString(Util.toSecond(playerdata.playtick)));
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※1分毎に更新");
		lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "統計データは");
		lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "各サバイバルサーバー間で");
		lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "共有されます");

		skullmeta.setLore(lore);
		skullmeta.setOwner(playerdata.name);
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(0,itemstack);

		//採掘速度上昇効果のトグルボタン
		itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "採掘速度上昇効果");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(EFButtonMeta(playerdata,itemmeta));
		inventory.setItem(1,itemstack);

		// ver0.3.2 四次元ポケットOPEN
		itemstack = new ItemStack(Material.ENDER_PORTAL_FRAME,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_PORTAL_FRAME);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "四次元ポケットを開く");
		lore.clear();
		if( playerdata.level < SeichiAssist.config.getPassivePortalInventorylevel()){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "整地レベルが"+SeichiAssist.config.getPassivePortalInventorylevel()+ "以上必要です");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "ポケットサイズ:" + playerdata.inventory.getSize() + "スタック");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで開く");
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※四次元ポケットの中身は");
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "各サバイバルサーバー間で");
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "共有されます");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(21,itemstack);

		// どこでもエンダーチェスト
		itemstack = new ItemStack(Material.ENDER_CHEST,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_CHEST);
		itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "どこでもエンダーチェスト");
		lore.clear();
		if( playerdata.level < SeichiAssist.config.getPassivePortalInventorylevel()){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "整地レベルが"+SeichiAssist.config.getDokodemoEnderlevel()+ "以上必要です");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで開く");
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(22,itemstack);

		// ver0.3.2 保護設定コマンド
		itemstack = new ItemStack(Material.GOLD_AXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_AXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護の申請");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		lore.clear();

		Selection selection = Util.getWorldEdit().getSelection(player);

		if(!player.hasPermission("worldguard.region.claim")){
			lore.addAll(Arrays.asList(ChatColor.RED + "このワールドでは"
					, ChatColor.RED + "保護を申請出来ません"
					));
		}else if (selection == null) {
			lore.addAll(Arrays.asList(ChatColor.RED + "範囲指定されてません"
					, ChatColor.RED + "先に木の斧で2か所クリックしてネ"
					));
		}else if(selection.getLength() < 10||selection.getWidth() < 10){
			lore.addAll(Arrays.asList(ChatColor.RED + "選択された範囲が狭すぎます"
					, ChatColor.RED + "1辺当たり最低10ブロック以上にしてネ"
					));
		}else{
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.addAll(Arrays.asList(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "範囲指定されています"
					, ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックすると保護を申請します"
					));
		}

		if(player.hasPermission("worldguard.region.claim")){
			lore.addAll(Arrays.asList(ChatColor.DARK_GRAY + "Y座標は自動で全範囲保護されます"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "" + "A new region has been claimed"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "" + "named '" + player.getName() + "_" + playerdata.rgnum + "'."
					, ChatColor.RESET + "" +  ChatColor.GRAY + "と出れば保護設定完了です"
					, ChatColor.RESET + "" +  ChatColor.RED + "赤色で別の英文が出た場合"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "保護の設定に失敗しています"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "・別の保護と被っていないか"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "・保護数上限に達していないか"
					, ChatColor.RESET + "" +  ChatColor.GRAY + "確認してください"
					));
		}

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);

		// MineStackを開く
		itemstack = new ItemStack(Material.CHEST,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack機能");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "説明しよう!MineStackとは…"
				, ChatColor.RESET + "" + "主要ブロックを無限にスタック出来る!"
				, ChatColor.RESET + "" + "スタックしたアイテムは"
				, ChatColor.RESET + "" + "ここから取り出せるゾ!"
				));
		if( playerdata.level < SeichiAssist.config.getMineStacklevel(1)){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "整地レベルが"+SeichiAssist.config.getMineStacklevel(1)+ "以上必要です");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで開く");
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※スタックしたブロックは");
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "各サバイバルサーバー間で");
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "共有されます");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(24,itemstack);

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

		// 整地神番付を開く
		itemstack = new ItemStack(Material.COOKIE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキングを見る");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(33,itemstack);

		//運営からの詫びガチャ配布ボタン
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "運営からのガチャ券を受け取る");
		skullmeta.setLore(SorryGachaGetButtonLore(playerdata));
		skullmeta.setOwner("whitecat_haru");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(29,itemstack);

		//投票特典受け取りボタン
		itemstack = new ItemStack(Material.DIAMOND,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND);
		itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票特典を受け取る");
		itemmeta.setLore(VoteGetButtonLore(playerdata));
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(30,itemstack);



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
		inventory.setItem(26,itemstack);

		// 不要ガチャ景品交換システムを開く
		itemstack = new ItemStack(Material.NOTE_BLOCK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NOTE_BLOCK);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "不要ガチャ景品交換システム");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "不必要な当たり、大当たり景品を"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "ガチャ券と交換出来ます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "出てきたインベントリ―に"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "交換したい景品を入れて"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "escキーを押してください"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "たまにアイテムが消失するから"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "大事なものはいれないでネ"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(31,itemstack);

		// ver0.3.2 homeコマンド
		itemstack = new ItemStack(Material.COMPASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームポイントにワープ");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "あらかじめ設定した"
				, ChatColor.RESET + "" + ChatColor.GRAY + "ホームポイントにワープします"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "うまく機能しない時は"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "再接続してみてください"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでワープ"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/home]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(18,itemstack);

		// ver0.3.2 sethomeコマンド
		itemstack = new ItemStack(Material.BED,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームポイントを設定");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "現在位置をホームポイント"
				, ChatColor.RESET + "" + ChatColor.GRAY + "として設定します"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※上書きされます"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(19,itemstack);


		// ver0.3.2 //wandコマンド
		itemstack = new ItemStack(Material.WOOD_AXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WOOD_AXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護設定用の木の斧を召喚");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで召喚"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※インベントリを空けておこう"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "保護のかけ方"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "①召喚された斧を手に持ちます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "②保護したい領域の一方の角を" + ChatColor.YELLOW + "左" + ChatColor.GREEN + "クリック"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "③もう一方の対角線上の角を" + ChatColor.RED + "右" + ChatColor.GREEN + "クリック"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "④メニューの" + ChatColor.RESET + "" +  ChatColor.YELLOW + "金の斧" + ChatColor.RESET + "" +  ChatColor.GREEN + "をクリック"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[//wand]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(3,itemstack);

		// ver0.3.2 保護リスト表示
		itemstack = new ItemStack(Material.STONE_AXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE_AXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護一覧を表示");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで表示"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "今いるワールドで"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "あなたが保護している"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "土地の一覧を表示します"
				, ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg info 保護名"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "該当保護の詳細情報を表示"
				, ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg rem 保護名"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "該当保護を削除する"
				, ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg addmem 保護名 プレイヤー名"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "該当保護に指定メンバーを追加"
				, ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg removemember 保護名 プレイヤー名"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "該当保護の指定メンバーを削除"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "その他のコマンドはWikiを参照"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/rg list]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(5,itemstack);

		// RegionGUIリンク
		itemstack = new ItemStack(Material.DIAMOND_AXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_AXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "RegionGUI機能");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				, ChatColor.RESET + "" +  ChatColor.RED + "保護の作成と管理が超簡単に！"
				, ChatColor.RESET + "" +  ChatColor.RED + "クリックした場所によって挙動が変わります"
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "自分の所有する保護内なら…"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "保護の各種設定や削除が行えます"
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "それ以外なら…"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "新規保護の作成画面が表示されます"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/land]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(6,itemstack);

		// fastcraftリンク
		itemstack = new ItemStack(Material.WORKBENCH,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WORKBENCH);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "FastCraft機能");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				, ChatColor.RESET + "" +  ChatColor.RED + "ただの作業台じゃないんです…"
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "自動レシピ補完機能付きの"
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "最強な作業台はこちら"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/fc craft]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(23,itemstack);


		// ver0.3.2 /spawnコマンド実行
		itemstack = new ItemStack(Material.BEACON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEACON);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スポーンワールドへワープ");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "・メインワールド"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・資源ワールド"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・整地ワールド"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・ロビーサーバー"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・各サバイバルサーバー"
				, ChatColor.RESET + "" + ChatColor.GRAY + "間を移動する時に使います"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとワープします"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/spawn]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8,itemstack);


		//パッシブスキルツリー
		itemstack = new ItemStack(Material.ENCHANTED_BOOK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
		itemmeta.addEnchant(Enchantment.DURABILITY, 100, false);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "パッシブスキルブック");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "整地に便利なスキルを使用できるゾ"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでスキル一覧を開く");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);


		//アクティブスキルツリー
		itemstack = new ItemStack(Material.ENCHANTED_BOOK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アクティブスキルブック");
		//整地ワールド外では整地スキルが発動しない
		if(!Util.isSkillEnable(p)){
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "このワールドでは"
					,ChatColor.RESET + "" +  ChatColor.RED + "整地スキルを使えません");
		}else{
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "整地に便利なスキルを使用できるゾ"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでスキル一覧を開く");
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(13,itemstack);



		//ガチャ券受け取りボタン
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		itemstack.setDurability((short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skullmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地報酬ガチャ券を受け取る");
		skullmeta.setLore(GachaGetButtonLore(playerdata));
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		//ガチャ券受け取り方法選択ボタン
		itemstack = new ItemStack(Material.STONE_BUTTON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE_BUTTON);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地報酬ガチャ券受け取り方法");
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
	//2ページメニュー
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
		inventory.setItem(12,itemstack);

		//死亡メッセージ表示のトグルボタン
		itemstack = new ItemStack(Material.FLINT_AND_STEEL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.FLINT_AND_STEEL);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "死亡メッセージ表示切替");
		itemstack.setItemMeta(dispKillLogToggleMeta(playerdata,itemmeta));
		inventory.setItem(14,itemstack);

		//ワールドガード保護表示のトグルボタン
		itemstack = new ItemStack(Material.BARRIER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ワールドガード保護メッセージ表示切替");
		//itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(dispWorldGuardLogToggleMeta(playerdata,itemmeta));
		inventory.setItem(15,itemstack);


		// ver0.3.2 hubコマンド
		itemstack = new ItemStack(Material.NETHER_STAR,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ロビーサーバーへ移動");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると移動します"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/hub]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8,itemstack);

		/*
		//PvPのトグルボタン
		itemstack = new ItemStack(Material.IRON_SWORD,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_SWORD);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "PvP切替");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(dispPvPToggleMeta(playerdata,itemmeta));
		inventory.setItem(15,itemstack);
		*/

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

		//サブホーム関係
		for(int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
			//サブホームに移動ボタン
			itemstack = new ItemStack(Material.COMPASS,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント"+  (x+1) + "にワープ");

			Location l = playerdata.GetSubHome(x);
			if (l == null ){
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "あらかじめ設定した"
						, ChatColor.RESET + "" + ChatColor.GRAY + "サブホームポイント" + (x+1) + "にワープします"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "うまく機能しない時は"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "再接続してみてください"
						, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでワープ"
						, ChatColor.RESET + "" + ChatColor.GRAY + "未設定"
						);
			}else{
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "あらかじめ設定した"
						, ChatColor.RESET + "" + ChatColor.GRAY + "サブホームポイント" + (x+1) + "にワープします"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "うまく機能しない時は"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "再接続してみてください"
						, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでワープ"
						, ChatColor.RESET + "" + ChatColor.GRAY + "" + l.getWorld().getName() + " x:" + (int)l.getX() + " y:" + (int)l.getY() + " z:" + (int)l.getZ()
						);
			}
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(29+x,itemstack);

			//サブホーム設定ボタン
			itemstack = new ItemStack(Material.BED,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント" + (x+1) + "を設定");
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "現在位置をサブホームポイント" + (x+1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "として設定します"
					, ChatColor.RESET + "" + ChatColor.	DARK_GRAY + "※上書きされます"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(20+x,itemstack);

		}

		return inventory;
	}
	//パッシブスキルメニュー
	public static Inventory getPassiveSkillMenuData(Player p){

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキル切り替え");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		//プレイヤーを取得
		Player player = p.getPlayer();

		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

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
		inventory.setItem(27,itemstack);

		//複数種類同時破壊スキルのトグルボタン
		itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "複数種類同時破壊スキル切替");
		//itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(MultipleIDBlockBreakToggleMeta(playerdata,itemmeta));
		inventory.setItem(0,itemstack);

		/*
		itemstack = new ItemStack(Material.COAL_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "デュアル・ブレイク");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "1×2マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getDualBreaklevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：1"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);


		itemstack = new ItemStack(Material.IRON_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "トリアル・ブレイク");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×2マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0.1秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル："  + SeichiAssist.config.getTrialBreaklevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：3"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);


		itemstack = new ItemStack(Material.GOLD_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エクスプロージョン");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0.1秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getExplosionlevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：15"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);

		itemstack = new ItemStack(Material.PACKED_ICE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PACKED_ICE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "フリーズ");
		 */
		/*lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "16×16×256マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getGravitylevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：2000");*/
		/*
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "水を凍らせる"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：35"
				, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(3,itemstack);


		itemstack = new ItemStack(Material.REDSTONE_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サンダーストーム");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3マス破壊×5"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getThunderStormlevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：40"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);


		itemstack = new ItemStack(Material.MAGMA,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MAGMA);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "メテオフリーズ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "溶岩を固める"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：45"
				, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(5,itemstack);


		itemstack = new ItemStack(Material.LAPIS_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAPIS_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブリザード");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×5マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：1.5秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getBlizzardlevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：70"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(6,itemstack);


		itemstack = new ItemStack(Material.ARROW,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ARROW);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エビフライ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "遠距離3*3*3"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：55"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(7,itemstack);

		itemstack = new ItemStack(Material.EMERALD_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "メテオ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "9*9*7マス破壊"
										, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.5秒"
										, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getMeteolevel()
										, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：100"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8,itemstack);


		itemstack = new ItemStack(Material.DIAMOND_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_ORE);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "グラビティ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "11*11*9"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：70"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(9,itemstack);

		itemstack = new ItemStack(Material.SNOW_BALL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SNOW_BALL);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アブソリュート");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "遠距離7*7*5"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：75"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(10,itemstack);


		itemstack = new ItemStack(Material.OBSIDIAN,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.OBSIDIAN);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブラックホール");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "13*13*11"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：80"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);


		itemstack = new ItemStack(Material.FIREBALL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.FIREBALL);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Air-K");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "遠距離9*9*7"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：85"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(12,itemstack);

		itemstack = new ItemStack(Material.GRASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "普通のパンチ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "15*15*13"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：90"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(13,itemstack);


		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 1);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ティロフィナーレ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "遠距離11*11*9"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：95"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(14,itemstack);


		itemstack = new ItemStack(Material.BEDROCK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "WORLD IS MINE");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "9*9*256"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：100"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(15,itemstack);


		itemstack = new ItemStack(Material.COAL_BLOCK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_BLOCK);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ギガンティックブラックホール");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "遠距離13*13*11"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：105"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(16,itemstack);

		itemstack = new ItemStack(Material.NETHER_STAR,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スターライトブレイカー");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "遠距離11*11*9*5"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：115"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(17,itemstack);


		itemstack = new ItemStack(Material.END_CRYSTAL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.END_CRYSTAL);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アサルトアーマー");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "未実装"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "パッシブ周囲ブロック破壊"
		, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：200"
		, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：???"
		, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");

		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(18,itemstack);
		*/

		return inventory;
	}
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキル選択");
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
		inventory.setItem(27,itemstack);

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
		if(playerdata.activeskilldata.condenskill >= 4){
			itemstack = new ItemStack(Material.SNOW_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SNOW_BLOCK);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホワイト・ブレス");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水7×7×7ブロックを凍らせます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：40"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホワイト・ブレス");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水7×7×7ブロックを凍らせます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：40"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：40"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：エクスプロージョン"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(30,itemstack);


		if(playerdata.activeskilldata.condenskill >= 5){
			itemstack = new ItemStack(Material.ICE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ICE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アブソリュート・ゼロ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水9×9×9ブロックを凍らせます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：70"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アブソリュート・ゼロ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水9×9×9ブロックを凍らせます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：70"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：50"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ホワイト・ブレス"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(31,itemstack);


		if(playerdata.activeskilldata.condenskill >= 6){
			itemstack = new ItemStack(Material.PACKED_ICE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PACKED_ICE);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ダイアモンド・ダスト");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水11×11×11ブロックを凍らせます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：140"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ダイアモンド・ダスト");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の水11×11×11ブロックを凍らせます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：140"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：60"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：アブソリュート・ゼロ"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(32,itemstack);


		if(playerdata.activeskilldata.condenskill >= 7){
			itemstack = new ItemStack(Material.NETHERRACK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHERRACK);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ラヴァ・コンデンセーション");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩7×7×7ブロックを固めます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：80"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ラヴァ・コンデンセーション");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩7×7×7ブロックを固めます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：80"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：70"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ダイアモンド・ダスト"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(33,itemstack);


		if(playerdata.activeskilldata.condenskill >= 8){
			itemstack = new ItemStack(Material.NETHER_BRICK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "モエラキ・ボールダーズ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩9×9×9ブロックを固めます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：150"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "モエラキ・ボールダーズ");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩9×9×9ブロックを固めます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：150"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：80"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：ラヴァ・コンデンセーション"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(34,itemstack);


		if(playerdata.activeskilldata.condenskill >= 9){
			itemstack = new ItemStack(Material.MAGMA,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MAGMA);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エルト・フェットル");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩11×11×11ブロックを固めます"
											, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
											, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：300"
											, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エルト・フェットル");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "周囲の溶岩11×11×11ブロックを固めます"
					, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：なし"
					, ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：300"
					, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要アクティブスキルポイント：90"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "前提スキル：モエラキ・ボールダーズ"
					, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
		}
		inventory.setItem(35,itemstack);

		if(playerdata.activeskilldata.multiskill >= 9 && playerdata.activeskilldata.breakskill >= 9 && playerdata.activeskilldata.arrowskill >= 9 && playerdata.activeskilldata.condenskill >= 9){
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

		return inventory;
	}
	// 採掘速度トグルボタン
	public static ItemMeta EFButtonMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<String>();
		if(playerdata.effectflag){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在ONです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
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
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "" + ed.EDtoString(ed.id,ed.duration,ed.amplifier));
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}
	// ガチャ券受け取りボタン
	public static List<String> GachaGetButtonLore(PlayerData playerdata){
		List<String> lore = new ArrayList<String>();
		int gachaget = (int) playerdata.gachapoint/1000;
		if(gachaget != 0){
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "未獲得ガチャ券：" + gachaget + "枚"
			, ChatColor.RESET + "" +  ChatColor.AQUA + "次のガチャ券まで:" + (int)(1000 - playerdata.gachapoint%1000) + "ブロック");
		}else{
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得できるガチャ券はありません"
			, ChatColor.RESET + "" +  ChatColor.AQUA + "次のガチャ券まで:" + (int)(1000 - playerdata.gachapoint%1000) + "ブロック");
		}
		return lore;
	}
	//運営ガチャ券受け取りボタン
	public static List<String> SorryGachaGetButtonLore(PlayerData playerdata){
		List<String> lore = new ArrayList<String>();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "運営からのガチャ券を受け取ります"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "以下の場合に配布されます"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "・各種不具合のお詫びとして"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "・イベント景品として"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "・各種謝礼として"));
		int gachaget = playerdata.numofsorryforbug;
		if(gachaget != 0){
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "未獲得ガチャ券：" + gachaget + "枚");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "獲得できるガチャ券はありません");
		}
		return lore;
	}

	//投票特典受け取りボタン
	public static List<String> VoteGetButtonLore(PlayerData playerdata){
		List<String> lore = new ArrayList<String>();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "投票特典を受け取るには"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "投票ページで投票した後"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "このボタンをクリックします"));
		lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "特典受取済投票回数：" + playerdata.p_givenvote);
		return lore;
	}



	//Minestack1ページ目
	public static Inventory getMineStackMenu(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		//インベントリ作成
		//Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack");
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack");

		ItemStack itemstack;
		ItemMeta itemmeta;

		//MineStack機能のトグルボタン
		itemstack = new ItemStack(Material.IRON_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "対象ブロック自動スタック機能");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(MineStackToggleMeta(playerdata,itemmeta));
		inventory.setItem(0,itemstack);

		int v1 = SeichiAssist.config.getMineStacklevel(1);
		int v2 = SeichiAssist.config.getMineStacklevel(2);
		int v3 = SeichiAssist.config.getMineStacklevel(3);
		int v4 = SeichiAssist.config.getMineStacklevel(4);
		int v5 = SeichiAssist.config.getMineStacklevel(5);
		int v6 = SeichiAssist.config.getMineStacklevel(6);
		int v7 = SeichiAssist.config.getMineStacklevel(7);
		int v8 = SeichiAssist.config.getMineStacklevel(8);
		int v9 = SeichiAssist.config.getMineStacklevel(9);
		int v10 = SeichiAssist.config.getMineStacklevel(10);
		int v11 = SeichiAssist.config.getMineStacklevel(11);//追加
		int v12 = SeichiAssist.config.getMineStacklevel(12);//追加
		int v13 = SeichiAssist.config.getMineStacklevel(13);//追加
		int v14 = SeichiAssist.config.getMineStacklevel(14);//追加
		int v15 = SeichiAssist.config.getMineStacklevel(15);//追加
		int v16 = SeichiAssist.config.getMineStacklevel(16);
		int v17 = SeichiAssist.config.getMineStacklevel(17);
		int v18 = SeichiAssist.config.getMineStacklevel(18);
		int v19 = SeichiAssist.config.getMineStacklevel(19);
		int v20 = SeichiAssist.config.getMineStacklevel(20);
		int v21 = SeichiAssist.config.getMineStacklevel(21);
		int v22 = SeichiAssist.config.getMineStacklevel(22);
		int v23 = SeichiAssist.config.getMineStacklevel(23);
		int v24 = SeichiAssist.config.getMineStacklevel(24);
		int v25 = SeichiAssist.config.getMineStacklevel(25);
		int v26 = SeichiAssist.config.getMineStacklevel(26);
		int v27 = SeichiAssist.config.getMineStacklevel(27);
		int v28 = SeichiAssist.config.getMineStacklevel(28);
		int v29 = SeichiAssist.config.getMineStacklevel(29);
		int v30 = SeichiAssist.config.getMineStacklevel(30);
		int v31 = SeichiAssist.config.getMineStacklevel(31);
		int v32 = SeichiAssist.config.getMineStacklevel(32);
		int v33 = SeichiAssist.config.getMineStacklevel(33);
		int v34 = SeichiAssist.config.getMineStacklevel(34);
		int v35 = SeichiAssist.config.getMineStacklevel(35);
		int v36 = SeichiAssist.config.getMineStacklevel(36);
		int v37 = SeichiAssist.config.getMineStacklevel(37);
		int v38 = SeichiAssist.config.getMineStacklevel(38);

		//1から

		setMineStackButton(inventory, playerdata.minestack.dirt, new ItemStack(Material.DIRT, 1, (short)0), v1, 1, "土");
		setMineStackButton(inventory, playerdata.minestack.grass, Material.GRASS, v1, 2, "草ブロック");
		setMineStackButton(inventory, playerdata.minestack.cobblestone, Material.COBBLESTONE, v2, 3, "丸石");
		setMineStackButton(inventory, playerdata.minestack.stone, new ItemStack(Material.STONE, 1, (short)0), v2, 4, "石");
		setMineStackButton(inventory, playerdata.minestack.granite, new ItemStack(Material.STONE, 1, (short)1), v3, 5, "花崗岩");
		setMineStackButton(inventory, playerdata.minestack.diorite, new ItemStack(Material.STONE, 1, (short)3), v3, 6, "閃緑岩");
		setMineStackButton(inventory, playerdata.minestack.andesite, new ItemStack(Material.STONE, 1, (short)5), v3, 7, "安山岩");
		setMineStackButton(inventory, playerdata.minestack.log, new ItemStack(Material.LOG, 1, (short)0), v4, 8, "オークの原木");
		setMineStackButton(inventory, playerdata.minestack.log1, new ItemStack(Material.LOG, 1, (short)1), v4, 9, "マツの原木");
		setMineStackButton(inventory, playerdata.minestack.log2, new ItemStack(Material.LOG, 1, (short)2), v4, 10, "シラカバの原木");
		setMineStackButton(inventory, playerdata.minestack.log3, new ItemStack(Material.LOG, 1, (short)3), v4, 11, "ジャングルの原木");
		setMineStackButton(inventory, playerdata.minestack.log_2, new ItemStack(Material.LOG_2, 1, (short)0), v4, 12, "アカシアの原木");
		setMineStackButton(inventory, playerdata.minestack.log_21, new ItemStack(Material.LOG_2, 1, (short)1), v4, 13, "ダークオークの原木");
		setMineStackButton(inventory, playerdata.minestack.gravel, Material.GRAVEL, v5, 14, "砂利");
		setMineStackButton(inventory, playerdata.minestack.sand, new ItemStack(Material.SAND, 1, (short)0),v5, 15, "砂");
		setMineStackButton(inventory, playerdata.minestack.sandstone, new ItemStack(Material.SANDSTONE, 1, (short)0), v5, 16, "砂岩");
		setMineStackButton(inventory, playerdata.minestack.netherrack, Material.NETHERRACK, v6, 17, "ネザーラック");
		setMineStackButton(inventory, playerdata.minestack.soul_sand, Material.SOUL_SAND, v6, 18, "ソウルサンド");
		setMineStackButton(inventory, playerdata.minestack.coal, new ItemStack(Material.COAL, 1, (short)0), v7, 19, "石炭");
		setMineStackButton(inventory, playerdata.minestack.coal_ore, Material.COAL_ORE, v7, 20, "石炭鉱石");
		setMineStackButton(inventory, playerdata.minestack.ender_stone, Material.ENDER_STONE, v8, 21, "エンドストーン");
		setMineStackButton(inventory, playerdata.minestack.iron_ore, Material.IRON_ORE, v9, 22, "鉄鉱石");
		setMineStackButton(inventory, playerdata.minestack.obsidian, Material.OBSIDIAN, v9, 23, "黒曜石");
		setMineStackButton(inventory, playerdata.minestack.packed_ice, Material.PACKED_ICE,v10, 24, "氷塊");
		setMineStackButton(inventory, playerdata.minestack.quartz, Material.QUARTZ, v11, 25, "ネザー水晶");
		setMineStackButton(inventory, playerdata.minestack.quartz_ore, Material.QUARTZ_ORE, v11, 26, "ネザー水晶鉱石");
		setMineStackButton(inventory, playerdata.minestack.magma, Material.MAGMA, v12, 27, "マグマブロック");
		setMineStackButton(inventory, playerdata.minestack.gold_ore, Material.GOLD_ORE, v13, 28, "金鉱石");
		setMineStackButton(inventory, playerdata.minestack.glowstone, Material.GLOWSTONE, v13, 29, "グロウストーン");
		setMineStackButton(inventory, playerdata.minestack.wood, new ItemStack(Material.WOOD, 1, (short)0), v14, 30, "オークの木材");
		setMineStackButton(inventory, playerdata.minestack.fence, Material.FENCE, v14, 31, "オークのフェンス");
		setMineStackButton(inventory, playerdata.minestack.redstone, Material.REDSTONE, v15, 32, "レッドストーン");
		setMineStackButton(inventory, playerdata.minestack.redstone_ore, Material.REDSTONE_ORE, v15, 33, "レッドストーン鉱石");
		setMineStackButton(inventory, playerdata.minestack.lapis_lazuli, new ItemStack(Material.INK_SACK, 1, (short)4), v16, 34, "ラピスラズリ");
		setMineStackButton(inventory, playerdata.minestack.lapis_ore, Material.LAPIS_ORE, v16, 35, "ラピスラズリ鉱石");
		setMineStackButton(inventory, playerdata.minestack.diamond, Material.DIAMOND, v17, 36, "ダイヤモンド");
		setMineStackButton(inventory, playerdata.minestack.diamond_ore, Material.DIAMOND_ORE, v17, 37, "ダイヤモンド鉱石");
		setMineStackButton(inventory, playerdata.minestack.emerald, Material.EMERALD, v18, 38, "エメラルド");
		setMineStackButton(inventory, playerdata.minestack.emerald_ore, Material.EMERALD_ORE, v18, 39, "エメラルド鉱石");
		setMineStackButton(inventory, playerdata.minestack.gachaimo, new ItemStack(Material.GOLDEN_APPLE, 1, (short)0), v19, 40, "がちゃりんご");
		setMineStackButton(inventory, playerdata.minestack.exp_bottle, Material.EXP_BOTTLE, v19, 41, "エンチャントの瓶");
		setMineStackButton(inventory, playerdata.minestack.red_sand, new ItemStack(Material.SAND, 1, (short)1),v20, 42, "赤い砂");
		setMineStackButton(inventory, playerdata.minestack.red_sandstone, new ItemStack(Material.RED_SANDSTONE, 1, (short)0), v20, 43, "赤い砂岩");
		setMineStackButton(inventory, playerdata.minestack.hard_clay, Material.HARD_CLAY, v21, 44, "堅焼き粘土");
		//44まで


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(45,itemstack);

		// MineStack2ページ目を開く(追加)
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack2ページ目へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(53,itemstack);


		return inventory;
	}

	//追加(Minestack2ページ目)
	public static Inventory getMineStackMenu2(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		//インベントリ作成
		//Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack");
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack");

		ItemStack itemstack;
		ItemMeta itemmeta;

		int v1 = SeichiAssist.config.getMineStacklevel(1);
		int v2 = SeichiAssist.config.getMineStacklevel(2);
		int v3 = SeichiAssist.config.getMineStacklevel(3);
		int v4 = SeichiAssist.config.getMineStacklevel(4);
		int v5 = SeichiAssist.config.getMineStacklevel(5);
		int v6 = SeichiAssist.config.getMineStacklevel(6);
		int v7 = SeichiAssist.config.getMineStacklevel(7);
		int v8 = SeichiAssist.config.getMineStacklevel(8);
		int v9 = SeichiAssist.config.getMineStacklevel(9);
		int v10 = SeichiAssist.config.getMineStacklevel(10);
		int v11 = SeichiAssist.config.getMineStacklevel(11);//追加
		int v12 = SeichiAssist.config.getMineStacklevel(12);//追加
		int v13 = SeichiAssist.config.getMineStacklevel(13);//追加
		int v14 = SeichiAssist.config.getMineStacklevel(14);//追加
		int v15 = SeichiAssist.config.getMineStacklevel(15);//追加
		int v16 = SeichiAssist.config.getMineStacklevel(16);
		int v17 = SeichiAssist.config.getMineStacklevel(17);
		int v18 = SeichiAssist.config.getMineStacklevel(18);
		int v19 = SeichiAssist.config.getMineStacklevel(19);
		int v20 = SeichiAssist.config.getMineStacklevel(20);
		int v21 = SeichiAssist.config.getMineStacklevel(21);
		int v22 = SeichiAssist.config.getMineStacklevel(22);
		int v23 = SeichiAssist.config.getMineStacklevel(23);
		int v24 = SeichiAssist.config.getMineStacklevel(24);
		int v25 = SeichiAssist.config.getMineStacklevel(25);
		int v26 = SeichiAssist.config.getMineStacklevel(26);
		int v27 = SeichiAssist.config.getMineStacklevel(27);
		int v28 = SeichiAssist.config.getMineStacklevel(28);
		int v29 = SeichiAssist.config.getMineStacklevel(29);
		int v30 = SeichiAssist.config.getMineStacklevel(30);
		int v31 = SeichiAssist.config.getMineStacklevel(31);
		int v32 = SeichiAssist.config.getMineStacklevel(32);
		int v33 = SeichiAssist.config.getMineStacklevel(33);
		int v34 = SeichiAssist.config.getMineStacklevel(34);
		int v35 = SeichiAssist.config.getMineStacklevel(35);
		int v36 = SeichiAssist.config.getMineStacklevel(36);
		int v37 = SeichiAssist.config.getMineStacklevel(37);
		int v38 = SeichiAssist.config.getMineStacklevel(38);


		//0から
		setMineStackButton(inventory, playerdata.minestack.stained_clay, new ItemStack(Material.STAINED_CLAY, 1, (short)0), v22, 0, "白色の堅焼き粘土");
		setMineStackButton(inventory, playerdata.minestack.stained_clay1, new ItemStack(Material.STAINED_CLAY, 1, (short)1), v22, 1, "橙色の堅焼き粘土");
		setMineStackButton(inventory, playerdata.minestack.stained_clay4, new ItemStack(Material.STAINED_CLAY, 1, (short)4), v22, 2, "黄色の堅焼き粘土");
		setMineStackButton(inventory, playerdata.minestack.stained_clay8, new ItemStack(Material.STAINED_CLAY, 1, (short)8), v22, 3, "薄灰色の堅焼き粘土");
		setMineStackButton(inventory, playerdata.minestack.stained_clay12, new ItemStack(Material.STAINED_CLAY, 1, (short)12), v22, 4, "茶色の堅焼き粘土");
		setMineStackButton(inventory, playerdata.minestack.stained_clay14, new ItemStack(Material.STAINED_CLAY, 1, (short)14), v22, 5, "赤色の堅焼き粘土");
		setMineStackButton(inventory, playerdata.minestack.clay, Material.CLAY, v23, 6, "粘土");
		setMineStackButton(inventory, playerdata.minestack.mossy_cobblestone, Material.MOSSY_COBBLESTONE, v24, 7, "苔石");
		setMineStackButton(inventory, playerdata.minestack.ice, Material.ICE, v25, 8, "氷");
		setMineStackButton(inventory, playerdata.minestack.dirt1, new ItemStack(Material.DIRT, 1, (short)1), v26, 9, "粗い土");
		setMineStackButton(inventory, playerdata.minestack.dirt2, new ItemStack(Material.DIRT, 1, (short)2), v26, 10, "ポドゾル");
		setMineStackButton(inventory, playerdata.minestack.wood5, new ItemStack(Material.WOOD, 1, (short)5), v27, 11, "ダークオークの木材");
		setMineStackButton(inventory, playerdata.minestack.dark_oak_fence, Material.DARK_OAK_FENCE, v27, 12, "ダークオークのフェンス");
		setMineStackButton(inventory, playerdata.minestack.web, Material.WEB, v28, 13, "クモの巣");
		setMineStackButton(inventory, playerdata.minestack.string, Material.STRING, v28, 14, "糸");
		setMineStackButton(inventory, playerdata.minestack.rails, Material.RAILS, v29, 15, "レール");
		setMineStackButton(inventory, playerdata.minestack.leaves, new ItemStack(Material.LEAVES, 1, (short)0), v30, 16, "オークの葉");
		setMineStackButton(inventory, playerdata.minestack.leaves1, new ItemStack(Material.LEAVES, 1, (short)1), v30, 17, "マツの葉");
		setMineStackButton(inventory, playerdata.minestack.leaves2, new ItemStack(Material.LEAVES, 1, (short)2), v30, 18, "シラカバの葉");
		setMineStackButton(inventory, playerdata.minestack.leaves3, new ItemStack(Material.LEAVES, 1, (short)3), v30, 19, "ジャングルの葉");
		setMineStackButton(inventory, playerdata.minestack.leaves_2, new ItemStack(Material.LEAVES_2, 1, (short)0), v30, 20, "アカシアの葉");
		setMineStackButton(inventory, playerdata.minestack.leaves_21, new ItemStack(Material.LEAVES_2, 1, (short)1), v30, 21, "ダークオークの葉");
		setMineStackButton(inventory, playerdata.minestack.snow_block, Material.SNOW_BLOCK, v31, 22, "雪");
		setMineStackButton(inventory, playerdata.minestack.huge_mushroom_1, Material.HUGE_MUSHROOM_1, v32, 23, "キノコ");
		setMineStackButton(inventory, playerdata.minestack.huge_mushroom_2, Material.HUGE_MUSHROOM_2, v32, 24, "キノコ");
		setMineStackButton(inventory, playerdata.minestack.mycel, Material.MYCEL, v33, 25, "菌糸");
		setMineStackButton(inventory, playerdata.minestack.sapling, new ItemStack(Material.SAPLING, 1, (short)0), v34, 26, "オークの苗木");
		setMineStackButton(inventory, playerdata.minestack.sapling1, new ItemStack(Material.SAPLING, 1, (short)1), v34, 27, "マツの苗木");
		setMineStackButton(inventory, playerdata.minestack.sapling2, new ItemStack(Material.SAPLING, 1, (short)2), v34, 28, "シラカバの苗木");
		setMineStackButton(inventory, playerdata.minestack.sapling3, new ItemStack(Material.SAPLING, 1, (short)3), v34, 29, "ジャングルの苗木");
		setMineStackButton(inventory, playerdata.minestack.sapling4, new ItemStack(Material.SAPLING, 1, (short)4), v34, 30, "アカシアの苗木");
		setMineStackButton(inventory, playerdata.minestack.sapling5, new ItemStack(Material.SAPLING, 1, (short)5), v34, 31, "ダークオークの苗木");












		// MineStack1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack1ページ目へ");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowUp");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(45,itemstack);

		// MineStack3ページ目を開く(追加)
		/*
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack3ページ目へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(53,itemstack);
		*/

		return inventory;
	}


	//追加(Minestack3ページ目)
	public static Inventory getMineStackMenu3(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		//インベントリ作成
		//Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack");
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack");

		ItemStack itemstack;
		ItemMeta itemmeta;

		//レベル
		/*
		int v1 = SeichiAssist.config.getMineStacklevel(1);
		int v2 = SeichiAssist.config.getMineStacklevel(2);
		int v3 = SeichiAssist.config.getMineStacklevel(3);
		int v4 = SeichiAssist.config.getMineStacklevel(4);
		int v5 = SeichiAssist.config.getMineStacklevel(5);
		int v6 = SeichiAssist.config.getMineStacklevel(6);
		int v7 = SeichiAssist.config.getMineStacklevel(7);
		int v8 = SeichiAssist.config.getMineStacklevel(8);
		int v9 = SeichiAssist.config.getMineStacklevel(9);
		int v10 = SeichiAssist.config.getMineStacklevel(10);
		int v11 = SeichiAssist.config.getMineStacklevel(11);//追加
		int v12 = SeichiAssist.config.getMineStacklevel(12);//追加
		int v13 = SeichiAssist.config.getMineStacklevel(13);//追加
		int v14 = SeichiAssist.config.getMineStacklevel(14);//追加
		int v15 = SeichiAssist.config.getMineStacklevel(15);//追加
		*/

		//setMineStackButtonここから


		// MineStack2ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack2ページ目へ");
		List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowUp");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(45,itemstack);

		return inventory;
	}




	// MineStackトグルボタン
	public static ItemMeta MineStackToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<String>();
		if(playerdata.minestackflag){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在ONです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "現在OFFです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックでON");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}
	//MineStackボタン作成 Material版
	public static Inventory setMineStackButton(Inventory inv,int minestack,Material type,int level,int set){
		ItemStack itemstack = new ItemStack(type,1);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(type);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + type.toString());
		List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + minestack +"個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "Lv" + level + "以上でスタック可能"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで1スタック取り出し");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inv.setItem(set,itemstack);
		return inv;
	}
	//MineStackボタン作成 Material版名前付き
	public static Inventory setMineStackButton(Inventory inv,int minestack,Material type,int level,int set,String name){
		ItemStack itemstack = new ItemStack(type,1);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(type);
		if(name!=null){
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + name);
		} else {
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + type.toString());
		}
		List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + minestack +"個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "Lv" + level + "以上でスタック可能"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで1スタック取り出し");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inv.setItem(set,itemstack);
		return inv;
	}
	//MineStackボタン作成 ItemStack版
	public static Inventory setMineStackButton(Inventory inv,int minestack,ItemStack itemstack,int level,int set){
		itemstack.setAmount(1);
		ItemMeta itemmeta = itemstack.getItemMeta();
		//itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + itemmeta.getDisplayName());
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + itemstack.getType().toString());
		List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + minestack +"個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "Lv" + level + "以上でスタック可能"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで1スタック取り出し");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inv.setItem(set,itemstack);
		return inv;
	}
	//MineStackボタン作成 ItemStack版名前付き
	public static Inventory setMineStackButton(Inventory inv,int minestack,ItemStack itemstack,int level,int set, String name){
		itemstack.setAmount(1);
		ItemMeta itemmeta = itemstack.getItemMeta();
		if(name!=null){
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + name);
		} else {
			//itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + itemmeta.getDisplayName());
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + itemstack.getType().toString());
		}
		List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + minestack +"個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "Lv" + level + "以上でスタック可能"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで1スタック取り出し");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inv.setItem(set,itemstack);
		return inv;
	}
	// 死亡メッセージ表示トグルボタン
	public static ItemMeta dispKillLogToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<String>();
		if(playerdata.dispkilllogflag){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "表示する");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで隠す");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "隠す");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで表示する");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	// ワールドガード保護メッセージ表示トグルボタン(追加)
	public static ItemMeta dispWorldGuardLogToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<String>();
		if(playerdata.dispworldguardlogflag){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);

			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "スキル使用時のワールドガード保護警告メッセージ");
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "表示する");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで隠す");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "スキル使用時のワールドガード保護警告メッセージ");
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "隠す");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで表示する");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	// 複数種類ブロック同時破壊トグルボタン(追加)
	public static ItemMeta MultipleIDBlockBreakToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<String>();
		if(playerdata.multipleidbreakflag){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "複数種類ブロック同時破壊");
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "ブロックに対応するツールを無視してスキルで");
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "破壊可能な全種類のブロックを同時に破壊します");
					//ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
			if(playerdata.level>=SeichiAssist.config.getMultipleIDBlockBreaklevel()){
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getMultipleIDBlockBreaklevel());
					//	ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：***"
					//	ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
				lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "ON");
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
			} else {
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "必要整地レベル：" + SeichiAssist.config.getMultipleIDBlockBreaklevel());
				lore.add(ChatColor.RESET + "" +  ChatColor.RED + "整地レベルが足りません");
				if(SeichiAssist.DEBUG){
					lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "ON");
				}
			}

		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "複数種類ブロック同時破壊");
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "ブロックに対応するツールを無視してスキルで");
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "破壊可能な全種類のブロックを同時に破壊します");
			//ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
			if(playerdata.level>=SeichiAssist.config.getMultipleIDBlockBreaklevel()){
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getMultipleIDBlockBreaklevel());
				//	ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：***"
				//	ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
				lore.add(ChatColor.RESET + "" +  ChatColor.RED + "OFF");
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックでON");
			} else {
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "必要整地レベル：" + SeichiAssist.config.getMultipleIDBlockBreaklevel());
				lore.add(ChatColor.RESET + "" +  ChatColor.RED + "整地レベルが足りません");
				if(SeichiAssist.DEBUG){
					lore.add(ChatColor.RESET + "" +  ChatColor.RED + "OFF");
				}
			}
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	// PvPトグルボタン
	public static ItemMeta dispPvPToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<String>();
		if(playerdata.pvpflag){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "ON(ONの相手とPvPが可能)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "OFF(全てのPvPを無効化)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックでON");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}
	//ランキングリスト
	public static Inventory getRankingList(Player p){
		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<String>();
		itemstack.setDurability((short) 3);
		RankData rankdata = null;
		for(int count = 0;count < 27;count++){
			if(count > SeichiAssist.ranklist.size() - 1){
				break;
			}
			rankdata = SeichiAssist.ranklist.get(count);
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(count,itemstack);
		}

		// 1ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		// 1ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地鯖統計データ");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "全プレイヤー総整地量:"
				,ChatColor.RESET + "" +  ChatColor.AQUA + SeichiAssist.allplayerbreakblockint
				));
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(35,itemstack);

		return inventory;
	}
	//エフェクト選択メニュー
	public static Inventory getActiveSkillEffectMenuData(Player p) {
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

				Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキルエフェクト選択");
				ItemStack itemstack;
				ItemMeta itemmeta;
				SkullMeta skullmeta;
				List<String> lore = new ArrayList<String>();

				// 1ページ目を開く
				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スキルメニューへ");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
						);
				skullmeta.setLore(lore);
				skullmeta.setOwner("MHF_ArrowLeft");
				itemstack.setItemMeta(skullmeta);
				inventory.setItem(45,itemstack);

				//1行目

				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.name + "のスキルエフェクトデータ");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "現在選択しているエフェクト：" + ActiveSkillEffect.getNamebyNum(playerdata.activeskilldata.effectnum)
						, ChatColor.RESET + "" +  ChatColor.YELLOW + "使えるエフェクトポイント：" + playerdata.activeskilldata.effectpoint
						, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※投票すると獲得出来ます"
						, ChatColor.RESET + "" +  ChatColor.LIGHT_PURPLE + "使えるプレミアムポイント：" + playerdata.activeskilldata.premiumeffectpoint
						, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※寄付をすると獲得できます"

						);
				skullmeta.setLore(lore);
				skullmeta.setOwner(playerdata.name);
				itemstack.setItemMeta(skullmeta);
				inventory.setItem(0,itemstack);


				itemstack = new ItemStack(Material.BOOK_AND_QUILL,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
				itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで閲覧");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(2,itemstack);

				itemstack = new ItemStack(Material.GLASS,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクトを使用しない");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(1,itemstack);


				ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();

				for(int i = 0; i < skilleffect.length;i++){
					//プレイヤーがそのスキルを取得している場合の処理
					if(skilleffect[i].isObtained(playerdata.activeskilldata.effectflagmap)){
						itemstack = new ItemStack(skilleffect[i].getMaterial(),1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(skilleffect[i].getMaterial());
						itemmeta.setDisplayName(skilleffect[i].getName());
						lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + skilleffect[i].getExplain()
								, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
								);
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
					}
					//プレイヤーがそのスキルをまだ取得していない場合の処理
					else{
						itemstack = new ItemStack(Material.BEDROCK,1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
						itemmeta.setDisplayName(skilleffect[i].getName());
						lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + skilleffect[i].getExplain()
								, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要エフェクトポイント：" + skilleffect[i].getUsePoint()
								, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
					}
					inventory.setItem(i + 9,itemstack);
				}
				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();

				for(int i = 0; i < premiumeffect.length;i++){
					//プレイヤーがそのスキルを取得している場合の処理
					if(premiumeffect[i].isObtained(playerdata.activeskilldata.premiumeffectflagmap)){
						itemstack = new ItemStack(premiumeffect[i].getMaterial(),1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(premiumeffect[i].getMaterial());
						itemmeta.setDisplayName(ChatColor.UNDERLINE + "" + ChatColor.BOLD + ChatColor.stripColor(premiumeffect[i].getName()));
						lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + premiumeffect[i].getExplain()
								, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
								);
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
					}
					//プレイヤーがそのスキルをまだ取得していない場合の処理
					else{
						itemstack = new ItemStack(Material.BEDROCK,1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
						itemmeta.setDisplayName(premiumeffect[i].getName());
						lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + premiumeffect[i].getExplain()
								, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要プレミアムポイント：" + premiumeffect[i].getUsePoint()
								, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
					}
					inventory.setItem(i + 27,itemstack);
				}

		return inventory;
	}
	//プレミア購入履歴表示
	public static Inventory getBuyRecordMenuData(Player player) {
		PlayerData playerdata = playermap.get(player.getUniqueId());
		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.BLUE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴");
		ItemStack itemstack;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクト選択メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		sql.loadDonateData(playerdata,inventory);



		return inventory;
	}
}
