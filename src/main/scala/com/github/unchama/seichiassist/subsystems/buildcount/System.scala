package com.github.unchama.seichiassist.subsystems.buildcount

import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.{ClassifyPlayerWorld, IncrementBuildExpWhenBuiltByHand, IncrementBuildExpWhenBuiltWithSkill}
import com.github.unchama.seichiassist.subsystems.buildcount.application.application.{BuildAmountDataRepositoryDefinitions, RateLimiterRepositoryDefinitions}
import com.github.unchama.seichiassist.subsystems.buildcount.application.{BuildExpMultiplier, Configuration}
import com.github.unchama.seichiassist.subsystems.buildcount.bukkit.actions.ClassifyBukkitPlayerWorld
import com.github.unchama.seichiassist.subsystems.buildcount.bukkit.listeners.BuildExpIncrementer
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.infrastructure.JdbcBuildAmountDataPersistence
import com.github.unchama.util.logging.log4cats.PrefixedLogger
import io.chrisdavenport.log4cats.Logger
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

import java.util.UUID

trait System[F[_], G[_]] extends Subsystem[F] {

  val api: BuildCountAPI[G, Player]

}

object System {

  import cats.implicits._

  def wired[
    F[_] : ConcurrentEffect : NonServerThreadContextShift : Timer,
    G[_] : SyncEffect : ContextCoercion[*[_], F]
  ](rootLogger: Logger[F])
   (implicit configuration: Configuration): G[System[F, G]] = {
    import com.github.unchama.minecraft.bukkit.actions.SendBukkitMessage._

    implicit val expMultiplier: BuildExpMultiplier = configuration.multipliers
    implicit val persistence: JdbcBuildAmountDataPersistence[G] = new JdbcBuildAmountDataPersistence[G]()
    implicit val logger: Logger[F] = PrefixedLogger[F]("BuildAssist-BuildAmount")(rootLogger)

    for {
      rateLimiterRepositoryControls <-
        BukkitRepositoryControls.createSinglePhasedRepositoryAndHandles(
          RateLimiterRepositoryDefinitions.initialization[F, G],
          RateLimiterRepositoryDefinitions.finalization[G, UUID]
        )

      buildAmountDataRepositoryControls <-
        BukkitRepositoryControls.createSinglePhasedRepositoryAndHandles(
          BuildAmountDataRepositoryDefinitions.initialization(persistence),
          BuildAmountDataRepositoryDefinitions.finalization(persistence)
        )
    } yield {
      implicit val classifyBukkitPlayerWorld: ClassifyPlayerWorld[G, Player] =
        new ClassifyBukkitPlayerWorld[G]
      implicit val incrementBuildExp: IncrementBuildExpWhenBuiltByHand[G, Player] =
        IncrementBuildExpWhenBuiltByHand.using(
          rateLimiterRepositoryControls.repository,
          buildAmountDataRepositoryControls.repository
        )
      implicit val incrementBuildExpWhenBuiltBySkills: IncrementBuildExpWhenBuiltWithSkill[G, Player] =
        IncrementBuildExpWhenBuiltWithSkill.withConfig(expMultiplier)

      new System[F, G] {
        override val api: BuildCountAPI[G, Player] = new BuildCountAPI[G, Player] {
          override val incrementBuildExpWhenBuiltByHand: IncrementBuildExpWhenBuiltByHand[G, Player] =
            incrementBuildExp
          override val incrementBuildExpWhenBuiltWithSkill: IncrementBuildExpWhenBuiltWithSkill[G, Player] =
            incrementBuildExpWhenBuiltBySkills
          override val playerBuildAmountRepository: KeyedDataRepository[Player, ReadOnlyRef[G, BuildAmountData]] =
            buildAmountDataRepositoryControls.repository.map(ref => ReadOnlyRef.fromRef(ref))
        }
        override val listeners: Seq[Listener] = List(
          rateLimiterRepositoryControls.initializer,
          buildAmountDataRepositoryControls.initializer,
          new BuildExpIncrementer[G],
        )
        override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = List(
          rateLimiterRepositoryControls.finalizer,
          buildAmountDataRepositoryControls.finalizer
        ).map(_.coerceContextTo[F])
        override val commands: Map[String, TabExecutor] = Map()
      }
    }
  }

}
