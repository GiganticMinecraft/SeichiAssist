package com.github.unchama.buildassist;

import com.github.unchama.buildassist.util.AsyncInventorySetter;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.ItemMetaFactory;
import com.github.unchama.util.collection.ImmutableListFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public final class MenuInventoryData {
	public static Inventory getMenuData(final Player p) {
		//プレイヤーを取得
		final Player player = p.getPlayer();
		//UUID取得
		final UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		final PlayerData playerdata = BuildAssist.playermap.get(uuid);
		//プレイヤーデータが無い場合は処理終了
		if(playerdata == null){
			return null;
		}

		final Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニューB");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<>();

		//flyflag/flytimeのメニュー表示用変換
		final String Flyallows ;
		if(playerdata.flyflag){
			Flyallows = "ON" ;
		}else{
			Flyallows = "OFF" ;
		}
		final String FlyTime ;
		if(playerdata.Endlessfly){
			FlyTime = "∞" ;
		}else {
			FlyTime = String.valueOf(playerdata.flytime);
		}

		final String ZSSkill ;
		if(playerdata.ZoneSetSkillFlag){
			ZSSkill = "ON" ;
		}else {
			ZSSkill = "OFF" ;
		}

		final String ZSSkill_Minestack;
		if(playerdata.zs_minestack_flag){
			ZSSkill_Minestack = "ON";
		}else{
			ZSSkill_Minestack = "OFF";
		}



//		int prank = Util.calcPlayerRank(player);
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.name + "の統計データ");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "建築レベル:" + playerdata.level
//				, ChatColor.RESET + "" +  ChatColor.AQUA + "次のレベルまで:" + (BuildAssist.levellist.get(playerdata.level + 1).intValue() - playerdata.totalbuildnum)
				, ChatColor.RESET + "" +  ChatColor.AQUA + "総建築量:" + playerdata.totalbuildnum.doubleValue()
//				, ChatColor.RESET + "" +  ChatColor.GOLD + "ランキング：" + prank + "位" + ChatColor.RESET + "" +  ChatColor.GRAY + "(" + BuildAssist.ranklist.size() +"人中)"

				));
		/*
		if(prank > 1){
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + (prank-1) + "位との差：" + (BuildAssist.ranklist.get(prank-2).intValue() - playerdata.totalbuildnum));
		}
		*/
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※1分毎に更新");

		skullmeta.setLore(lore);
		skullmeta.setOwner(playerdata.name);
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,0,itemstack);


		//FLY 状況表示
		itemstack = new ItemStack(Material.COOKED_CHICKEN,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKED_CHICKEN);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "FLY機能 情報表示");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "" + "FLY 効果："+ Flyallows
				, ChatColor.RESET + "" + ChatColor.AQUA + "FLY 残り時間："+ FlyTime );
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);

		//1分限定、FLY ON
		itemstack = new ItemStack(Material.FEATHER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.FEATHER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "FLY機能、ON" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "(1分)");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.YELLOW + "クリックすると以降1分間に渡り"
				, ChatColor.RESET + "" + ChatColor.YELLOW + "経験値を消費しつつFLYが可能になります。"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "必要経験値量：毎分 "+BuildAssist.config.getFlyExp());
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(3,itemstack);

		//5分限定、FLY ON
		itemstack = new ItemStack(Material.FEATHER,5);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.FEATHER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "FLY機能、ON" + ChatColor.GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "(5分)");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.YELLOW + "クリックすると以降5分間に渡り"
				, ChatColor.RESET + "" + ChatColor.YELLOW + "経験値を消費しつつFLYが可能になります。"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "必要経験値量：毎分 "+BuildAssist.config.getFlyExp());
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);

		//エンドレス、FLY ON
		itemstack = new ItemStack(Material.ELYTRA,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ELYTRA);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "FLY機能、ON" + ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "(無制限)");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.YELLOW + "クリックすると以降OFFにするまで"
				, ChatColor.RESET + "" + ChatColor.YELLOW + "経験値を消費しつつFLYが可能になります。"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "必要経験値量：毎分 "+BuildAssist.config.getFlyExp());
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(5,itemstack);

		//FLY 効果 OFF
		itemstack = new ItemStack(Material.CHAINMAIL_BOOTS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHAINMAIL_BOOTS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "FLY機能、OFF");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "クリックすると、残り時間に関わらず"
				, ChatColor.RESET + "" + ChatColor.RED + "FLYを終了します。") ;
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(6,itemstack);

		//範囲設置スキル ON/OFFボタン
		itemstack = new ItemStack(Material.STONE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
		itemmeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「範囲設置スキル」現在：" + ZSSkill );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.YELLOW + "「スニーク+左クリック」をすると、"
				, ChatColor.RESET + "" + ChatColor.YELLOW + "オフハンドに持っているブロックと同じ物を"
				, ChatColor.RESET + "" + ChatColor.YELLOW  + "インベントリ内から消費し設置します。"
				, ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "＜クリックでON/OFF切り替え＞"
				, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getZoneSetSkillLevel() + "以上で利用可能");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(18,itemstack);


		//範囲設置スキル 設定画面移動
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「範囲設置スキル」設定画面へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
							,ChatColor.RESET + "" + ChatColor.GRAY + "現在の設定"
							,ChatColor.RESET + "" + ChatColor.GRAY + "MineStack優先設定:" + ZSSkill_Minestack);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_Exclamation");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,19,itemstack);


		//ブロックを並べるスキル設定
		itemstack = new ItemStack(Material.WOOD,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WOOD);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブロックを並べるスキル（仮） ：" + BuildAssist.line_up_str[playerdata.line_up_flg]);
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "オフハンドに木の棒、メインハンドに設置したいブロックを持って"
				, ChatColor.RESET + "" + ChatColor.GRAY + "左クリックすると向いてる方向に並べて設置します。"
				, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getblocklineuplevel() + "以上で利用可能"
				, ChatColor.RESET + "" + ChatColor.GRAY + "クリックで切り替え"
