package com.github.unchama.seichiassist.subsystems.subhome

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.SeichiAssist.Scopes.globalChatInterceptionScope
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.subhome.bukkit.command.SubHomeCommand
import com.github.unchama.seichiassist.subsystems.subhome.infrastructure.{SubHomePersistence, SubHomeReadAPI, SubHomeWriteAPI}
import org.bukkit.command.TabExecutor

trait System[F[_]] extends Subsystem[F] {
  def api: SubHomeReadAPI[F] with SubHomeWriteAPI[F]
}

object System {
  def wired[
    F[_]
    : ConcurrentEffect
    : NonServerThreadContextShift
  ]: System[F] = new System[F] {
    val persistence = new SubHomePersistence[F]()
    override def api: SubHomeReadAPI[F] with SubHomeWriteAPI[F] = persistence

    private implicit val writer: SubHomeWriteAPI[F] = api
    private implicit val reader: SubHomeReadAPI[F] = api

    override val commands: Map[String, TabExecutor] =
      Map(
        "subhome" -> SubHomeCommand.executor
      )
  }
}
