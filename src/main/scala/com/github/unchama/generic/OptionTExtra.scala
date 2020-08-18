package com.github.unchama.generic

import cats.Applicative
import cats.data.OptionT

object OptionTExtra {
  /**
   * `failCondition` が true のとき失敗するような計算を返す。
   */
  def failIf[F[_]: Applicative](failCondition: Boolean): OptionT[F, Unit] =
    OptionT.fromOption[F](Option.unless(failCondition)(()))
}
