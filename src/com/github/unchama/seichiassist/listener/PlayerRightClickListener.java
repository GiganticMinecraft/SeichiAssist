package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MenuInventoryData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.ArrowRemoveTaskRunnable;
import com.github.unchama.seichiassist.task.AssaultArmorTaskRunnable;
import com.github.unchama.seichiassist.task.CondenSkillTaskRunnable;
import com.github.unchama.seichiassist.task.CoolDownTaskRunnable;
import com.github.unchama.seichiassist.util.Util;

public class PlayerRightClickListener implements Listener {
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	List<GachaData> gachadatalist = SeichiAssist.gachadatalist;
	//アクティブスキル処理
	@EventHandler
	public void onPlayerActiveSkillEvent(PlayerInteractEvent event){
		//プレイヤー型を取得
		Player player = event.getPlayer();
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//使った手を取得
		EquipmentSlot equipmentslot = event.getHand();
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータを取得
		PlayerData playerdata = playermap.get(uuid);

		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[blockbreaklistener処理]でエラー発生");
			plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return;
		}
		if(equipmentslot==null){
			return;
		}
		//オフハンドから実行された時処理を終了
		if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
			return;
		}

		if(player.isSneaking()){
			return;
		}


		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){

			//サバイバルでない時　または　フライ中の時終了
			if(!player.getGameMode().equals(GameMode.SURVIVAL) || player.isFlying()){
				return;
			}

			//アクティブスキルフラグがオフの時処理を終了
			if(playerdata.activeskilldata.mineflagnum == 0 || playerdata.activeskilldata.skillnum == 0){
				return;
			}

			//クールダウンタイム中は処理を終了
			if(!playerdata.activeskilldata.skillcanbreakflag){
				//SEを再生
				player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, (float)0.5, 1);
				return;
			}

			if(SeichiAssist.breakmateriallist.contains(event.getMaterial())){
				if(playerdata.activeskilldata.skilltype == ActiveSkill.ARROW.gettypenum()){
					runArrowSkillofLaunch(player,Arrow.class);
				}else if(playerdata.activeskilldata.skilltype == ActiveSkill.CONDENSE.gettypenum()){
					if(playerdata.activeskilldata.skillnum < 7){
						runCondenSkillofLaunch(player,Snowball.class);
					}else{
						runCondenSkillofLaunch(player,Fireball.class);
					}
				}
			}
		}
	}


	private <T extends org.bukkit.entity.Projectile> void runCondenSkillofLaunch(Player player, Class<T> clazz) {
		//プレイヤーの位置を取得
		Location ploc = player.getLocation();
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//ぷれいやーでーたを取得
		PlayerData playerdata = playermap.get(uuid);

		//発射する音を再生する.
		if(playerdata.activeskilldata.skillnum < 7){
			player.playSound(ploc, Sound.ENTITY_SNOWBALL_THROW, 1, 1);
		}else{
			player.playSound(ploc, Sound.ENTITY_GHAST_SHOOT, 1, 1);
		}


    	//スキルを実行する処理
        Location loc = player.getLocation();
        loc.add(loc.getDirection()).add(0,1.6,0);
        Vector vec = loc.getDirection();
        int k = 1;
        vec.setX(vec.getX() * k);
        vec.setY(vec.getY() * k);
        vec.setZ(vec.getZ() * k);
        final T proj = player.getWorld().spawn(loc, clazz);
        proj.setShooter(player);
        proj.setGravity(false);
        //読み込み方法
        /*
         * Projectile proj = event.getEntity();
		    if ( proj instanceof Arrow && proj.hasMetadata("ArrowSkill") ) {
		    }
         */
        proj.setMetadata("CondenSkill", new FixedMetadataValue(plugin, true));
        proj.setVelocity(vec);

        //矢を途中で破裂させる処理
        new CondenSkillTaskRunnable((Projectile)proj).runTaskLater(plugin,playerdata.activeskilldata.explosiontime*20);

        //クールダウン処理
        long cooldown = ActiveSkill.CONDENSE.getCoolDown(playerdata.activeskilldata.skillnum);
        if(cooldown > 5){
        	new CoolDownTaskRunnable(player,false,true).runTaskLater(plugin,cooldown);
        }else{
        	new CoolDownTaskRunnable(player,false,false).runTaskLater(plugin,cooldown);
        }
	}


	private <T extends org.bukkit.entity.Projectile> void runArrowSkillofLaunch(Player player, Class<T> clazz) {
		//プレイヤーの位置を取得
		Location ploc = player.getLocation();
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//ぷれいやーでーたを取得
		PlayerData playerdata = playermap.get(uuid);

		//発射する音を再生する.
    	player.playSound(ploc, Sound.ENTITY_ARROW_SHOOT, 1, 1);

    	//スキルを実行する処理
        Location loc = player.getLocation();
        loc.add(loc.getDirection()).add(0,1.6,0);
        Vector vec = loc.getDirection();
        int k = 1;
        vec.setX(vec.getX() * k);
        vec.setY(vec.getY() * k);
        vec.setZ(vec.getZ() * k);
        final T proj = player.getWorld().spawn(loc, clazz);
        proj.setShooter(player);
        proj.setGravity(false);
        //読み込み方法
        /*
         * Projectile proj = event.getEntity();
		    if ( proj instanceof Arrow && proj.hasMetadata("ArrowSkill") ) {
		    }
         */
        proj.setMetadata("ArrowSkill", new FixedMetadataValue(plugin, true));
        proj.setVelocity(vec);

        //矢を消去する処理
        new ArrowRemoveTaskRunnable((Projectile)proj).runTaskLater(plugin,100);

        //クールダウン処理
        long cooldown = ActiveSkill.ARROW.getCoolDown(playerdata.activeskilldata.skillnum);
        if(cooldown > 5){
        	new CoolDownTaskRunnable(player,false,true).runTaskLater(plugin,cooldown);
        }else{
        	new CoolDownTaskRunnable(player,false,false).runTaskLater(plugin,cooldown);
        }
	}


	//プレイヤーが右クリックした時に実行(ガチャを引く部分の処理)
	@EventHandler
	public void onPlayerRightClickGachaEvent(PlayerInteractEvent event){
		//プレイヤー型を取得
		Player player = event.getPlayer();
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();

		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			if(event.getMaterial().equals(Material.SKULL_ITEM)){
				//プレイヤーが起こした対象のitemstackを取得
				ItemStack itemstack = event.getItem();
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
					//ガチャデータが設定されていない場合
					if(gachadatalist.isEmpty()){
						player.sendMessage("ガチャが設定されていません");
						return;
					}

					//使った手を取得
					EquipmentSlot equipmentslot = event.getHand();

					//オフハンドから実行された時処理を終了
					if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
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
					//プレゼント用ガチャデータ作成
					GachaData present;
					//ガチャ実行
					present = GachaData.runGacha();
					//ガチャデータのitemstackの数を再設定（バグのため）
					present.itemstack.setAmount(present.amount);
					//メッセージ設定
					String str = ChatColor.AQUA + "プレゼントがドロップしました。";

					//プレゼントを格納orドロップ
					if(!Util.isPlayerInventryFill(player)){
						Util.addItem(player,present.itemstack);
					}else{
						Util.dropItem(player,present.itemstack);
					}
					/*
					//プレゼントをドロップ
					Util.dropItem(player, present.itemstack);
					*/

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
	//スキル切り替えのイベント
	@EventHandler
	public void onPlayerActiveSkillToggleEvent(PlayerInteractEvent event){
		//プレイヤーを取得
		Player player = event.getPlayer();
		//プレイヤーの起こしたアクションの取得
		Action action = event.getAction();
		//アクションを起こした手を取得
		EquipmentSlot equipmentslot = event.getHand();
		if(player.getInventory().getItemInMainHand().getType().equals(Material.STICK)){
			return;
		}
		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			//UUIDを取得
			UUID uuid = player.getUniqueId();
			//playerdataを取得
			PlayerData playerdata = playermap.get(uuid);
			//念のためエラー分岐
			if(playerdata == null){
				player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[スキルスニークトグル処理]でエラー発生");
				plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
				return;
			}
			if(SeichiAssist.breakmateriallist.contains(player.getInventory().getItemInMainHand().getType())
					&& equipmentslot.equals(EquipmentSlot.HAND)
					){
				//メインハンドで指定ツールを持っていた時の処理
				//スニークしていなかったら処理終了
				if(!player.isSneaking()){
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


				//アクティブスキルを発動できるレベルに達していない場合処理終了
				if( playerdata.level < SeichiAssist.config.getDualBreaklevel()){
					return;
				}

				//設置をキャンセル
				event.setCancelled(true);


				int activemineflagnum = 0;

				if((playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum() && playerdata.activeskilldata.skillnum == 1)
						|| (playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum() && playerdata.activeskilldata.skillnum == 2)){

					activemineflagnum = (playerdata.activeskilldata.mineflagnum + 1) % 3;
					switch (activemineflagnum){
					case 0:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype,playerdata.activeskilldata.skillnum) + "：OFF");
						break;
					case 1:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype,playerdata.activeskilldata.skillnum) + ":ON-Above(上向き）");
						break;
					case 2:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype,playerdata.activeskilldata.skillnum) + ":ON-Under(下向き）");
						break;
					}
					player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
					playerdata.activeskilldata.mineflagnum = activemineflagnum;
				}else if(playerdata.activeskilldata.skilltype > 0 && playerdata.activeskilldata.skillnum > 0){
					activemineflagnum = (playerdata.activeskilldata.mineflagnum + 1) % 2;
					switch (activemineflagnum){
					case 0:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype,playerdata.activeskilldata.skillnum) + "：OFF");
						break;
					case 1:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype,playerdata.activeskilldata.skillnum) + ":ON");
						break;
					}
					player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
					playerdata.activeskilldata.mineflagnum = activemineflagnum;
				}

			}else if(SeichiAssist.breakmateriallist.contains(player.getInventory().getItemInOffHand().getType())
					&& equipmentslot.equals(EquipmentSlot.OFF_HAND)
					){

				//オフハンドで指定ツールを持っていた時の処理

				//設置をキャンセル
				event.setCancelled(true);

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
				if(playerdata.activeskilldata.skillnum == 10 && playerdata.activeskilldata.skilltype == 5){
					activemineflagnum = (playerdata.activeskilldata.mineflagnum + 1) % 2;
					switch (activemineflagnum){
					case 0:
						player.sendMessage(ChatColor.GOLD + "アサルト・アーマー" + "：OFF");
						break;
					case 1:
						player.sendMessage(ChatColor.GOLD + "アサルト・アーマー" + ":ON");
						new AssaultArmorTaskRunnable(player).runTaskTimer(plugin,0,1);
						break;
					}
					player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
					playerdata.activeskilldata.mineflagnum = activemineflagnum;
				}
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
	//棒メニューを開くイベント
	@EventHandler
	public void onPlayerMenuEvent(PlayerInteractEvent event){
		//プレイヤーを取得
		Player player = event.getPlayer();
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//アクションを起こした手を取得
		EquipmentSlot equipmentslot = event.getHand();

		if(player.getInventory().getItemInMainHand().getType().equals(Material.STICK)){
			//メインハンドに棒を持っているときの処理
			//アクションキャンセル
			event.setCancelled(true);
			if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
				//右クリックの処理
				//オフハンドのアクション実行時処理を終了
				if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				Inventory inv = MenuInventoryData.getMenuData(player);
				if(inv == null){
					return;
				}
				player.openInventory(inv);
			}
		}
	}
	//プレイヤーの拡張インベントリを開くイベント
	@EventHandler
	public void onPlayerOpenInventorySkillEvent(PlayerInteractEvent event){
		//プレイヤーを取得
		Player player = event.getPlayer();
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//使った手を取得
		EquipmentSlot equipmentslot = event.getHand();

		if(event.getMaterial().equals(Material.ENDER_PORTAL_FRAME)){
			//設置をキャンセル
			event.setCancelled(true);
			//UUIDを取得
			UUID uuid = player.getUniqueId();
			//playerdataを取得
			PlayerData playerdata = playermap.get(uuid);
			//念のためエラー分岐
			if(playerdata == null){
				player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[インベントリから四次元ポケットOPEN処理]でエラー発生");
				plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
				return;
			}
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
