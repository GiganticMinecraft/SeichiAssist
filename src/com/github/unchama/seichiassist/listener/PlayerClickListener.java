package com.github.unchama.seichiassist.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MenuInventoryData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.CoolDownTaskRunnable;
import com.github.unchama.seichiassist.task.EntityRemoveTaskRunnable;
import com.github.unchama.seichiassist.util.Util;

public class PlayerClickListener implements Listener {
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

		//playerdataがない場合はreturn
		if(playerdata == null){
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
		//サバイバルでない時　または　フライ中の時終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL) || player.isFlying()){
			return;
		}
		//アクティブスキルフラグがオフの時処理を終了
		if(playerdata.activeskilldata.mineflagnum == 0 || playerdata.activeskilldata.skillnum == 0){
			return;
		}

		//スキル発動条件がそろってなければ終了
		if(!Util.isSkillEnable(player)){
			return;
		}


		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			//アサルトアーマー使用中の時は終了左クリックで判定
			if(playerdata.activeskilldata.assaulttype!=0){
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
			        //クールダウン処理
			        long cooldown = ActiveSkill.ARROW.getCoolDown(playerdata.activeskilldata.skillnum);
			        if(cooldown > 5){
			        	new CoolDownTaskRunnable(player,false,true,false).runTaskLater(plugin,cooldown);
			        }else{
			        	new CoolDownTaskRunnable(player,false,false,false).runTaskLater(plugin,cooldown);
			        }
					//エフェクトが指定されていないときの処理
					if(playerdata.activeskilldata.effectnum == 0){
						runArrowSkill(player,Arrow.class);
					}
					//エフェクトが指定されているときの処理
					else if(playerdata.activeskilldata.effectnum <= 100){
						ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
						skilleffect[playerdata.activeskilldata.effectnum - 1].runArrowEffect(player);
					}else if(playerdata.activeskilldata.effectnum > 100){
						ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
						premiumeffect[playerdata.activeskilldata.effectnum - 1 -100].runArrowEffect(player);
					}
				}
			}
		}else if(action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)){
			//アサルトアーマーをどっちも使用していない時終了
			if(playerdata.activeskilldata.assaulttype == 0){
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
			        //クールダウン処理
			        long cooldown = ActiveSkill.ARROW.getCoolDown(playerdata.activeskilldata.skillnum);
			        if(cooldown > 5){
			        	new CoolDownTaskRunnable(player,false,true,false).runTaskLater(plugin,cooldown);
			        }else{
			        	new CoolDownTaskRunnable(player,false,false,false).runTaskLater(plugin,cooldown);
			        }
					//エフェクトが指定されていないときの処理
					if(playerdata.activeskilldata.effectnum == 0){
						runArrowSkill(player,Arrow.class);
					}
					//通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
					else if(playerdata.activeskilldata.effectnum <= 100){
						ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
						skilleffect[playerdata.activeskilldata.effectnum - 1].runArrowEffect(player);
					}

					//スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
					else if(playerdata.activeskilldata.effectnum > 100){
						ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
						premiumeffect[playerdata.activeskilldata.effectnum - 1 - 100].runArrowEffect(player);
					}

				}
			}
		}
	}
	private <T extends org.bukkit.entity.Projectile> void runArrowSkill(Player player, Class<T> clazz) {
		//プレイヤーの位置を取得
		Location ploc = player.getLocation();

		//発射する音を再生する.
    	player.playSound(ploc, Sound.ENTITY_ARROW_SHOOT, 1, 1);

    	//スキルを実行する処理
        Location loc = player.getLocation();
        loc.add(loc.getDirection()).add(0,1.6,0);
        Vector vec = loc.getDirection();
        double k = 1.0;
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
        new EntityRemoveTaskRunnable(proj).runTaskLater(plugin,100);
	}


	//プレイヤーが右クリックした時に実行(ガチャを引く部分の処理)
	@EventHandler
	public void onPlayerRightClickGachaEvent(PlayerInteractEvent event){
		//プレイヤー型を取得
		Player player = event.getPlayer();
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータを取得
		PlayerData playerdata = playermap.get(uuid);
		//playerdataがない場合はreturn
		if(playerdata == null){
			return;
		}

		String name = playerdata.name;
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//使った手を取得
		EquipmentSlot equipmentslot = event.getHand();
		//もしサバイバルでなければ処理を終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return;
		}
		//使ったアイテムを取得
		if(event.getItem() == null){
			return ;
		}
		ItemStack itemstack = event.getItem();
		//ガチャ用の頭でなければ終了
		if(!Util.isGachaTicket(itemstack)){
			return;
		}
		event.setCancelled(true);

		//以下サバイバル時のガチャ券の処理↓

		//連打防止クールダウン処理
		if(!playerdata.gachacooldownflag){
			return;
		}else{
	        //連打による負荷防止の為クールダウン処理
			new CoolDownTaskRunnable(player,false,false,true).runTaskLater(plugin,4);
		}

		//オフハンドから実行された時処理を終了
		if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
			return;
		}
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

		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			int count = 1;
			if(player.isSneaking()){
				count = itemstack.getAmount();
				player.sendMessage(ChatColor.AQUA + "" + count + "回ガチャを回しました。");
			}

			if(!Util.removeItemfromPlayerInventory(player.getInventory(),itemstack,count)){
				player.sendMessage(ChatColor.RED + "ガチャ券の数が不正です。");
				return;
			}
			for(int c = 0 ; c < count ; c++){
				//プレゼント用ガチャデータ作成
				GachaData present;
				//ガチャ実行
				present = GachaData.runGacha();
				if(present.probability < 0.1){
					present.addname(name);
				}
				//ガチャデータのitemstackの数を再設定（バグのため）
				present.itemstack.setAmount(present.amount);
				//メッセージ設定
				String str = "";

				//プレゼントを格納orドロップ
				if(!Util.isPlayerInventryFill(player)){
					Util.addItem(player,present.itemstack);
				}else{
					Util.dropItem(player,present.itemstack);
					str += ChatColor.AQUA + "プレゼントがドロップしました。";
				}

				//確率に応じてメッセージを送信
				if(present.probability < 0.001){
					Util.sendEverySoundWithoutIgnore(Sound.ENTITY_ENDERDRAGON_DEATH,(float)0.5, 2);
					if (!playerdata.everysoundflag) {
						player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, (float) 0.5, 2);
					}
					List<String> enchantname = new ArrayList<String>();
					List<String> lore = present.itemstack.getItemMeta().getLore();
					Map<Enchantment, Integer> enchantment = present.itemstack.getItemMeta().getEnchants();

					for(Enchantment enchant : enchantment.keySet())
					{
						enchantname.add(ChatColor.GRAY + Util.getEnchantName(enchant.getName(), enchantment.get(enchant)));
					}
					lore.remove(lore.indexOf("§r§2所有者：" + player.getName()));

					TextComponent message = new TextComponent();
					message.setText(ChatColor.AQUA + present.itemstack.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
					message.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(" " + present.itemstack.getItemMeta().getDisplayName() +  "\n" + Util.getDescFormat(enchantname) + Util.getDescFormat(lore)).create() ) );

					player.sendMessage(ChatColor.RED + "おめでとう！！！！！Gigantic☆大当たり！" + str);
					Util.sendEveryMessageWithoutIgnore(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり！");
					Util.sendEveryMessageWithoutIgnore(message);
				}else if(present.probability < 0.01){
					//大当たり時にSEを鳴らす(自分だけ)
					player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, (float) 0.8, 1);
					//ver 0.3.1以降 大当たり時の全体通知を削除
					// Util.sendEverySound(Sound.ENTITY_WITHER_SPAWN, (float) 0.8, 1);
					player.sendMessage(ChatColor.GOLD + "おめでとう！！大当たり！" + str);

					//Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり！\n" + ChatColor.DARK_BLUE + present.itemstack.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
				}else if(present.probability < 0.1){
					player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！" + str);
				}else{
					if(count == 1){
						player.sendMessage(ChatColor.WHITE + "はずれ！また遊んでね！" + str);
					}
				}
			}
			player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, (float) 0.1);
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
		if(player.getInventory().getItemInMainHand().getType().equals(Material.STICK)
			|| player.getInventory().getItemInMainHand().getType().equals(Material.SKULL_ITEM)
			){
			return;
		}
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//playerdataがない場合はreturn
		if(playerdata == null){
			return;
		}


		//スキル発動条件がそろってなければ終了
		if(!Util.isSkillEnable(player)){
			return;
		}

		//アクティブスキルを発動できるレベルに達していない場合処理終了
		if( playerdata.level < SeichiAssist.config.getDualBreaklevel()){
			return;
		}

		//クールダウンタイム中は処理を終了
		if(!playerdata.activeskilldata.skillcanbreakflag){
			//SEを再生
			//player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, (float)0.5, 1);
			return;
		}

		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){

			boolean mainhandflag = SeichiAssist.breakmateriallist.contains(player.getInventory().getItemInMainHand().getType());
			boolean offhandflag = SeichiAssist.breakmateriallist.contains(player.getInventory().getItemInOffHand().getType());

			int activemineflagnum = playerdata.activeskilldata.mineflagnum;
			//どちらにも対応したアイテムを持っていない場合終了
			if(!mainhandflag && !offhandflag){
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

			if(mainhandflag && equipmentslot.equals(EquipmentSlot.HAND)){
				//メインハンドで指定ツールを持っていた時の処理
				//スニークしていないかつアサルトタイプが選択されていない時処理を終了
				if(!player.isSneaking() && playerdata.activeskilldata.assaulttype == 0){
					return;
				}

				//設置をキャンセル
				event.setCancelled(true);

				if((playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum() && playerdata.activeskilldata.skillnum == 1)
						|| (playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum() && playerdata.activeskilldata.skillnum == 2)){

					activemineflagnum = (activemineflagnum + 1) % 3;
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
					playerdata.activeskilldata.updataSkill(player, playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum, activemineflagnum);
					player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				}else if(playerdata.activeskilldata.skilltype > 0 && playerdata.activeskilldata.skillnum > 0
						&& playerdata.activeskilldata.skilltype < 4
						){
					activemineflagnum = (activemineflagnum + 1) % 2;
					switch (activemineflagnum){
					case 0:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype,playerdata.activeskilldata.skillnum) + "：OFF");
						break;
					case 1:
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype,playerdata.activeskilldata.skillnum) + ":ON");
						break;
					}
					playerdata.activeskilldata.updataSkill(player, playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum, activemineflagnum);
					player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				}
			}

			if(SeichiAssist.breakmateriallist.contains(player.getInventory().getItemInOffHand().getType())
					&& equipmentslot.equals(EquipmentSlot.OFF_HAND)
					){
				//オフハンドで指定ツールを持っていた時の処理

				//設置をキャンセル
				event.setCancelled(true);


				if(playerdata.activeskilldata.assaultnum >=4 && playerdata.activeskilldata.assaulttype >=4){
					//メインハンドでも指定ツールを持っていたらフラグは変えない
					if(!mainhandflag || playerdata.activeskilldata.skillnum == 0){
						activemineflagnum = (activemineflagnum + 1) % 2;
					}
					if(activemineflagnum == 0){
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.assaulttype,playerdata.activeskilldata.assaultnum) + "：OFF");
					}else{
						player.sendMessage(ChatColor.GOLD + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.assaulttype,playerdata.activeskilldata.assaultnum) + ":ON");
					}
					playerdata.activeskilldata.updataAssaultSkill(player, playerdata.activeskilldata.assaulttype, playerdata.activeskilldata.assaultnum,activemineflagnum);
					player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
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
				Util.sendPlayerDataNullMessage(player);
				plugin.getLogger().warning(player.getName() + " -> PlayerData not found.");
				plugin.getLogger().warning("PlayerClickListener.onPlayerOpenInventorySkillEvent");
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

	//　経験値瓶を持った状態でのShift右クリック…一括使用
	@EventHandler
	public void onPlayerRightClickExpBottleEvent(PlayerInteractEvent event){
		// 経験値瓶を持った状態でShift右クリックをした場合
		if (event.getPlayer().isSneaking() && event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.EXP_BOTTLE)
				&& (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			event.setCancelled(true);
			int num = event.getItem().getAmount();
			for(int cnt = 0; cnt < num; cnt++) {
				event.getPlayer().launchProjectile(ThrownExpBottle.class);
			}
			event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
		}
	}

/*
	//芋を食べる
	@EventHandler
	public void onPlayerUseGachaimoEvent(PlayerInteractEvent event){
		//プレイヤーを取得
		Player player = event.getPlayer();
		//プレイヤーが起こしたアクションを取得
		Action action = event.getAction();
		//アクションを起こした手を取得
		EquipmentSlot equipmentslot = event.getHand();
		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//マナを取得
		Mana mana = playerdata.activeskilldata.mana;
		//レベルを取得
		int level = playerdata.level;

		//オフハンドのアクション実行時処理を終了
		if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
			return;
		}
		ItemStack gachaimo = player.getInventory().getItemInMainHand();
		ItemMeta meta = gachaimo.getItemMeta();
		if(gachaimo.equals(Material.BAKED_POTATO)&& Util.LoreContains(meta.getLore(), "マナ回復（小）")){
			player.sendMessage("呼び出されました");
			//メインハンドに芋マナ回復（小）を持っているときの処理
			if(action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)){
				player.sendMessage("呼び出されました");
				//左クリックの処理
				final PlayerItemConsumeEvent re = new PlayerItemConsumeEvent(player,gachaimo);
				Bukkit.getPluginManager().callEvent(re);
			}
		}
	}
*/
}
