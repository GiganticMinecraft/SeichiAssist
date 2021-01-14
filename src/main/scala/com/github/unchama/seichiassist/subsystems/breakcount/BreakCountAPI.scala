package com.github.unchama.seichiassist.subsystems.breakcount

import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.breakcount.application.actions.IncrementSeichiExp
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData

trait BreakCountAPI[F[_], G[_], Player] {

  /**
   * プレーヤーの整地量データを増加させるアクション
   */
  val incrementSeichiExp: IncrementSeichiExp[G, Player]

  /**
   * プレーヤーの整地量データの読み取り専用リポジトリ
   */
  val breakCountRepository: KeyedDataRepository[Player, ReadOnlyRef[G, SeichiAmountData]]

  /**
   * プレーヤーの整地量データの更新が流れる [[fs2.Stream]]。
   */
  val breakCountUpdates: fs2.Stream[F, (Player, SeichiAmountData)]

}
