package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

trait FairyRoutine[F[_], Player] {

  /**
   * @return 妖精の定期実行プロセスを開始する作用
   */
  def start(player: Player): F[Nothing]

}
