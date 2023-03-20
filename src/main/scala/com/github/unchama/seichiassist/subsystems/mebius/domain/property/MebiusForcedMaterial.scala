package com.github.unchama.seichiassist.subsystems.mebius.domain.property

/**
 * Mebiusの材質を強制的に変更する設定。
 *
 * 「30レベル以上でのみ革に戻すことができる」という機能のため。
 */
sealed trait MebiusForcedMaterial {

  def allowedAt(level: MebiusLevel): Boolean

  def next: MebiusForcedMaterial

}

object MebiusForcedMaterial {
  import cats.implicits._

  // ダイヤモンド
  case object None extends MebiusForcedMaterial {
    override def allowedAt(level: MebiusLevel): Boolean = true
    override def next: MebiusForcedMaterial = Leather
  }

  // 革
  case object Leather extends MebiusForcedMaterial {
    override def allowedAt(level: MebiusLevel): Boolean = level >= MebiusLevel(30)
    override def next: MebiusForcedMaterial = None
  }

  // 金
  case object Gold extends MebiusForcedMaterial {
    override def allowedAt(level: MebiusLevel): Boolean = level >= MebiusLevel(30)

    override def next: MebiusForcedMaterial = Gold
  }

  // 鉄
  case object Iron extends MebiusForcedMaterial {
    override def allowedAt(level: MebiusLevel): Boolean = level >= MebiusLevel(30)

    override def next: MebiusForcedMaterial = Iron
  }

  // チェーン
  case object Chain extends MebiusForcedMaterial {
    override def allowedAt(level: MebiusLevel): Boolean = level >= MebiusLevel(30)

    override def next: MebiusForcedMaterial = Chain
  }
}
