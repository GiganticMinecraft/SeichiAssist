package com.github.unchama.seichiassist.subsystems.present

import cats.effect.Async
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.domain.actions.UuidToLastSeenName
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.present.bukkit.command.PresentCommand
import com.github.unchama.seichiassist.subsystems.present.infrastructure.JdbcBackedPresentPersistence
import org.bukkit.command.TabExecutor

object System {
  def wired[ConcurrentContext[_]: Async: OnMinecraftServerThread: [f[_]] =>> ContextCoercion[
    f,
    cats.effect.IO
  ]](
    implicit uuidToLastSeenName: UuidToLastSeenName[ConcurrentContext]
  ): Subsystem[ConcurrentContext] = {
    implicit val repo: JdbcBackedPresentPersistence[ConcurrentContext] =
      new JdbcBackedPresentPersistence[ConcurrentContext]

    new Subsystem[ConcurrentContext] {
      override val commands: Map[String, TabExecutor] = Map(
        "present" -> new PresentCommand().executor
      )
    }
  }
}
