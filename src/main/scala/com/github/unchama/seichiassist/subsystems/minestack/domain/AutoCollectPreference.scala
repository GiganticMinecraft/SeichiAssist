package com.github.unchama.seichiassist.subsystems.minestack.domain

case class AutoCollectPreference(isEnabled: Boolean)

object AutoCollectPreference {
  val initial: AutoCollectPreference = AutoCollectPreference(true);
}
