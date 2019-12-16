package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}
import com.github.unchama.seichiassist.task.{CoolDownTask, MultiBreakTask}
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import com.github.unchama.seichiassist._
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
  private val playermap = SeichiAssist.playermap
  private val plugin = SeichiAssist.instance

  //アクティブスキルの実行
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  def onPlayerActiveSkillEvent(event: BlockBreakEvent): Unit = {
    //実行したプレイヤーを取得
    val player = event.getPlayer

    //壊されるブロックを取得
    val block = event.getBlock

    //他人の保護がかかっている場合は処理を終了
    if (!ExternalPlugins.getWorldGuard.canBuild(player, block.getLocation)) return

    //ブロックのタイプを取得
    val material = block.getType

    //UUIDを取得
    val uuid = player.getUniqueId

    //UUIDを基にプレイヤーデータ取得
    val playerdata = SeichiAssist.playermap.apply(uuid)

    //重力値によるキャンセル判定(スキル判定より先に判定させること)
    if (!MaterialSets.gravityMaterials.contains(block.getType) && !MaterialSets.cancelledMaterials.contains(block.getType))
      if (BreakUtil.getGravity(player, block, false) > 15) {
        player.sendMessage(ChatColor.RED + "整地ワールドでは必ず上から掘ってください。")
        event.setCancelled(true)
        return
      }

    // 保護と重力値に問題無く、ブロックタイプがmateriallistに登録されていたらMebiusListenerを呼び出す
    if (MaterialSets.materials.contains(material)) MebiusListener.onBlockBreak(event)
    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) return
    //プレイヤーインベントリを取得
    val inventory = player.getInventory
    //メインハンドとオフハンドを取得
    val mainhanditem = inventory.getItemInMainHand
    val offhanditem = inventory.getItemInOffHand
    //メインハンドにツールがあるか
    val mainhandtoolflag = MaterialSets.breakMaterials.contains(mainhanditem.getType)
    //オフハンドにツールがあるか
    val offhandtoolflag = MaterialSets.breakMaterials.contains(offhanditem.getType)
    if (!Util.isSkillEnable(player)) return
    //場合分け

    //実際に使用するツールを格納する
    val tool = if (mainhandtoolflag) { //メインハンドの時
      mainhanditem
    } else if (offhandtoolflag) { //サブハンドの時
      return
    } else { //どちらにももっていない時処理を終了
      return
    }

    //耐久値がマイナスかつ耐久無限ツールでない時処理を終了
    if (tool.getDurability > tool.getType.getMaxDurability && !tool.getItemMeta.spigot.isUnbreakable) return
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
    if (playerdata.activeskilldata.mineflagnum == 0 || playerdata.activeskilldata.skillnum == 0 || playerdata.activeskilldata.skilltype == 0 || playerdata.activeskilldata.skilltype == ActiveSkill.ARROW.gettypenum) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "スキルオフ時の破壊")
      return
    }
    if (playerdata.activeskilldata.skilltype == ActiveSkill.MULTI.gettypenum) runMultiSkill(player, block, tool)
    else if (playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum) runBreakSkill(player, block, tool)
  }

  //複数範囲破壊
  private def runMultiSkill(player: Player, block: Block, tool: ItemStack): Unit = {
    val uuid = player.getUniqueId
    //playerdataを取得
    val playerdata = playermap.apply(uuid)
    //レベルを取得
    //int skilllevel = playerdata.activeskilldata.skillnum;
    //マナを取得
    val mana = playerdata.activeskilldata.mana
    //プレイヤーの足のy座標を取得
    val playerlocy = player.getLocation.getBlockY - 1
    //元ブロックのマテリアルを取得
    val material = block.getType
    //元ブロックの真ん中の位置を取得
    val centerofblock = block.getLocation.add(0.5, 0.5, 0.5)
    //実際に破壊するブロック数
    var breakblocknum = 0
    val area = playerdata.activeskilldata.area
    //現在のプレイヤーの向いている方向
    val dir = BreakUtil.getCardinalDirection(player)
    //もし前回とプレイヤーの向いている方向が違ったら範囲を取り直す
    if (!(dir == area.getDir)) {
      area.setDir(dir)
      area.makeArea()
    }

    import scala.jdk.CollectionConverters._

    val startlist = area.getStartList.asScala.toList
    val endlist = area.getEndList.asScala.toList

    //エフェクト用に壊されるブロック全てのリストデータ
    val multibreaklist = new ArrayBuffer[ArrayBuffer[Block]]
    //壊される溶岩の全てのリストデータ
    val multilavalist = new ArrayBuffer[ArrayBuffer[Block]]
    //繰り返す回数
    val breaknum = area.getBreakNum
    //一回の破壊の範囲
    val breaklength = area.getBreakLength
    //１回の全て破壊したときのブロック数
    val ifallbreaknum = breaklength.x * breaklength.y * breaklength.z * breaknum
    //全てのマナ消費量
    var useAllMana = 0.0
    //全ての耐久消費量
    var alldurability = tool.getDurability.toInt
    //重力値計算
    val gravity = BreakUtil.getGravity(player, block, false)

    val b = new Breaks

    //繰り返し回数だけ繰り返す
    b.breakable {
      (0 until breaknum).foreach { i =>
        val breaklist = new ArrayBuffer[Block]
        val lavalist = new ArrayBuffer[Block]

        val start = startlist(i)
        val end = endlist(i)
        import com.github.unchama.seichiassist.data.syntax._
        AxisAlignedCuboid(start, end).gridPoints().foreach { case XYZTuple(x, y, z) =>
          val breakblock = block.getRelative(x, y, z)
          if (!(x == 0 && y == 0 && z == 0)) {
            if (playerdata.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel && playerdata.settings.multipleidbreakflag) { //追加テスト(複数種類一括破壊スキル)
              if ((breakblock.getType ne Material.AIR) && (breakblock.getType ne Material.BEDROCK))
                if ((breakblock.getType eq Material.STATIONARY_LAVA) || BreakUtil.BlockEqualsMaterialList(breakblock))
                  if (playerlocy < breakblock.getLocation.getBlockY || player.isSneaking || breakblock == block)
                    if (BreakUtil.canBreak(player, Some.apply(breakblock)))
                      if (breakblock.getType eq Material.STATIONARY_LAVA) lavalist.addOne(breakblock)
                      else {
                        breaklist.addOne(breakblock)
                        SeichiAssist.managedBlocks.$plus$eq(breakblock)
                      }
            }
            else { //条件を満たしていない
              //もし壊されるブロックがもともとのブロックと同じ種類だった場合
              if ((breakblock.getType eq material) ||
                ((block.getType eq Material.DIRT) && (breakblock.getType eq Material.GRASS)) ||
                ((block.getType eq Material.GRASS) && (breakblock.getType eq Material.DIRT)) ||
                ((block.getType eq Material.GLOWING_REDSTONE_ORE) && (breakblock.getType eq Material.REDSTONE_ORE)) ||
                ((block.getType eq Material.REDSTONE_ORE) && (breakblock.getType eq Material.GLOWING_REDSTONE_ORE)) ||
                (breakblock.getType eq Material.STATIONARY_LAVA))
                if (playerlocy < breakblock.getLocation.getBlockY || player.isSneaking || breakblock == block)
                  if (BreakUtil.canBreak(player, Some.apply(breakblock)))
                    if (breakblock.getType eq Material.STATIONARY_LAVA) lavalist.addOne(breakblock)
                    else {
                      breaklist.addOne(breakblock)
                      SeichiAssist.managedBlocks.$plus$eq(breakblock)
                    }
            }
          }
        }

        //減る経験値計算
        //実際に破壊するブロック数  * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力
        useAllMana +=
          (breaklist.size + 1).toDouble *
            (gravity + 1) *
            ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) / ifallbreaknum

        //減る耐久値の計算
        alldurability += BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breaklist.size)

        //１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
        alldurability += BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), 10 * lavalist.size)

        //重力値の判定
        if (gravity > 15) {
          player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。")
          SeichiAssist.managedBlocks.$minus$minus$eq(breaklist)
          b.break()
        }

        //実際に経験値を減らせるか判定
        if (!mana.has(useAllMana)) {
          if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナが足りません")
          SeichiAssist.managedBlocks.$minus$minus$eq(breaklist)
          b.break()
        }

        //実際に耐久値を減らせるか判定
        if (tool.getType.getMaxDurability <= alldurability && !tool.getItemMeta.spigot.isUnbreakable) {
          if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません")
          SeichiAssist.managedBlocks.$minus$minus$eq(breaklist)
          b.break()
        }

        //選択されたブロックを破壊せずに保存する処理
        multibreaklist.addOne(breaklist)
        multilavalist.addOne(lavalist)

        breakblocknum += breaklist.size
      }
    }

    //自身のみしか壊さない時自然に処理する
    if (breakblocknum == 0) {
      BreakUtil.breakBlock(player, block, centerofblock, tool, shouldPlayBreakSound = true)
      return
    } else {
      //スキルの処理}
      multibreaklist(0).addOne(block)
      SeichiAssist.managedBlocks.$plus$eq(block)
      new MultiBreakTask(
        player, block, tool,
        multibreaklist.map(_.toList).toList,
        multilavalist.map(_.toList).toList,
        startlist, endlist)
        .runTaskTimer(plugin, 0, 4)
    }

    //経験値を減らす
    mana.decrease(useAllMana, player, playerdata.level)

    //耐久値を減らす
    if (!tool.getItemMeta.isUnbreakable) tool.setDurability(alldurability.toShort)

    //壊したブロック数に応じてクールダウンを発生させる
    val cooldown = ActiveSkill.MULTI.getCoolDown(playerdata.activeskilldata.skillnum) * breakblocknum / ifallbreaknum
    if (cooldown >= 5) new CoolDownTask(player, false, true, false).runTaskLater(plugin, cooldown)
  }

  //範囲破壊実行処理
  private def runBreakSkill(player: Player, block: Block, tool: ItemStack): Unit = {
    val uuid = player.getUniqueId
    val playerdata = playermap.apply(uuid)
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
    val lavalist = new mutable.HashSet[Block]

    //範囲内の破壊されるブロックを取得
    import com.github.unchama.seichiassist.data.syntax._
    AxisAlignedCuboid(start, end).gridPoints().foreach { case XYZTuple(x, y, z) =>
      val breakblock = block.getRelative(x, y, z)
      if (!(x == 0 && y == 0 && z == 0)) {
        if (playerdata.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel && (Util.isSeichiWorld(player) || playerdata.settings.multipleidbreakflag)) if ((breakblock.getType ne Material.AIR) && (breakblock.getType ne Material.BEDROCK)) if ((breakblock.getType eq Material.STATIONARY_LAVA) || BreakUtil.BlockEqualsMaterialList(breakblock)) if (playerlocy < breakblock.getLocation.getBlockY || player.isSneaking || breakblock == block) if (BreakUtil.canBreak(player, Some.apply(breakblock))) if (breakblock.getType eq Material.STATIONARY_LAVA) lavalist.add(breakblock)
        else {
          breakBlocks.add(breakblock)
          SeichiAssist.managedBlocks.$plus$eq(breakblock)
        } else if ((breakblock.getType eq material) || ((block.getType eq Material.DIRT) && (breakblock.getType eq Material.GRASS)) || ((block.getType eq Material.GRASS) && (breakblock.getType eq Material.DIRT)) || ((block.getType eq Material.GLOWING_REDSTONE_ORE) && (breakblock.getType eq Material.REDSTONE_ORE)) || ((block.getType eq Material.REDSTONE_ORE) && (breakblock.getType eq Material.GLOWING_REDSTONE_ORE)) || (breakblock.getType eq Material.STATIONARY_LAVA)) if (playerlocy < breakblock.getLocation.getBlockY || player.isSneaking || breakblock == block) if (BreakUtil.canBreak(player, Some.apply(breakblock))) if (breakblock.getType eq Material.STATIONARY_LAVA) lavalist.add(breakblock)
        else {
          breakBlocks.add(breakblock)
          SeichiAssist.managedBlocks.$plus$eq(breakblock)
        }
      }
    }
    val gravity = BreakUtil.getGravity(player, block, false)
    //減るマナ計算
    val breaklength = area.getBreakLength
    val ifallbreaknum = breaklength.x * breaklength.y * breaklength.z
    val useMana = (breakBlocks.size + 1).toDouble * (gravity + 1) * ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) / ifallbreaknum
    if (SeichiAssist.DEBUG) {
      player.sendMessage(ChatColor.RED + "必要経験値：" + ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum))
      player.sendMessage(ChatColor.RED + "全ての破壊数：" + ifallbreaknum)
      player.sendMessage(ChatColor.RED + "実際の破壊数：" + breakBlocks.size)
      player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナ：" + useMana)
    }
    val durability =
      tool.getDurability +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breakBlocks.size) +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), 10 * lavalist.size)

    if (gravity > 15) {
      player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。")
      SeichiAssist.managedBlocks.$plus$plus$eq(breakBlocks)
      return
    }

    //実際に経験値を減らせるか判定
    if (!mana.has(useMana)) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナが足りません")
      SeichiAssist.managedBlocks.$plus$plus$eq(breakBlocks)
      return
    }

    //実際に耐久値を減らせるか判定
    if (tool.getType.getMaxDurability <= durability && !tool.getItemMeta.spigot.isUnbreakable) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません")
      SeichiAssist.managedBlocks.$plus$plus$eq(breakBlocks)
      return
    }

    //破壊する処理
    //溶岩の破壊する処理
    lavalist.foreach(_.setType(Material.AIR))

    //選択されたブロックを破壊する処理
    if (breakBlocks.isEmpty) {
      BreakUtil.breakBlock(player, block, centerofblock, tool, true)
      return
    }

    if (playerdata.activeskilldata.effectnum == 0) {
      //エフェクトが指定されていないときの処理}
      breakBlocks.add(block)
      SeichiAssist.managedBlocks.$plus$eq(block)
      breakBlocks.foreach { b =>
        BreakUtil.breakBlock(player, b, centerofblock, tool, false)
        SeichiAssist.managedBlocks.$minus$eq(b)
      }
    } else if (playerdata.activeskilldata.effectnum <= 100) {
      //通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
      breakBlocks.add(block)
      SeichiAssist.managedBlocks.$plus$eq(block)
      val skilleffect = ActiveSkillEffect.values(playerdata.activeskilldata.effectnum - 1)
      skilleffect.runBreakEffect(player, playerdata.activeskilldata, tool, breakBlocks.toSet, start, end, centerofblock)
    } else if (playerdata.activeskilldata.effectnum > 100) {
      //スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
      breakBlocks.add(block)
      SeichiAssist.managedBlocks.$plus$eq(block)
      val premiumeffect = ActiveSkillPremiumEffect.values(playerdata.activeskilldata.effectnum - 1 - 100)
      premiumeffect.runBreakEffect(player, tool, breakBlocks.toSet, start, end, centerofblock)
    }

    //経験値を減らす
    mana.decrease(useMana, player, playerdata.level)

    //耐久値を減らす
    if (!tool.getItemMeta.spigot.isUnbreakable) tool.setDurability(durability.toShort)

    val cooldown = ActiveSkill.BREAK.getCoolDown(playerdata.activeskilldata.skillnum) * breakBlocks.size / ifallbreaknum
    if (cooldown >= 5) new CoolDownTask(player, false, true, false).runTaskLater(plugin, cooldown)
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
