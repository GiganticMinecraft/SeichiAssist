package com.github.unchama.seichiassist.subsystems.gachapoint.application.process

import cats.Applicative
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint

object AddSeichiExpAsGachaPoint {

  import cats.implicits._

  def stream[F[_]: Applicative, Player: HasUuid](
    refRepository: KeyedDataRepository[Player, Ref[F, GachaPoint]]
  )(seichiExpStream: fs2.Stream[F, (Player, SeichiExpAmount)]): fs2.Stream[F, Unit] =
    seichiExpStream.evalMap {
      case (player, amount) =>
        val point = GachaPoint(amount)

        refRepository.lift(player).traverse(ref => ref.update(_.add(point))).as(())
    }

}
