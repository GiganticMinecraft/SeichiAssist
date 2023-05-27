package com.github.unchama.seichiassist.subsystems.mana

import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.mana.domain.{
  LevelCappedManaAmount,
  ManaManipulation,
  ManaMultiplier
}

trait ManaReadApi[F[_], G[_], Player] {

  val readManaAmount: KeyedDataRepository[Player, G[LevelCappedManaAmount]]

  val manaAmountUpdates: fs2.Stream[F, (Player, LevelCappedManaAmount)]

}

trait ManaWriteApi[G[_], Player] {

  val manaAmount: KeyedDataRepository[Player, ManaManipulation[G]]

}

trait ManaMultiplierApiForDragonNightTime[F[_]] {

  def setManaConsumptionWithDragonNightTime(manaMultiplier: ManaMultiplier): F[Unit]

}

trait ManaApi[F[_], G[_], Player]
    extends ManaReadApi[F, G, Player]
    with ManaWriteApi[G, Player]
    with ManaMultiplierApiForDragonNightTime[G]
