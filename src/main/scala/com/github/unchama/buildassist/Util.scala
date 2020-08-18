package com.github.unchama.buildassist

import java.math.BigDecimal

import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Util {
  def getName(p: Player): String = p.getName.toLowerCase

  //ワールドガードAPIを返す
  def getWorldGuard: WorldGuardPlugin = {
    Bukkit.getServer.getPluginManager.getPlugin("WorldGuard") match {
      case plugin: WorldGuardPlugin => plugin
      case _ => throw new IllegalStateException("WorldGuard is not loaded!")
    }
  }

  /**
   * プレイヤーの居るワールドでスキルが発動できるか判定する
   *
   * @param player 対象となるプレイヤー
   * @return 発動できる場合はtrue、できない場合はfalse
   */
  def isSkillEnable(player: Player): Boolean = {
    // プレイヤーの場所が各種整地ワールド(world_SWで始まるワールド)または
    // 各種メインワールド(world)または各種TTワールドにいる場合
    // TODO: ManagedWorldへ移行

    val name = player.getWorld.getName
    name.toLowerCase.startsWith(SeichiAssist.SEICHIWORLDNAME) ||
      name.equalsIgnoreCase("world") ||
      name.equalsIgnoreCase("world_2") ||
      name.equalsIgnoreCase("world_nether") ||
      name.equalsIgnoreCase("world_the_end") ||
      name.equalsIgnoreCase("world_TT") ||
      name.equalsIgnoreCase("world_nether_TT") ||
      name.equalsIgnoreCase("world_the_end_TT") ||
      name.equalsIgnoreCase("world_dot")
  }

  /**
   * ブロックがカウントされるワールドにプレイヤーが居るか判定する
   *
   * @param player 対象のプレイヤー
   * @return いる場合はtrue、いない場合はfalse
   */
  def inTrackedWorld(player: Player): Boolean = {
    if (SeichiAssist.DEBUG) return true
    val name = player.getWorld.getName
    //プレイヤーの場所がメインワールド(world)または各種整地ワールド(world_SW)にいる場合
    name.toLowerCase.startsWith(SeichiAssist.SEICHIWORLDNAME) ||
      name.equalsIgnoreCase("world") ||
      name.equalsIgnoreCase("world_2") ||
      name.equalsIgnoreCase("world_nether") ||
      name.equalsIgnoreCase("world_the_end") ||
      name.equalsIgnoreCase("world_dot")
  }

  /**
   * 指定した名前のマインスタックオブジェクトを返す
   */
  // FIXME: これはここにあるべきではない
  @deprecated def findMineStackObjectByName(name: String): MineStackObj = {
    MineStackObjectList.minestacklist.find(name == _.mineStackObjName).orNull
  }

  /**
   * 1分間の設置量を指定量増加させます。
   * ワールドによって倍率も加味されます。
   *
   * @param player 増加させるプレイヤー
   * @param amount 増加量
   */
  def addBuild1MinAmount(player: Player, amount: BigDecimal): Unit = { //プレイヤーデータ取得
    val playerData = BuildAssist.playermap(player.getUniqueId)
    //ワールドによって倍率変化
    playerData.build_num_1min = {
      if (player.getWorld.getName.toLowerCase.startsWith(SeichiAssist.SEICHIWORLDNAME)) {
        playerData.build_num_1min.add(amount.multiply(new BigDecimal("0.1")))
      } else {
        playerData.build_num_1min.add(amount)
      }
    }
  }
}
