package com.github.unchama.seichiassist.util

import org.bukkit._
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

import java.util.Calendar

object Util {

  def isVotingFairyPeriod(start: Calendar, end: Calendar): Boolean = {
    val cur = Calendar.getInstance()
    cur.after(start) && cur.before(end)
  }

  def setDifficulty(worldNameList: List[String], difficulty: Difficulty): Unit = {
    worldNameList.foreach { name =>
      val world = Bukkit.getWorld(name)

      if (world == null)
        Bukkit.getLogger.warning(name + "という名前のワールドは存在しません。")
      else
        world.setDifficulty(difficulty)
    }
  }

  /**
   * 死亡したエンティティの死因が棘の鎧かどうか
   */
  def isEntityKilledByThornsEnchant(entity: LivingEntity): Boolean = {
    if (entity == null) return false
    val event = entity.getLastDamageCause
    if (event == null) return false

    event.getCause == DamageCause.THORNS
  }
}
