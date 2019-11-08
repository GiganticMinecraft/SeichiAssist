package com.github.unchama.seichiassist.achievement

import com.github.unchama.seichiassist.achievement.SeichiAchievement.{ParameterizedText, PlayerPredicate}

case class WithPlaceholder[A](placeholder: A)
object WithPlaceholder {
  implicit val stringIsWithPlaceholder: WithPlaceholder[String] = WithPlaceholder("???")
  implicit val unitIsWithPlaceholder: WithPlaceholder[Unit] = WithPlaceholder(())
}

case class AchievementCondition[P](shouldUnlock: PlayerPredicate,
                                   conditionTemplate: ParameterizedText[P],
                                   parameter: P) {
  val condition: String = conditionTemplate(parameter)
}

case class HiddenAchievementCondition[P: WithPlaceholder](shouldDisplayToUI: PlayerPredicate,
                                                          condition: AchievementCondition[P]) {
  val hiddenCondition: String =
    condition.conditionTemplate(implicitly[WithPlaceholder[P]].placeholder)
}

