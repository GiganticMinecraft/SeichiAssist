package com.github.unchama.seichiassist.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MineBlock;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class PlayerRightClickListener implements Listener {
	HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	List<GachaData> gachadatalist = SeichiAssist.gachadatalist;
	private Config config = SeichiAssist.config;
	//プレイヤーが右クリックした時に実行(ガチャを引く部分の処理)
	@EventHandler
	public void onPlayerRightClickGachaEvent(PlayerInteractEvent event){
		//プレイヤー型を取得
		Player player = event.getPlayer();
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//プレイヤーが起こした対象のitemstackを取得
		ItemStack itemstack = event.getItem();
		//使った手を取得
		EquipmentSlot equipmentslot = event.getHand();




		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			if(event.getMaterial().equals(Material.SKULL_ITEM)){
				//プレゼント用ガチャデータ作成
				GachaData present;
				//対象スカルのskullmetaを取得
				SkullMeta skullmeta = (SkullMeta) itemstack.getItemMeta();

				//ownerがいない場合処理終了
				if(!skullmeta.hasOwner()){
					return;
				}
				//ownerがうんちゃまの時の処理
				if(skullmeta.getOwner().equals("unchama")){

					//もしサバイバルでなければ処理を終了
					if(!player.getGameMode().equals(GameMode.SURVIVAL)){
						return;
					}

					//これ以前のフラグに引っかかると設置できる
					//設置キャンセル
					event.setCancelled(true);
					//これより下のフラグに引っかかると設置できない

					//ガチャシステムメンテナンス中は処理を終了
					if(SeichiAssist.gachamente){
						player.sendMessage("現在ガチャシステムはメンテナンス中です。\nしばらく経ってからもう一度お試しください");
						return;
					}

					//オフハンドから実行された時処理を終了
					if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
						return;
					}
					//ガチャデータが設定されていない場合
					if(gachadatalist.isEmpty()){
						player.sendMessage("ガチャが設定されていません");
						return;
					}
					//持っているガチャ券を減らす処理
					if (itemstack.getAmount() == 1) {
						// がちゃ券を1枚使うので、プレイヤーの手を素手にする
						player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
					} else {
						// プレイヤーが持っているガチャ券を1枚減らす
						itemstack.setAmount(itemstack.getAmount()-1);
					}
					//ガチャ実行
					present = GachaData.runGacha();
					//ガチャデータのitemstackの数を再設定（バグのため）
					present.itemstack.setAmount(present.amount);
					//メッセージ設定
					String str = ChatColor.AQUA + "プレゼントがドロップしました。";
					//プレゼントをドロップ
					Util.dropItem(player, present.itemstack);

					//確率に応じてメッセージを送信
					if(present.probability < 0.001){
						Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, 2);
						player.sendMessage(ChatColor.RED + "おめでとう！！！！！Gigantic☆大当たり！" + str);
						Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり！\n" + ChatColor.AQUA + present.itemstack.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
					}else if(present.probability < 0.01){
						//大当たり時にSEを鳴らす(自分だけ)
						player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, (float) 0.8, 1);
						//ver 0.3.1以降 大当たり時の全体通知を削除
						// Util.sendEverySound(Sound.ENTITY_WITHER_SPAWN, (float) 0.8, 1);
						player.sendMessage(ChatColor.GOLD + "おめでとう！！大当たり！" + str);
						// Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり！\n" + ChatColor.DARK_BLUE + present.itemstack.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
					}else if(present.probability < 0.1){
						player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！" + str);
					}else{
						player.sendMessage(ChatColor.WHITE + "はずれ！また遊んでね！" + str);
					}
					player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, (float) 0.1);
				}
			}
		}
	}
	@EventHandler
	public void onPlayerActiveSkillToggleEvent(PlayerInteractEvent event){
		//プレイヤーを取得
		Player player = event.getPlayer();
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//プレイヤーの起こしたアクションの取得
		Action action = event.getAction();
		//アクションを起こした手を取得
		EquipmentSlot equipmentslot = event.getHand();

		//アクティブスキルを発動できるレベルに達していない場合処理終了
		if( playerdata.level < SeichiAssist.config.getDualBreaklevel()){
			return;
		}

		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			//スニークしていなかったら処理終了
			if(!player.isSneaking()){
				return;
			}else if(SeichiAssist.breakmateriallist.contains(player.getInventory().getItemInMainHand().getType())){
				//メインハンドで指定ツールを持っていた時の処理

				//アクション実行がオフハンドだった時の処理終了
				if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
					//設置をキャンセル
					event.setCancelled(true);
					return;
				}
				//アクション実行されたブロックがある場合の処理
				if(action.equals(Action.RIGHT_CLICK_BLOCK)){
					//クリックされたブロックの種類を取得
					Material cmaterial = event.getClickedBlock().getType();
					//cancelledmateriallistに存在すれば処理終了
					if(SeichiAssist.cancelledmateriallist.contains(cmaterial)){
						return;
					}
				}
				int activemineflagnum = 0;

				if(playerdata.activenum == ActiveSkill.DUALBREAK.getNum() || playerdata.activenum == ActiveSkill.TRIALBREAK.getNum()){
					activemineflagnum = (playerdata.activemineflagnum + 1) % 3;
					switch (activemineflagnum){
					case 0:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getStringByNum(playerdata.activenum) + "：OFF");
						break;
					case 1:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getStringByNum(playerdata.activenum) + ":ON-Above(上向き）");
						break;
					case 2:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getStringByNum(playerdata.activenum) + ":ON-Under(下向き）");
						break;
					}
					player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				}else if(playerdata.activenum >= ActiveSkill.EXPLOSION.getNum()){
					activemineflagnum = (playerdata.activemineflagnum + 1) % 2;
					switch (activemineflagnum){
					case 0:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getStringByNum(playerdata.activenum) + "：OFF");
						break;
					case 1:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getStringByNum(playerdata.activenum) + ":ON");
						break;
					case 2:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getStringByNum(playerdata.activenum) + ":ON");
						break;
					}
					player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				}
				playerdata.activemineflagnum = activemineflagnum;
			}
		}
	}

	/* ver0.3.2 左クリックトグル無効化
	@EventHandler
	public void onPlayerEffectToggleEvent(PlayerInteractEvent event){
		//プレイヤーを取得
		Player player = event.getPlayer();
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータを取得
		PlayerData playerdata = playermap.get(uuid);
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//アクションを起こした手を取得
		EquipmentSlot equipmentslot = event.getHand();



		if(action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)){
			//左クリックの処理
			if(player.getInventory().getItemInMainHand().getType().equals(Material.STICK)){
				//メインハンドに棒を持っているときの処理

				//オフハンドのアクション実行時処理を終了
				if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
					return;
				}
				//エフェクトフラグを取得
				boolean effectflag = !playerdata.effectflag;
				if (effectflag){
					player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON");
				}else{
					player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:OFF");
				}
				player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				playerdata.effectflag = effectflag;
			}
		}
	}
	*/

	@EventHandler
	public void onPlayerActiveSkillUIEvent(PlayerInteractEvent event){
		//プレイヤーを取得
		Player player = event.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//アクションを起こした手を取得
		EquipmentSlot equipmentslot = event.getHand();
		//プレイヤーデータ
		PlayerData playerdata = playermap.get(uuid);


		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			//右クリックの処理
			if(player.getInventory().getItemInMainHand().getType().equals(Material.STICK)){
				//メインハンドに棒を持っているときの処理

				//オフハンドのアクション実行時処理を終了
				if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);

				Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー");
				ItemStack itemstack;
				ItemMeta itemmeta;
				SkullMeta skullmeta;
				List<String> lore;


				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.name + "の統計データ");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "整地レベル:" + playerdata.level
						, ChatColor.RESET + "" +  ChatColor.AQUA + "次のレベルまで:" + (SeichiAssist.levellist.get(playerdata.level + 1).intValue() - MineBlock.calcMineBlock(player))
						, ChatColor.RESET + "" +  ChatColor.GRAY + "パッシブスキル効果："
						, ChatColor.RESET + "" +  ChatColor.GRAY + "1ブロック破壊ごとに10%の確率で"
						, ChatColor.RESET + "" +  ChatColor.GRAY + DisplayPassiveExp(playerdata) + "の経験値を獲得します"
						, ChatColor.RESET + "" +  ChatColor.AQUA + "破壊したブロック数:" + MineBlock.calcMineBlock(player)
						, ChatColor.RESET + "" +  ChatColor.GOLD + "ランキング：" + PlayerData.calcPlayerRank(player) + "位" + ChatColor.RESET + "" +  ChatColor.GRAY + "(" + SeichiAssist.ranklist.size() +"人中)"
						);
				/*
				if(PlayerData.calcPlayerRank(player) > 1){
					lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + (PlayerData.calcPlayerRank(player)-1) + "位との差：" + (SeichiAssist.ranklist.get(PlayerData.calcPlayerRank(player)-2).intValue() - MineBlock.calcMineBlock(player)));
				}
				*/

				skullmeta.setLore(lore);
				skullmeta.setOwner(playerdata.name);
				itemstack.setItemMeta(skullmeta);
				inventory.setItem(0,itemstack);

				// ver0.3.2 採掘速度上昇トグル
				/*
				String msg[] = null;
				int i = 0;
				for(EffectData ed : playerdata.effectdatalist){
					msg[i] = (ed.string);
					msg[i+1] = ("(持続時間:" + Util.toTimeString(ed.duration/20) + ")");
					i += 2;
				}
				*/
				//ボタン表示部分
				itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "採掘速度上昇効果");
				if(playerdata.effectflag){
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "現在ONになっています"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると変更できます"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "採掘速度上昇効果とは"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "現在の接続人数と過去1分間の採掘量に応じて"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "採掘速度が変化するシステムです"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "" + ChatColor.UNDERLINE + "/ef smart"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "で効果の内訳を表示できます"
							);
				}else {
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "現在OFFになっています"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると変更できます"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "採掘速度上昇効果とは"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "現在の接続人数と過去1分間の採掘量に応じて"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "採掘速度が変化するシステムです"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "" + ChatColor.UNDERLINE + "/ef smart"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "で効果の内訳を表示できます"
							);
				}
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(1,itemstack);

				// ver0.3.2 四次元ポケットOPEN
				itemstack = new ItemStack(Material.ENDER_PORTAL_FRAME,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_PORTAL_FRAME);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "四次元ポケットを開く");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると開きます"
						, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※整地レベルが"+SeichiAssist.config.getPassivePortalInventorylevel()+ "以上必要です"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(21,itemstack);




				// 自分の頭召喚
				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "自分の頭を召喚");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "経験値10000を消費して"
						, ChatColor.RESET + "" +  ChatColor.GRAY + "自分の頭を召喚します"
						, ChatColor.RESET + "" +  ChatColor.GRAY + "装飾用にドウゾ！"
						, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると召喚します"
						);
				skullmeta.setLore(lore);
				skullmeta.setOwner("MHF_Villager");
				itemstack.setItemMeta(skullmeta);
				inventory.setItem(23,itemstack);




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
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※インベントリが満杯だと受け取れません"
						, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "保護のかけ方"
						, ChatColor.RESET + "" +  ChatColor.GREEN + "①召喚された斧を手に持ちます"
						, ChatColor.RESET + "" +  ChatColor.GREEN + "②保護したい領域の一方の角を" + ChatColor.YELLOW + "左" + ChatColor.GREEN + "クリック"
						, ChatColor.RESET + "" +  ChatColor.GREEN + "③もう一方の対角線上の角を" + ChatColor.RED + "右" + ChatColor.GREEN + "クリック"
						, ChatColor.RESET + "" +  ChatColor.GREEN + "③メニューの「" + ChatColor.RESET + "" +  ChatColor.YELLOW + "保護領域の申請" + ChatColor.RESET + "" +  ChatColor.GREEN + "」ボタンをクリック"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(3,itemstack);

				// ver0.3.2 保護設定コマンド
				itemstack = new ItemStack(Material.GOLD_AXE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_AXE);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護領域の申請");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "木の斧で2か所クリックした後"
						, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "このボタンをクリック"
						, ChatColor.RESET + "" +  ChatColor.YELLOW + "" + "A new region has been claimed"
						, ChatColor.RESET + "" +  ChatColor.YELLOW + "" + "named '" + player.getName() + "_" + playerdata.rgnum + "'."
						, ChatColor.RESET + "" +  ChatColor.GRAY + "と出れば、保護の設定が完了しています"
						, ChatColor.RESET + "" +  ChatColor.RED + "赤色で別の英文が出た場合"
						, ChatColor.RESET + "" +  ChatColor.GRAY + "保護の設定に失敗しています"
						, ChatColor.RESET + "" +  ChatColor.GRAY + "別の保護と被ってないか等ご確認の上"
						, ChatColor.RESET + "" +  ChatColor.GRAY + "始めからやり直してください"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(4,itemstack);

				// ver0.3.2 保護リスト表示
				itemstack = new ItemStack(Material.STONE_AXE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE_AXE);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護リストを表示");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると表示"
						, ChatColor.RESET + "" +  ChatColor.GRAY + "現在あなたが保護している"
						, ChatColor.RESET + "" +  ChatColor.GRAY + "土地の一覧を表示します"
						, ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg remove 保護名"
						, ChatColor.RESET + "" +  ChatColor.GRAY + "で保護の削除が出来ます"
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




				// ver0.3.2 wikiページ表示
				itemstack = new ItemStack(Material.BOOK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "公式Wikiにアクセス");
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄にURLが表示されますので"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してからそのURLをクリックしてください"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(35,itemstack);

				// ver0.3.2 投票ページ表示
				itemstack = new ItemStack(Material.BOOK_AND_QUILL,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票ページにアクセス");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "投票すると様々な特典があります！1日1回投票出来ます"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄にURLが表示されますので"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押しからてそのURLをクリックしてください"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(34,itemstack);

				// ver0.3.2 運営方針とルールページを表示
				itemstack = new ItemStack(Material.PAPER,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "運営方針とルールを確認");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "当鯖で遊ぶ前に確認してネ！"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄にURLが表示されますので"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してからそのURLをクリックしてください"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(33,itemstack);

				// ver0.3.2 鯖Mapを表示
				itemstack = new ItemStack(Material.MAP,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MAP);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "鯖Mapを見る");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "webブラウザから鯖Mapを閲覧出来ます"
						, ChatColor.RESET + "" +  ChatColor.GREEN + "他人の居場所や保護の場所を確認出来ます"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄にURLが表示されますので"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してからそのURLをクリックしてください"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(32,itemstack);

				// ver0.3.2 掲示板を表示
				itemstack = new ItemStack(Material.SIGN,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SIGN);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "掲示板を見る");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "管理人へのお問い合わせは"
						, ChatColor.RESET + "" +  ChatColor.GREEN + "掲示板に書き込みをｵﾈｶﾞｲｼナス"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄にURLが表示されますので"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してからそのURLをクリックしてください"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(31,itemstack);




				/* 一番左のピッケル装飾コメントアウト ver0.3.2
				itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アクティブスキル選択");
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.YELLOW + "選択するとアクティブスキルがONになります");
				itemmeta.setLore(lore);
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
				*/

				itemstack = new ItemStack(Material.COAL_ORE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "デュアルブレイク");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "1×2マス破壊"
												, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getDualBreaklevel()
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
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル："  + config.getTrialBreaklevel()
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
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getExplosionlevel()
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
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getThunderStormlevel()
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
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getBlizzardlevel()
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
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getMeteolevel()
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
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getGravitylevel()
				, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：???");

				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(16,itemstack);


				/* 一番右のピッケル装飾コメントアウト ver0.3.2
				itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アクティブスキル選択");
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.YELLOW + "選択するとアクティブスキルがONになります");
				itemmeta.setLore(lore);
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(17,itemstack);
				*/


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
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "受け取り方法");
				if(playerdata.gachaflag){
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "毎分受け取っています"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると変更できます"
							);
				}else {
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "毎分受け取っていません"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると変更できます"
							);
				}
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(28,itemstack);






				player.openInventory(inventory);
			}
		}
	}
	//プレイヤーの拡張インベントリを開くイベント
	@EventHandler
	public void onPlayerOpenInventorySkillEvent(PlayerInteractEvent event){
		//プレイヤーを取得
		Player player = event.getPlayer();
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//使った手を取得
		EquipmentSlot equipmentslot = event.getHand();

		if(event.getMaterial().equals(Material.ENDER_PORTAL_FRAME)){
			//設置をキャンセル
			event.setCancelled(true);
			//パッシブスキル[4次元ポケット]（PortalInventory）を発動できるレベルに達していない場合処理終了
			if( playerdata.level < SeichiAssist.config.getPassivePortalInventorylevel()){
				player.sendMessage(ChatColor.GREEN + "4次元ポケットを入手するには整地レベルが"+SeichiAssist.config.getPassivePortalInventorylevel()+ "以上必要です。");
				return;
			}
			if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
					//オフハンドから実行された時処理を終了
					if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
						return;
					}
					//開く音を再生
					player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 1, (float) 0.1);
					//インベントリを開く
					player.openInventory(playerdata.inventory);
			}
		}
	}
	//パッシブスキルの獲得量表示
	public static int DisplayPassiveExp(PlayerData playerdata) {
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
		}else{
			return SeichiAssist.config.getDropExplevel6();
		}
	}


}
