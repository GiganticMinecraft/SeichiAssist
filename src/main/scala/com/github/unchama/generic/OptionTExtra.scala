package com.github.unchama.generic

import cats.data.OptionT
import cats.{Applicative, ~>}

object OptionTExtra {
  /**
   * `failCondition` が true のとき失敗するような計算を返す。
   */
  def failIf[F[_]: Applicative](failCondition: Boolean): OptionT[F, Unit] =
    OptionT.fromOption[F](Option.unless(failCondition)(()))

  def unwrapOptionTK[F[_]]: OptionT[F, *] ~> Lambda[x => F[Option[x]]] =
    λ[OptionT[F, *] ~> Lambda[x => F[Option[x]]]](_.value)
}
