package com.github.unchama.seichiassist.subsystems.buildcount

import cats.effect.{Clock, ConcurrentEffect, SyncEffect}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.minecraft.bukkit.actions.SendBukkitMessage.apply
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.{
  ClassifyPlayerWorld,
  IncrementBuildExpWhenBuiltByHand,
  IncrementBuildExpWhenBuiltWithSkill
}
import com.github.unchama.seichiassist.subsystems.buildcount.application.application.BuildAmountDataRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.buildcount.application.{
  BuildExpMultiplier,
  Configuration
}
import com.github.unchama.seichiassist.subsystems.buildcount.bukkit.actions.ClassifyBukkitPlayerWorld
import com.github.unchama.seichiassist.subsystems.buildcount.bukkit.listeners.BuildExpIncrementer
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.infrastructure.{
  JdbcBuildAmountDataPersistence,
  JdbcBuildAmountRateLimitPersistence
}
import io.chrisdavenport.cats.effect.time.JavaTime
import io.chrisdavenport.log4cats.{ErrorLogger, Logger}
import org.bukkit.entity.Player
import org.bukkit.event.Listener

trait System[F[_], G[_]] extends Subsystem[F] {

  val api: BuildCountAPI[F, G, Player]

}

object System {

  import cats.effect.implicits._
  import cats.implicits._

  def wired[F[_]: ConcurrentEffect: NonServerThreadContextShift: ErrorLogger, G[
    _
  ]: SyncEffect: ContextCoercion[*[_], F]: Clock](
    rootLogger: Logger[F]
  )(implicit configuration: Configuration): F[System[F, G]] = {

    implicit val expMultiplier: BuildExpMultiplier = configuration.multipliers
    implicit val persistence: JdbcBuildAmountDataPersistence[G] =
      new JdbcBuildAmountDataPersistence[G]()
    implicit val rateLimitPersistence: JdbcBuildAmountRateLimitPersistence[G] =
      new JdbcBuildAmountRateLimitPersistence[G]()
    implicit val javaTimeG: JavaTime[G] = JavaTime.fromClock

    val createSystem: F[System[F, G]] = for {
      buildCountTopic <- Fs3Topic[F, Option[(Player, BuildAmountData)]](None)

      buildAmountDataRepositoryControls <-
        ContextCoercion(
          BukkitRepositoryControls
            .createHandles(BuildAmountDataRepositoryDefinition.withPersistence(persistence))
        )
    } yield {
      implicit val classifyBukkitPlayerWorld: ClassifyPlayerWorld[G, Player] =
        new ClassifyBukkitPlayerWorld[G]
      implicit val incrementBuildExp: IncrementBuildExpWhenBuiltByHand[G, Player] =
        IncrementBuildExpWhenBuiltByHand.using(
          buildAmountDataRepositoryControls.repository,
          buildCountTopic
        )
      implicit val incrementBuildExpWhenBuiltBySkills
        : IncrementBuildExpWhenBuiltWithSkill[G, Player] =
        IncrementBuildExpWhenBuiltWithSkill.withConfig(expMultiplier)

      new System[F, G] {
        override val api: BuildCountAPI[F, G, Player] = new BuildCountAPI[F, G, Player] {
          override val incrementBuildExpWhenBuiltByHand
            : IncrementBuildExpWhenBuiltByHand[G, Player] =
            incrementBuildExp
          override val incrementBuildExpWhenBuiltWithSkill
            : IncrementBuildExpWhenBuiltWithSkill[G, Player] =
            incrementBuildExpWhenBuiltBySkills
          override val playerBuildAmountRepository
            : KeyedDataRepository[Player, ReadOnlyRef[G, BuildAmountData]] =
            buildAmountDataRepositoryControls.repository.map(ReadOnlyRef.fromRef)
          override val buildAmountUpdates: fs2.Stream[F, (Player, BuildAmountData)] =
            buildCountTopic.subscribe(1).mapFilter(identity)
        }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          buildAmountDataRepositoryControls.coerceFinalizationContextTo[F]
        )

        override val listeners: Seq[Listener] = List(new BuildExpIncrementer[G])
      }
    }
    createSystem.flatTap { system =>
      subsystems.notification.System.backgroundProcess(system.api).start
    }
  }

}
