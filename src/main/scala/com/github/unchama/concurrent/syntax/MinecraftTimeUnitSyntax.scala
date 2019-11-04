package com.github.unchama.concurrent.syntax

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

trait MinecraftTimeUnitSyntax {
  implicit def intTickAmountToMinecraftTimeUnitOps(int: Int): MinecraftTimeUnitOps = MinecraftTimeUnitOps(int.toLong)

  implicit def longTickAmountToMinecraftTimeUnitOps(long: Long): MinecraftTimeUnitOps = MinecraftTimeUnitOps(long)
}

case class MinecraftTimeUnitOps(tickAmount: Long) extends AnyVal {
  def ticks: FiniteDuration = FiniteDuration(tickAmount * 50, scala.concurrent.duration.MILLISECONDS)
}
