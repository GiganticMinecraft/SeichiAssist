package com.github.unchama.seichiassist.subsystems.gridregion

import cats.data.Kleisli
import cats.effect.SyncEffect
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gridregion.application.repository.{RegionUnitPerClickSettingRepositoryDefinition, RegionUnitsRepositoryDefinition}
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{CreateRegionResult, RegionUnit, RegionUnitLimit, RegionUnits, RegionUnitsPersistence}
import com.github.unchama.seichiassist.subsystems.gridregion.infrastructure.JdbcRegionUnitsPersistence
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {

  val api: GridRegionAPI[F, Player]

}

object System {

  import cats.implicits._

  def wired[F[_]: SyncEffect]: F[System[F, Player]] = {
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

      new System[F, Player] {
        override val api: GridRegionAPI[F, Player] = new GridRegionAPI[F, Player] {
          override def toggleUnitPerClick: Kleisli[F, Player, Unit] = Kleisli { player =>
            regionUnitPerClickSettingRepository(player).toggleUnitPerClick
          }

          override def unitPerClick(player: Player): F[RegionUnit] =
            regionUnitPerClickSettingRepository(player).unitPerClick

          override def isWithinLimits(regionUnits: RegionUnits, worldName: String): Boolean = {
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

          override def canCreateRegion(player: Player, regionUnits: RegionUnits): CreateRegionResult = {
            if (!SeichiAssist.seichiAssistConfig.isGridProtectionEnabled(player.getWorld))
              CreateRegionResult.ThisWorldRegionCanNotBeCreated
            else if ()
          }
        }
      }
    }
  }

}
