package com.github.unchama.seichiassist.subsystems.gridregion

import cats.data.Kleisli
import cats.effect.SyncEffect
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gridregion.application.actions.{
  CreateRegion,
  GridRegionRegistrar
}
import com.github.unchama.seichiassist.subsystems.gridregion.application.repository.{
  RULChangePerClickSettingRepositoryDefinition,
  RegionCountAllUntilNowRepositoryDefinition,
  RegionUnitsRepositoryDefinition
}
import com.github.unchama.seichiassist.subsystems.gridregion.bukkit.{
  BukkitCreateRegion,
  BukkitGetGridUnitSizeLimitPerWorld,
  BukkitGetRegionCountLimit,
  BukkitRegionCreationPolicy,
  BukkitRegionDefiner,
  BukkitRegionRegister
}
import com.github.unchama.seichiassist.subsystems.gridregion.domain._
import com.github.unchama.seichiassist.subsystems.gridregion.domain.persistence.{
  RegionCountAllUntilNowPersistence,
  RegionTemplatePersistence
}
import com.github.unchama.seichiassist.subsystems.gridregion.infrastructure.{
  JdbcRegionCountAllUntilNowPersistence,
  JdbcRegionTemplatePersistence
}
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.World

trait System[F[_], Player, Location, World] extends Subsystem[F] {

  val api: GridRegionAPI[F, Player, Location, World]

}

object System {

  import cats.implicits._

  def wired[F[_], G[_]: SyncEffect: ContextCoercion[*[_], F]]
    : G[System[F, Player, Location, World]] = {
    implicit val regionCountPersistence: RegionCountAllUntilNowPersistence[G] =
      new JdbcRegionCountAllUntilNowPersistence[G]
    val regionTemplatePersistence: RegionTemplatePersistence[G] =
      new JdbcRegionTemplatePersistence[G]

    for {
      rulChangePerClickSettingRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            RULChangePerClickSettingRepositoryDefinition.initialization[G, Player],
            RULChangePerClickSettingRepositoryDefinition.finalization[G, Player]
          )
      )
      regionUnitsRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            RegionUnitsRepositoryDefinition.initialization[G, Player],
            RegionUnitsRepositoryDefinition.finalization[G, Player]
          )
      )
      regionCountAllUntilNowRepositoryControls <- BukkitRepositoryControls.createHandles(
        RegionCountAllUntilNowRepositoryDefinition.withContext[G, Player]
      )
    } yield {
      val rulPerClickSettingRepository =
        rulChangePerClickSettingRepositoryControls.repository
      val regionUnitsRepository =
        regionUnitsRepositoryControls.repository
      implicit val regionCountRepository: KeyedDataRepository[Player, Ref[G, RegionCount]] =
        regionCountAllUntilNowRepositoryControls.repository
      implicit val regionRegister: RegionRegister[G, Location, Player] =
        new BukkitRegionRegister
      implicit val regionDefiner: RegionDefiner[G, Location] = new BukkitRegionDefiner[G]
      implicit val getGridLimitPerWorld: GetGridUnitSizeLimitPerWorld[G, World] =
        new BukkitGetGridUnitSizeLimitPerWorld[G]
      implicit val regionCreationPolicy: RegionCreationPolicy[G, Player, World, Location] =
        new BukkitRegionCreationPolicy[G]
      implicit val getRegionCountLimit: GetRegionCountLimit[G, World] =
        new BukkitGetRegionCountLimit[G]
      implicit val createRegion: CreateRegion[G, Player, Location] = new BukkitCreateRegion[G]
      val gridRegionRegistrar = new GridRegionRegistrar[G, Location, Player, World]

      new System[F, Player, Location, World] {
        override val api: GridRegionAPI[F, Player, Location, World] =
          new GridRegionAPI[F, Player, Location, World] {
            override def toggleRulChangePerClick: Kleisli[F, Player, Unit] = Kleisli { player =>
              ContextCoercion(rulPerClickSettingRepository(player).toggleUnitPerClick)
            }

            override def lengthChangePerClick(player: Player): F[RegionUnitLength] =
              ContextCoercion(rulPerClickSettingRepository(player).rulChangePerClick)

            override def currentlySelectedShape(player: Player): F[SubjectiveRegionShape] =
              ContextCoercion(regionUnitsRepository(player).currentShape)

            override def updateCurrentRegionShapeSettings(
              regionUnits: SubjectiveRegionShape
            ): Kleisli[F, Player, Unit] =
              Kleisli { player =>
                ContextCoercion(regionUnitsRepository(player).set(regionUnits))
              }

            override def regionUnitLimit(world: World): F[RegionUnitSizeLimit] =
              ContextCoercion(getGridLimitPerWorld.apply(world))

            override def canCreateRegion(
              player: Player,
              shape: SubjectiveRegionShape
            ): F[RegionCreationResult] =
              ContextCoercion(regionRegister.canCreateRegion(player, shape))

            override def regionSelection(
              player: Player,
              shape: SubjectiveRegionShape
            ): F[RegionSelectionCorners[Location]] =
              ContextCoercion(regionDefiner.getSelectionCorners(player.getLocation, shape))

            override def regionCount(player: Player): F[RegionCount] =
              ContextCoercion(regionCountRepository(player).get)

            override def savedGridRegionTemplates(player: Player): F[Vector[RegionTemplate]] =
              ContextCoercion(regionTemplatePersistence.regionTemplates(player.getUniqueId))

            override def saveGridRegionTemplate(
              regionTemplate: RegionTemplate
            ): Kleisli[F, Player, Unit] = Kleisli { player =>
              ContextCoercion(
                regionTemplatePersistence.saveRegionTemplate(player.getUniqueId, regionTemplate)
              )
            }

            override def claimRegionByShapeSettings(
              shape: SubjectiveRegionShape
            ): Kleisli[F, Player, Unit] = Kleisli { player =>
              ContextCoercion(
                gridRegionRegistrar
                  .validateAndCreateRegion(player, player.getWorld, player.getLocation, shape)
                  .void
              )
            }

            override def increaseRegionCount: Kleisli[F, Player, Unit] =
              Kleisli { player =>
                ContextCoercion(regionCountRepository(player).update(_.increment))
              }
          }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          rulChangePerClickSettingRepositoryControls,
          regionUnitsRepositoryControls,
          regionCountAllUntilNowRepositoryControls
        ).map(_.coerceFinalizationContextTo[F])
      }
    }
  }

}
