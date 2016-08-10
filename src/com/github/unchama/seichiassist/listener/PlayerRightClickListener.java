package com.github.unchama.seichiassist.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
					//設置をキャンセル
					event.setCancelled(true);

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
						player.sendMessage(ChatColor.YELLOW + "おめでとう！！！！！Gigantic☆大当たり！" + str);
						Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり！\n" + ChatColor.AQUA + present.itemstack.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
					}else if(present.probability < 0.01){
						//ver 0.3.1以降 大当たり時の全体通知を削除
						// Util.sendEverySound(Sound.ENTITY_WITHER_SPAWN, (float) 0.8, 1);
						player.sendMessage(ChatColor.YELLOW + "おめでとう！！大当たり！" + str);
						// Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり！\n" + ChatColor.DARK_BLUE + present.itemstack.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
					}else if(present.probability < 0.1){
						player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！" + str);
					}else{
						player.sendMessage(ChatColor.YELLOW + "はずれ！また遊んでね！" + str);
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

				Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "メニュー");
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
				, ChatColor.RESET + "" +  ChatColor.AQUA + "破壊したブロック数:" + MineBlock.calcMineBlock(player)
				, ChatColor.RESET + "" +  ChatColor.AQUA + "次のレベルまで:" + (SeichiAssist.levellist.get(playerdata.level + 1).intValue() - MineBlock.calcMineBlock(player))
						);

				skullmeta.setLore(lore);
				skullmeta.setOwner(playerdata.name);
				itemstack.setItemMeta(skullmeta);
				inventory.setItem(0,itemstack);

				// ver0.3.2 採掘速度上昇トグル
				itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "採掘速度上昇効果");
				if(playerdata.effectflag){
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "ON"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると変更できます"
							);
				}else {
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "OFF"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると変更できます"
							);
				}
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(1,itemstack);

				// ver0.3.2 /spawnコマンド実行
				itemstack = new ItemStack(Material.BOOKSHELF,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOKSHELF);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スポーンワールドへワープ");
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "メインワールド、資源ワールド、整地ワールド間を移動する時に使います"
						, ChatColor.RESET + "" + ChatColor.GRAY + "建築はメインワールド、資材集めは資源ワールド、整地は整地ワールドを利用しましょう"
						, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとワープします");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(8,itemstack);

				// ver0.3.2 四次元ポケットOPEN
				itemstack = new ItemStack(Material.ENDER_PORTAL_FRAME,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_PORTAL_FRAME);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "四次元ポケットを開く");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると開きます"
						, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※整地レベルが"+SeichiAssist.config.getPassivePortalInventorylevel()+ "以上必要です"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(2,itemstack);

				// ver0.3.2 sethomeコマンド
				itemstack = new ItemStack(Material.BED,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在位置をホームポイントに設定");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると設定されます"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(5,itemstack);

				// ver0.3.2 homeコマンド
				itemstack = new ItemStack(Material.COMPASS,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームポイントにワープ");
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "予め設定したホームポイントにワープできます"
						, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとワープします"
						);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(4,itemstack);


				/*
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
												, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとこのスキルをアクティブにします");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);


				itemstack = new ItemStack(Material.IRON_ORE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "トリアルブレイク");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×2マス破壊"
												, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル："  + config.getTrialBreaklevel()
												, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：3"
												, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとこのスキルをアクティブにします");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(10,itemstack);


				itemstack = new ItemStack(Material.GOLD_ORE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エクスプロージョン");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3マス破壊"
												, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getExplosionlevel()
												, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：10"
												, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとこのスキルをアクティブにします");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(11,itemstack);


				itemstack = new ItemStack(Material.REDSTONE_ORE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_ORE);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サンダーストーム");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "3×3×3マス破壊×5"
												, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2秒"
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getThunderStormlevel()
												, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：30"
												, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとこのスキルをアクティブにします");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(12,itemstack);


				itemstack = new ItemStack(Material.LAPIS_ORE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAPIS_ORE);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ブリザード");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "7×7×5マス破壊"
												, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：2.5秒"
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getBlizzardlevel()
												, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：70"
												, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとこのスキルをアクティブにします");

				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(13,itemstack);


				itemstack = new ItemStack(Material.EMERALD_ORE,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "メテオ");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "9*9*7マス破壊"
												, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：3秒"
												, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + config.getMeteolevel()
												, ChatColor.RESET + "" +  ChatColor.BLUE + "消費経験値：100"
												, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとこのスキルをアクティブにします");

				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(14,itemstack);


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
				inventory.setItem(15,itemstack);


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
				itemstack = new ItemStack(Material.SKULL_ITEM,gachaget);
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



}
