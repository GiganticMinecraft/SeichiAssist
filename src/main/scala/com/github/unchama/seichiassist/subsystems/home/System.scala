package com.github.unchama.seichiassist.subsystems.home

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist.Scopes.globalChatInterceptionScope
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.home.bukkit.command.HomeCommand
import com.github.unchama.seichiassist.subsystems.home.bukkit.listeners.RespawnLocationOverwriter
import com.github.unchama.seichiassist.subsystems.home.domain.{Home, HomeId, HomeLocation}
import com.github.unchama.seichiassist.subsystems.home.domain.OperationResult.RenameResult
import com.github.unchama.seichiassist.subsystems.home.infrastructure.JdbcHomePersistence
import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: HomeReadAPI[F] with HomeWriteAPI[F]
}

object System {
  def wired[F[_]: OnMinecraftServerThread: ConcurrentEffect: NonServerThreadContextShift]
    : System[F] = {
    val persistence = new JdbcHomePersistence[F]()

    new System[F] {
      override implicit val api: HomeAPI[F] = new HomeAPI[F] {
        override def upsertLocation(ownerUuid: UUID, id: HomeId)(
          location: HomeLocation
        ): F[Unit] =
          persistence.upsertLocation(ownerUuid, id)(location)
        override def rename(ownerUuid: UUID, id: HomeId)(name: String): F[RenameResult] =
          persistence.rename(ownerUuid, id)(name)
        override def get(ownerUuid: UUID, id: HomeId): F[Option[Home]] =
          persistence.get(ownerUuid, id)
        override def list(ownerUuid: UUID): F[Map[HomeId, Home]] =
          persistence.list(ownerUuid)
        override def remove(ownerUuid: UUID, id: HomeId): F[Boolean] =
          persistence.remove(ownerUuid, id)
      }
      override val commands: Map[String, TabExecutor] =
        Map("home" -> HomeCommand.executor)

      override val listeners: Seq[Listener] = Seq(new RespawnLocationOverwriter[F])
    }
  }
}
