package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.bukkit.actions

import cats.effect.{Sync, SyncIO}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions.GrantFastDiggingEffect
import org.bukkit.entity.Player
import org.bukkit.potion.{PotionEffect, PotionEffectType}

class GrantBukkitFastDiggingEffect[F[_]: Sync: OnMinecraftServerThread]
    extends GrantFastDiggingEffect[F, Player] {

  override def forTwoSeconds(player: Player)(amount: Int): F[Unit] = {
    val potionEffect = new PotionEffect(PotionEffectType.FAST_DIGGING, 40, amount, false, false)

    // ポーション効果の削除及び付与はメインスレッドでのみ許可される(Spigot 1.12)
    OnMinecraftServerThread[F].runAction(SyncIO[Unit] {
      player.removePotionEffect(PotionEffectType.FAST_DIGGING)
      player.addPotionEffect(potionEffect)
    })
  }

}
