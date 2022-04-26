package com.github.unchama.seichiassist.util

import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

object EntityDeathCause {

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
