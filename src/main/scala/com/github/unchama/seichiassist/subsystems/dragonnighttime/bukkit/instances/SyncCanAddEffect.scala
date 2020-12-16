package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.CanAddEffect

object SyncCanAddEffect {
  def apply[F[_] : Sync]: CanAddEffect[F] = new CanAddEffect[F] {
    override val addEffect: F[Unit] = Sync[F].delay {
      // FastDiggingEffectはミュータブルなのでプレイヤーごとにインスタンス化する
      SeichiAssist.playermap.values.foreach(
        _.effectdatalist.addOne(new FastDiggingEffect(20 * 60 * 60, 10, 3))
      )
    }
  }
}
