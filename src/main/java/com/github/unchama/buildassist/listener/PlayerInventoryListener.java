package com.github.unchama.buildassist.listener;

import java.util.HashMap;
import java.util.UUID;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.ExperienceManager;
import com.github.unchama.buildassist.MenuInventoryData;
import com.github.unchama.buildassist.PlayerData;
import com.github.unchama.buildassist.Util;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.minestack.MineStackObj;

public class PlayerInventoryListener implements Listener {
	HashMap<UUID, PlayerData> playermap = BuildAssist.playermap;

	/*
	//プレイヤーが4次元ポケットを閉じた時に実行
	@EventHandler
	public void onPlayerPortalCloseEvent(InventoryCloseEvent event){
		HumanEntity he = event.getPlayer();
		Inventory inventory = event.getInventory();

		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}
		//インベントリサイズが２７でない時終了
		if(inventory.getSize() != 27){
			return;
		}
		if(inventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "4次元ポケット")){
			Player player = (Player)he;
			PlayerInventory pinventory = player.getInventory();
			ItemStack itemstack = pinventory.getItemInMainHand();
			if(itemstack.getType().equals(Material.ENDER_PORTAL_FRAME)){
				//閉まる音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_CLOSE, 1, (float) 0.1);
			}
		}
	}
	*/

