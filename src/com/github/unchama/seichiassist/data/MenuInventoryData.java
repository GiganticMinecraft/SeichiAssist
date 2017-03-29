package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.bukkit.inventory.meta.SkullMeta;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.MineStackObj;
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
		if(!Util.isSeichiWorld(p)){
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

		//ExpBar用ダミー
		lore.add("");
		lore.add("");
		lore.add("");

		skullmeta.setLore(lore);
		skullmeta.setOwner(playerdata.name);
		//ExpBarトグル追加
		itemstack.setItemMeta(dispExpBarToggleMeta(playerdata,skullmeta));
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
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "(整地レベル100以上のプレイヤーのみ表記されます)"
				,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(32,itemstack);

		// 整地神番付を開く
		itemstack = new ItemStack(Material.COOKIE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキングを見る");
		/*
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "(整地レベル100以上のプレイヤーのみ表記されます)"
				,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		*/
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(33,itemstack);

		// 整地神番付を開く
		itemstack = new ItemStack(Material.COOKIE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキングを見る");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "(投票しているプレイヤーのみ表記されます)"
				,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		/*
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		*/
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(34,itemstack);

		/*
		if(SeichiAssist.DEBUG){
			// 整地神番付を開く
			itemstack = new ItemStack(Material.COOKIE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキングを見る");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "(寄付しているプレイヤーのみ表記されます)"
				,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(16,itemstack);
		}
		*/


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
		inventory.setItem(16,itemstack);

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


		//実績画面
		itemstack = new ItemStack(Material.END_CRYSTAL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.END_CRYSTAL);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名システム");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "様々な実績に挑んで、"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "いろんな二つ名を手に入れよう！"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定画面へ移動");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(9,itemstack);


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

		//鉱石・交換券変換システムを開く
		itemstack = new ItemStack(Material.DIAMOND_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_ORE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "鉱石・交換券変換システム");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "不必要な各種鉱石を"
				, ChatColor.RESET + "" + ChatColor.DARK_RED + "交換券" + ChatColor.RESET + ChatColor.GREEN + "と交換できます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "出てきたインベントリ―に"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "交換したい景品を入れて"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "escキーを押してください"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "たまにアイテムが消失するから"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "大事なものはいれないでネ"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(17,itemstack);


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

		//全体通知音消音のトグルボタン
		itemstack = new ItemStack(Material.JUKEBOX,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.JUKEBOX);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "全体通知音消音切替");
		itemstack.setItemMeta(dispWinSoundToggleMeta(playerdata,itemmeta));
		inventory.setItem(13,itemstack);

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
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "JapanMinecraftServerリンク");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		//現在掲示板は存在しないので、名前を変更して「JapanMinecraftServerリンク」とする
		//消去部分
		//Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "管理人へのお問い合わせは"
		//, ChatColor.RESET + "" +  ChatColor.GREEN + "掲示板に書き込みをｵﾈｶﾞｲｼﾅｽ"
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


		//椎名林檎変換システムを開く
		itemstack = new ItemStack(Material.GOLDEN_APPLE,1);
		itemstack.setDurability((short) 1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "GT景品→椎名林檎変換システム");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "不必要なGT大当り景品を"
				, ChatColor.RESET + "" + ChatColor.GOLD + "椎名林檎" + ChatColor.RESET + ChatColor.GREEN + "と交換できます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "出てきたインベントリ―に"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "交換したい景品を入れて"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "escキーを押してください"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "たまにアイテムが消失しますが"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "補償はしていません(ごめんなさい)"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "神に祈りながら交換しよう"
				, ChatColor.RESET + "現在の交換レート:GT景品1つにつき" + SeichiAssist.config.rateGiganticToRingo() + "個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(35,itemstack);


		//サブホーム関係
		for(int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
			//サブホームに移動ボタン
			itemstack = new ItemStack(Material.COMPASS,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント"+  (x+1) + "にワープ");

			Location l = playerdata.GetSubHome(x);
			if (l == null || l.getWorld() == null){
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

		// インベントリ共有ボタン
		itemstack = new ItemStack(Material.TRAPPED_CHEST,1);
		itemstack.setItemMeta(dispShareInvMeta(playerdata));
		inventory.setItem(6,itemstack);

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

		return inventory;
	}

	// 採掘速度トグルボタン
	public static ItemMeta EFButtonMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<String>();
		if(playerdata.effectflag == 0){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在有効です(無制限)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで200制限");
		}else if(playerdata.effectflag == 1){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在有効です" + ChatColor.YELLOW + "(200制限)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで400制限");
		}else if(playerdata.effectflag == 2){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在有効です" + ChatColor.YELLOW + "(400制限)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで600制限");
		}else if(playerdata.effectflag == 3){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在有効です" + ChatColor.YELLOW + "(600制限)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "現在OFFです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで無制限");
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



	//Minestackメインページ
	public static Inventory getMineStackMainMenu(Player p){

		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackメインメニュー");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		ItemMeta itemmeta;
		List<String> lore = new ArrayList<String>();

		itemstack = new ItemStack(Material.STONE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "採掘系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);


		itemstack = new ItemStack(Material.ENDER_PEARL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_PEARL);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ドロップ系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);


		itemstack = new ItemStack(Material.SEEDS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SEEDS);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "農業系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);


		itemstack = new ItemStack(Material.SMOOTH_BRICK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SMOOTH_BRICK);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "建築系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(3,itemstack);


		itemstack = new ItemStack(Material.REDSTONE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "レッドストーン系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);


		itemstack = new ItemStack(Material.GOLDEN_APPLE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ガチャ品");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(5,itemstack);
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		itemstack.setDurability((short) 3);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);

		// 1ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(45,itemstack);

		return inventory;
	}

	//Minestack全ページ(切り替え式)
	public static Inventory getMineStackMenu(Player p, int page, int stack_type){
		int minestack_stacktype_size=0;
		for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
			if(SeichiAssist.minestacklist.get(i).getStacktype()==stack_type){
				minestack_stacktype_size++;
			}
		}

		//現在の最大ページ数を取得(1ページ=0,2ページ=1,...)
		int maxpage = (minestack_stacktype_size + 1) / 45;
		if((minestack_stacktype_size + 1) % 45 == 0){
			maxpage--;
		}

		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		//インベントリ作成
		//Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack");
		Inventory inventory = null;
		if(stack_type==0){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "採掘系MineStack");
		} else if(stack_type==1){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "ドロップ系MineStack");
		} else if(stack_type==2){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "農業系MineStack");
		} else if(stack_type==3){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "建築系MineStack");
		} else if(stack_type==4){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "レッドストーン系MineStack");
		} else if(stack_type==5){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "ガチャ系MineStack");
		}

		ItemStack itemstack;
		ItemMeta itemmeta;

		if(page==0){
		//MineStack機能のトグルボタン
		itemstack = new ItemStack(Material.IRON_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "対象ブロック自動スタック機能");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(MineStackToggleMeta(playerdata,itemmeta));
		inventory.setItem(0,itemstack);
		}

		//1から

		int start=0;
		if(page==0){
			start=1;
		}
		int max = 0;
		if(page==maxpage){
			max = (minestack_stacktype_size+1)%45;
			if(max == 0){
				max = 45;
			}
		} else {
			max = 45;
		}

		int i=start;
		int ii_temp=0;
		int iii=0;
		int ii = start + page*45 - 1;
		while(ii_temp<ii){
			if(SeichiAssist.minestacklist.get(iii).getStacktype()!=stack_type){//対象外
				iii++;
			} else {
				iii++;
				ii_temp++;
			}
		}

		while(i<max){
			ii = i + page*45 - 1;
			while(SeichiAssist.minestacklist.get(iii).getStacktype()!=stack_type){
				iii++;
			}
			//この時点で「stack_typeのii番目」のインデックスになっている
			MineStackObj msobj = SeichiAssist.minestacklist.get(iii);
			if(msobj.getItemStack()==null){
				setMineStackButton(inventory, playerdata.minestack.getNum(iii), new ItemStack(msobj.getMaterial(), 1, (short)msobj.getDurability()),  SeichiAssist.config.getMineStacklevel(msobj.getLevel()), i, msobj.getJapaneseName());
				iii++;
			} else {
				setMineStackButton(inventory, playerdata.minestack.getNum(iii), msobj.getItemStack(), SeichiAssist.config.getMineStacklevel(msobj.getLevel()), i, msobj.getJapaneseName());
				iii++;
			}
			i++;
		}

		/*
		for(int i=start; i<max; i++){ //minestackbuttonのインベントリの位置
			  int ii = i + page*45 - 1; //minestacklistのindex
			MineStackObj msobj = SeichiAssist.minestacklist.get(ii);
			if(msobj.getItemStack()==null){
				setMineStackButton(inventory, playerdata.minestack.getNum(ii), new ItemStack(msobj.getMaterial(), 1, (short)msobj.getDurability()),  SeichiAssist.config.getMineStacklevel(msobj.getLevel()), i, msobj.getJapaneseName());
			} else {
				setMineStackButton(inventory, playerdata.minestack.getNum(ii), msobj.getItemStack(), SeichiAssist.config.getMineStacklevel(msobj.getLevel()), i, msobj.getJapaneseName());
			}
		}
		*/

		//44まで

		if(page==0){
			// メインページ1ページ目を開く
			itemstack = new ItemStack(Material.SKULL_ITEM,1);
			SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			itemstack.setDurability((short) 3);
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStackメインメニューへ");
			List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
					);
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		} else if(page>=1){
			// MineStackの前のページを開く
			itemstack = new ItemStack(Material.SKULL_ITEM,1);
			SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			itemstack.setDurability((short) 3);
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack" + page + "ページ目へ");
			List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		}

		if(maxpage>=1 && page!=maxpage){ //3ページ以降があって現在は最大ページ目でない
			//MineStackの次のページを開く
			itemstack = new ItemStack(Material.SKULL_ITEM,1);
			SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			itemstack.setDurability((short) 3);
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack" + (page+2) + "ページ目へ");
			List<String> lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowDown");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(53,itemstack);
		}

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
	// 全体通知音消音トグルボタン
	public static ItemMeta dispWinSoundToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<String>();
		if(playerdata.everysoundflag){
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "消音しない");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定");
		}else{
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "消音する");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで解除");
		}
		itemmeta.setLore(lore);
		return itemmeta;
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
			lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "整地ワールドではON/OFFに関わらず同時破壊されます");
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
			lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "整地ワールドではON/OFFに関わらず同時破壊されます");
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

	// ExpBarトグルボタン
	public static SkullMeta dispExpBarToggleMeta(PlayerData playerdata,SkullMeta skullmeta){
		List<String> lore = skullmeta.getLore();
		if(playerdata.expbar.isVisible()){
			lore.set(lore.size() - 2, ChatColor.RESET + "" +  ChatColor.GREEN + "整地量バーを表示");
			lore.set(lore.size() - 1, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで非表示");
		}else{
			lore.set(lore.size() - 2, ChatColor.RESET + "" +  ChatColor.RED + "整地量バーを非表示");
			lore.set(lore.size() - 1, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで表示");
		}
		skullmeta.setLore(lore);
		return skullmeta;
	}

	// インベントリ共有トグルボタン
	public static ItemMeta dispShareInvMeta(PlayerData playerdata){
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.TRAPPED_CHEST);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "インベントリ共有");
		List<String> lore = new ArrayList<String>();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "現在の装備・アイテムを移動します。"
				, ChatColor.RESET + "" + "サーバー間のアイテム移動にご利用ください。"
				, ""
				));

		if(playerdata.shareinv){
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "収納中");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでアイテムを取り出します。");
			lore.add(ChatColor.RESET + "" + ChatColor.RED + "現在の装備・アイテムが空であることを確認してください。");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "非収納中");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでアイテムを収納します。");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	//ランキングリスト
	public static Inventory getRankingList(Player p, int page){
		int maxpage=2;
		final int MIN_LEVEL = 100;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<String>();
		itemstack.setDurability((short) 3);
		RankData rankdata = null;
		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
			if(count >= SeichiAssist.ranklist.size()){
				break;
			}
			if(count2==45){count2+=2;}
			rankdata = SeichiAssist.ranklist.get(count);
			if(rankdata.totalbreaknum<SeichiAssist.levellist.get(MIN_LEVEL-1)){ //レベル100相当の総整地量判定に変更
				break;
			}

			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(count2,itemstack);
		}

		if(page!=maxpage){
		// 整地神ランキング次ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング"+(page+2)+"ページ目へ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(52,itemstack);
		}

		// 1ページ目を開く
		if(page==0){
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		} else {
			// 整地神ランキング前ページ目を開く;
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング"+page+"ページ目へ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		}

		// 総整地量の表記
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地鯖統計データ");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "全プレイヤー総整地量:"
				,ChatColor.RESET + "" +  ChatColor.AQUA + SeichiAssist.allplayerbreakblockint
				));
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(53,itemstack);

		return inventory;
	}

	//ランキングリスト(ログイン時間)
	public static Inventory getRankingList_playtick(Player p, int page){
		int maxpage=2;
		//final int MIN_LEVEL = 100;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ログイン神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<String>();
		itemstack.setDurability((short) 3);
		RankData rankdata = null;
		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
			if(count >= SeichiAssist.ranklist_playtick.size()){
				break;
			}
			if(count2==45){count2+=2;}
			rankdata = SeichiAssist.ranklist_playtick.get(count);
			//if(rankdata.totalbreaknum<SeichiAssist.levellist.get(MIN_LEVEL-1)){ //レベル100相当の総整地量判定に変更
			//	break;
			//}

			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総ログイン時間:" + Util.toTimeString(Util.toSecond(rankdata.playtick)));

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(count2,itemstack);
		}

		if(page!=maxpage){
		// 整地神ランキング次ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング"+(page+2)+"ページ目へ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(52,itemstack);
		}

		// 1ページ目を開く
		if(page==0){
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		} else {
			// 整地神ランキング前ページ目を開く;
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング"+page+"ページ目へ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		}

		// 総整地量の表記
		/*
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地鯖統計データ");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "全プレイヤー総整地量:"
				,ChatColor.RESET + "" +  ChatColor.AQUA + SeichiAssist.allplayerbreakblockint
				));
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(53,itemstack);
		*/

		return inventory;
	}

	//ランキングリスト(投票回数)
	public static Inventory getRankingList_p_vote(Player p, int page){
		int maxpage=2;
		final int MIN_LEVEL = 1;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<String>();
		itemstack.setDurability((short) 3);
		RankData rankdata = null;
		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
			if(count >= SeichiAssist.ranklist_p_vote.size()){
				break;
			}
			if(count2==45){count2+=2;}
			rankdata = SeichiAssist.ranklist_p_vote.get(count);
			if(rankdata.p_vote<MIN_LEVEL){ //投票数0
				break;
			}

			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総ログイン時間:" + Util.toTimeString(Util.toSecond(rankdata.playtick)));
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総投票回数:" + rankdata.p_vote);

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(count2,itemstack);
		}

		if(page!=maxpage){
		// 投票神ランキング次ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキング"+(page+2)+"ページ目へ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(52,itemstack);
		}

		// 1ページ目を開く
		if(page==0){
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		} else {
			// 整地神ランキング前ページ目を開く;
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキング"+page+"ページ目へ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		}

		// 総整地量の表記
		/*
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地鯖統計データ");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "全プレイヤー総整地量:"
				,ChatColor.RESET + "" +  ChatColor.AQUA + SeichiAssist.allplayerbreakblockint
				));
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(53,itemstack);
		*/

		return inventory;
	}

	//ランキングリスト(プレミアムエフェクトポイント)
	public static Inventory getRankingList_premiumeffectpoint(Player p, int page){
		int maxpage=2;
		final int MIN_LEVEL = 1;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "寄付神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<String>();
		itemstack.setDurability((short) 3);
		RankData rankdata = null;
		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
			if(count >= SeichiAssist.ranklist_premiumeffectpoint.size()){
				break;
			}
			if(count2==45){count2+=2;}
			rankdata = SeichiAssist.ranklist_premiumeffectpoint.get(count);
			if(rankdata.premiumeffectpoint<MIN_LEVEL){ //寄付金額0
				break;
			}

			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総ログイン時間:" + Util.toTimeString(Util.toSecond(rankdata.playtick)));
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総寄付金額:" + rankdata.premiumeffectpoint*100);

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(count2,itemstack);
		}

		if(page!=maxpage){
		// 整地神ランキング次ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキング"+(page+2)+"ページ目へ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(52,itemstack);
		}

		// 1ページ目を開く
		if(page==0){
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		} else {
			// 整地神ランキング前ページ目を開く;
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキング"+page+"ページ目へ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			inventory.setItem(45,itemstack);
		}

		// 総整地量の表記
		/*
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地鯖統計データ");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "全プレイヤー総整地量:"
				,ChatColor.RESET + "" +  ChatColor.AQUA + SeichiAssist.allplayerbreakblockint
				));
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(53,itemstack);
		*/

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

	//実績メニュー
	public static Inventory getTitleMenuData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績・二つ名システム");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		//表示切り替え(LV・二つ名)
		itemstack = new ItemStack(Material.REDSTONE_TORCH_ON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_TORCH_ON);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地レベルを表示" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "このボタンをクリックすると、"
							,ChatColor.RESET + "" +  ChatColor.RED + "「整地LV」に表示を切り替えます。"
							,ChatColor.RESET + "" +  ChatColor.YELLOW + "※反映されるまで最大1分ほどかかります。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		//予約付与受け取りボタン
		if(!(playerdata.giveachvNo == 0)){
		itemstack = new ItemStack(Material.EMERALD_BLOCK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_BLOCK);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "【予約付与システム】" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "運営チームからあなたへ、"
							,ChatColor.RESET + "" +  ChatColor.RED + "「二つ名」のプレゼントが届いています。"
							,ChatColor.RESET + "" +  ChatColor.YELLOW + "クリックすることで受け取れます！");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);
		}

		//実績確認画面へ移動
		itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「整地神ランキング」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得状況を表示します。");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(10,itemstack);

		itemstack = new ItemStack(Material.GOLD_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「30分ランキング」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "【近日実装予定！】");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);

		itemstack = new ItemStack(Material.IRON_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「整地量」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得状況を表示します。");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(12,itemstack);

		itemstack = new ItemStack(Material.COMPASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「参加時間」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(13,itemstack);

		itemstack = new ItemStack(Material.BREAD,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BREAD);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「連続ログイン」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "【近日実装予定！】");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(14,itemstack);

		itemstack = new ItemStack(Material.YELLOW_FLOWER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.YELLOW_FLOWER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「外部支援」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(15,itemstack);

		itemstack = new ItemStack(Material.NETHER_STAR,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「特殊」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(16,itemstack);

		itemstack = new ItemStack(Material.DIAMOND_BARDING,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BARDING);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「極秘任務」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得状況を表示します。"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "※獲得前の実績は表示されません。" );
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(22,itemstack);

		itemstack = new ItemStack(Material.BLAZE_POWDER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BLAZE_POWDER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「公式イベント」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);


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

		return inventory;
	}

	//実績「整地神ランキング」
	public static Inventory getTitleRankData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地神ランキング」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		//1位
		if(playerdata.TitleFlags.get(1001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1001「"+ SeichiAssist.config.getTitle(1001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」1位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」1位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		//5位
		if(playerdata.TitleFlags.get(1002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1002「"+ SeichiAssist.config.getTitle(1002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」5位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」5位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		//27位
		if(playerdata.TitleFlags.get(1003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1003「"+ SeichiAssist.config.getTitle(1003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」27位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」27位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		//50位
		if(playerdata.TitleFlags.get(1004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1004「"+ SeichiAssist.config.getTitle(1004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」50位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」50位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		//100位
		if(playerdata.TitleFlags.get(1005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1005「"+ SeichiAssist.config.getTitle(1005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」100位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」100位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		//250位
		if(playerdata.TitleFlags.get(1006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1006「"+ SeichiAssist.config.getTitle(1006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」250位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」250位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		//500位
		if(playerdata.TitleFlags.get(1007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1007「"+ SeichiAssist.config.getTitle(1007) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」500位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」500位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//1000位
		if(playerdata.TitleFlags.get(1008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1008「"+ SeichiAssist.config.getTitle(1008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」1000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」1000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		//3000位
		if(playerdata.TitleFlags.get(1009)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1009「"+ SeichiAssist.config.getTitle(1009) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」3000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1009「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」3000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}



		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}


	//実績「30分ランキング」
	public static Inventory getTitleHalfRankData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「30分ランキング」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		if(playerdata.TitleFlags.get(2001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No2001「"+ SeichiAssist.config.getTitle(2001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：雛型"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は未実装です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No2001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：雛型"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は未実装です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}



	//実績「整地量」
	public static Inventory getTitleAmountData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地量」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		//int限界突破
		//「10億」実績突破前は表示されない処理
		if(playerdata.TitleFlags.get(3002)){
			if(playerdata.TitleFlags.get(3001)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3001「"+ SeichiAssist.config.getTitle(3001) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が「int型の壁」を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3001「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}
		}else{
		}
		//10億突破
		if(playerdata.TitleFlags.get(3002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3002「"+ SeichiAssist.config.getTitle(3002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		//5億突破
		if(playerdata.TitleFlags.get(3003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3003「"+ SeichiAssist.config.getTitle(3003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 5億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 5億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		//1億突破
		if(playerdata.TitleFlags.get(3004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3004「"+ SeichiAssist.config.getTitle(3004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 1億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 1億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		//5000万突破
		if(playerdata.TitleFlags.get(3005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3005「"+ SeichiAssist.config.getTitle(3005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 5000万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 5000万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		//1000万突破
		if(playerdata.TitleFlags.get(3006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3006「"+ SeichiAssist.config.getTitle(3006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 1000万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 1000万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		//500万突破
		if(playerdata.TitleFlags.get(3007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3007「"+ SeichiAssist.config.getTitle(3007) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 500万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 500万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		//100万突破
		if(playerdata.TitleFlags.get(3008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3008「"+ SeichiAssist.config.getTitle(3008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 100万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 100万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//50万突破
		if(playerdata.TitleFlags.get(3009)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3009「"+ SeichiAssist.config.getTitle(3009) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 50万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3009「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 50万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		//10万突破
		if(playerdata.TitleFlags.get(3010)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3010「"+ SeichiAssist.config.getTitle(3010) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3010「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}
		//1万突破
		if(playerdata.TitleFlags.get(3011)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3011「"+ SeichiAssist.config.getTitle(3011) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10000 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(17,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3011「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10000 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(17,itemstack);
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}



	//実績「参加時間」
	public static Inventory getTitleTimeData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「参加時間」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		//2400h突破
		//「1000h」実績突破前は表示されない処理
		if(playerdata.TitleFlags.get(4002)){
			if(playerdata.TitleFlags.get(4001)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4001「"+ SeichiAssist.config.getTitle(4001) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 2400時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4001「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}
		}else{
		}
		//1000h突破
		if(playerdata.TitleFlags.get(4002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4002「"+ SeichiAssist.config.getTitle(4002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 1000時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 1000時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		//500h突破
		if(playerdata.TitleFlags.get(4003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4003「"+ SeichiAssist.config.getTitle(4003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 500時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 500時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		//250h突破
		if(playerdata.TitleFlags.get(4004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4004「"+ SeichiAssist.config.getTitle(4004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 250時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 250時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		//100h突破
		if(playerdata.TitleFlags.get(4005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4005「"+ SeichiAssist.config.getTitle(4005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 100時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 100時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		//50h突破
		if(playerdata.TitleFlags.get(4006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4006「"+ SeichiAssist.config.getTitle(4006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 50時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 50時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		//24h突破
		if(playerdata.TitleFlags.get(4007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4007「"+ SeichiAssist.config.getTitle(4007) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 24時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 24時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		//10h突破
		if(playerdata.TitleFlags.get(4008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4008「"+ SeichiAssist.config.getTitle(4008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 10時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 10時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//5h突破
		if(playerdata.TitleFlags.get(4009)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4009「"+ SeichiAssist.config.getTitle(4009) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 5時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4009「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 5時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		//1h突破
		if(playerdata.TitleFlags.get(4010)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4010「"+ SeichiAssist.config.getTitle(4010) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 1時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4010「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 1時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}
		if(playerdata.playtick % 576000 >= 0 && playerdata.playtick % 576000 <= 1199 && !(playerdata.TitleFlags.get(8003))){
			itemstack = new ItemStack(Material.EMERALD_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "タイムカード、切りましょう？" );
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(35,itemstack);
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}

	//実績「連続ログイン」
	public static Inventory getTitleKeepJoinData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「連続ログイン」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		if(playerdata.TitleFlags.get(5001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5001「"+ SeichiAssist.config.getTitle(5001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：雛型"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は未実装です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：雛型"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は未実装です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}


	//実績「外部支援」
	public static Inventory getTitleSupportData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「外部支援」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		//投票数365
		if(playerdata.TitleFlags.get(6001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6001「"+ SeichiAssist.config.getTitle(6001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が365を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が365を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		//投票数200
		if(playerdata.TitleFlags.get(6002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6002「"+ SeichiAssist.config.getTitle(6002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が200を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が200を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		//投票数100
		if(playerdata.TitleFlags.get(6003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6003「"+ SeichiAssist.config.getTitle(6003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が100を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が100を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		//投票数50
		if(playerdata.TitleFlags.get(6004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6004「"+ SeichiAssist.config.getTitle(6004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が50を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が50を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		//投票数25
		if(playerdata.TitleFlags.get(6005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6005「"+ SeichiAssist.config.getTitle(6005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が25を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が25を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		//投票数10
		if(playerdata.TitleFlags.get(6006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6006「"+ SeichiAssist.config.getTitle(6006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が10を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が10を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		//投票数5
		if(playerdata.TitleFlags.get(6007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6007「"+ SeichiAssist.config.getTitle(6007) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が5を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が5を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//投票数1
		if(playerdata.TitleFlags.get(6008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6008「"+ SeichiAssist.config.getTitle(6008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が1を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が1を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}

	//実績「公式イベント」
	public static Inventory getTitleEventData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「公式イベント」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		if(playerdata.TitleFlags.get(7001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7001「"+ SeichiAssist.config.getTitle(7001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「整地大会」に参加する"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「整地大会」に参加する"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		if(playerdata.TitleFlags.get(7002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7002「"+ SeichiAssist.config.getTitle(7002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「整地大会」にて総合優勝"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「整地大会」にて総合優勝"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		if(playerdata.TitleFlags.get(7003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7003「"+ SeichiAssist.config.getTitle(7003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で最優秀賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で最優秀賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		if(playerdata.TitleFlags.get(7004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7004「"+ SeichiAssist.config.getTitle(7004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で優秀賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で優秀賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		if(playerdata.TitleFlags.get(7005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7005「"+ SeichiAssist.config.getTitle(7005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で佳作賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で佳作賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}


	//実績「特殊」
	public static Inventory getTitleExtraData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「特殊」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		if(playerdata.TitleFlags.get(9001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9001「"+ SeichiAssist.config.getTitle(9001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある始まりの日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある始まりの日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		if(playerdata.TitleFlags.get(9002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9002「"+ SeichiAssist.config.getTitle(9002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある聖夜の日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある聖夜の日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		if(playerdata.TitleFlags.get(9003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9003「"+ SeichiAssist.config.getTitle(9003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある終わりの日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある終わりの日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		if(playerdata.TitleFlags.get(9004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9004「"+ SeichiAssist.config.getTitle(9004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：1月にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：1月にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		if(playerdata.TitleFlags.get(9005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9005「"+ SeichiAssist.config.getTitle(9005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：2月にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：2月にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		if(playerdata.TitleFlags.get(9006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9006「"+ SeichiAssist.config.getTitle(9006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるお豆の絨毯爆撃の日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるお豆の絨毯爆撃の日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		if(playerdata.TitleFlags.get(9007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9007「"+ SeichiAssist.config.getTitle(9007) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：建国記念日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：建国記念日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		if(playerdata.TitleFlags.get(9008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9008「"+ SeichiAssist.config.getTitle(9008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるカカオまみれの日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるカカオまみれの日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		if(playerdata.TitleFlags.get(9009)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9009「"+ SeichiAssist.config.getTitle(9009) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：3月にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9009「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：3月にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}
		if(playerdata.TitleFlags.get(9010)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9010「"+ SeichiAssist.config.getTitle(9010) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある女の子の日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(9,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9010「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある女の子の日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(9,itemstack);
		}
		if(playerdata.TitleFlags.get(9011)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9011「"+ SeichiAssist.config.getTitle(9011) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：燃え尽きたカカオだらけの日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(10,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9011「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：燃え尽きたカカオだらけの日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(10,itemstack);
		}
		if(playerdata.TitleFlags.get(9012)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9012「"+ SeichiAssist.config.getTitle(9012) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：春分の日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(11,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9012「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：春分の日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(11,itemstack);
		}
		if(playerdata.TitleFlags.get(9013)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9013「"+ SeichiAssist.config.getTitle(9013) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：4月にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(12,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9013「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：4月にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(12,itemstack);
		}
		if(playerdata.TitleFlags.get(9014)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9014「"+ SeichiAssist.config.getTitle(9014) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある嘘の日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(13,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9014「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある嘘の日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(13,itemstack);
		}
		if(playerdata.TitleFlags.get(9015)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9015「"+ SeichiAssist.config.getTitle(9015) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある良い子の日にプレイ");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(14,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9015「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある良い子の日にプレイ"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(14,itemstack);
		}
		if(playerdata.TitleFlags.get(9016)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9016「"+ SeichiAssist.config.getTitle(9016) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：ﾚｯﾂ大掃除ｷｬﾝﾍﾟｰﾝ(日付実績)");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(15,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9016「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：ﾚｯﾂ大掃除ｷｬﾝﾍﾟｰﾝ(日付実績)"
								,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(15,itemstack);
		}

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}

	//実績「極秘任務」(隠し実績群)
	public static Inventory getTitleSecretData(Player p){
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

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「極秘任務」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();


		//実績設定・解禁ボタン
		if(playerdata.TitleFlags.get(8001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8001「"+ SeichiAssist.config.getTitle(8001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：未来の使者、器を満たさん"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			if(Calendar.getInstance().get(Calendar.SECOND)  == 0 &&
				Calendar.getInstance().get(Calendar.MINUTE)  == 0){
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：未来の使者、器を満たさん"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
			}
		}
		if(playerdata.TitleFlags.get(8002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8002「"+ SeichiAssist.config.getTitle(8002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：幸せの7は3人兄弟、1000組に及ぶ彼らに新たな1組"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			if(playerdata.totalbreaknum % 1000000 == 0 && !(playerdata.totalbreaknum == 0)){
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：幸せの7は3人兄弟、1000組に及ぶ彼らに新たな1組"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
			}
		}
		if(playerdata.TitleFlags.get(8003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8003「"+ SeichiAssist.config.getTitle(8003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：きっちり定時でタイムカードを切りましょ！"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は手動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			if(playerdata.playtick % 72000 >= 0 && playerdata.playtick % 72000 <= 1199){
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：きっちり定時でタイムカードを切りましょ！"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は手動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
			}
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		inventory.setItem(27,itemstack);

		return inventory;
	}

}