//				, ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "※スキル発動時にマナを消費します。 最大消費マナ："+(BuildAssist.config.getblocklineupmana_mag()*64)

				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(27,itemstack);

		//ブロックを並べる設定メニューへ
		itemstack = new ItemStack(Material.PAPER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ブロックを並べるスキル（仮） 」設定画面へ");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				, ChatColor.RESET + "" + ChatColor.GRAY + "現在の設定"
				, ChatColor.RESET + "" + ChatColor.GRAY + "スキル設定 ：" + BuildAssist.line_up_str[playerdata.line_up_flg]
				, ChatColor.RESET + "" + ChatColor.GRAY + "ハーフブロック設定 ：" + BuildAssist.line_up_step_str[playerdata.line_up_step_flg]
				, ChatColor.RESET + "" + ChatColor.GRAY + "破壊設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_des_flg]
				, ChatColor.RESET + "" + ChatColor.GRAY + "MineStack優先設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_minestack_flg]
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(28,itemstack);

		//MineStackブロック一括クラフトメニュー画面へ
		itemstack = new ItemStack(Material.WORKBENCH,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WORKBENCH);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト画面へ");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(35,itemstack);

		return inventory;

	}


	public static Inventory getSetBlockSkillData(final Player p) {
		//プレイヤーを取得
		final Player player = p.getPlayer();
		//UUID取得
		final UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		final PlayerData playerdata = BuildAssist.playermap.get(uuid);

		final Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "「範囲設置スキル」設定画面");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		final String ZSSkill ;
		if(playerdata.ZoneSetSkillFlag){
			ZSSkill = "ON" ;
		}else {
			ZSSkill = "OFF" ;
		}


		final String ZSDirt ;
		if(playerdata.zsSkillDirtFlag){
			ZSDirt = "ON" ;
		}else {
			ZSDirt = "OFF" ;
		}

		final String ZSSkill_Minestack;
		if(playerdata.zs_minestack_flag){
			ZSSkill_Minestack = "ON";
		}else{
			ZSSkill_Minestack = "OFF";
		}

		final int ZSSkillA =(playerdata.AREAint) * 2 + 1;



		//初期画面へ移動
		itemstack = new ItemStack(Material.BARRIER,1);
		itemmeta =Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
		itemstack.setDurability((short) 3);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "元のページへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);


		//土設置のON/OFF
		itemstack = new ItemStack(Material.DIRT,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "設置時に下の空洞を埋める機能");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "機能の使用設定：" + ZSDirt
							,ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "機能の範囲：地下5マスまで");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);


		//設定状況の表示
		itemstack = new ItemStack(Material.STONE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在の設定は以下の通りです");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "スキルの使用設定：" + ZSSkill
							,ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "スキルの範囲設定：" + ZSSkillA + "×" + ZSSkillA
							,ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "MineStack優先設定:" + ZSSkill_Minestack);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(13,itemstack);


		//範囲をMAXへ
		itemstack = new ItemStack(Material.SKULL_ITEM,11);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を最大値に変更");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA
							,ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定：11×11");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowUp");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,19,itemstack);


		//範囲を一段階増加
		itemstack = new ItemStack(Material.SKULL_ITEM,7);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を一段階大きくする");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA
							,ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定："+ (ZSSkillA + 2) +"×"+(ZSSkillA + 2)
							,ChatColor.RESET + "" +  ChatColor.RED + "" + "※範囲設定の最大値は11×11※");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowUp");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,20,itemstack);


		//範囲を初期値へ
		itemstack = new ItemStack(Material.SKULL_ITEM,5);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を初期値に変更");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA
							,ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定：5×5");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_TNT");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,22,itemstack);


		//範囲を一段階減少
		itemstack = new ItemStack(Material.SKULL_ITEM,3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を一段階小さくする");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA
							,ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定："+ (ZSSkillA - 2) +"×"+(ZSSkillA - 2)
							,ChatColor.RESET + "" +  ChatColor.RED + "" + "※範囲設定の最小値は3×3※");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,24,itemstack);


		//範囲をMINへ
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "範囲設定を最小値に変更");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "" + "現在の範囲設定：" + ZSSkillA + "×" + ZSSkillA
							,ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "変更後の範囲設定：3×3");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,25,itemstack);

		//35番目にMineStack優先設定を追加
		//MineStackの方を優先して消費する設定
		itemstack = new ItemStack(Material.CHEST,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack優先設定：" + ZSSkill_Minestack);
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "スキルでブロックを並べるとき"
				, ChatColor.RESET + "" + ChatColor.GRAY + "MineStackの在庫を優先して消費します。"
				, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getZoneskillMinestacklevel() + "以上で利用可能"
				, ChatColor.RESET + "" + ChatColor.GRAY + "クリックで切り替え"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(35,itemstack);

		return inventory;
	}

	//ブロックを並べる設定メニュー
	public static Inventory getBlockLineUpData(final Player p) {
		//プレイヤーを取得
		final Player player = p.getPlayer();
		//UUID取得
		final UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		final PlayerData playerdata = BuildAssist.playermap.get(uuid);

		final Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "「ブロックを並べるスキル（仮）」設定");
		ItemStack itemstack;
		ItemMeta itemmeta;
		final SkullMeta skullmeta;
		List<String> lore;

		// ホームを開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack);

		//ブロックを並べるスキル設定
		itemstack = new ItemStack(Material.WOOD,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WOOD);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブロックを並べるスキル（仮） ：" + BuildAssist.line_up_str[playerdata.line_up_flg]);
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "オフハンドに木の棒、メインハンドに設置したいブロックを持って"
				, ChatColor.RESET + "" + ChatColor.GRAY + "左クリックすると向いてる方向に並べて設置します。"
				, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getblocklineuplevel() + "以上で利用可能"
				, ChatColor.RESET + "" + ChatColor.GRAY + "クリックで切り替え"
