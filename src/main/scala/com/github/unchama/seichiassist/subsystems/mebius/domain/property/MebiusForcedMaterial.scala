package com.github.unchama.seichiassist.subsystems.mebius.domain.property

/**
 * Mebiusの材質を強制的に変更する設定。
 * 『30レベル以上で革や鉄、金、チェーンなどの素材に変更できる』機能のため。
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

    override def next: MebiusForcedMaterial = Gold

  }

  // 金
  case object Gold extends MebiusForcedMaterial {

    override def allowedAt(level: MebiusLevel): Boolean = level >= MebiusLevel(30)

    override def next: MebiusForcedMaterial = Iron

  }

  // 鉄
  case object Iron extends MebiusForcedMaterial {

    override def allowedAt(level: MebiusLevel): Boolean = level >= MebiusLevel(30)

    override def next: MebiusForcedMaterial = Chain

  }

  // チェーン
  case object Chain extends MebiusForcedMaterial {

    override def allowedAt(level: MebiusLevel): Boolean = level >= MebiusLevel(30)

    override def next: MebiusForcedMaterial = None

  }

}
