package com.github.unchama.seichiassist.subsystems.managedfly.application

sealed abstract class InternalInterruption extends Throwable

case object PlayerExpNotEnough extends InternalInterruption

case object FlyDurationExpired extends InternalInterruption