	@EventHandler
	public void onPlayerClickActiveSkillSellectEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 36){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//プレイヤーデータが無い場合は処理終了
		if(playerdata == null){
			return;
		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニューB")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */


			if(itemstackcurrent.getType().equals(Material.FEATHER)){
				if(itemstackcurrent.getAmount() == 1){
					//fly 1分予約追加
					player.closeInventory();
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.chat("/fly 1");
				}else if(itemstackcurrent.getAmount() == 5){
					//fly 5分予約追加
					player.closeInventory();
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.chat("/fly 5");
				}

			} else if (itemstackcurrent.getType().equals(Material.ELYTRA)){
				//fly ENDLESSモード
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/fly endless");

			} else if (itemstackcurrent.getType().equals(Material.CHAINMAIL_BOOTS)){
				//fly OFF
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/fly finish");

			} else if (itemstackcurrent.getType().equals(Material.STONE)){
				//範囲設置スキル ON/OFF
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(playerdata.level < BuildAssist.config.getZoneSetSkillLevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					if(!playerdata.ZoneSetSkillFlag){
						playerdata.ZoneSetSkillFlag = true ;
						player.sendMessage(ChatColor.RED + "範囲設置スキルON" ) ;
						player.openInventory(MenuInventoryData.getMenuData(player));
					}else {
						playerdata.ZoneSetSkillFlag = false ;
						player.sendMessage(ChatColor.RED + "範囲設置スキルOFF" ) ;
						player.openInventory(MenuInventoryData.getMenuData(player));
					}
				}


			} else if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && itemstackcurrent.getItemMeta().getDisplayName().contains("「範囲設置スキル」設定画面へ")){
				//範囲設置スキル設定画面を開く
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				if(playerdata.level < BuildAssist.config.getblocklineuplevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
				}
			} else if (itemstackcurrent.getType().equals(Material.WOOD)){
				//ブロックを並べるスキル設定
				if(playerdata.level < BuildAssist.config.getblocklineuplevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					if ( playerdata.line_up_flg >= 2 ){
						playerdata.line_up_flg = 0;
					}else{
						playerdata.line_up_flg++;
					}
					player.sendMessage(ChatColor.GREEN + "ブロックを並べるスキル（仮） ：" + BuildAssist.line_up_str[playerdata.line_up_flg] ) ;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.openInventory(MenuInventoryData.getMenuData(player));
				}

			} else if (itemstackcurrent.getType().equals(Material.PAPER)){
				//ブロックを並べる設定メニューを開く
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockLineUpData(player));

			} else if (itemstackcurrent.getType().equals(Material.WORKBENCH)){
				//MineStackブロック一括クラフトメニュー画面へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockCraftData(player));

			}




		}
		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "「範囲設置スキル」設定画面")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType().equals(Material.BARRIER)){
				//ホームメニューへ帰還
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));

			}else if(itemstackcurrent.getType().equals(Material.SKULL_ITEM)) {
				if(itemstackcurrent.getAmount() == 11){
					//範囲MAX
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					playerdata.AREAint = 5;
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));

				}else if(itemstackcurrent.getAmount() == 7){
					//範囲++
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if(playerdata.AREAint == 5){
						player.sendMessage(ChatColor.RED + "[範囲スキル設定]これ以上範囲を広くできません！" ) ;
					}else {
						playerdata.AREAint ++ ;
					}
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));

				}else if(itemstackcurrent.getAmount() == 5){
					//範囲初期化
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					playerdata.AREAint = 2;
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));

				}else if(itemstackcurrent.getAmount() == 3){
					//範囲--
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if(playerdata.AREAint == 1){
						player.sendMessage(ChatColor.RED + "[範囲スキル設定]これ以上範囲を狭くできません！" ) ;
					}else {
						playerdata.AREAint -- ;
					}
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));

				}else if(itemstackcurrent.getAmount() == 1){
					//範囲MIN
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					playerdata.AREAint = 1;
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
				}
			} else if (itemstackcurrent.getType().equals(Material.STONE)){
				//範囲設置スキル ON/OFF
				//範囲設置スキル ON/OFF
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(playerdata.level < BuildAssist.config.getZoneSetSkillLevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					if(!playerdata.ZoneSetSkillFlag){
						playerdata.ZoneSetSkillFlag = true ;
						player.sendMessage(ChatColor.RED + "範囲設置スキルON" ) ;
						player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
					}else {
						playerdata.ZoneSetSkillFlag = false ;
						player.sendMessage(ChatColor.RED + "範囲設置スキルOFF" ) ;
						player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
					}
				}


			} else if (itemstackcurrent.getType().equals(Material.DIRT)){
				//範囲設置スキル、土設置 ON/OFF
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(!playerdata.zsSkillDirtFlag){
					playerdata.zsSkillDirtFlag = true ;
					player.sendMessage(ChatColor.RED + "土設置機能ON" ) ;
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
				}else {
					playerdata.zsSkillDirtFlag = false ;
					player.sendMessage(ChatColor.RED + "土設置機能OFF" ) ;
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
				}
			}else if(itemstackcurrent.getType().equals(Material.CHEST)){
				//MineStack優先設定
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(playerdata.level < BuildAssist.config.getZoneskillMinestacklevel()){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					if(playerdata.zs_minestack_flag){
						playerdata.zs_minestack_flag = false;
						player.sendMessage(ChatColor.RED + "MineStack優先設定OFF");
						player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
					}else{
						playerdata.zs_minestack_flag = true;
						player.sendMessage(ChatColor.RED + "MineStack優先設定ON");
						player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
					}
				}
			}

		}

	}


	//ブロックを並べるスキル（仮）設定画面
	@EventHandler
	public void onPlayerClickBlockLineUpEvent(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが36でない時終了
		if(topinventory.getSize() != 36){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//プレイヤーデータが無い場合は処理終了
		if(playerdata == null){
			return;
		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "「ブロックを並べるスキル（仮）」設定")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM)){
				//ホームメニューへ帰還
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));

			} else if (itemstackcurrent.getType().equals(Material.WOOD)){
				//ブロックを並べるスキル設定
				if(playerdata.level < BuildAssist.config.getblocklineuplevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					if ( playerdata.line_up_flg >= 2 ){
						playerdata.line_up_flg = 0;
					}else{
						playerdata.line_up_flg++;
					}
					player.sendMessage(ChatColor.GREEN + "ブロックを並べるスキル（仮） ：" + BuildAssist.line_up_str[playerdata.line_up_flg] ) ;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.openInventory(MenuInventoryData.getBlockLineUpData(player));
				}

			} else if (itemstackcurrent.getType().equals(Material.STEP)){
				//ブロックを並べるスキルハーフブロック設定
				if ( playerdata.line_up_step_flg >= 2 ){
					playerdata.line_up_step_flg = 0;
				}else{
					playerdata.line_up_step_flg++;
				}
				player.sendMessage(ChatColor.GREEN + "ハーフブロック設定 ：" + BuildAssist.line_up_step_str[playerdata.line_up_step_flg] ) ;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getBlockLineUpData(player));

			} else if (itemstackcurrent.getType().equals(Material.TNT)){
				//ブロックを並べるスキル一部ブロックを破壊して並べる設定
				playerdata.line_up_des_flg ^= 1;
				player.sendMessage(ChatColor.GREEN + "破壊設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_des_flg] ) ;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getBlockLineUpData(player));

			} else if (itemstackcurrent.getType().equals(Material.CHEST)){
				//マインスタックの方を優先して消費する設定
				if(playerdata.level < BuildAssist.config.getblocklineupMinestacklevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					playerdata.line_up_minestack_flg ^= 1;
					player.sendMessage(ChatColor.GREEN + "マインスタック優先設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_minestack_flg] ) ;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.openInventory(MenuInventoryData.getBlockLineUpData(player));
				}
			}
		}
	}



	//MineStackブロック一括クラフト画面1
	@EventHandler
	public void onPlayerClickBlockCraft(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが54でない時終了
		if(topinventory.getSize() != 54){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//プレイヤーデータが無い場合は処理終了
		if(playerdata == null){
			return;
		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト1")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft") ){
				//ホームメニューへ帰還
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getMenuData(player));

			} else if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown") ){
				//2ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockCraftData2(player));

				//石を石ハーフブロックに変換10～10万
			} else if (itemstackcurrent.getType().equals(Material.STEP)){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(1) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("stone");
					final MineStackObj id_2 = Util.findMineStackObjectByName("step0");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < (int)Math.pow(10, x)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) * 2 );
						player.sendMessage(ChatColor.GREEN + "石"+ (int)Math.pow(10, x) +"個→石ハーフブロック"+ ((int)Math.pow(10, x)*2) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//石を石レンガに変換10～10万
			} else if (itemstackcurrent.getType().equals(Material.SMOOTH_BRICK)){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(1) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("stone");
					final MineStackObj id_2 = Util.findMineStackObjectByName("smooth_brick0");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < (int)Math.pow(10, x)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "石"+ (int)Math.pow(10, x) +"個→石レンガ"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//花崗岩を磨かれた花崗岩に変換10～1万
			} else if (itemstackcurrent.getType().equals(Material.STONE) && (itemstackcurrent.getDurability() == 2 ) ){
//				player.sendMessage(ChatColor.RED + "data:"+itemstackcurrent.getDurability() );

				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("granite");
					final MineStackObj id_2 = Util.findMineStackObjectByName("polished_granite");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < (int)Math.pow(10, x)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "花崗岩"+ (int)Math.pow(10, x) +"個→磨かれた花崗岩"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//閃緑岩を磨かれた閃緑岩に変換10～1万
			} else if (itemstackcurrent.getType().equals(Material.STONE) && (itemstackcurrent.getDurability() == 4 ) ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("diorite");
					final MineStackObj id_2 = Util.findMineStackObjectByName("polished_diorite");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < (int)Math.pow(10, x)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "閃緑岩"+ (int)Math.pow(10, x) +"個→磨かれた閃緑岩"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//安山岩を磨かれた安山岩に変換10～1万
			} else if (itemstackcurrent.getType().equals(Material.STONE) && (itemstackcurrent.getDurability() == 6 ) ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("andesite");
					final MineStackObj id_2 = Util.findMineStackObjectByName("polished_andesite");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < (int)Math.pow(10, x)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "安山岩"+ (int)Math.pow(10, x) +"個→磨かれた安山岩"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//ネザー水晶をネザー水晶ブロックに変換10～1万
			} else if (itemstackcurrent.getType().equals(Material.QUARTZ_BLOCK) ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("quartz");
					final MineStackObj id_2 = Util.findMineStackObjectByName("quartz_block");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "ネザー水晶"+ ((int)Math.pow(10, x)*4) +"個→ネザー水晶ブロック"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//レンガをレンガブロックに変換10～1万
			} else if (itemstackcurrent.getType().equals(Material.BRICK) ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("brick_item");
					final MineStackObj id_2 = Util.findMineStackObjectByName("brick");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "レンガ"+ ((int)Math.pow(10, x)*4) +"個→レンガブロック"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}
				//ネザーレンガをネザーレンガブロックに変換10～1万
			} else if (itemstackcurrent.getType().equals(Material.NETHER_BRICK) ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("nether_brick_item");
					final MineStackObj id_2 = Util.findMineStackObjectByName("nether_brick");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "ネザーレンガ"+ ((int)Math.pow(10, x)*4) +"個→ネザーレンガブロック"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

			}

		}

	}


	//MineStackブロック一括クラフト画面2
	@EventHandler
	public void onPlayerClickBlockCraft2(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが54でない時終了
		if(topinventory.getSize() != 54){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//プレイヤーデータが無い場合は処理終了
		if(playerdata == null){
			return;
		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト2")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp") ){
				//1ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockCraftData(player));

			} else if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown") ){
				//3ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockCraftData3(player));

				//雪玉を雪（ブロック）に変換10～1万
			} else if (itemstackcurrent.getType().equals(Material.SNOW_BLOCK) ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("snow_ball");
					final MineStackObj id_2 = Util.findMineStackObjectByName("snow_block");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "雪玉"+ ((int)Math.pow(10, x)*4) +"個→雪（ブロック）"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//ネザーウォートとネザーレンガを赤いネザーレンガに変換10～10万
			} else if (itemstackcurrent.getType().equals(Material.RED_NETHER_BRICK) ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("nether_stalk");
					final MineStackObj id_2 = Util.findMineStackObjectByName("red_nether_brick");
					final MineStackObj id_3 = Util.findMineStackObjectByName("nether_brick_item");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*2) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < ((int)Math.pow(10, x)*2) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*2) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, ((int)Math.pow(10, x)*2) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "ネザーウォート"+ ((int)Math.pow(10, x)*2) +"個+ネザーレンガ"+ ((int)Math.pow(10, x)*2) +"個→赤いネザーレンガ"+ ((int)Math.pow(10, x)) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
			} else if (itemstackcurrent.getType().equals(Material.IRON_INGOT) && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("iron_ore");
					final MineStackObj id_2 = Util.findMineStackObjectByName("iron_ingot");
					final MineStackObj id_3 = Util.findMineStackObjectByName("coal");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*4) );
						player.sendMessage(ChatColor.GREEN + "鉄鉱石"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→鉄インゴット"+ ((int)Math.pow(10, x)*4) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
			} else if (itemstackcurrent.getType().equals(Material.IRON_INGOT) && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("iron_ore");
					final MineStackObj id_2 = Util.findMineStackObjectByName("iron_ingot");
					final MineStackObj id_3 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj id_4 = Util.findMineStackObjectByName("bucket");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*50) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().addStackedAmountOf(id_4, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "鉄鉱石"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→鉄インゴット"+ ((int)Math.pow(10, x)*50) +"個+バケツ"+ (int)Math.pow(10, x) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}


				//石炭を消費して金鉱石を金インゴットに変換4～4000
			} else if (itemstackcurrent.getType().equals(Material.GOLD_INGOT) && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("gold_ore");
					final MineStackObj id_2 = Util.findMineStackObjectByName("gold_ingot");
					final MineStackObj id_3 = Util.findMineStackObjectByName("coal");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*4) );
						player.sendMessage(ChatColor.GREEN + "金鉱石"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→金インゴット"+ ((int)Math.pow(10, x)*4) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
			} else if (itemstackcurrent.getType().equals(Material.GOLD_INGOT) && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("gold_ore");
					final MineStackObj id_2 = Util.findMineStackObjectByName("gold_ingot");
					final MineStackObj id_3 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj id_4 = Util.findMineStackObjectByName("bucket");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*50) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().addStackedAmountOf(id_4, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "金鉱石"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→金インゴット"+ ((int)Math.pow(10, x)*50) +"個+バケツ"+ (int)Math.pow(10, x) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}


				//石炭を消費して砂をガラスに変換4～4000
			} else if (itemstackcurrent.getType().equals(Material.GLASS) && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("sand");
					final MineStackObj id_2 = Util.findMineStackObjectByName("glass");
					final MineStackObj id_3 = Util.findMineStackObjectByName("coal");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*4) );
						player.sendMessage(ChatColor.GREEN + "砂"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→ガラス"+ ((int)Math.pow(10, x)*4) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//溶岩バケツを消費して砂をガラスに変換50～5万
			} else if (itemstackcurrent.getType().equals(Material.GLASS) && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("sand");
					final MineStackObj id_2 = Util.findMineStackObjectByName("glass");
					final MineStackObj id_3 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj id_4 = Util.findMineStackObjectByName("bucket");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*50) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().addStackedAmountOf(id_4, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "砂"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→ガラス"+ ((int)Math.pow(10, x)*50) +"個+バケツ"+ (int)Math.pow(10, x) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}


				//石炭を消費してネザーラックをネザーレンガに変換4～4000
			} else if (itemstackcurrent.getType().equals(Material.NETHER_BRICK_ITEM) && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("netherrack");
					final MineStackObj id_2 = Util.findMineStackObjectByName("nether_brick_item");
					final MineStackObj id_3 = Util.findMineStackObjectByName("coal");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*4) );
						player.sendMessage(ChatColor.GREEN + "ネザーラック"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→ネザーレンガ"+ ((int)Math.pow(10, x)*4) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//溶岩バケツを消費してネザーラックをネザーレンガに変換50～5万
			} else if (itemstackcurrent.getType().equals(Material.NETHER_BRICK_ITEM) && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("netherrack");
					final MineStackObj id_2 = Util.findMineStackObjectByName("nether_brick_item");
					final MineStackObj id_3 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj id_4 = Util.findMineStackObjectByName("bucket");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*50) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().addStackedAmountOf(id_4, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "ネザーラック"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→ネザーレンガ"+ ((int)Math.pow(10, x)*50) +"個+バケツ"+ (int)Math.pow(10, x) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}
			}
		}
	}

	//MineStackブロック一括クラフト画面3
	@EventHandler
	public void onPlayerClickBlockCraft3(InventoryClickEvent event){
		//外枠のクリック処理なら終了
		if(event.getClickedInventory() == null){
			return;
		}

		ItemStack itemstackcurrent = event.getCurrentItem();
		InventoryView view = event.getView();
		HumanEntity he = view.getPlayer();
		//インベントリを開けたのがプレイヤーではない時終了
		if(!he.getType().equals(EntityType.PLAYER)){
			return;
		}

		Inventory topinventory = view.getTopInventory();
		//インベントリが存在しない時終了
		if(topinventory == null){
			return;
		}
		//インベントリサイズが54でない時終了
		if(topinventory.getSize() != 54){
			return;
		}
		Player player = (Player)he;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//プレイヤーデータが無い場合は処理終了
		if(playerdata == null){
			return;
		}

		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackブロック一括クラフト3")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp") ){
				//2ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockCraftData2(player));

/*			} else if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown") ){
				//4ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockCraftData4(player));
*/

				//石炭を消費して粘土をレンガに変換4～4000
			} else if (itemstackcurrent.getType().equals(Material.CLAY_BRICK) && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("clay_ball");
					final MineStackObj id_2 = Util.findMineStackObjectByName("brick_item");
					final MineStackObj id_3 = Util.findMineStackObjectByName("coal");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*4) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*4) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*4) );
						player.sendMessage(ChatColor.GREEN + "粘土"+ ((int)Math.pow(10, x)*4) +"個+石炭"+ (int)Math.pow(10, x) +"個→レンガ"+ ((int)Math.pow(10, x)*4) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData3(player));
				}

				//溶岩バケツを消費して粘土をレンガに変換50～5万
			} else if ( itemstackcurrent.getType().equals(Material.CLAY_BRICK) && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj id_1 = Util.findMineStackObjectByName("clay_ball");
					final MineStackObj id_2 = Util.findMineStackObjectByName("brick_item");
					final MineStackObj id_3 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj id_4 = Util.findMineStackObjectByName("bucket");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(id_1) < ((int)Math.pow(10, x)*50) ) || ( playerdata_s.getMinestack().getStackedAmountOf(id_3) < (int)Math.pow(10, x) )){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(id_1, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().subtractStackedAmountOf(id_3, (int)Math.pow(10, x) );
						playerdata_s.getMinestack().addStackedAmountOf(id_2, ((int)Math.pow(10, x)*50) );
						playerdata_s.getMinestack().addStackedAmountOf(id_4, (int)Math.pow(10, x) );
						player.sendMessage(ChatColor.GREEN + "粘土"+ ((int)Math.pow(10, x)*50) +"個+溶岩バケツ"+ (int)Math.pow(10, x) +"個→レンガ"+ ((int)Math.pow(10, x)*50) +"個+バケツ"+ (int)Math.pow(10, x) +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData3(player));
				}
			}
		}
	}



}
