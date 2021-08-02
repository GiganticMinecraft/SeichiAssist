package com.github.unchama.seichiassist.subsystems.subhome

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.SeichiAssist.Scopes.globalChatInterceptionScope
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.subhome.bukkit.command.SubHomeCommand
import com.github.unchama.seichiassist.subsystems.subhome.domain.OperationResult.RenameResult
import com.github.unchama.seichiassist.subsystems.subhome.domain.{SubHome, SubHomeId, SubHomeLocation}
import com.github.unchama.seichiassist.subsystems.subhome.infrastructure.JdbcSubHomePersistence
import org.bukkit.command.TabExecutor

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: SubHomeReadAPI[F] with SubHomeWriteAPI[F]
}

object System {
  def wired[
    F[_]
    : ConcurrentEffect
    : NonServerThreadContextShift
  ]: System[F] = {
    val persistence = new JdbcSubHomePersistence[F]()

    new System[F] {
      override implicit val api: SubHomeAPI[F] = new SubHomeAPI[F] {
        override def upsertLocation(ownerUuid: UUID, id: SubHomeId, location: SubHomeLocation): F[Unit] =
          persistence.upsert(ownerUuid, id)(SubHome(None, location))
        override def rename(ownerUuid: UUID, id: SubHomeId)(name: String): F[RenameResult] =
          persistence.rename(ownerUuid, id)(name)
        override def get(ownerUuid: UUID, id: SubHomeId): F[Option[SubHome]] =
          persistence.get(ownerUuid, id)
        override def list(ownerUuid: UUID): F[Map[SubHomeId, SubHome]] =
          persistence.list(ownerUuid)
      }
      override val commands: Map[String, TabExecutor] =
        Map(
          "subhome" -> SubHomeCommand.executor
        )
    }
  }
}
