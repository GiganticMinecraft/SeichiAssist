package com.github.unchama.seichiassist.subsystems.gridregion

import cats.data.Kleisli
import cats.effect.SyncEffect
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gridregion.application.repository.{
  RegionUnitPerClickSettingRepositoryDefinition,
  RegionUnitsRepositoryDefinition
}
import com.github.unchama.seichiassist.subsystems.gridregion.bukkit.BukkitRegionOperations
import com.github.unchama.seichiassist.subsystems.gridregion.domain._
import com.github.unchama.seichiassist.subsystems.gridregion.infrastructure.JdbcRegionUnitsPersistence
import com.github.unchama.util.external.ExternalPlugins
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import org.bukkit.Location
import org.bukkit.entity.Player

trait System[F[_], Player, Location] extends Subsystem[F] {

  val api: GridRegionAPI[F, Player, Location]

}

object System {

  import cats.implicits._

  def wired[F[_]: SyncEffect]: F[System[F, Player, Location]] = {
    implicit val regionUnitsPersistence: RegionUnitsPersistence[F] =
      new JdbcRegionUnitsPersistence[F]

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
        RegionUnitsRepositoryDefinition.withContext[F, Player]
      )
    } yield {
      val regionUnitPerClickSettingRepository =
        regionUnitPerClickSettingRepositoryControls.repository
      val regionUnitsRepository = regionUnitsRepositoryControls.repository
      val regionOperations: RegionOperations[Location] = new BukkitRegionOperations
      val we: WorldEditPlugin = ExternalPlugins.getWorldEdit
      val wg: WorldGuardPlugin = ExternalPlugins.getWorldGuard

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
              regionUnitsRepository(player).get

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
            ): CreateRegionResult = {
              if (!SeichiAssist.seichiAssistConfig.isGridProtectionEnabled(player.getWorld))
                return CreateRegionResult.ThisWorldRegionCanNotBeCreated

              val selection = Some(we.getSelection(player))
              if (selection.isEmpty) return CreateRegionResult.RegionCanNotBeCreatedByOtherError

              // TODO: regionNumをRepository保存にする
              val region = new ProtectedCuboidRegion(
                s"${player.getName}_1",
                selection.get.getNativeMinimumPoint.toBlockVector,
                selection.get.getNativeMaximumPoint.toBlockVector
              )
              val wgManager = wg.getRegionManager(player.getWorld)
              val regions = wgManager.getApplicableRegions(region)
              if (regions.size != 0) return CreateRegionResult.RegionCanNotBeCreatedByOtherError

              val wgConfig = wg.getGlobalStateManager.get(player.getWorld)
              val maxRegionCount = wgConfig.getMaxRegionCount(player)
              if (
                maxRegionCount >= 0 && wgManager.getRegionCountOfPlayer(
                  wg.wrapPlayer(player)
                ) >= maxRegionCount
              )
                CreateRegionResult.RegionCanNotBeCreatedByOtherError
              else
                CreateRegionResult.Success
            }

            override def regionSelection(
              player: Player,
              regionUnits: RegionUnits,
              direction: Direction
            ): RegionSelection[Location] =
              regionOperations.getSelection(player.getLocation, regionUnits, direction)
          }
      }
    }
  }

}