//				, ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "※スキル発動時にマナを消費します。 最大消費マナ："+(BuildAssist.config.getblocklineupmana_mag()*64)

				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		//ブロックを並べるスキルハーフブロック設定
		itemstack = new ItemStack(Material.STEP,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STEP);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ハーフブロック設定 ：" + BuildAssist.line_up_step_str[playerdata.line_up_step_flg]);
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ハーフブロックを並べる時の位置を決めます。"
				, ChatColor.RESET + "" + ChatColor.GRAY + "クリックで切り替え"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);

		//ブロックを並べるスキル一部ブロックを破壊して並べる設定
		itemstack = new ItemStack(Material.TNT,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.TNT);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "破壊設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_des_flg]);
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ブロックを並べるとき特定のブロックを破壊して並べます。"
				, ChatColor.RESET + "" + ChatColor.GRAY + "破壊対象ブロック：草,花,水,雪,松明,きのこ"
				, ChatColor.RESET + "" + ChatColor.GRAY + "クリックで切り替え"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);

		//MineStackの方を優先して消費する設定
		itemstack = new ItemStack(Material.CHEST,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack優先設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_minestack_flg]);
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "スキルでブロックを並べるとき"
				, ChatColor.RESET + "" + ChatColor.GRAY + "MineStackの在庫を優先して消費します。"
				, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getblocklineupMinestacklevel() + "以上で利用可能"
				, ChatColor.RESET + "" + ChatColor.GRAY + "クリックで切り替え"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8,itemstack);

		return inventory;
	}


	//MineStackブロック一括クラフトメニュー
	public static Inventory getBlockCraftData(final Player p) {
		//プレイヤーを取得
		final Player player = p.getPlayer();
		//UUID取得
		final UUID uuid = player.getUniqueId();
		final com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

		final Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト1");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		// ホーム目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,45,itemstack);

		// 2ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "2ページ目へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,53,itemstack);

		//石を石ハーフブロックに変換10～10万
		long num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("stone"));
		long num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("step0"));
		for(int x = 1 ; x <= 5 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.STEP,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STEP);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石を石ハーフブロックに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "石"+ p10 +"個→石ハーフブロック"+ (p10 * 2) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "石の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "石ハーフブロックの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(1) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x-1 , itemstack);
		}


		//石を石レンガに変換10～10万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("stone"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("smooth_brick0"));
		for(int x = 1 ; x <= 5 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.SMOOTH_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SMOOTH_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石を石レンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "石"+ p10 +"個→石レンガ"+ (p10) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "石の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "石レンガの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(1) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+8 , itemstack);
		}

		//花崗岩を磨かれた花崗岩に変換10～1万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("granite"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("polished_granite"));
		for(int x = 1 ; x <= 4 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.STONE,x,(short)2);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "花崗岩を磨かれた花崗岩に変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "花崗岩"+ p10 +"個→磨かれた花崗岩"+ (p10) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "花崗岩の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "磨かれた花崗岩の数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+17 , itemstack);
		}

		//閃緑岩を磨かれた閃緑岩に変換10～1万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("diorite"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("polished_diorite"));
		for(int x = 1 ; x <= 4 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.STONE,x,(short)4);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "閃緑岩を磨かれた閃緑岩に変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "閃緑岩"+ p10 +"個→磨かれた閃緑岩"+ (p10) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "閃緑岩の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "磨かれた閃緑岩の数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+22 , itemstack);
		}

		//安山岩を磨かれた安山岩に変換10～1万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("andesite"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("polished_andesite"));
		for(int x = 1 ; x <= 4 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.STONE,x,(short)6);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "安山岩を磨かれた安山岩に変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "安山岩"+ p10 +"個→磨かれた安山岩"+ (p10) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "安山岩の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "磨かれた安山岩の数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+26 , itemstack);
		}

		//ネザー水晶をネザー水晶ブロックに変換10～1万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("quartz"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("quartz_block"));
		for(int x = 1 ; x <= 4 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.QUARTZ_BLOCK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.QUARTZ_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザー水晶をネザー水晶ブロックに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ネザー水晶"+ (p10 * 4) +"個→ネザー水晶ブロック"+ (p10) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザー水晶の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザー水晶ブロックの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+31 , itemstack);
		}

		//レンガをレンガブロックに変換10～1万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("brick_item"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("brick"));
		for(int x = 1 ; x <= 4 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "レンガをレンガブロックに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "レンガ"+ (p10 * 4) +"個→レンガブロック"+ (p10) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "レンガの数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "レンガブロックの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+35 , itemstack);
		}

		//ネザーレンガをネザーレンガブロックに変換10～1万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("nether_brick"));
		for(int x = 1 ; x <= 4 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.NETHER_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザーレンガをネザーレンガブロックに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ネザーレンガ"+ (p10 * 4) +"個→ネザーレンガブロック"+ (p10) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザーレンガの数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザーレンガブロックの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+40 , itemstack);
		}
		return inventory;
	}

	//MineStackブロック一括クラフトメニュー2
	public static Inventory getBlockCraftData2(final Player p){
		//プレイヤーを取得
		final Player player = p.getPlayer();
		//UUID取得
		final UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		final com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

		final Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト2");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = ItemMetaFactory.SKULL.getValue();
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "1ページ目へ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowUp");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,45,itemstack);

		// 3ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = ItemMetaFactory.SKULL.getValue();
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "3ページ目へ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,53,itemstack);

		//雪玉を雪（ブロック）に変換10～1万
		long num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("snow_ball"));
		long num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("snow_block"));
		long num_3;
		for(int x = 1 ; x <= 4 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.SNOW_BLOCK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SNOW_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "雪玉を雪（ブロック）に変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "雪玉"+ (p10 * 4) +"個→雪（ブロック）"+ p10 +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "雪玉の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "雪（ブロック）の数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x-1 , itemstack);
		}


		//ネザーウォートとネザーレンガを赤いネザーレンガに変換10～10万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("nether_stalk"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("red_nether_brick"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item"));
		for(int x = 1 ; x <= 5 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.RED_NETHER_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.RED_NETHER_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザーウォートとネザーレンガを赤いネザーレンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ネザーウォート"+ (p10 * 2) +"個+ネザーレンガ"+ (p10 * 2) +"個→赤いネザーレンガ"+ (p10) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザーウォートの数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザーレンガの数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "赤いネザーレンガの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(2) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+4 , itemstack);
		}

		//石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("iron_ore"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("iron_ingot"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("coal"));
		for(int x = 0 ; x <= 3 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.IRON_INGOT,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_INGOT);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して鉄鉱石を鉄インゴットに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "鉄鉱石"+ (p10 * 4) +"個+石炭"+ p10 +"個→鉄インゴット"+ (p10 * 4) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "鉄鉱石の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "石炭の数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "鉄インゴットの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+9 , itemstack);
		}

		//溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("iron_ore"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("iron_ingot"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"));
		for(int x = 0 ; x <= 3 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.IRON_INGOT,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_INGOT);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して鉄鉱石を鉄インゴットに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "鉄鉱石"+ (p10 * 50) +"個+溶岩バケツ"+ p10 +"個→鉄インゴット"+ (p10 * 50) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "鉄鉱石の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "溶岩バケツの数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "鉄インゴットの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+14 , itemstack);
		}

		//石炭を消費して金鉱石を金インゴットに変換4～4000
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("gold_ore"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("gold_ingot"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("coal"));
		for(int x = 0 ; x <= 3 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.GOLD_INGOT,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して金鉱石を金インゴットに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "金鉱石"+ (p10 * 4) +"個+石炭"+ p10 +"個→金インゴット"+ (p10 * 4) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "金鉱石の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "石炭の数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "金インゴットの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+18 , itemstack);
		}

		//溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("gold_ore"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("gold_ingot"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"));
		for(int x = 0 ; x <= 3 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.GOLD_INGOT,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して金鉱石を金インゴットに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "金鉱石"+ (p10 * 50) +"個+溶岩バケツ"+ p10 +"個→金インゴット"+ (p10 * 50) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "金鉱石の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "溶岩バケツの数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "金インゴットの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+23 , itemstack);
		}

		//石炭を消費して砂をガラスに変換4～4000
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("sand"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("glass"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("coal"));
		for(int x = 0 ; x <= 3 ; x++) {
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.GLASS,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して砂をガラスに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "砂"+ (p10 * 4) +"個+石炭"+ p10 +"個→ガラス"+ (p10 * 4) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "砂の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "石炭の数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "ガラスの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+27 , itemstack);
		}

		//溶岩バケツを消費して砂をガラスに変換50～5万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("sand"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("glass"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"));
		for(int x = 0 ; x <= 3 ; x++) {
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.GLASS,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して砂をガラスに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "砂"+ (p10 * 50) +"個+溶岩バケツ"+ p10 +"個→ガラス"+ (p10 * 50) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "砂の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "溶岩バケツの数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "ガラスの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+32 , itemstack);
		}

		//石炭を消費してネザーラックをネザーレンガに変換4～4000
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("netherrack"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("coal"));
		for(int x = 0 ; x <= 3 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.NETHER_BRICK_ITEM,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK_ITEM);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費してネザーラックをネザーレンガに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "ネザーラック"+ (p10 * 4) +"個+石炭"+ p10 +"個→ネザーレンガ"+ (p10 * 4) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザーラックの数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "石炭の数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザーレンガの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+36 , itemstack);
		}

		//溶岩バケツを消費してネザーラックをネザーレンガに変換50～5万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("netherrack"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("nether_brick_item"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"));
		for(int x = 0 ; x <= 3 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.NETHER_BRICK_ITEM,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK_ITEM);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費してネザーラックをネザーレンガに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "ネザーラック"+ (p10 * 50) +"個+溶岩バケツ"+ p10 +"個→ネザーレンガ"+ (p10 * 50) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザーラックの数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "溶岩バケツの数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "ネザーレンガの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+41 , itemstack);
		}
		return inventory;
	}

	//MineStackブロック一括クラフトメニュー3
	public static Inventory getBlockCraftData3(final Player p){
		//プレイヤーを取得
		final Player player = p.getPlayer();
		//UUID取得
		final UUID uuid = player.getUniqueId();
		final com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

		final Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト3");
		ItemStack itemstack;
		ItemMeta itemmeta;
		final SkullMeta skullmeta;
		List<String> lore;

		// 2ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = ItemMetaFactory.SKULL.getValue(); // (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "2ページ目へ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowUp");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,45,itemstack);

		//石炭を消費して粘土をレンガに変換4～4000
		long num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("clay_ball"));
		long num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("brick_item"));
		long num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("coal"));
		for(int x = 0 ; x <= 3 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.CLAY_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CLAY_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して粘土をレンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "粘土"+ (p10 * 4) +"個+石炭"+ p10 +"個→レンガ"+ (p10 * 4) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "粘土の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "石炭の数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "レンガの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x , itemstack);
		}

		//溶岩バケツを消費して粘土をレンガに変換50～5万
		num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("clay_ball"));
		num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("brick_item"));
		num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("lava_bucket"));
		for(int x = 0 ; x <= 3 ; x++){
			final int p10 = power10.get(x);
			itemstack = new ItemStack(Material.CLAY_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CLAY_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して粘土をレンガに変換します" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GRAY + "粘土"+ (p10 * 50) +"個+溶岩バケツ"+ p10 +"個→レンガ"+ (p10 * 50) +"個"
					, ChatColor.RESET + "" + ChatColor.GRAY + "粘土の数:" + String.format("%,d",num_1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "溶岩バケツの数:" + String.format("%,d",num_3)
					, ChatColor.RESET + "" + ChatColor.GRAY + "レンガの数:" + String.format("%,d",num_2)
					, ChatColor.RESET + "" + ChatColor.GRAY + "建築LV" + BuildAssist.config.getMinestackBlockCraftlevel(3) + "以上で利用可能"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変換"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(x+5 , itemstack);
		}
		return inventory;
	}

	/*
	 * 10^11 > 2^32 > 10^10
	 */
	private static final int limit = 10;

	/**
	 * インデックス0 = 10 ** 0 (-> 1)
	 * インデックス1 = 10 ** 1 (-> 10)
	 * インデックス2 = 10 ** 2 (-> 100)
	 * ...
	 * インデックスn = 10 ** n
	 */
	private static final List<Integer> power10 = IntStream.iterate(1, operand -> operand * 10)
			.limit(limit)
			.collect(() -> new ArrayList<>(limit), ArrayList::add, ArrayList::addAll);
}
