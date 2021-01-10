package com.github.unchama.seichiassist.subsystems.breakcount

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

trait BreakCountAPI[F[_], Player] {

  /**
   * プレーヤーの整地経験値量の増加分が流れる [[fs2.Stream]]。
   */
  val breakCountUpdates: fs2.Stream[F, (Player, SeichiExpAmount)]

}
