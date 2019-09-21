package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.data.BreakArea;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.task.GiganticBerserkTask;
import com.github.unchama.seichiassist.util.BreakUtil;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.seichiassist.util.external.ExternalPlugins;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityListener implements Listener {
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap();

	@EventHandler
	public void onPlayerActiveSkillEvent(ProjectileHitEvent event){
		//矢を取得する
		final Projectile e = event.getEntity();
		ProjectileSource projsource;
		Player player;


		if(!e.hasMetadata("ArrowSkill")) {
			return;
		}
		Projectile proj = e;
		projsource = proj.getShooter();
		if(!(projsource instanceof Player)){
			return;
		}
		player = (Player)projsource;

		if(SeichiAssist.getDEBUG()){
			player.sendMessage(ChatColor.RED + "ProjectileHitEventの呼び出し");
		}

		//もしサバイバルでなければ処理を終了
		//もしフライ中なら終了
		if(player.getGameMode() != GameMode.SURVIVAL || player.isFlying()){
			return;
		}


		//壊されるブロックを取得
		Block block;
		block = player.getWorld().getBlockAt(proj.getLocation().add(proj.getVelocity().normalize()));


		//他人の保護がかかっている場合は処理を終了
		if(!ExternalPlugins.worldGuard().canBuild(player, block.getLocation())){
			return;
		}

		//ブロックのタイプを取得
		Material material = block.getType();

		//ブロックタイプがmateriallistに登録されていなければ処理終了
		if(!MaterialSets.INSTANCE.getMaterials().contains(material) && e.hasMetadata("ArrowSkill")){
			return;
		}

		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//UUIDを基にプレイヤーデータ取得
		PlayerData playerdata = SeichiAssist.playermap().get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			Util.INSTANCE.sendPlayerDataNullMessage(player);
			Bukkit.getLogger().warning(player.getName() + " -> PlayerData not found.");
			Bukkit.getLogger().warning("EntityListener.onPlayerActiveSkillEvent");
			return;
		}

		//整地ワールドでは重力値によるキャンセル判定を行う(スキル判定より先に判定させること)
		if(BreakUtil.INSTANCE.getGravity(player, block, false) > 3){
			player.sendMessage(ChatColor.RED + "整地ワールドでは必ず上から掘ってください。");
			return;
		}
		//スキル発動条件がそろってなければ終了
		if(!Util.INSTANCE.isSkillEnable(player)){
			return;
		}







		//プレイヤーインベントリを取得
		PlayerInventory inventory = player.getInventory();
		//メインハンドとオフハンドを取得
		ItemStack mainhanditem = inventory.getItemInMainHand();
		ItemStack offhanditem = inventory.getItemInOffHand();
		//実際に使用するツールを格納する
		ItemStack tool;
		//メインハンドにツールがあるか
		boolean mainhandtoolflag = MaterialSets.INSTANCE.getBreakMaterials().contains(mainhanditem.getType());
		//オフハンドにツールがあるか
		boolean offhandtoolflag = MaterialSets.INSTANCE.getBreakMaterials().contains(offhanditem.getType());

		//場合分け
		if(mainhandtoolflag){
			//メインハンドの時
			tool = mainhanditem;
		}else if(offhandtoolflag){
			//サブハンドの時
			return;
		}else{
			//どちらにももっていない時処理を終了
			return;
		}
		//耐久値がマイナスかつ耐久無限ツールでない時処理を終了
		if(tool.getDurability() > tool.getType().getMaxDurability() && !tool.getItemMeta().spigot().isUnbreakable()){
			return;
		}
		//スキルで破壊されるブロックの時処理を終了
		if(SeichiAssist.allblocklist().contains(block)){
			if(SeichiAssist.getDEBUG()){
				player.sendMessage("スキルで使用中のブロックです。");
			}
			return;
		}

		runArrowSkillofHitBlock(player,proj, block, tool);

		SeichiAssist.entitylist().remove(proj);
		proj.remove();
	}


	private void runArrowSkillofHitBlock(Player player,Projectile proj,
			Block block, ItemStack tool) {
		/*遠距離破壊スキルリスナー*/

		//UUIDを取得
		UUID uuid = player.getUniqueId();
		//playerdataを取得
		PlayerData playerdata = playermap.get(uuid);
		//レベルを取得
		//int skilllevel = playerdata.activeskilldata.skillnum;
		//マナを取得
		Mana mana = playerdata.getActiveskilldata().mana;
		//元ブロックのマテリアルを取得
		Material material = block.getType();
		//元ブロックの真ん中の位置を取得
		Location centerofblock = block.getLocation().add(0.5, 0.5, 0.5);

		//壊されるブロックの宣言
		Block breakblock;
		BreakArea area = playerdata.getActiveskilldata().area;
		//現在のプレイヤーの向いている方向
		String dir = BreakUtil.INSTANCE.getCardinalDirection(player);
		//もし前回とプレイヤーの向いている方向が違ったら範囲を取り直す
		if(!dir.equals(area.getDir())){
			area.setDir(dir);
			area.makeArea();
		}
		Coordinate start = area.getStartList().get(0);
		Coordinate end = area.getEndList().get(0);

		//エフェクト用に壊されるブロック全てのリストデータ

        //一回の破壊の範囲
		final Coordinate breaklength = area.getBreakLength();
		//１回の全て破壊したときのブロック数
		final int ifallbreaknum = (breaklength.x * breaklength.y * breaklength.z);
		Set<Block> breakBlock = new HashSet<>(ifallbreaknum);
		Set<Block> lavas = new HashSet<>(ifallbreaknum / 10);

		for(int y = end.y; y >=start.y ; y--){ //上から処理に変更
			for(int x = start.x ; x <= end.x ; x++){
				for(int z = start.z ; z <= end.z ; z++){
					breakblock = block.getRelative(x, y, z);

					if(playerdata.getLevel() >= SeichiAssist.seichiAssistConfig().getMultipleIDBlockBreaklevel() && playerdata.getSettings().getMultipleidbreakflag()) { //追加テスト(複数種類一括破壊スキル)
						if(breakblock.getType() != Material.AIR && breakblock.getType() != Material.BEDROCK) {
							if(breakblock.getType() == Material.STATIONARY_LAVA || BreakUtil.INSTANCE.BlockEqualsMaterialList(breakblock)){
								if(BreakUtil.INSTANCE.canBreak(player, breakblock)){
									if(breakblock.getType() == Material.STATIONARY_LAVA){
										lavas.add(breakblock);
									}else{
										breakBlock.add(breakblock);
										SeichiAssist.allblocklist().add(breakblock);
									}
								}
							}
						}
					} else { //条件を満たしていない
						//player.sendMessage("x:" + x + "y:" + y + "z:" + z + "Type:"+ breakblock.getType().name());
						//もし壊されるブロックがもともとのブロックと同じ種類だった場合
						if(breakblock.getType() == material
								|| (block.getType() == Material.DIRT && breakblock.getType() == Material.GRASS)
								|| (block.getType() == Material.GRASS && breakblock.getType() == Material.DIRT)
								|| (block.getType() == Material.GLOWING_REDSTONE_ORE && breakblock.getType() == Material.REDSTONE_ORE)
								|| (block.getType() == Material.REDSTONE_ORE && breakblock.getType() == Material.GLOWING_REDSTONE_ORE)
								|| breakblock.getType() == Material.STATIONARY_LAVA
								){
							if(BreakUtil.INSTANCE.canBreak(player, breakblock)){
								if(breakblock.getType() == Material.STATIONARY_LAVA){
									lavas.add(breakblock);
								}else{
									breakBlock.add(breakblock);
									SeichiAssist.allblocklist().add(breakblock);
								}
							}
						}
					}
				}
			}
		}


		//重力値計算
		int gravity = BreakUtil.INSTANCE.getGravity(player,block,false);


		//減る経験値計算
		//実際に破壊するブロック数  * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力

		double useMana = (double) (breakBlock.size()) * (gravity + 1)
				* ActiveSkill.activeSkillUseExp(playerdata.getActiveskilldata().skilltype, playerdata.getActiveskilldata().skillnum)
				/(ifallbreaknum) ;
		if(SeichiAssist.getDEBUG()){
			player.sendMessage(ChatColor.RED + "必要経験値：" + ActiveSkill.activeSkillUseExp(playerdata.getActiveskilldata().skilltype, playerdata.getActiveskilldata().skillnum));
			player.sendMessage(ChatColor.RED + "全ての破壊数：" + (ifallbreaknum));
			player.sendMessage(ChatColor.RED + "実際の破壊数：" + breakBlock.size());
			player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナ：" + useMana);
		}
		//減る耐久値の計算
		short durability = (short) (tool.getDurability() + BreakUtil.INSTANCE.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),breakBlock.size()));
		//１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
		durability += BreakUtil.INSTANCE.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY),10 * lavas.size());


		//重力値の判定
		if(gravity > 15) {
			player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。");
			SeichiAssist.allblocklist().removeAll(breakBlock);
			return;
		}

		//実際に経験値を減らせるか判定
		if(!mana.has(useMana)){
			//デバッグ用
			if(SeichiAssist.getDEBUG()){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナが足りません");
			}
			SeichiAssist.allblocklist().removeAll(breakBlock);
			return;
		}
		if(SeichiAssist.getDEBUG()){
			player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値:" + durability);
		}
		//実際に耐久値を減らせるか判定
		if(tool.getType().getMaxDurability() <= durability && !tool.getItemMeta().spigot().isUnbreakable()){
			//デバッグ用
			if(SeichiAssist.getDEBUG()){
				player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません");
			}
			SeichiAssist.allblocklist().removeAll(breakBlock);
			return;
		}


		//経験値を減らす
		mana.decrease(useMana,player, playerdata.getLevel());

		//耐久値を減らす
		if(!tool.getItemMeta().spigot().isUnbreakable()){
			tool.setDurability(durability);
		}

		//以降破壊する処理

		//溶岩を破壊する処理
		for (final Block value : lavas) {
			value.setType(Material.AIR);
		}


		//選択されたブロックを破壊する処理

		//エフェクトが指定されていないときの処理
		if(playerdata.getActiveskilldata().effectnum == 0){
			for(final Block b : breakBlock){
				BreakUtil.INSTANCE.breakBlock(player, b, player.getLocation(), tool,false);
				SeichiAssist.allblocklist().remove(b);
			}
		}
		//通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
		else if(playerdata.getActiveskilldata().effectnum <= 100){
			ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();
			skilleffect[playerdata.getActiveskilldata().effectnum - 1].runBreakEffect(player, playerdata.getActiveskilldata(), tool, breakBlock, start, end,centerofblock);
		}

		//スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
		else if(playerdata.getActiveskilldata().effectnum > 100){
			ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();

			premiumeffect[playerdata.getActiveskilldata().effectnum - 1 - 100].runBreakEffect(player, tool, breakBlock, start, end, centerofblock);
		}

	}


	@EventHandler
	public void onEntityExplodeEvent(EntityExplodeEvent event){
		Entity e = event.getEntity();
		if ( e instanceof Projectile){
			if(e.hasMetadata("ArrowSkill") || e.hasMetadata("Effect")){
				event.setCancelled(true);
			}
		}

	}


	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		Entity e = event.getDamager();
		if ( e instanceof Projectile){
			if(e.hasMetadata("ArrowSkill") || e.hasMetadata("Effect")){
				event.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void onPotionSplashEvent(PotionSplashEvent event){
		ThrownPotion thrown = event.getPotion();
		if (thrown != null){
			if(thrown.hasMetadata("ArrowSkill") || thrown.hasMetadata("Effect")){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		/*GiganticBerserk用*/

		//死んだMOBがGiganticBerserkの対象MOBでなければ終了
		if(!Util.INSTANCE.isEnemy(event.getEntity().getType())){
			return;
		}

		Player player = event.getEntity().getKiller();
		//MOBを倒したプレイヤーがいなければ終了
		if (player == null) {
			return;
		}
		//プレイヤーが整地ワールドに居ない場合終了
		if (!Util.INSTANCE.isSeichiWorld(player)){
			return;
		}

		GiganticBerserkTask GBTR = new GiganticBerserkTask();

		GBTR.PlayerKillEnemy(player);

	}
}
