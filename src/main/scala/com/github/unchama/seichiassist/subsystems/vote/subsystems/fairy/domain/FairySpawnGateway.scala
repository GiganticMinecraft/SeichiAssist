package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

trait FairySpawnGateway[F[_]] {

  /**
   * 妖精をスポーンさせる作用
   */
  def spawn(): F[Unit]

  /**
   * 妖精をデスポーンさせる作用
   */
  def despawn(): F[Unit]

}
