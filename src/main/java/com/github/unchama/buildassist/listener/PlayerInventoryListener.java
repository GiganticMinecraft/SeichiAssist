package com.github.unchama.buildassist.listener;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.MenuInventoryData;
import com.github.unchama.buildassist.PlayerData;
import com.github.unchama.buildassist.Util;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.MineStack;
import com.github.unchama.seichiassist.minestack.MineStackObj;
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

import java.util.HashMap;
import java.util.UUID;

import static com.github.unchama.buildassist.PowerOf10.getPower10;

public class PlayerInventoryListener implements Listener {
	HashMap<UUID, PlayerData> playermap = BuildAssist.Companion.getPlayermap();

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
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_CLOSE, 1, 0.1f);
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}

			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */


			if(itemstackcurrent.getType() == Material.FEATHER){
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

			} else if (itemstackcurrent.getType() == Material.ELYTRA){
				//fly ENDLESSモード
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/fly endless");

			} else if (itemstackcurrent.getType() == Material.CHAINMAIL_BOOTS){
				//fly OFF
				player.closeInventory();
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.chat("/fly finish");

			} else if (itemstackcurrent.getType() == Material.STONE){
				//範囲設置スキル ON/OFF
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(playerdata.level < BuildAssist.Companion.getConfig().getZoneSetSkillLevel() ){
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


			} else if (itemstackcurrent.getType() == Material.SKULL_ITEM && itemstackcurrent.getItemMeta().getDisplayName().contains("「範囲設置スキル」設定画面へ")){
				//範囲設置スキル設定画面を開く
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				if(playerdata.level < BuildAssist.Companion.getConfig().getblocklineuplevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
				}
			} else if (itemstackcurrent.getType() == Material.WOOD){
				//ブロックを並べるスキル設定
				if(playerdata.level < BuildAssist.Companion.getConfig().getblocklineuplevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					if ( playerdata.line_up_flg >= 2 ){
						playerdata.line_up_flg = 0;
					}else{
						playerdata.line_up_flg++;
					}
					player.sendMessage(ChatColor.GREEN + "ブロックを並べるスキル（仮） ：" + BuildAssist.Companion.getLine_up_str()[playerdata.line_up_flg] ) ;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.openInventory(MenuInventoryData.getMenuData(player));
				}

			} else if (itemstackcurrent.getType() == Material.PAPER){
				//ブロックを並べる設定メニューを開く
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getBlockLineUpData(player));

			} else if (itemstackcurrent.getType() == Material.WORKBENCH){
				//MineStackブロック一括クラフトメニュー画面へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getBlockCraftData(player));

			}




		}
		//インベントリ名が以下の時処理
		if(topinventory.getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "「範囲設置スキル」設定画面")){
			event.setCancelled(true);

			//プレイヤーインベントリのクリックの場合終了
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

			if(itemstackcurrent.getType() == Material.BARRIER){
				//ホームメニューへ帰還
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getMenuData(player));

			}else if(itemstackcurrent.getType() == Material.SKULL_ITEM) {
				final int amount = itemstackcurrent.getAmount();
				if(amount == 11){
					//範囲MAX
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					playerdata.AREAint = 5;
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));

				}else if(amount == 7){
					//範囲++
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if(playerdata.AREAint == 5){
						player.sendMessage(ChatColor.RED + "[範囲スキル設定]これ以上範囲を広くできません！" ) ;
					}else {
						playerdata.AREAint ++ ;
					}
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));

				}else if(amount == 5){
					//範囲初期化
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					playerdata.AREAint = 2;
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));

				}else if(amount == 3){
					//範囲--
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if(playerdata.AREAint == 1){
						player.sendMessage(ChatColor.RED + "[範囲スキル設定]これ以上範囲を狭くできません！" ) ;
					}else {
						playerdata.AREAint -- ;
					}
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));

				}else if(amount == 1){
					//範囲MIN
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					playerdata.AREAint = 1;
					player.sendMessage(ChatColor.RED + "現在の範囲設定は"+(playerdata.AREAint *2 +1)+"×"+ (playerdata.AREAint *2 +1)+"です");
					player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
				}
			} else if (itemstackcurrent.getType() == Material.STONE){
				//範囲設置スキル ON/OFF
				//範囲設置スキル ON/OFF
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(playerdata.level < BuildAssist.Companion.getConfig().getZoneSetSkillLevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					if(!playerdata.ZoneSetSkillFlag) {
						playerdata.ZoneSetSkillFlag = true ;
						player.sendMessage(ChatColor.RED + "範囲設置スキルON" ) ;
						player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
					}else {
						playerdata.ZoneSetSkillFlag = false ;
						player.sendMessage(ChatColor.RED + "範囲設置スキルOFF" ) ;
						player.openInventory(MenuInventoryData.getSetBlockSkillData(player));
					}
				}


			} else if (itemstackcurrent.getType() == Material.DIRT){
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
			}else if(itemstackcurrent.getType() == Material.CHEST){
				//MineStack優先設定
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(playerdata.level < BuildAssist.Companion.getConfig().getZoneskillMinestacklevel()){
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}
			final Material type = itemstackcurrent.getType();
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(type == Material.SKULL_ITEM){
				//ホームメニューへ帰還
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getMenuData(player));
			} else if (type == Material.WOOD){
				//ブロックを並べるスキル設定
				if(playerdata.level < BuildAssist.Companion.getConfig().getblocklineuplevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					if ( playerdata.line_up_flg >= 2 ){
						playerdata.line_up_flg = 0;
					}else{
						playerdata.line_up_flg++;
					}
					player.sendMessage(ChatColor.GREEN + "ブロックを並べるスキル（仮） ：" + BuildAssist.Companion.getLine_up_str()[playerdata.line_up_flg] ) ;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					player.openInventory(MenuInventoryData.getBlockLineUpData(player));
				}

			} else if (type == Material.STEP){
				//ブロックを並べるスキルハーフブロック設定
				if ( playerdata.line_up_step_flg >= 2 ){
					playerdata.line_up_step_flg = 0;
				}else{
					playerdata.line_up_step_flg++;
				}
				player.sendMessage(ChatColor.GREEN + "ハーフブロック設定 ：" + BuildAssist.Companion.getLine_up_step_str()[playerdata.line_up_step_flg] ) ;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getBlockLineUpData(player));

			} else if (type == Material.TNT){
				//ブロックを並べるスキル一部ブロックを破壊して並べる設定
				playerdata.line_up_des_flg ^= 1;
				player.sendMessage(ChatColor.GREEN + "破壊設定 ：" + BuildAssist.Companion.getLine_up_off_on_str()[playerdata.line_up_des_flg] ) ;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				player.openInventory(MenuInventoryData.getBlockLineUpData(player));

			} else if (type == Material.CHEST){
				//マインスタックの方を優先して消費する設定
				if(playerdata.level < BuildAssist.Companion.getConfig().getblocklineupMinestacklevel() ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					playerdata.line_up_minestack_flg ^= 1;
					player.sendMessage(ChatColor.GREEN + "マインスタック優先設定 ：" + BuildAssist.Companion.getLine_up_off_on_str()[playerdata.line_up_minestack_flg] ) ;
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType() == Material.SKULL_ITEM && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowLeft") ){
				//ホームメニューへ帰還
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getMenuData(player));

			} else if (itemstackcurrent.getType() == Material.SKULL_ITEM && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown") ){
				//2ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getBlockCraftData2(player));

				//石を石ハーフブロックに変換10～10万
			} else if (itemstackcurrent.getType() == Material.STEP){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(1) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();

					final MineStackObj inStack = Util.findMineStackObjectByName("stone");
					final int inAmount = getPower10().get(x);

					final MineStackObj outStack = Util.findMineStackObjectByName("step0");
					final int outAmount = getPower10().get(x) * 2;
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack) < inAmount){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, inAmount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, outAmount );
						player.sendMessage(ChatColor.GREEN + "石"+ inAmount +"個→石ハーフブロック"+ outAmount +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//石を石レンガに変換10～10万
			} else if (itemstackcurrent.getType() == Material.SMOOTH_BRICK){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(1) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();

					final MineStackObj inStack = Util.findMineStackObjectByName("stone");
					final MineStackObj outStack = Util.findMineStackObjectByName("smooth_brick0");
					final int amount = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack) < amount) {
						player.sendMessage(ChatColor.RED + "アイテムが足りません");
					} else {
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, amount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, amount);
						player.sendMessage(ChatColor.GREEN + "石"+ amount +"個→石レンガ"+ amount +"個変換");
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//花崗岩を磨かれた花崗岩に変換10～1万
			} else if (itemstackcurrent.getType() == Material.STONE && (itemstackcurrent.getDurability() == 2 ) ){
//				player.sendMessage(ChatColor.RED + "data:"+itemstackcurrent.getDurability() );

				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();

					final MineStackObj inStack = Util.findMineStackObjectByName("granite");
					final MineStackObj outStack = Util.findMineStackObjectByName("polished_granite");
					final int amount = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if (playerdata_s.getMinestack().getStackedAmountOf(inStack) < amount) {
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					} else {
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, amount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, amount);
						player.sendMessage(ChatColor.GREEN + "花崗岩"+ amount +"個→磨かれた花崗岩"+ amount +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//閃緑岩を磨かれた閃緑岩に変換10～1万
			} else if (itemstackcurrent.getType() == Material.STONE && (itemstackcurrent.getDurability() == 4 ) ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();

					final MineStackObj inStack = Util.findMineStackObjectByName("diorite");
					final MineStackObj outStack = Util.findMineStackObjectByName("polished_diorite");
					final int amount = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack) < amount){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, amount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, amount);
						player.sendMessage(ChatColor.GREEN + "閃緑岩"+ amount +"個→磨かれた閃緑岩"+ amount +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//安山岩を磨かれた安山岩に変換10～1万
			} else if (itemstackcurrent.getType() == Material.STONE && (itemstackcurrent.getDurability() == 6 ) ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack = Util.findMineStackObjectByName("andesite");
					final MineStackObj outStack = Util.findMineStackObjectByName("polished_andesite");
					final int amount = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack) < amount){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, amount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, amount);
						player.sendMessage(ChatColor.GREEN + "安山岩"+ amount +"個→磨かれた安山岩"+ amount +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//ネザー水晶をネザー水晶ブロックに変換10～1万
			} else if (itemstackcurrent.getType() == Material.QUARTZ_BLOCK){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack = Util.findMineStackObjectByName("quartz");
					final int inAmount = getPower10().get(x) * 4;

					final MineStackObj outStack = Util.findMineStackObjectByName("quartz_block");
					final int outAmount = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack) < inAmount){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, inAmount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, outAmount);
						player.sendMessage(ChatColor.GREEN + "ネザー水晶"+ inAmount +"個→ネザー水晶ブロック"+ outAmount +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}

				//レンガをレンガブロックに変換10～1万
			} else if (itemstackcurrent.getType() == Material.BRICK){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack = Util.findMineStackObjectByName("brick_item");
					final int inAmount = getPower10().get(x) * 4;

					final MineStackObj outStack = Util.findMineStackObjectByName("brick");
					final int outAmount = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if (playerdata_s.getMinestack().getStackedAmountOf(inStack) < inAmount){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, inAmount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, outAmount);
						player.sendMessage(ChatColor.GREEN + "レンガ"+ inAmount +"個→レンガブロック"+ outAmount +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData(player));
				}
				//ネザーレンガをネザーレンガブロックに変換10～1万
			} else if (itemstackcurrent.getType() == Material.NETHER_BRICK){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack = Util.findMineStackObjectByName("nether_brick_item");
					final int inAmount = getPower10().get(x) * 4;

					final MineStackObj outStack = Util.findMineStackObjectByName("nether_brick");
					final int outAmount = getPower10().get(x);
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack) < inAmount){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, inAmount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, outAmount);
						player.sendMessage(ChatColor.GREEN + "ネザーレンガ"+ inAmount +"個→ネザーレンガブロック"+ outAmount +"個変換" );
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			final Material type = itemstackcurrent.getType();
			if(type == Material.SKULL_ITEM && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp") ){
				//1ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getBlockCraftData(player));

			} else if (type == Material.SKULL_ITEM && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown") ){
				//3ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getBlockCraftData3(player));

				//雪玉を雪（ブロック）に変換10～1万
			} else if (type == Material.SNOW_BLOCK){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack = Util.findMineStackObjectByName("snow_ball");
					final int inAmount = getPower10().get(x) * 4;

					final MineStackObj outStack = Util.findMineStackObjectByName("snow_block");
					final int outAmount = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack) < inAmount){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack, inAmount);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, outAmount);
						player.sendMessage(ChatColor.GREEN + "雪玉"+ inAmount +"個→雪（ブロック）"+ outAmount +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//ネザーウォートとネザーレンガを赤いネザーレンガに変換10～10万
			} else if (type == Material.RED_NETHER_BRICK){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(2) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();

					final MineStackObj inStack1 = Util.findMineStackObjectByName("nether_stalk");
					final int inAmount1 = getPower10().get(x) * 2;

					final MineStackObj inStack2 = Util.findMineStackObjectByName("nether_brick_item");
					final int inAmount2 = getPower10().get(x) * 2;

					final MineStackObj outStack = Util.findMineStackObjectByName("red_nether_brick");
					final int outAmount = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < inAmount1) || ( playerdata_s.getMinestack().getStackedAmountOf(inStack2) < inAmount2)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, inAmount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, inAmount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, outAmount);
						player.sendMessage(ChatColor.GREEN + "ネザーウォート"+ inAmount1 +"個+ネザーレンガ"+ inAmount2 +"個→赤いネザーレンガ"+ outAmount +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
			} else if (type == Material.IRON_INGOT && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack1 = Util.findMineStackObjectByName("iron_ore");
					final MineStackObj outStack = Util.findMineStackObjectByName("iron_ingot");
					final int amount1 = getPower10().get(x) * 4;

					final MineStackObj inStack2 = Util.findMineStackObjectByName("coal");
					final int inAmount2 = getPower10().get(x);

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1) || ( playerdata_s.getMinestack().getStackedAmountOf(inStack2) < inAmount2)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, inAmount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack, amount1);
						player.sendMessage(ChatColor.GREEN + "鉄鉱石"+ amount1 +"個+石炭"+ inAmount2 +"個→鉄インゴット"+ amount1 +"個変換");
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
			} else if (type == Material.IRON_INGOT && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{
					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack1 = Util.findMineStackObjectByName("iron_ore");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("iron_ingot");
					final int amount1 = getPower10().get(x) * 50;

					final MineStackObj inStack2 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj outStack2 = Util.findMineStackObjectByName("bucket");
					final int amount2 = getPower10().get(x);
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1) || ( playerdata_s.getMinestack().getStackedAmountOf(inStack2) < getPower10().get(x))){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, amount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack1, amount1);
						playerdata_s.getMinestack().addStackedAmountOf(outStack2, amount2);
						player.sendMessage(ChatColor.GREEN + "鉄鉱石"+ amount1 +"個+溶岩バケツ"+ amount2 +"個→鉄インゴット"+ amount1 +"個+バケツ"+ amount2 +"個変換");
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}


				//石炭を消費して金鉱石を金インゴットに変換4～4000
			} else if (type == Material.GOLD_INGOT && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					final MineStackObj inStack2 = Util.findMineStackObjectByName("coal");
					final int amount2 = getPower10().get(x);

					final MineStackObj inStack1 = Util.findMineStackObjectByName("gold_ore");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("gold_ingot");
					final int amount1 = amount2 * 4;
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1 || playerdata_s.getMinestack().getStackedAmountOf(inStack2) < amount2){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, amount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack1, amount1);
						player.sendMessage(ChatColor.GREEN + "金鉱石"+ amount1 +"個+石炭"+ amount2 +"個→金インゴット"+ amount1 +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				//溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
			} else if (type == Material.GOLD_INGOT && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					final MineStackObj inStack2 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj outStack2 = Util.findMineStackObjectByName("bucket");
					final int amount2 = getPower10().get(x);

					final MineStackObj inStack1 = Util.findMineStackObjectByName("gold_ore");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("gold_ingot");
					final int amount1 = amount2 * 50;
					if ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1 || playerdata_s.getMinestack().getStackedAmountOf(inStack2) < amount2){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, amount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack1, amount1);
						playerdata_s.getMinestack().addStackedAmountOf(outStack2, amount2);
						player.sendMessage(ChatColor.GREEN + "金鉱石"+ amount1 +"個+溶岩バケツ"+ amount2 +"個→金インゴット"+ amount1 +"個+バケツ"+ amount2 +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}


				//石炭を消費して砂をガラスに変換4～4000
			} else if (type == Material.GLASS && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack1 = Util.findMineStackObjectByName("sand");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("glass");
					final MineStackObj inStack2 = Util.findMineStackObjectByName("coal");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					final MineStack m = playerdata_s.getMinestack();
					final int amount2 = getPower10().get(x);
					final int amount1 = amount2 * 4;
					if (m.getStackedAmountOf(inStack1) < amount1 || m.getStackedAmountOf(inStack2) < amount2){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						m.subtractStackedAmountOf(inStack1, amount1);
						m.subtractStackedAmountOf(inStack2, amount2);
						m.addStackedAmountOf(outStack1, amount1);
						player.sendMessage(ChatColor.GREEN + "砂"+ amount1 +"個+石炭"+ amount2 +"個→ガラス"+ amount1 +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				/*
				 * 溶岩バケツ: 1 + ネザーラック: 50
				 * ---> 空バケツ: 1 + ネザーレンガ: 50
				 * 50から10倍で5万まで
				 */
			} else if (type == Material.GLASS && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack1 = Util.findMineStackObjectByName("sand");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("glass");
					final MineStackObj inStack2 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj outStack2 = Util.findMineStackObjectByName("bucket");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					final int amount2 = getPower10().get(x);
					final int amount1 = amount2 * 50;
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1) || ( playerdata_s.getMinestack().getStackedAmountOf(inStack2) < amount2)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, amount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack1, amount1);
						playerdata_s.getMinestack().addStackedAmountOf(outStack2, amount2);
						player.sendMessage(ChatColor.GREEN + "砂"+ amount1 +"個+溶岩バケツ"+ amount2 +"個→ガラス"+ amount1 +"個+バケツ"+ amount2 +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				/*
				 * 溶岩バケツ: 1 + ネザーラック: 4
				 * ---> 空バケツ: 1 + ネザーレンガ: 4
				 * 4から10倍で4000まで
				 */
			} else if (type == Material.NETHER_BRICK_ITEM && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack1 = Util.findMineStackObjectByName("netherrack");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("nether_brick_item");
					final MineStackObj inStack2 = Util.findMineStackObjectByName("coal");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					final int amount2 = getPower10().get(x);
					final int amount1 = amount2 * 4;
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1) || ( playerdata_s.getMinestack().getStackedAmountOf(inStack2) < amount2)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, amount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack1, amount1);
						player.sendMessage(ChatColor.GREEN + "ネザーラック"+ amount1 +"個+石炭"+ amount2 +"個→ネザーレンガ"+ amount1 +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData2(player));
				}

				/*
				 * 溶岩バケツ: 1 + ネザーラック: 50
				 * ---> 空バケツ: 1 + ネザーレンガ: 50
				 * 50から10倍で5万まで
				 */
			} else if (type == Material.NETHER_BRICK_ITEM && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					final MineStackObj inStack1 = Util.findMineStackObjectByName("netherrack");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("nether_brick_item");
					final MineStackObj inStack2 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj outStack2 = Util.findMineStackObjectByName("bucket");
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					final int amount2 = getPower10().get(x);
					final int amount1 = amount2 * 50;
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1) || ( playerdata_s.getMinestack().getStackedAmountOf(inStack2) < amount2)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, amount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack1, amount1);
						playerdata_s.getMinestack().addStackedAmountOf(outStack2, amount2);
						player.sendMessage(ChatColor.GREEN + "ネザーラック"+ amount1 +"個+溶岩バケツ"+ amount2 +"個→ネザーレンガ"+ amount1 +"個+バケツ"+ amount2 +"個変換" );
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
		if(he.getType() != EntityType.PLAYER){
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
			if(event.getClickedInventory().getType() == InventoryType.PLAYER){
				return;
			}
			/*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
			if(itemstackcurrent.getType() == Material.SKULL_ITEM && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowUp") ){
				//2ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getBlockCraftData2(player));

/*			} else if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown") ){
				//4ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, 0.1f);
				player.openInventory(MenuInventoryData.getBlockCraftData4(player));
*/

				//石炭を消費して粘土をレンガに変換4～4000
			} else if (itemstackcurrent.getType() == Material.CLAY_BRICK && itemstackcurrent.getItemMeta().getDisplayName().contains("石炭") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();
					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					final MineStackObj inStack2 = Util.findMineStackObjectByName("coal");
					final int amount2 = getPower10().get(x);

					final MineStackObj inStack1 = Util.findMineStackObjectByName("clay_ball");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("brick_item");
					final int amount1 = amount2 * 4;
					if (playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1 || playerdata_s.getMinestack().getStackedAmountOf(inStack2) < amount2){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, amount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack1, amount1);
						player.sendMessage(ChatColor.GREEN + "粘土"+ amount1 +"個+石炭"+ amount2 +"個→レンガ"+ amount1 +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData3(player));
				}

				//溶岩バケツを消費して粘土をレンガに変換50～5万
			} else if (itemstackcurrent.getType() == Material.CLAY_BRICK && itemstackcurrent.getItemMeta().getDisplayName().contains("溶岩") ){
				if(playerdata.level < BuildAssist.Companion.getConfig().getMinestackBlockCraftlevel(3) ){
					player.sendMessage(ChatColor.RED + "建築LVが足りません") ;
				}else{

					com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
					int x = itemstackcurrent.getAmount();

					final MineStackObj inStack2 = Util.findMineStackObjectByName("lava_bucket");
					final MineStackObj outStack2 = Util.findMineStackObjectByName("bucket");
					final int amount2 = getPower10().get(x);

					final MineStackObj inStack1 = Util.findMineStackObjectByName("clay_ball");
					final MineStackObj outStack1 = Util.findMineStackObjectByName("brick_item");
					final int amount1 = amount2 * 50;

					player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
					if ( ( playerdata_s.getMinestack().getStackedAmountOf(inStack1) < amount1) || ( playerdata_s.getMinestack().getStackedAmountOf(inStack2) < amount2)){
						player.sendMessage(ChatColor.RED + "アイテムが足りません" );
					}else{
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack1, amount1);
						playerdata_s.getMinestack().subtractStackedAmountOf(inStack2, amount2);
						playerdata_s.getMinestack().addStackedAmountOf(outStack1, amount1);
						playerdata_s.getMinestack().addStackedAmountOf(outStack2, amount2);
						player.sendMessage(ChatColor.GREEN + "粘土"+ amount1 +"個+溶岩バケツ"+ amount2 +"個→レンガ"+ amount1 +"個+バケツ"+ amount2 +"個変換" );
					}
					player.openInventory(MenuInventoryData.getBlockCraftData3(player));
				}
			}
		}
	}



}
