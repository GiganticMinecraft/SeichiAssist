package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import cats.Monad
import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gacha.domain.GrantState
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrize
import org.bukkit.entity.Player

trait GrantGachaPrize[F[_], ItemStack] {

  import cats.implicits._

  implicit val F: Monad[F]

  /**
   * @param prizes MineStackに格納したい[[GachaPrize]]のVector
   * @return `prizes`をMineStackに格納することを試み、格納できなかった[[GachaPrize]]のVectorを返す作用
   */
  def tryInsertIntoMineStack(
    prizes: Vector[GachaPrize[ItemStack]]
  ): Kleisli[F, Player, Vector[GachaPrize[ItemStack]]]

  /**
   * @param prizes プレイヤーに付与する[[GachaPrize]]のVector
   * @param ownerName `prizes`の各アイテムに対して、署名をする名義
   * @return `prizes`の各アイテムをプレイヤーのインベントリに挿入するか、
   *         それができなかった場合には地面にドロップする作用
   */
  def insertIntoPlayerInventoryOrDrop(
    prizes: Vector[GachaPrize[ItemStack]],
    ownerName: Option[String]
  ): Kleisli[F, Player, Unit]

  final def grantGachaPrize(
    prizes: Vector[GachaPrize[ItemStack]]
  ): Kleisli[F, Player, GrantState] =
    Kleisli { player =>
      for {
        failedIntoMineStackGachaPrizes <- tryInsertIntoMineStack(prizes)(player)
        _ <- insertIntoPlayerInventoryOrDrop(
          failedIntoMineStackGachaPrizes,
          Some(player.getName)
        )(player)
      } yield {
        if (failedIntoMineStackGachaPrizes.isEmpty) GrantState.GrantedMineStack
        else GrantState.GrantedInventory
      }
    }

}
