package com.github.unchama.seichiassist.subsystems.present.infrastructure

import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.util.UUID

trait PresentRepository[F[_]] {
  type PresentID = Int

  /**
   *
   * @return 今までサーバーに参加したことがある全てのプレイヤーの[[UUID]]
   */
  def getUUIDs: F[Set[UUID]]

  /**
   * 指定したUUIDを持つプレイヤーに対して`itemstack`を受け取れるような新しいプレゼントを定義する。
   * @param itemstack 追加するアイテム
   * @param players 配るプレイヤーのUUID
   * @return 定義に成功した場合新たに取得した`F[Some[PresentID]]`、失敗した場合`F[None]`
   */
  def performAddPresent(itemstack: ItemStack, players: Seq[UUID]): F[Option[PresentID]]

  /**
   * 永続化層でプレゼントを受け取ったことにする。
   * @param player
   * @param presentId
   * @return 永続化層への書き込みを確定する作用
   */
  def claimPresent(player: Player, presentId: PresentID): F[Unit]

  /**
   * 有効な[[PresentID]]とそれに紐付いた[[ItemStack]]を列挙する。
   * @return 全てのプレゼント。
   */
  def getAllPresent: F[Map[PresentID, ItemStack]]

  /**
   * プレイヤーが有効なプレゼントを受け取ることができるかどうか列挙する。
   * @param player チェックするプレイヤー
   * @return [[PresentID]]とそれに紐付けられたプレゼントを受け取ることができるかどうかの[[Map]]
   */
  def fetchPresentsState(player: Player): F[Map[PresentID, PresentClaimingState]]

  /**
   * 有効な[[PresentID]]を全て列挙する。
   * @return
   */
  def getAllPresentId: F[Set[PresentID]]
}
