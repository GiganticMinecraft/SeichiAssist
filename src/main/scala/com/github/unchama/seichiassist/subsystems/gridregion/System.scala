package com.github.unchama.seichiassist.subsystems.gridregion

import cats.data.Kleisli
import cats.effect.SyncEffect
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gridregion.application.repository.{RegionUnitPerClickSettingRepositoryDefinition, RegionUnitsRepositoryDefinition}
import com.github.unchama.seichiassist.subsystems.gridregion.bukkit.BukkitRegionOperations
import com.github.unchama.seichiassist.subsystems.gridregion.domain._
import com.github.unchama.util.external.ExternalPlugins
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.Location
import org.bukkit.entity.Player

trait System[F[_], Player, Location] extends Subsystem[F] {

  val api: GridRegionAPI[F, Player, Location]

}

object System {

  import cats.implicits._

  def wired[F[_]: SyncEffect]: F[System[F, Player, Location]] = {
    for {
      regionUnitPerClickSettingRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            RegionUnitPerClickSettingRepositoryDefinition.initialization[F, Player],
            RegionUnitPerClickSettingRepositoryDefinition.finalization[F, Player]
          )
      )
      regionUnitsRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            RegionUnitsRepositoryDefinition.initialization[F, Player],
            RegionUnitsRepositoryDefinition.finalization[F, Player]
          )
      )
    } yield {
      val regionUnitPerClickSettingRepository =
        regionUnitPerClickSettingRepositoryControls.repository
      val regionUnitsRepository = regionUnitsRepositoryControls.repository
      implicit val we: WorldEditPlugin = ExternalPlugins.getWorldEdit
      implicit val wg: WorldGuardPlugin = ExternalPlugins.getWorldGuard
      val regionOperations: RegionOperations[F, Location, Player] = new BukkitRegionOperations

      new System[F, Player, Location] {
        override val api: GridRegionAPI[F, Player, Location] =
          new GridRegionAPI[F, Player, Location] {
            override def toggleUnitPerClick: Kleisli[F, Player, Unit] = Kleisli { player =>
              regionUnitPerClickSettingRepository(player).toggleUnitPerClick
            }

            override def unitPerClick(player: Player): F[RegionUnit] =
              regionUnitPerClickSettingRepository(player).unitPerClick

            override def isWithinLimits(
              regionUnits: RegionUnits,
              worldName: String
            ): Boolean = {
              val totalRegionUnits =
                regionUnits.computeTotalRegionUnits
              val limit = SeichiAssist.seichiAssistConfig.getGridLimitPerWorld(worldName)

              totalRegionUnits.units <= limit
            }

            override def regionUnits(player: Player): F[RegionUnits] =
              regionUnitsRepository(player).regionUnits

            override def saveRegionUnits(regionUnits: RegionUnits): Kleisli[F, Player, Unit] =
              Kleisli { player => regionUnitsRepository(player).set(regionUnits) }

            override def regionUnitLimit(worldName: String): RegionUnitLimit = {
              val limit = SeichiAssist.seichiAssistConfig.getGridLimitPerWorld(worldName)
              RegionUnitLimit(limit)
            }

            override def canCreateRegion(
              player: Player,
              regionUnits: RegionUnits,
              direction: Direction
            ): CreateRegionResult =
              regionOperations.canCreateRegion(player, regionUnits, direction)

            override def regionSelection(
              player: Player,
              regionUnits: RegionUnits,
              direction: Direction
            ): RegionSelection[Location] =
              regionOperations.getSelection(player.getLocation, regionUnits, direction)

            override def createRegion: Kleisli[F, Player, Unit] = Kleisli { player =>
              regionOperations.createRegion(player)
            }
          }
      }
    }
  }

}
