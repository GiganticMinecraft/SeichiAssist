package com.github.unchama.seichiassist.subsystems.present.infrastructure

import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.util.UUID

/**
 * プレゼントシステムに関する永続化のトレイト。
 * 他で指定がない限り、以下の条件・制約を満たす。違反した時の動作は未定義である:
 *   - 引数で渡される`PresentID`は対応するプレゼントが存在し、一意でなければならない
 *   - 返り値としての`PresentID`は対応するプレゼントが存在する
 */
trait PresentPersistence[F[_]] {
  type PresentID = Int

  /**
   * 指定した[[ItemStack]]に対応するプレゼントを新しく定義する。
   * @param itemstack プレゼントの中身
   * @return 定義に成功した場合新しく割り振られたF[Some[ [[PresentID]] ]、失敗した場合F[None]
   */
  def defineNewPresent(itemstack: ItemStack): F[Option[PresentID]]

  /**
   * 指定したUUIDを持つプレイヤーに対して`presentID`で指定されたプレゼントを受け取ることができるようにする。
   *
   * @param presentID 対象のプレゼントID
   * @param players   受け取ることができるようにするプレイヤーのUUID
   */
  def addScope(presentID: PresentID, players: Set[UUID]): F[Unit]

  /**
   * 指定したUUIDを持つプレイヤーが`presentID`で指定されたプレゼントを受け取ることができないようにする。
   * @param presentID 対象のプレゼントID
   * @param players 受け取ることができないようにするプレイヤーのUUID
   */
  def removeScope(presentID: PresentID, players: Set[UUID]): F[Unit]

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
  def getPresentMapping: F[Map[PresentID, ItemStack]]

  /**
   * プレイヤーが有効なプレゼントを受け取ることができるかどうか列挙する。
   * @param player チェックするプレイヤー
   * @return [[PresentID]]とそれに紐付けられたプレゼントを受け取ることができるかどうかの[[Map]]
   */
  def fetchPresentsState(player: Player): F[Map[PresentID, PresentClaimingState]]

  /**
   * 指定したプレゼントIDからプレゼントを引き出す。
   * @param presentID そのプレゼントID
   * @return 存在した場合は`F[Some[ItemStack]]`、存在しない場合は`F[None]`
   */
  def lookupPresent(presentID: PresentID): F[Option[ItemStack]]
}
