package com.github.unchama.seichiassist.subsystems.managedfly.bukkit.routines

import cats.effect.IO

import scala.concurrent.duration.FiniteDuration

object PeriodicMebiusSpeechRoutine {

  val getRepeatInterval: IO[FiniteDuration] = IO {
    import scala.concurrent.duration._

    1.minute
  }


}
