package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import cats.Monad
import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gacha.domain.GrantState
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrizeTableEntry

trait GrantGachaPrize[F[_], ItemStack, Player] {

  import cats.implicits._

  implicit val F: Monad[F]

  /**
   * @param prizes MineStackに格納したい[[GachaPrizeTableEntry]]のVector
   * @return `prizes`をMineStackに格納することを試み、格納できなかった[[GachaPrizeTableEntry]]のVectorを返す作用
   */
  def tryInsertIntoMineStack(
    prizes: Vector[GachaPrizeTableEntry[ItemStack]]
  ): Kleisli[F, Player, Vector[GachaPrizeTableEntry[ItemStack]]]

  /**
   * @param prizes プレイヤーに付与する[[GachaPrizeTableEntry]]のVector
   * FIXME: 「記名する」というドメインロジックはシステムのより中核に近いところに移動したい…
   * @return `prizes` の各アイテムを (必要ならば記名した上で、) プレイヤーのインベントリに挿入するか、
   *         それができなかった場合には地面にドロップする作用
   */
  def insertIntoPlayerInventoryOrDrop(
    prizes: Vector[GachaPrizeTableEntry[ItemStack]]
  ): Kleisli[F, Player, Unit]

  final def grantGachaPrize(
    prizes: Vector[GachaPrizeTableEntry[ItemStack]]
  ): Kleisli[F, Player, Vector[(GachaPrizeTableEntry[ItemStack], GrantState)]] =
    Kleisli { player =>
      for {
        prizesNotInsertedIntoMineStack <- tryInsertIntoMineStack(prizes)(player)
        _ <- insertIntoPlayerInventoryOrDrop(prizesNotInsertedIntoMineStack)(player)
      } yield {
        val prizesInsertedIntoMineStack = prizes.diff(prizesNotInsertedIntoMineStack)
        prizesInsertedIntoMineStack.map(_ -> GrantState.GrantedMineStack) ++
          prizesNotInsertedIntoMineStack.map(_ -> GrantState.GrantedInventoryOrDrop)
      }
    }

}
