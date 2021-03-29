package com.github.unchama.seichiassist.subsystems.present.infrastructure

import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
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
   * ページネーション付きでプレイヤーがプレゼントを受け取ることができるかどうか列挙する。
   * このときの出現順序は、[[PresentID]]が最も若いエントリから先に出現する。
   *
   * 例として以下のような状況を仮定する:
   *   - 既知のPresentIDとItemStackのエントリ: `List((1, aaa), (3, ccc), (6, fff), (4, ddd), (5, eee), (2, bbb))`
   *   - PresentPersistenceのインスタンス `pp`
   *   - 調査対象のプレイヤー `A`
   *   - `A` が対象となっているプレゼントのPresentID: `Set(1, 2, 4, 6)`
   *   - `A` が受け取ったPresentID: `Set(1, 2, 3)`
   *
   * この時 `pp.mappingWithPagination(A, 1, 5)` を呼び出すと、作用の中で計算される結果は次のとおりになる:
   *
   *    `Map(1 -> Claimed, 2 -> Claimed, 3 -> Claimed, 4 -> NotClaimed, 5 -> Unavailable)`
   *
   * 備考:
   *   - 実装によっては、[[fetchState]]などを呼び出して既知のエントリを全列挙する可能性がある。
   *   - このメソッドは一貫性のために[[fetchState]]のドキュメントにある制約を継承する。
   *
   * @param player 調べる対象のプレイヤー
   * @param perPage ページごとのエントリの数
   * @param page ページ、1オリジン
   * @return ページネーションを計算して返す作用
   */
  def fetchStateWithPagination(player: UUID, perPage: Int Refined Positive, page: Int Refined Positive): F[Map[PresentID, PresentClaimingState]]

  /**
   * プレイヤーがプレゼントを受け取ることができるかどうか列挙する。このとき、計算されるMapは次の性質を持つ:
   *
   *  - すでに受け取ったプレゼントに対応するPresentIDに対して[[PresentClaimingState.Claimed]]がマッピングされる
   *  - 受け取ることができるが、まだ受け取っていないプレゼントに対応するPresentIDに対して[[PresentClaimingState.NotClaimed]]がマッピングされる
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
