package com.github.unchama.seichiassist.subsystems.mana

import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.mana.domain.LevelCappedManaAmount

trait ManaReadApi[F[_], G[_], Player] {

  val readManaAmount: KeyedDataRepository[Player, G[LevelCappedManaAmount]]

  val manaAmountUpdates: fs2.Stream[F, (Player, LevelCappedManaAmount)]

}

trait ManaWriteApi[G[_], Player] {

  val manaAmount: KeyedDataRepository[Player, Ref[G, LevelCappedManaAmount]]

}

trait ManaApi[F[_], G[_], Player]
  extends ManaReadApi[F, G, Player]
    with ManaWriteApi[F, Player]
