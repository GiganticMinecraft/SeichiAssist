package com.github.unchama.seichiassist.subsystems.minestack.domain

/**
 * 永続化されたマインスタックカタログに対する操作の抽象。
 */
trait PersistedMineStackCatalogue[F[_], IS] {

  /**
   * カタログデータ全体を [[Map]] として読みだす作用。
   *
   * UIレイヤなどは、ここで渡された [[MineStackItemId]] を信頼して [[delete]] 等の操作を行ってよい。
   * ただし、永続化されたカタログは複数のプロセスから参照されるため、
   * 返ってきた [[MineStackItemId]] に対応する [[MineStackEntry]] が存在し続ける保証はない。
   * 存在していなかった場合の挙動は [[delete]] 等の作用が各々定める。
   */
  val read: F[Map[MineStackItemId, MineStackEntry[IS]]]

  /**
   * カタログから `id` によって指定されるエントリを削除する作用。
   */
  def delete(id: MineStackItemId): F[Boolean]

  /**
   * カタログが `id` に対応して持っているエントリを `entry` により置き換える作用
   */
  def set(id: MineStackItemId, entry: MineStackEntry[IS]): F[Boolean]

  /**
   * カタログへマインスタックのエントリを登録する作用。
   *
   * 登録時、重複が見つかった場合には [[CatalogueModificationResult.OnRegister.Duplicate]] が
   * 重複していたエントリのIDを含んで返り、
   * そうでない場合は登録がされた後 [[CatalogueModificationResult.OnRegister.Success]] が戻る。
   */
  def register(entry: MineStackEntry[IS]): F[CatalogueModificationResult.OnRegister]

}
