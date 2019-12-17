package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}
import com.github.unchama.seichiassist.effect.{ActiveSkillNormalEffect, ActiveSkillPremiumEffect}
import com.github.unchama.seichiassist.task.{CoolDownTask, MultiBreakTask}
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.ItemStack

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

class PlayerBlockBreakListener extends Listener {
  private val plugin = SeichiAssist.instance

  //アクティブスキルの実行
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  def onPlayerActiveSkillEvent(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer
    val block = event.getBlock

    //他人の保護がかかっている場合は処理を終了
    if (!ExternalPlugins.getWorldGuard.canBuild(player, block.getLocation)) return

    val material = block.getType

    //UUIDを基にプレイヤーデータ取得
    val playerdata = SeichiAssist.playermap(player.getUniqueId)

    //重力値によるキャンセル判定(スキル判定より先に判定させること)
    if (!MaterialSets.gravityMaterials.contains(block.getType) && !MaterialSets.cancelledMaterials.contains(block.getType))
      if (BreakUtil.getGravity(player, block, isAssault = false) > 15) {
        player.sendMessage(ChatColor.RED + "整地ワールドでは必ず上から掘ってください。")
        event.setCancelled(true)
        return
      }

    // 保護と重力値に問題無く、ブロックタイプがmateriallistに登録されていたらMebiusListenerを呼び出す
    if (MaterialSets.materials.contains(material)) MebiusListener.onBlockBreak(event)

    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) return

    //メインハンドとオフハンドを取得
    val mainhanditem = player.getInventory.getItemInMainHand

    //実際に使用するツールを格納する
    val tool = if (MaterialSets.breakMaterials.contains(mainhanditem.getType)) mainhanditem else return

    //耐久値がマイナスかつ耐久無限ツールでない時処理を終了
    if (tool.getDurability > tool.getType.getMaxDurability && !tool.getItemMeta.isUnbreakable) return

    //スキルで破壊されるブロックの時処理を終了
    if (SeichiAssist.managedBlocks.contains(block)) {
      event.setCancelled(true)
      if (SeichiAssist.DEBUG) player.sendMessage("スキルで使用中のブロックです。")
      return
    }

