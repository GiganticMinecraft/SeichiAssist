package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}
import com.github.unchama.seichiassist.effect.ActiveSkillEffect
import com.github.unchama.seichiassist.task.GiganticBerserkTask
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{Player, Projectile}
import org.bukkit.event.entity._
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

import scala.collection.mutable

class EntityListener extends Listener {
  private val playermap = SeichiAssist.playermap

  @EventHandler def onPlayerActiveSkillEvent(event: ProjectileHitEvent): Unit = { //矢を取得する
    val e = event.getEntity
    if (!e.hasMetadata("ArrowSkill")) return
    val proj = e
    val projsource = proj.getShooter
    if (!projsource.isInstanceOf[Player]) return
    val player = projsource.asInstanceOf[Player]

    //もしサバイバルでなければ処理を終了
    //もしフライ中なら終了
    if ((player.getGameMode ne GameMode.SURVIVAL) || player.isFlying) return

    //壊されるブロックを取得
    val block = player.getWorld.getBlockAt(proj.getLocation.add(proj.getVelocity.normalize))

    //他人の保護がかかっている場合は処理を終了
    if (!ExternalPlugins.getWorldGuard.canBuild(player, block.getLocation)) return

    //ブロックのタイプを取得
    val material = block.getType

    //ブロックタイプがmateriallistに登録されていなければ処理終了
    if (!MaterialSets.materials.contains(material)) return

    //整地ワールドでは重力値によるキャンセル判定を行う(スキル判定より先に判定させること)
    if (BreakUtil.getGravity(player, block, isAssault = false) > 3) {
      player.sendMessage(ChatColor.RED + "整地ワールドでは必ず上から掘ってください。")
      return
    }

    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) return

    //プレイヤーインベントリを取得
    val inventory = player.getInventory

    //メインハンドとオフハンドを取得
    val mainhanditem = inventory.getItemInMainHand

    //メインハンドにツールがあるか
    val mainhandtoolflag = MaterialSets.breakMaterials.contains(mainhanditem.getType)

    //実際に使用するツール
    val tool = if (mainhandtoolflag) mainhanditem else return

    //耐久値がマイナスかつ耐久無限ツールでない時処理を終了
    if (tool.getDurability > tool.getType.getMaxDurability && !tool.getItemMeta.spigot.isUnbreakable) return

    //スキルで破壊されるブロックの時処理を終了
    if (SeichiAssist.managedBlocks.contains(block)) {
      if (SeichiAssist.DEBUG) player.sendMessage("スキルで使用中のブロックです。")
      return
    }

    runArrowSkillofHitBlock(player, proj, block, tool)

    SeichiAssist.managedEntities.$minus$eq(proj)
    proj.remove()
  }

  private def runArrowSkillofHitBlock(player: Player, proj: Projectile, hitBlock: Block, tool: ItemStack): Unit = {
    val uuid = player.getUniqueId
    val playerdata = playermap.apply(uuid)

    //マナを取得
    val mana = playerdata.activeskilldata.mana

    //元ブロックのマテリアルを取得
    val material = hitBlock.getType

    val area = playerdata.activeskilldata.area

    //現在のプレイヤーの向いている方向
    val dir = BreakUtil.getCardinalDirection(player)

    //もし前回とプレイヤーの向いている方向が違ったら範囲を取り直す
    if (!(dir == area.getDir)) {
      area.setDir(dir)
      area.makeArea()
    }

    val start = area.getStartList.get(0)
    val end = area.getEndList.get(0)

    //エフェクト用に壊されるブロック全てのリストデータ
    //一回の破壊の範囲
    val breaklength = area.getBreakLength

    //１回の全て破壊したときのブロック数
    val ifallbreaknum = breaklength.x * breaklength.y * breaklength.z

    val breakBlocks = new mutable.HashSet[Block]
    val lavas = new mutable.HashSet[Block]

    import ManagedWorld._
    import com.github.unchama.seichiassist.data.syntax._
    AxisAlignedCuboid(start, end).gridPoints().foreach { case XYZTuple(x, y, z) =>
      val targetBlock = hitBlock.getRelative(x, y, z)
      if (playerdata.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel &&
        (player.getWorld.isSeichi || playerdata.settings.multipleidbreakflag)) {
        if ((targetBlock.getType ne Material.AIR) && (targetBlock.getType ne Material.BEDROCK))
          if ((targetBlock.getType eq Material.STATIONARY_LAVA) || BreakUtil.BlockEqualsMaterialList(targetBlock))
            if (BreakUtil.canBreak(player, Some.apply(targetBlock)))
              if (targetBlock.getType eq Material.STATIONARY_LAVA)
                lavas.add(targetBlock)
              else
                breakBlocks.add(targetBlock)
      } else if ((targetBlock.getType eq material) ||
          ((hitBlock.getType eq Material.DIRT) && (targetBlock.getType eq Material.GRASS)) ||
          ((hitBlock.getType eq Material.GRASS) && (targetBlock.getType eq Material.DIRT)) ||
          ((hitBlock.getType eq Material.GLOWING_REDSTONE_ORE) && (targetBlock.getType eq Material.REDSTONE_ORE)) ||
          ((hitBlock.getType eq Material.REDSTONE_ORE) && (targetBlock.getType eq Material.GLOWING_REDSTONE_ORE)) ||
          (targetBlock.getType eq Material.STATIONARY_LAVA))
          if (BreakUtil.canBreak(player, Some.apply(targetBlock)))
            if (targetBlock.getType eq Material.STATIONARY_LAVA)
              lavas.add(targetBlock)
            else
              breakBlocks.add(targetBlock)
    }

    //重力値計算
    val gravity = BreakUtil.getGravity(player, hitBlock, isAssault = false)

    //減る経験値計算
    //実際に破壊するブロック数  * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力
    val useMana =
      breakBlocks.size.toDouble *
        (gravity + 1) *
        ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) / ifallbreaknum

    //減る耐久値の計算
    //１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
    val durability =
      (tool.getDurability +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breakBlocks.size) +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), 10 * lavas.size)).toShort

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
    mana.decrease(useMana, player, playerdata.level)

    //耐久値を減らす
    if (!tool.getItemMeta.spigot.isUnbreakable) tool.setDurability(durability)

    //以降破壊する処理
    //溶岩を破壊する処理
    lavas.foreach(_.setType(Material.AIR))

    //元ブロックの真ん中の位置
    val centerofblock = hitBlock.getLocation.add(0.5, 0.5, 0.5)

    SeichiAssist.managedBlocks ++= breakBlocks

    ActiveSkillEffect
      .fromEffectNum(playerdata.activeskilldata.effectnum)
      .runBreakEffect(player, playerdata.activeskilldata, tool, breakBlocks.toSet, start, end, centerofblock)
  }

  @EventHandler def onEntityExplodeEvent(event: EntityExplodeEvent) = {
    val e = event.getEntity
    if (e.isInstanceOf[Projectile]) if (e.hasMetadata("ArrowSkill") || e.hasMetadata("Effect")) event.setCancelled(true)
  }

  @EventHandler def onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) = {
    val e = event.getDamager
    if (e.isInstanceOf[Projectile]) if (e.hasMetadata("ArrowSkill") || e.hasMetadata("Effect")) event.setCancelled(true)
  }

  @EventHandler def onPotionSplashEvent(event: PotionSplashEvent) = {
    val thrown = event.getPotion
    if (thrown != null) if (thrown.hasMetadata("ArrowSkill") || thrown.hasMetadata("Effect")) event.setCancelled(true)
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