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

  case object None extends MebiusForcedMaterial {
    override def allowedAt(level: MebiusLevel): Boolean = true
    override def next: MebiusForcedMaterial = Leather
  }

  case object Leather extends MebiusForcedMaterial {
    override def allowedAt(level: MebiusLevel): Boolean = level >= MebiusLevel(30)
    override def next: MebiusForcedMaterial = None
  }
}
