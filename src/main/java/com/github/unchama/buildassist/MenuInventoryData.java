package com.github.unchama.buildassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.github.unchama.buildassist.util.AsyncInventorySetter;
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

import com.github.unchama.seichiassist.SeichiAssist;

public class MenuInventoryData {

	public static Inventory getSetBlockSkillData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = BuildAssist.playermap.get(uuid);

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "「範囲設置スキル」設定画面");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		String ZSSkill ;
		if(playerdata.ZoneSetSkillFlag){
			ZSSkill = "ON" ;
		}else {
			ZSSkill = "OFF" ;
		}


		String ZSDirt ;
		if(playerdata.zsSkillDirtFlag){
			ZSDirt = "ON" ;
		}else {
			ZSDirt = "OFF" ;
		}

		String ZSSkill_Minestack;
		if(playerdata.zs_minestack_flag){
			ZSSkill_Minestack = "ON";
		}else{
			ZSSkill_Minestack = "OFF";
		}

		int ZSSkillA =(playerdata.AREAint) * 2 + 1;



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
	public static Inventory getBlockLineUpData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = BuildAssist.playermap.get(uuid);

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "「ブロックを並べるスキル（仮）」設定");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

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
	public static Inventory getBlockCraftData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
//		PlayerData playerdata = BuildAssist.playermap.get(uuid);
		com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト1");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		// ホーム目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
//		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,45,itemstack);

		// 2ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
//		itemstack.setDurability((short) 3);
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
			itemstack = new ItemStack(Material.STEP,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STEP);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石を石ハーフブロックに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "石"+ (int)Math.pow(10, x) +"個→石ハーフブロック"+ ((int)Math.pow(10, x)*2) +"個"
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
			itemstack = new ItemStack(Material.SMOOTH_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SMOOTH_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石を石レンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "石"+ (int)Math.pow(10, x) +"個→石レンガ"+ ((int)Math.pow(10, x)) +"個"
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
			itemstack = new ItemStack(Material.STONE,x,(short)2);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "花崗岩を磨かれた花崗岩に変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "花崗岩"+ (int)Math.pow(10, x) +"個→磨かれた花崗岩"+ ((int)Math.pow(10, x)) +"個"
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
			itemstack = new ItemStack(Material.STONE,x,(short)4);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "閃緑岩を磨かれた閃緑岩に変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "閃緑岩"+ (int)Math.pow(10, x) +"個→磨かれた閃緑岩"+ ((int)Math.pow(10, x)) +"個"
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
			itemstack = new ItemStack(Material.STONE,x,(short)6);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "安山岩を磨かれた安山岩に変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "安山岩"+ (int)Math.pow(10, x) +"個→磨かれた安山岩"+ ((int)Math.pow(10, x)) +"個"
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
			itemstack = new ItemStack(Material.QUARTZ_BLOCK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.QUARTZ_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザー水晶をネザー水晶ブロックに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ネザー水晶"+ ((int)Math.pow(10, x)*4) +"個→ネザー水晶ブロック"+ ((int)Math.pow(10, x)) +"個"
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
			itemstack = new ItemStack(Material.BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "レンガをレンガブロックに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "レンガ"+ ((int)Math.pow(10, x)*4) +"個→レンガブロック"+ ((int)Math.pow(10, x)) +"個"
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
			itemstack = new ItemStack(Material.NETHER_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザーレンガをネザーレンガブロックに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ネザーレンガ"+ ((int)Math.pow(10, x)*4) +"個→ネザーレンガブロック"+ ((int)Math.pow(10, x)) +"個"
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
	public static Inventory getBlockCraftData2(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
//		PlayerData playerdata = BuildAssist.playermap.get(uuid);
		com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト2");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
//		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "1ページ目へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowUp");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,45,itemstack);

		// 3ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
//		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "3ページ目へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,53,itemstack);

		//雪玉を雪（ブロック）に変換10～1万
		long num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("snow_ball"));
		long num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("snow_block"));
		long num_3;
		for(int x = 1 ; x <= 4 ; x++){
			itemstack = new ItemStack(Material.SNOW_BLOCK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SNOW_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "雪玉を雪（ブロック）に変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "雪玉"+ ((int)Math.pow(10, x)*4) +"個→雪（ブロック）"+ (int)Math.pow(10, x) +"個"
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
			itemstack = new ItemStack(Material.RED_NETHER_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.RED_NETHER_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ネザーウォートとネザーレンガを赤いネザーレンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ネザーウォート"+ ((int)Math.pow(10, x)*2) +"個+ネザーレンガ"+ ((int)Math.pow(10, x)*2) +"個→赤いネザーレンガ"+ ((int)Math.pow(10, x)) +"個"
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
			itemstack = new ItemStack(Material.IRON_INGOT,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_INGOT);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して鉄鉱石を鉄インゴットに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "鉄鉱石"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→鉄インゴット"+ ((int)Math.pow(10, x)*4) +"個"
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
			itemstack = new ItemStack(Material.IRON_INGOT,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_INGOT);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して鉄鉱石を鉄インゴットに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "鉄鉱石"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→鉄インゴット"+ ((int)Math.pow(10, x)*50) +"個"
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
			itemstack = new ItemStack(Material.GOLD_INGOT,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して金鉱石を金インゴットに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "金鉱石"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→金インゴット"+ ((int)Math.pow(10, x)*4) +"個"
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
			itemstack = new ItemStack(Material.GOLD_INGOT,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して金鉱石を金インゴットに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "金鉱石"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→金インゴット"+ ((int)Math.pow(10, x)*50) +"個"
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
		for(int x = 0 ; x <= 3 ; x++){
			itemstack = new ItemStack(Material.GLASS,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して砂をガラスに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "砂"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→ガラス"+ ((int)Math.pow(10, x)*4) +"個"
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
		for(int x = 0 ; x <= 3 ; x++){
			itemstack = new ItemStack(Material.GLASS,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して砂をガラスに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "砂"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→ガラス"+ ((int)Math.pow(10, x)*50) +"個"
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
			itemstack = new ItemStack(Material.NETHER_BRICK_ITEM,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK_ITEM);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費してネザーラックをネザーレンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ネザーラック"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→ネザーレンガ"+ ((int)Math.pow(10, x)*4) +"個"
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
			itemstack = new ItemStack(Material.NETHER_BRICK_ITEM,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_BRICK_ITEM);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費してネザーラックをネザーレンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ネザーラック"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→ネザーレンガ"+ ((int)Math.pow(10, x)*50) +"個"
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
	public static Inventory getBlockCraftData3(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
//		PlayerData playerdata = BuildAssist.playermap.get(uuid);
		com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト3");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<String>();

		// 2ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
//		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "2ページ目へ");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowUp");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,45,itemstack);

		//石炭を消費して粘土をレンガに変換4～4000
		long num_1 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("clay_ball"));
		long num_2 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("brick_item"));
		long num_3 = playerdata_s.getMinestack().getStackedAmountOf(Util.findMineStackObjectByName("coal"));
		for(int x = 0 ; x <= 3 ; x++){
			itemstack = new ItemStack(Material.CLAY_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CLAY_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "石炭を消費して粘土をレンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "粘土"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→レンガ"+ ((int)Math.pow(10, x)*4) +"個"
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
			itemstack = new ItemStack(Material.CLAY_BRICK,x);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CLAY_BRICK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "溶岩バケツを消費して粘土をレンガに変換します" );
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "粘土"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→レンガ"+ ((int)Math.pow(10, x)*50) +"個"
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

}
