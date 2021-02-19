package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.bukkit.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions.GrantFastDiggingEffect
import org.bukkit.entity.Player
import org.bukkit.potion.{PotionEffect, PotionEffectType}

class GrantBukkitFastDiggingEffect[F[_] : Sync] extends GrantFastDiggingEffect[F, Player] {

  override def forASecond(player: Player)(amount: Int): F[Unit] = {
    val potionEffect = new PotionEffect(PotionEffectType.FAST_DIGGING, 20, amount, false, false)

    Sync[F].delay {
      player.addPotionEffect(potionEffect)
    }
  }

}
