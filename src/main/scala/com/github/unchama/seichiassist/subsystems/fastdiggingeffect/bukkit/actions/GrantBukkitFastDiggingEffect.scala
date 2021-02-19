package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.bukkit.actions

import cats.effect.Sync
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions.GrantFastDiggingEffect
import org.bukkit.entity.Player
import org.bukkit.potion.{PotionEffect, PotionEffectType}

class GrantBukkitFastDiggingEffect[
  F[_] : Sync : MinecraftServerThreadShift
] extends GrantFastDiggingEffect[F, Player] {

  import cats.implicits._

  override def forASecond(player: Player)(amount: Int): F[Unit] = {
    val potionEffect = new PotionEffect(PotionEffectType.FAST_DIGGING, 20, amount, false, false)

    // ポーション効果付与はメインスレッドでのみ許可される(Spigot 1.12)
    MinecraftServerThreadShift[F].shift >> Sync[F].delay {
      player.addPotionEffect(potionEffect)
    }.as(())
  }

}
