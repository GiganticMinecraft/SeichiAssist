package com.github.unchama.buildassist

import com.github.unchama.seichiassist.MineStackObjectList
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
}
