package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.activeskill.BlockSearching
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillEffect
import com.github.unchama.seichiassist.task.GiganticBerserkTask
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import com.github.unchama.util.external.ExternalPlugins
import org.bukkit._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{Player, Projectile}
import org.bukkit.event.entity._
import org.bukkit.event.{EventHandler, Listener}

class EntityListener extends Listener {
  private val playermap = SeichiAssist.playermap

  @EventHandler def onPlayerActiveSkillEvent(event: ProjectileHitEvent): Unit = { //矢を取得する
    val projectile = event.getEntity

    if (!SeichiAssist.instance.arrowSkillProjectileScope.isTracked(projectile).unsafeRunSync()) return

    SeichiAssist.instance.arrowSkillProjectileScope.release(projectile).unsafeRunSync()

    val player = projectile.getShooter match {
      case p: Player => p
      case _ => return
    }

    //もしサバイバルでなければ処理を終了
    //もしフライ中なら終了
    if ((player.getGameMode ne GameMode.SURVIVAL) || player.isFlying) return

    //壊されるブロックを取得
    val block =
      MaterialSets.refineBlock(
        player.getWorld.getBlockAt(projectile.getLocation.add(projectile.getVelocity.normalize)),
        MaterialSets.materials
      ).getOrElse(return)

    //整地ワールドでは重力値によるキャンセル判定を行う(スキル判定より先に判定させること)
    if (BreakUtil.getGravity(player, block, isAssault = false) > 3) {
      player.sendMessage(ChatColor.RED + "整地ワールドでは必ず上から掘ってください。")
      return
    }

    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) return

    //破壊不可能な場合は処理を終了
    if (!BreakUtil.canBreakWithSkill(player, block)) return

    //実際に使用するツール
    val tool = MaterialSets.refineItemStack(
      player.getInventory.getItemInMainHand,
      MaterialSets.breakToolMaterials
    ).getOrElse(return)

    //耐久値がマイナスかつ耐久無限ツールでない時処理を終了
    if (tool.getDurability > tool.getType.getMaxDurability && !tool.getItemMeta.spigot.isUnbreakable) return

    runArrowSkillOfHitBlock(player, block, tool)
  }

  private def runArrowSkillOfHitBlock(player: Player, hitBlock: BlockBreakableBySkill, tool: BreakTool): Unit = {
    val uuid = player.getUniqueId
    val playerData = playermap.apply(uuid)

    //マナを取得
    val mana = playerData.activeskilldata.mana

    val area = playerData.activeskilldata.area.makeBreakArea(player).unsafeRunSync()(0)

    //エフェクト用に壊されるブロック全てのリストデータ
    //一回の破壊の範囲
    val breakLength = playerData.activeskilldata.area.breakLength

    //１回の全て破壊したときのブロック数
    val ifAllBreakNum = breakLength.x * breakLength.y * breakLength.z

    val isMultiTypeBreakingSkillEnabled = {
      val playerData = SeichiAssist.playermap(player.getUniqueId)

      import ManagedWorld._
      playerData.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel &&
        (player.getWorld.isSeichi || playerData.settings.multipleidbreakflag)
    }

    import com.github.unchama.seichiassist.data.syntax._
    val BlockSearching.Result(breakBlocks, _, lavaBlocks) =
      BlockSearching
        .searchForBreakableBlocks(player, area.gridPoints(), hitBlock)
        .unsafeRunSync()
        .filterSolids(targetBlock =>
          isMultiTypeBreakingSkillEnabled || BlockSearching.multiTypeBreakingFilterPredicate(hitBlock)(targetBlock)
        )

    //重力値計算
    val gravity = BreakUtil.getGravity(player, hitBlock, isAssault = false)

    //減る経験値計算
    //実際に破壊するブロック数  * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力
    val useMana =
      breakBlocks.size.toDouble *
        (gravity + 1) *
        ActiveSkill.getActiveSkillUseExp(playerData.activeskilldata.skilltype, playerData.activeskilldata.skillnum) / ifAllBreakNum

    //減る耐久値の計算
    //１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
    val durability =
      (tool.getDurability +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breakBlocks.size) +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), 10 * lavaBlocks.size)).toShort

    //重力値の判定
    if (gravity > 15) {
      player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。")
      return
    }

    //実際に経験値を減らせるか判定
    if (!mana.has(useMana)) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナが足りません")
      return
    }

    //実際に耐久値を減らせるか判定
    if (tool.getType.getMaxDurability <= durability && !tool.getItemMeta.spigot.isUnbreakable) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません")
      return
    }

    //経験値を減らす
    mana.decrease(useMana, player, playerData.level)

    //耐久値を減らす
    if (!tool.getItemMeta.spigot.isUnbreakable) tool.setDurability(durability)

    //以降破壊する処理
    //溶岩を破壊する処理
    lavaBlocks.foreach(_.setType(Material.AIR))

    //元ブロックの真ん中の位置
    val centerOfBlock = hitBlock.getLocation.add(0.5, 0.5, 0.5)

    com.github.unchama.seichiassist.unsafe.runIOAsync(
      "破壊エフェクトを再生する",
      ActiveSkillEffect
        .fromEffectNum(playerData.activeskilldata.effectnum, playerData.activeskilldata.skillnum)
        .runBreakEffect(player, playerData.activeskilldata, tool, breakBlocks.toSet, area, centerOfBlock)
    )
  }

  @EventHandler def onEntityExplodeEvent(event: EntityExplodeEvent) = {
    event.getEntity match {
      case e: Projectile =>
        if (SeichiAssist.instance.arrowSkillProjectileScope.isTracked(e).unsafeRunSync())
          event.setCancelled(true)
      case _ =>
    }
  }

  @EventHandler def onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) = {
    event.getDamager match {
      case e: Projectile =>
        if (SeichiAssist.instance.arrowSkillProjectileScope.isTracked(e).unsafeRunSync())
          event.setCancelled(true)
      case _ =>
    }
  }

  @EventHandler def onPotionSplashEvent(event: PotionSplashEvent) = {
    event.getPotion match {
      case e if e != null =>
        if (SeichiAssist.instance.arrowSkillProjectileScope.isTracked(e).unsafeRunSync())
          event.setCancelled(true)
      case _ =>
    }
  }

  @EventHandler def onDeath(event: EntityDeathEvent): Unit = {
    /*GiganticBerserk用*/
    //死んだMOBがGiganticBerserkの対象MOBでなければ終了
    if (!Util.isEnemy(event.getEntity.getType)) return
    val player = event.getEntity.getKiller
    //MOBを倒したプレイヤーがいなければ終了
    if (player == null) return
    //プレイヤーが整地ワールドに居ない場合終了
    if (!Util.isSeichiWorld(player)) return
    val GBTR = new GiganticBerserkTask
    GBTR.PlayerKillEnemy(player)
  }
}