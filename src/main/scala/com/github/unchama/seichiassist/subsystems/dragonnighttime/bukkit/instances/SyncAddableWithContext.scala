package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.AddableWithContext

object SyncAddableWithContext {
  def apply[F[_] : Sync]: AddableWithContext[F] = new AddableWithContext[F] {
    override val addEffect: F[Unit] = Sync[F].delay {
      // FastDiggingEffectはミュータブルなのでプレイヤーごとにインスタンス化する
      SeichiAssist.playermap.values.foreach(
        _.effectdatalist.addOne(new FastDiggingEffect(20 * 60 * 60, 10, 3))
      )
    }
  }
}
