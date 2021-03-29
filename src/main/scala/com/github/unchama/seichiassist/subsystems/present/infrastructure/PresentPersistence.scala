package com.github.unchama.seichiassist.subsystems.present.infrastructure

import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.util.UUID

/**
 * プレゼントシステムに関する永続化のインターフェースを規定するトレイト。
 * 他で指定がない限り、以下の条件・制約を満たす。違反した時の動作は未定義である:
 *   - 引数で渡される`PresentID`は対応するプレゼントが存在し、一意でなければならない
 *   - 返り値としての`PresentID`は対応するプレゼントが存在する
 */
trait PresentPersistence[F[_]] {
  type PresentID

  /**
   * 指定した[[ItemStack]]に対応するプレゼントを新しく定義する。
   *
   * @param itemstack プレゼントの中身
   * @return 定義を行った後、新しく割り振られたPresentIDを返し、かつ定義に失敗した場合例外を投げる作用
   */
  def define(itemstack: ItemStack): F[PresentID]

  /**
   * 指定したPresentIDに対応するプレゼントを消去する。
   * @param presentID プレゼントID
   */
  def delete(presentID: PresentID): F[Unit]

  /**
   * 指定したUUIDを持つプレイヤーに対して`presentID`で指定されたプレゼントを受け取ることができるようにする。
   *
   * @param presentID 対象のプレゼントID
   * @param players   受け取ることができるようにするプレイヤーのUUID
   * @return 永続化層への書き込みを行う作用
   */
  def grant(presentID: PresentID, players: Set[UUID]): F[Unit]

  /**
   * 指定したUUIDを持つプレイヤーが`presentID`で指定されたプレゼントを受け取ることができないようにする。
   *
   * @param presentID 対象のプレゼントID
   * @param players   受け取ることができないようにするプレイヤーのUUID
   * @return 永続化層への書き込みを行う作用
   */
  def revoke(presentID: PresentID, players: Set[UUID]): F[Unit]

  /**
   * 永続化層でプレゼントを受け取ったことにする。
   *
   * @param player プレイヤーのUUID
   * @param presentID プレゼントID
   * @return 永続化層への書き込みを行う作用
   */
  def markAsClaimed(presentID: PresentID, player: UUID): F[Unit]

  /**
   * 全ての有効な[[PresentID]]とそれに紐付けられた[[ItemStack]]を列挙する。
   *
   * @return 全てのプレゼントを列挙する作用
   */
  def mapping: F[Map[PresentID, ItemStack]]

  /**
   * プレイヤーがプレゼントを受け取ることができるかどうか列挙する。このとき、計算されるMapは次の性質を持つ:
   *
   *  - 受け取ることができるが、まだ受け取っていないプレゼントに対応するPresentIDに対して[[PresentClaimingState.NotClaimed]]がマッピングされる
   *  - すでに受け取ったプレゼントに対応するPresentIDに対して[[PresentClaimingState.Claimed]]がマッピングされる
   *  - 有効かつ、プレイヤーが受け取ることができないPresentIDに対して[[PresentClaimingState.Unavailable]]がマッピングされる
   *
   * @param player チェックするプレイヤー
   * @return [[PresentID]]とそれに紐付けられたプレゼントを受け取ることができるかを
   *         [[PresentClaimingState]]で記述する[[Map]]を計算する作用
   */
  def fetchState(player: UUID): F[Map[PresentID, PresentClaimingState]]

  /**
   * 指定したプレゼントIDからプレゼントを引き出す。
   *
   * @param presentID プレゼントID
   * @return 存在する場合は`Some[ItemStack]`、存在しない場合は`None`を返す作用
   */
  def lookup(presentID: PresentID): F[Option[ItemStack]]
}
