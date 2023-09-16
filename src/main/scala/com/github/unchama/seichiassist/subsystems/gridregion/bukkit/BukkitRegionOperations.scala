package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gridregion.domain._
import com.github.unchama.util.external.{WorldEditWrapper, WorldGuardWrapper}
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitRegionOperations[F[_]: Sync](
  implicit regionCountRepository: KeyedDataRepository[Player, Ref[F, RegionCount]]
) extends RegionOperations[F, Location, Player] {

  override def getSelection(
                             currentLocation: Location,
                             regionUnits: SubjectiveRegionShape,
                             direction: CardinalDirection
  ): RegionSelection[Location] = {
    val computedAheadBlockAmount = regionUnits.ahead.rul
    val computedLeftBlockAmount = regionUnits.left.rul
    val computedBehindBlockAmount = regionUnits.behind.rul
    val computedRightBlockAmount = regionUnits.right.rul

    /*
     * startPosition - 北西
     * endPosition - 南東
     * に合わせる
     */
    val (startPosition, endPosition) = direction match {
      case CardinalDirection.East =>
        (
          currentLocation
            .clone()
            .subtract(computedBehindBlockAmount, 0.0, computedLeftBlockAmount),
          currentLocation.clone().add(computedAheadBlockAmount, 0.0, computedRightBlockAmount)
        )
      case CardinalDirection.North =>
        (
          currentLocation
            .clone()
            .subtract(computedLeftBlockAmount, 0.0, computedAheadBlockAmount),
          currentLocation.clone().add(computedRightBlockAmount, 0.0, computedBehindBlockAmount)
        )
      case CardinalDirection.South =>
        (
          currentLocation
            .clone()
            .subtract(computedRightBlockAmount, 0.0, computedBehindBlockAmount),
          currentLocation.clone().add(computedLeftBlockAmount, 0.0, computedAheadBlockAmount)
        )
      case CardinalDirection.West =>
        (
          currentLocation
            .clone()
            .subtract(computedAheadBlockAmount, 0.0, computedRightBlockAmount),
          currentLocation.clone().add(computedBehindBlockAmount, 0.0, computedLeftBlockAmount)
        )
    }

    RegionSelection(startPosition, endPosition)
  }

  import cats.implicits._

  override def tryCreateRegion(player: Player): F[Unit] = for {
    regionCount <- regionCountRepository(player).get
    regionCreateResult <- Sync[F].delay {
      WorldEditWrapper
        .getSelection(player)
        .map { selection =>
          val regionName = s"${player.getName}_${regionCount.value}"

          WorldGuardWrapper.tryCreateRegion(
            regionName,
            player,
            player.getWorld,
            selection.getNativeMinimumPoint.toBlockVector,
            selection.getNativeMaximumPoint.toBlockVector
          )
        }
        .getOrElse(())
    }
    _ <- regionCountRepository(player).update(_.increment)
  } yield regionCreateResult

  override def canCreateRegion(
                                player: Player,
                                regionUnits: SubjectiveRegionShape,
                                direction: CardinalDirection
  ): F[CreateRegionResult] = {
    val selection = WorldEditWrapper.getSelection(player)
    for {
      regionCount <- regionCountRepository(player).get
      world <- Sync[F].delay(player.getWorld)
      wgManager = WorldGuardWrapper.getRegionManager(world)
      result <-
        if (!SeichiAssist.seichiAssistConfig.isGridProtectionEnabled(world)) {
          Sync[F].pure(CreateRegionResult.ThisWorldRegionCanNotBeCreated)
        } else if (selection.isEmpty || wgManager.isEmpty) {
          Sync[F].pure(CreateRegionResult.RegionCanNotBeCreatedByOtherError)
        } else {
          Sync[F].delay {
            val regions = WorldGuardWrapper.getApplicableRegionCount(
              world,
              s"${player.getName}_${regionCount.value}",
              selection.get.getNativeMinimumPoint.toBlockVector,
              selection.get.getNativeMaximumPoint.toBlockVector
            )
            if (regions != 0) {
              CreateRegionResult.RegionCanNotBeCreatedByOtherError
            } else {
              val maxRegionCount = WorldGuardWrapper.getMaxRegionCount(player, world)
              val regionCountPerPlayer = WorldGuardWrapper.getRegionCountOfPlayer(player, world)

              if (maxRegionCount >= 0 && regionCountPerPlayer >= maxRegionCount) {
                CreateRegionResult.RegionCanNotBeCreatedByOtherError
              } else {
                CreateRegionResult.Success
              }
            }
          }
        }
    } yield result

  }
}
