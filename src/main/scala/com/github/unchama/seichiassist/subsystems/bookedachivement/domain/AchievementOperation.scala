package com.github.unchama.seichiassist.subsystems.bookedachivement.domain

/**
 * 実績の操作を表す
 */
sealed trait AchievementOperation

object AchievementOperation {
  case object GIVE extends AchievementOperation {
    override def toString: String = "give"
  }

  case object DEPRIVE extends AchievementOperation {
    override def toString: String = "deprive"
  }

  def fromString(string: String): Option[AchievementOperation] = string match {
    case "give" => Some(GIVE)
    case "deprive" => Some(DEPRIVE)
    case _ => None
  }
}
