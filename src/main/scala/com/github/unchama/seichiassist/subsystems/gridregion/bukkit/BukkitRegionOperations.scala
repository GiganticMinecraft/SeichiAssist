package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gridregion.domain._
import com.github.unchama.util.external.WorldGuardWrapper
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.bukkit.commands.AsyncCommandHelper
import com.sk89q.worldguard.bukkit.commands.task.RegionAdder
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.util.DomainInputResolver
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitRegionOperations[F[_]: Sync](
  implicit we: WorldEditPlugin,
  wg: WorldGuardPlugin,
  regionCountRepository: KeyedDataRepository[Player, Ref[F, RegionCount]]
) extends RegionOperations[F, Location, Player] {

  override def getSelection(
    currentLocation: Location,
    regionUnits: RegionUnits,
    direction: Direction
  ): RegionSelection[Location] = {

    /*
     * startPosition - 北西
     * endPosition - 南東
     * に合わせる
     */
    val (startPosition, endPosition) = direction match {
      case Direction.East =>
        (
          currentLocation
            .clone()
            .subtract(
              regionUnits.behind.computeBlockAmount,
              0.0,
              regionUnits.left.computeBlockAmount
            ),
          currentLocation
            .clone()
            .add(
              regionUnits.ahead.computeBlockAmount,
              0.0,
              regionUnits.right.computeBlockAmount
            )
        )
      case Direction.North =>
        (
          currentLocation
            .clone()
            .subtract(
              regionUnits.left.computeBlockAmount,
              0.0,
              regionUnits.ahead.computeBlockAmount
            ),
          currentLocation
            .clone()
            .add(
              regionUnits.right.computeBlockAmount,
              0.0,
              regionUnits.behind.computeBlockAmount
            )
        )
      case Direction.South =>
        (
          currentLocation
            .clone()
            .subtract(
              regionUnits.right.computeBlockAmount,
              0.0,
              regionUnits.behind.computeBlockAmount
            ),
          currentLocation
            .clone()
            .add(regionUnits.left.computeBlockAmount, 0.0, regionUnits.ahead.computeBlockAmount)
        )
      case Direction.West =>
        (
          currentLocation
            .clone()
            .subtract(
              regionUnits.ahead.computeBlockAmount,
              0.0,
              regionUnits.right.computeBlockAmount
            ),
          currentLocation
            .clone()
            .add(
              regionUnits.behind.computeBlockAmount,
              0.0,
              regionUnits.left.computeBlockAmount
            )
        )
    }

    RegionSelection(startPosition, endPosition)
  }

  import cats.implicits._

  override def createRegion(player: Player): F[Unit] = for {
    regionCount <- regionCountRepository(player).get
    _ <- Sync[F].delay {
      val selection = we.getSelection(player)
      val regionName = s"${player.getName}_${regionCount.value}"

      val region = new ProtectedCuboidRegion(
        regionName,
        selection.getNativeMinimumPoint.toBlockVector,
        selection.getNativeMaximumPoint.toBlockVector
      )
      val manager = wg.getRegionManager(player.getWorld)

      val task = new RegionAdder(wg, manager, region)
      task.setLocatorPolicy(DomainInputResolver.UserLocatorPolicy.UUID_ONLY)
      task.setOwnersInput(Array(player.getName))
      val future = wg.getExecutorService.submit(task)

      AsyncCommandHelper
        .wrap(future, wg, player)
        .formatUsing(regionName)
        .registerWithSupervisor("保護申請中")
        .thenRespondWith("保護申請完了。保護名: '%s'", "保護作成失敗")
    }
    _ <- regionCountRepository(player).update(_.increment)
  } yield ()

  override def canCreateRegion(
    player: Player,
    regionUnits: RegionUnits,
    direction: Direction
  ): F[CreateRegionResult] = {
    val selection = Option(we.getSelection(player))
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
