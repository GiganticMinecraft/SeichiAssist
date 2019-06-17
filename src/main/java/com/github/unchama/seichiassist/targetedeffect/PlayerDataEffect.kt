package com.github.unchama.seichiassist.targetedeffect

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

/**
 * プレーヤーの[PlayerData]に干渉するような[TargetedEffect]を[effect]から作成する.
 */
fun playerDataEffect(effect: suspend PlayerData.() -> Unit): TargetedEffect<Player> {
  return object : TargetedEffect<Player> {
    override suspend fun runFor(minecraftObject: Player) {
      val data = SeichiAssist.playermap[minecraftObject.uniqueId]!!

      data.effect()
    }
  }
}
