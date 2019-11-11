package com.github.unchama.seichiassist.achievement

case class WithPlaceholder[A](placeholder: A)
object WithPlaceholder {
  implicit val stringIsWithPlaceholder: WithPlaceholder[String] = WithPlaceholder("???")
  implicit val unitIsWithPlaceholder: WithPlaceholder[Unit] = WithPlaceholder(())
}

case class AchievementCondition[P](shouldUnlock: PlayerPredicate,
                                   conditionTemplate: ParameterizedText[P],
                                   parameter: P) {
  val parameterizedDescription: String = conditionTemplate(parameter)
}

case class HiddenAchievementCondition[P: WithPlaceholder](shouldDisplayToUI: PlayerPredicate,
                                                          underlying: AchievementCondition[P]) {
  val maskedDescription: String =
    underlying.conditionTemplate(implicitly[WithPlaceholder[P]].placeholder)
}

