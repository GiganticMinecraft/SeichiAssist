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
