package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.actions

trait GrantFastDiggingEffect[F[_], Player] {

  /**
   * `player` に、Minecraftのポーション効果値 `amount` に対応する採掘速度上昇効果を二秒間付与する作用。
   *
   * もしプレーヤーがすでに採掘速度上昇効果を付与されていた場合、既存の効果に上書きする。
   * これによりバニラのビーコンで得られるレベル1及びレベル2の採掘速度上昇効果が無効化されるが、仕様とする。
   */
  def forTwoSeconds(player: Player)(amount: Int): F[Unit]

}

object GrantFastDiggingEffect {

  def apply[F[_], Player](
    implicit ev: GrantFastDiggingEffect[F, Player]
  ): GrantFastDiggingEffect[F, Player] = ev

}
