package com.github.unchama.seichiassist.subsystems.gachapoint.domain

/**
 * 特定のプレーヤーへガチャ券を配布する作用。
 *
 * TODO ガチャに関するサブシステムが作成されたらこの代数はそちらへ移してよい
 */
trait GrantGachaTicketToAPlayer[F[_]] {

  def give(count: Int): F[Unit]

}
