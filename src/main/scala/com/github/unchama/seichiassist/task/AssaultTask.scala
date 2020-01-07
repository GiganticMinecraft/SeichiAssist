package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.activeskill.BlockSearching
import com.github.unchama.seichiassist.data.Mana
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import com.github.unchama.seichiassist.{ActiveSkill, SeichiAssist}
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.{ChatColor, GameMode, Material}

class AssaultTask(val player: Player, val tool: ItemStack) extends BukkitRunnable {
  private val playerdata: PlayerData = SeichiAssist.playermap(player.getUniqueId)

  //放置判定用位置データ
  private var lastloc = player.getLocation

  //放置判定用
  private var idletime = 0

  private val errorflag = false

  private def setCancel() = {
    playerdata.activeskilldata.assaultflag = false
    playerdata.activeskilldata.mineflagnum = 0
    this.cancel()
  }

  override def run(): Unit = {
    if (isCanceled) {
      setCancel()
      return
    }

    //もしサバイバルでなければ処理を終了
    //もしフライ中なら終了
    if (player.getGameMode != GameMode.SURVIVAL) {
      player.sendMessage(s"${ChatColor.GREEN}ゲームモードをサバイバルに変更してください。")
      setCancel()
      return
    }

    //整地ワールドではない時スキルを発動しない。
    if (!Util.isSkillEnable(player)) {
      player.sendMessage(s"${ChatColor.GREEN}スキルは整地ワールドでのみ使用可能です。")
      setCancel()
      return
    }

    //放置判定、動いてなかったら処理終了
    if (((lastloc.getBlockX - 10) < player.getLocation.getBlockX) &&
      ((lastloc.getBlockX + 10) > player.getLocation.getBlockX) &&
      ((lastloc.getBlockY - 10) < player.getLocation.getBlockY) &&
      ((lastloc.getBlockY + 10) > player.getLocation.getBlockY) &&
      ((lastloc.getBlockZ - 10) < player.getLocation.getBlockZ) &&
      ((lastloc.getBlockZ + 10) > player.getLocation.getBlockZ)) {

      if (SeichiAssist.DEBUG) player.sendMessage(s"${ChatColor.RED}放置を検出")
      idletime += 1

      if (idletime > 20) {
        player.sendMessage(s"${ChatColor.YELLOW}アサルトスキルがOFFになりました")
        setCancel()
        return
      }
    } else {
      //動いてたら次回判定用に場所更新しとく
      lastloc = player.getLocation
      idletime = 0
    }

    //プレイヤーの足のy座標を取得
    val playerLocY = player.getLocation.getBlockY - 1

    val block = player.getLocation.getBlock
    val offHandItem = player.getInventory.getItemInOffHand

    //最初に登録したツールと今のツールが違う場合
    if (!(tool == offHandItem)) {
      if (SeichiAssist.DEBUG) player.sendMessage(s"${ChatColor.RED}ツールの変更を検知しました")
      setCancel()
      return
    }

    val skillArea = playerdata.activeskilldata.assaultarea
    val breakArea = skillArea.makeBreakArea(player).unsafeRunSync()(0)

    val breakLength = skillArea.breakLength
    val areaTotalBlockCount = breakLength.x * breakLength.y * breakLength.z

    //壊すフラグを指定
    val shouldBreakAllBlocks = playerdata.activeskilldata.assaulttype == ActiveSkill.ARMOR.gettypenum
    val shouldCondenseFluids = playerdata.activeskilldata.assaulttype == ActiveSkill.FLUIDCONDENSE.gettypenum || shouldBreakAllBlocks
    val shouldCondenseWater = playerdata.activeskilldata.assaulttype == ActiveSkill.WATERCONDENSE.gettypenum || shouldCondenseFluids
    val shouldCondenseLava = playerdata.activeskilldata.assaulttype == ActiveSkill.LAVACONDENSE.gettypenum || shouldCondenseFluids

    //重力値計算
    val gravity = BreakUtil.getGravity(player, block, isAssault = true)

    //重力値の判定
    if (gravity > 15) {
      player.sendMessage(s"${ChatColor.RED}スキルを使用するには上から掘ってください。")
      setCancel()
      return
    }

    import com.github.unchama.seichiassist.data.syntax._
    val BlockSearching.Result(foundBlocks, foundWaters, foundLavas) =
      BlockSearching.searchForBreakableBlocks(player, breakArea.gridPoints(), block)
          .unsafeRunSync()
          .mapAll(
            if (player.isSneaking || !shouldBreakAllBlocks)
              identity
            else
              _.filter { targetBlock =>
                targetBlock.getLocation.getBlockY > playerLocY || targetBlock == block
              }
          )

    // 実際に破壊するブロック数の計算
    val breakTargets =
      (if (shouldCondenseWater) foundWaters.size else 0) +
        (if (shouldCondenseLava) foundLavas.size else 0) +
        (if (shouldBreakAllBlocks) foundBlocks.size else 0)

    // 減るマナ計算
    // 実際に破壊するブロック数 * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力
    val useMana =
      breakTargets.toDouble *
        (gravity + 1) *
        ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.assaulttype, playerdata.activeskilldata.assaultnum) / areaTotalBlockCount

    //減る耐久値の計算
    val durability = (tool.getDurability + BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breakTargets)).toShort

    val playerMana: Mana = playerdata.activeskilldata.mana

    //実際に経験値を減らせるか判定
    if (!playerMana.has(useMana)) {
      if (SeichiAssist.DEBUG) player.sendMessage(s"${ChatColor.RED}アクティブスキル発動に必要なマナが足りません")
      setCancel()
      return
    }

    // 実際に耐久値を減らせるか判定
    if (tool.getType.getMaxDurability <= durability && !tool.getItemMeta.isUnbreakable) {
      if (SeichiAssist.DEBUG) player.sendMessage(s"${ChatColor.RED}アクティブスキル発動に必要なツールの耐久値が足りません")
      setCancel()
      return
    }

    // 経験値を減らす
    playerMana.decrease(useMana, player, playerdata.level)

    // 耐久値を減らす
    if (!tool.getItemMeta.isUnbreakable) tool.setDurability(durability)

    // ブロックを書き換える
    if (shouldBreakAllBlocks) {
      (foundWaters ++ foundLavas).foreach(_.setType(Material.AIR))
      com.github.unchama.seichiassist.unsafe.runIOAsync(
        "ブロックを大量破壊する",
        BreakUtil.massBreakBlock(player, foundBlocks, player.getLocation, tool, shouldPlayBreakSound = false)
      )
    } else {
      if (shouldCondenseWater) foundWaters.foreach(_.setType(Material.PACKED_ICE))
      if (shouldCondenseLava) foundLavas.foreach(_.setType(Material.MAGMA))
    }
  }

  private def isCanceled =
    playerdata.activeskilldata.mineflagnum == 0 ||
      errorflag ||
      playerdata.activeskilldata.assaulttype == 0
}