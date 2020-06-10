package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.seichiskill.{BlockSearching, BreakArea}
import com.github.unchama.seichiassist.task.GiganticBerserkTask
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
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
    if (!Util.seichiSkillsAllowedIn(player.getWorld)) return

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
    val playerData = playermap(player.getUniqueId)

    val mana = playerData.manaState

    val skillState = playerData.skillState.get.unsafeRunSync()
    val selectedSkill = skillState.activeSkill.getOrElse(return)
    val activeSkillArea = BreakArea(selectedSkill, skillState.usageMode)

    val breakArea = activeSkillArea.makeBreakArea(player).unsafeRunSync().head

    // 破壊範囲のブロック数
    val breakAreaVolume = {
      val breakLength = activeSkillArea.breakLength
      breakLength.x * breakLength.y * breakLength.z
    }

    val isMultiTypeBreakingSkillEnabled = {
      import ManagedWorld._

      playerData.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel &&
        (player.getWorld.isSeichi || playerData.settings.multipleidbreakflag)
    }

    import com.github.unchama.seichiassist.data.syntax._
    val BlockSearching.Result(breakBlocks, _, lavaBlocks) =
      BlockSearching
        .searchForBlocksBreakableWithSkill(player, breakArea.gridPoints(), hitBlock)
        .unsafeRunSync()
        .filterSolids(targetBlock =>
          isMultiTypeBreakingSkillEnabled || BlockSearching.multiTypeBreakingFilterPredicate(hitBlock)(targetBlock)
        )

    //重力値計算
    val gravity = BreakUtil.getGravity(player, hitBlock, isAssault = false)

    //減る経験値計算
    //実際に破壊するブロック数 * 全てのブロックを破壊したときの消費経験値 ÷ すべての破壊するブロック数 * 重力
    val manaConsumption = breakBlocks.size.toDouble * (gravity + 1) * selectedSkill.manaCost / breakAreaVolume

    //セットする耐久値の計算
    //１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
    val nextDurability = {
      val durabilityEnchantment = tool.getEnchantmentLevel(Enchantment.DURABILITY)

      tool.getDurability +
        BreakUtil.calcDurability(durabilityEnchantment, breakBlocks.size) +
        BreakUtil.calcDurability(durabilityEnchantment, 10 * lavaBlocks.size)
    }.toShort

    //重力値の判定
    if (gravity > 15) {
      player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。")
      return
    }

    //実際に経験値を減らせるか判定
    if (!mana.has(manaConsumption)) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナが足りません")
      return
    }

    //実際に耐久値を減らせるか判定
    if (tool.getType.getMaxDurability <= nextDurability && !tool.getItemMeta.isUnbreakable) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません")
      return
    }

    //経験値を減らす
    mana.decrease(manaConsumption, player, playerData.level)

    //耐久値を減らす
    if (!tool.getItemMeta.isUnbreakable) tool.setDurability(nextDurability)

    //以降破壊する処理
    //溶岩を破壊する処理
    lavaBlocks.foreach(_.setType(Material.AIR))

    //元ブロックの真ん中の位置
    val centerOfBlock = hitBlock.getLocation.add(0.5, 0.5, 0.5)

    com.github.unchama.seichiassist.unsafe.runIOAsync(
      "破壊エフェクトを再生する",
      playerData.skillEffectState.selection
        .runBreakEffect(player, selectedSkill, tool, breakBlocks.toSet, breakArea, centerOfBlock)
    )
  }

  @EventHandler def onEntityExplodeEvent(event: EntityExplodeEvent): Unit = {
    event.getEntity match {
      case e: Projectile =>
        if (SeichiAssist.instance.arrowSkillProjectileScope.isTracked(e).unsafeRunSync())
          event.setCancelled(true)
      case _ =>
    }
  }

  @EventHandler def onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent): Unit = {
    event.getDamager match {
      case e: Projectile =>
        if (SeichiAssist.instance.arrowSkillProjectileScope.isTracked(e).unsafeRunSync())
          event.setCancelled(true)
      case _ =>
    }
  }

  @EventHandler def onPotionSplashEvent(event: PotionSplashEvent): Unit = {
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