    //ブロックタイプがmateriallistに登録されていなければ処理終了
    if (!MaterialSets.materials.contains(material)) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "破壊対象でない")
      return
    }

    //もしサバイバルでなければ処理を終了
    //もしフライ中なら終了
    if ((player.getGameMode ne GameMode.SURVIVAL) || player.isFlying) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "fly中の破壊")
      return
    }

    //クールダウンタイム中は処理を終了
    if (!playerdata.activeskilldata.skillcanbreakflag) { //SEを再生
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "クールタイムの破壊")
      player.playSound(player.getLocation, Sound.BLOCK_DISPENSER_FAIL, 0.5f, 1)
      return
    }

    //これ以前の終了処理はマナは回復しません
    //追加マナ獲得
    playerdata.activeskilldata.mana.increase(BreakUtil.calcManaDrop(playerdata), player, playerdata.level)

    //これ以降の終了処理はマナが回復します
    //アクティブスキルフラグがオフの時処理を終了
    if (playerdata.activeskilldata.mineflagnum == 0 ||
      playerdata.activeskilldata.skillnum == 0 ||
      playerdata.activeskilldata.skilltype == 0 ||
      playerdata.activeskilldata.skilltype == ActiveSkill.ARROW.gettypenum) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "スキルオフ時の破壊")
      return
    }

    playerdata.activeskilldata.skilltype match {
      case ActiveSkill.MULTI.typenum => runMultiSkill(player, block, tool)
      case ActiveSkill.BREAK.typenum => runBreakSkill(player, block, tool)
    }
  }

  //複数範囲破壊
  private def runMultiSkill(player: Player, block: Block, tool: ItemStack): Unit = {
    //playerdataを取得
    val playerdata = SeichiAssist.playermap(player.getUniqueId)

    val mana = playerdata.activeskilldata.mana

    //プレイヤーの足のy座標を取得
    val playerlocy = player.getLocation.getBlockY - 1

    val material = block.getType
    val centerofblock = block.getLocation.add(0.5, 0.5, 0.5)

    val area = playerdata.activeskilldata.area
    //現在のプレイヤーの向いている方向
    val dir = BreakUtil.getCardinalDirection(player)
    //もし前回とプレイヤーの向いている方向が違ったら範囲を取り直す
    if (!(dir == area.getDir)) {
      area.setDir(dir)
      area.makeArea()
    }

    import scala.jdk.CollectionConverters._

    val startList = area.getStartList.asScala.toList
    val endList = area.getEndList.asScala.toList

    //エフェクト用に壊されるブロック全てのリストデータ
    val multiBreakList = new ArrayBuffer[ArrayBuffer[Block]]
    //壊される溶岩の全てのリストデータ
    val multiLavaList = new ArrayBuffer[ArrayBuffer[Block]]

    // 繰り返す回数
    val breakNum = area.getBreakNum
    // 一回の破壊の範囲
    val breakLength = area.getBreakLength
    // 全て破壊したときのブロック数
    val totalBreakRangeVolume = breakLength.x * breakLength.y * breakLength.z * breakNum

    // 全てのマナ消費量
    var manaConsumption = 0.0
    // 全ての耐久消費量
    var toolDamageToSet = tool.getDurability.toInt

    // 重力値計算
    val gravity = BreakUtil.getGravity(player, block, isAssault = false)

    //繰り返し回数だけ繰り返す
    val b = new Breaks
    b.breakable {
      (0 until breakNum).foreach { i =>
        val breakBlockList = new ArrayBuffer[Block]
        val lavaList = new ArrayBuffer[Block]

        val start = startList(i)
        val end = endList(i)
        import ManagedWorld._
        import com.github.unchama.seichiassist.data.syntax._
        AxisAlignedCuboid(start, end).gridPoints().foreach { case XYZTuple(x, y, z) =>
          val targetBlock = block.getRelative(x, y, z)
          if (playerdata.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel &&
            (player.getWorld.isSeichi || playerdata.settings.multipleidbreakflag)) {
            if ((targetBlock.getType ne Material.AIR) && (targetBlock.getType ne Material.BEDROCK))
              if ((targetBlock.getType eq Material.STATIONARY_LAVA) || BreakUtil.BlockEqualsMaterialList(targetBlock))
                if (playerlocy < targetBlock.getLocation.getBlockY || player.isSneaking || targetBlock == block)
                  if (BreakUtil.canBreak(player, Some.apply(targetBlock)))
                    if (targetBlock.getType eq Material.STATIONARY_LAVA)
                      lavaList.addOne(targetBlock)
                    else
                      breakBlockList.addOne(targetBlock)
          } else if ((targetBlock.getType eq material) ||
            ((block.getType eq Material.DIRT) && (targetBlock.getType eq Material.GRASS)) ||
            ((block.getType eq Material.GRASS) && (targetBlock.getType eq Material.DIRT)) ||
            ((block.getType eq Material.GLOWING_REDSTONE_ORE) && (targetBlock.getType eq Material.REDSTONE_ORE)) ||
            ((block.getType eq Material.REDSTONE_ORE) && (targetBlock.getType eq Material.GLOWING_REDSTONE_ORE)) ||
            (targetBlock.getType eq Material.STATIONARY_LAVA))
            if (playerlocy < targetBlock.getLocation.getBlockY || player.isSneaking || targetBlock == block)
              if (BreakUtil.canBreak(player, Some.apply(targetBlock)))
                if (targetBlock.getType eq Material.STATIONARY_LAVA)
                  lavaList.addOne(targetBlock)
                else
                  breakBlockList.addOne(targetBlock)
        }

        //減る経験値計算
        //実際に破壊するブロック数  * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力
        manaConsumption +=
          (breakBlockList.size + 1).toDouble *
            (gravity + 1) *
            ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) / totalBreakRangeVolume

        //減る耐久値の計算
        toolDamageToSet += BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breakBlockList.size)

        //１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
        toolDamageToSet += BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), 10 * lavaList.size)

        //重力値の判定
        if (gravity > 15) {
          player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。")
          b.break()
        }

        //実際に経験値を減らせるか判定
        if (!mana.has(manaConsumption)) {
          if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナが足りません")
          b.break()
        }

        //実際に耐久値を減らせるか判定
        if (tool.getType.getMaxDurability <= toolDamageToSet && !tool.getItemMeta.spigot.isUnbreakable) {
          if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません")
          b.break()
        }

        //選択されたブロックを破壊せずに保存する処理
        multiBreakList.addOne(breakBlockList)
        multiLavaList.addOne(lavaList)
      }
    }

    if (multiBreakList.size == 1) {
      // 破壊するブロックがプレーヤーが最初に破壊を試みたブロックだけの場合
      BreakUtil.breakBlock(player, block, centerofblock, tool, shouldPlayBreakSound = true)
    } else {
      // スキルの処理
      SeichiAssist.managedBlocks ++= multiBreakList.flatten

      new MultiBreakTask(
        player, block, tool,
        multiBreakList.map(_.toList).toList,
        multiLavaList.map(_.toList).toList,
        startList, endList)
        .runTaskTimer(plugin, 0, 4)

      //経験値を減らす
      mana.decrease(manaConsumption, player, playerdata.level)

      //耐久値を減らす
      if (!tool.getItemMeta.isUnbreakable) tool.setDurability(toolDamageToSet.toShort)

      //壊したブロック数に応じてクールダウンを発生させる
      val breakBlockNum = multiBreakList.size
      val coolDownTime = ActiveSkill.MULTI.getCoolDown(playerdata.activeskilldata.skillnum) * breakBlockNum / totalBreakRangeVolume
      if (coolDownTime >= 5) new CoolDownTask(player, false, true, false).runTaskLater(plugin, coolDownTime)
    }
  }

  //範囲破壊実行処理
  private def runBreakSkill(player: Player, block: Block, tool: ItemStack): Unit = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val mana = playerdata.activeskilldata.mana
    val playerlocy = player.getLocation.getBlockY - 1
    val material = block.getType
    val centerofblock = block.getLocation.add(0.5, 0.5, 0.5)

    //壊される範囲を設定
    val area = playerdata.activeskilldata.area
    val dir = BreakUtil.getCardinalDirection(player)
    if (!(dir == area.getDir)) {
      area.setDir(dir)
      area.makeArea()
    }
    val start = area.getStartList.get(0)
    val end = area.getEndList.get(0)

    val breakBlocks = new mutable.HashSet[Block]
    val lavas = new mutable.HashSet[Block]

    //範囲内の破壊されるブロックを取得
    import ManagedWorld._
    import com.github.unchama.seichiassist.data.syntax._
    AxisAlignedCuboid(start, end).gridPoints().foreach { case XYZTuple(x, y, z) =>
      val breakblock = block.getRelative(x, y, z)

      if (playerdata.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel &&
        (player.getWorld.isSeichi || playerdata.settings.multipleidbreakflag)) {
        if ((breakblock.getType ne Material.AIR) && (breakblock.getType ne Material.BEDROCK))
          if ((breakblock.getType eq Material.STATIONARY_LAVA) || BreakUtil.BlockEqualsMaterialList(breakblock))
            if (playerlocy < breakblock.getLocation.getBlockY || player.isSneaking || breakblock == block)
              if (BreakUtil.canBreak(player, Some.apply(breakblock)))
                if (breakblock.getType eq Material.STATIONARY_LAVA)
                  lavas.add(breakblock)
                else
                  breakBlocks.add(breakblock)
      } else if ((breakblock.getType eq material) ||
        ((block.getType eq Material.DIRT) && (breakblock.getType eq Material.GRASS)) ||
        ((block.getType eq Material.GRASS) && (breakblock.getType eq Material.DIRT)) ||
        ((block.getType eq Material.GLOWING_REDSTONE_ORE) && (breakblock.getType eq Material.REDSTONE_ORE)) ||
        ((block.getType eq Material.REDSTONE_ORE) && (breakblock.getType eq Material.GLOWING_REDSTONE_ORE)) ||
        (breakblock.getType eq Material.STATIONARY_LAVA))
          if (playerlocy < breakblock.getLocation.getBlockY || player.isSneaking || breakblock == block)
            if (BreakUtil.canBreak(player, Some.apply(breakblock)))
              if (breakblock.getType eq Material.STATIONARY_LAVA)
                lavas.add(breakblock)
              else
                breakBlocks.add(breakblock)
    }

    val gravity = BreakUtil.getGravity(player, block, isAssault = false)

    //減るマナ計算
    val breakLength = area.getBreakLength
    val totalBreakRangeVolume = breakLength.x * breakLength.y * breakLength.z
    val useMana =
      (breakBlocks.size + 1).toDouble *
        (gravity + 1) *
        ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) / totalBreakRangeVolume

    val durability =
      tool.getDurability +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breakBlocks.size) +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), 10 * lavas.size)

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

    // 破壊する処理
    // 溶岩の破壊する処理
    lavas.foreach(_.setType(Material.AIR))

    // ブロックを破壊する処理
    if (breakBlocks.size == 1) {
      // 破壊するブロックがプレーヤーが最初に破壊を試みたブロックだけの場合
      BreakUtil.breakBlock(player, block, centerofblock, tool, shouldPlayBreakSound = true)
    } else {
      SeichiAssist.managedBlocks ++= breakBlocks

      if (playerdata.activeskilldata.effectnum == 0) {
        //エフェクトが指定されていないときの処理}
        breakBlocks.foreach { b => BreakUtil.breakBlock(player, b, centerofblock, tool, shouldPlayBreakSound = false) }
        SeichiAssist.managedBlocks --= breakBlocks
      } else if (playerdata.activeskilldata.effectnum <= 100) {
        //通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
        val skilleffect = ActiveSkillNormalEffect.values(playerdata.activeskilldata.effectnum - 1)
        skilleffect.runBreakEffect(player, playerdata.activeskilldata, tool, breakBlocks.toSet, start, end, centerofblock)
      } else if (playerdata.activeskilldata.effectnum > 100) {
        //スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
        val premiumeffect = ActiveSkillPremiumEffect.values(playerdata.activeskilldata.effectnum - 1 - 100)
        premiumeffect.runBreakEffect(player, tool, breakBlocks.toSet, start, end, centerofblock)
      }

      // 経験値を減らす
      mana.decrease(useMana, player, playerdata.level)

      // 耐久値を減らす
      if (!tool.getItemMeta.isUnbreakable) tool.setDurability(durability.toShort)

      val coolDownTime = ActiveSkill.BREAK.getCoolDown(playerdata.activeskilldata.skillnum) * breakBlocks.size / totalBreakRangeVolume
      if (coolDownTime >= 5) new CoolDownTask(player, false, true, false).runTaskLater(plugin, coolDownTime)
    }
  }

  /**
   * y5ハーフブロック破壊抑制
   *
   * @param event BlockBreakEvent
   */
  @EventHandler(priority = EventPriority.LOWEST)
  @SuppressWarnings(Array("deprecation"))
  def onPlayerBlockHalf(event: BlockBreakEvent): Unit = {
    val p = event.getPlayer
    val b = event.getBlock
    val world = p.getWorld
    val data = SeichiAssist.playermap.apply(p.getUniqueId)
    //そもそも自分の保護じゃなきゃ処理かけない
    if (!ExternalPlugins.getWorldGuard.canBuild(p, b.getLocation)) return
    if ((b.getType eq Material.DOUBLE_STEP) && b.getData == 0) {
      b.setType(Material.STEP)
      b.setData(0.toByte)
      val location = b.getLocation
      world.dropItemNaturally(location, new ItemStack(Material.STEP))
    }
    if (b.getType ne Material.STEP) return
    if (b.getY != 5) return
    if (b.getData != 0) return
    if (!world.getName.toLowerCase.startsWith(SeichiAssist.SEICHIWORLDNAME)) return
    if (data.canBreakHalfBlock) return
    event.setCancelled(true)
    p.sendMessage(ChatColor.RED + "Y5に敷かれたハーフブロックは破壊不可能です.")
  }
}
