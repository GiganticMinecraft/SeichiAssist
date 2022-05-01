package com.github.unchama.seichiassist.subsystems.gacha.application.repository

import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrize

object GachaPrizesRepositoryDefinition {

  type TemporaryValue[F[_]] = Ref[F, GachaPrize]

  case class RepositoryValue[F[_]](prizeRef: Ref[F, GachaPrize])

}
