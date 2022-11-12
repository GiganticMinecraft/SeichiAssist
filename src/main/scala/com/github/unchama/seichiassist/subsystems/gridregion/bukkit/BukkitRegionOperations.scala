package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  Direction,
  RegionOperations,
  RegionSelection,
  RegionUnits
}
import com.github.unchama.util.external.ExternalPlugins
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.bukkit.commands.AsyncCommandHelper
import com.sk89q.worldguard.bukkit.commands.task.RegionAdder
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.util.DomainInputResolver
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitRegionOperations[F[_]: Sync] extends RegionOperations[F, Location, Player] {

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
          currentLocation.subtract(
            regionUnits.behind.unitPerBlockAmount,
            0.0,
            regionUnits.left.unitPerBlockAmount
          ),
          currentLocation.add(
            regionUnits.ahead.unitPerBlockAmount,
            0.0,
            regionUnits.right.unitPerBlockAmount
          )
        )
      case Direction.North =>
        (
          currentLocation.subtract(
            regionUnits.left.unitPerBlockAmount,
            0.0,
            regionUnits.ahead.unitPerBlockAmount
          ),
          currentLocation.add(
            regionUnits.right.unitPerBlockAmount,
            0.0,
            regionUnits.behind.unitPerBlockAmount
          )
        )
      case Direction.South =>
        (
          currentLocation.subtract(
            regionUnits.right.unitPerBlockAmount,
            0.0,
            regionUnits.behind.unitPerBlockAmount
          ),
          currentLocation.add(
            regionUnits.left.unitPerBlockAmount,
            0.0,
            regionUnits.ahead.unitPerBlockAmount
          )
        )
      case Direction.West =>
        (
          currentLocation.subtract(
            regionUnits.ahead.unitPerBlockAmount,
            0.0,
            regionUnits.right.unitPerBlockAmount
          ),
          currentLocation.add(
            regionUnits.behind.unitPerBlockAmount,
            0.0,
            regionUnits.left.unitPerBlockAmount
          )
        )
    }

    RegionSelection(startPosition, endPosition)
  }

  override def createRegion(player: Player): F[Unit] = Sync[F].delay {
    val we: WorldEditPlugin = ExternalPlugins.getWorldEdit
    val wg: WorldGuardPlugin = ExternalPlugins.getWorldGuard

    val selection = we.getSelection(player)

    val region = new ProtectedCuboidRegion(
      s"${player.getName}_1", // TODO: regionCountをRepositoryにする
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
      .formatUsing(s"${player.getName}_1")
      .registerWithSupervisor("保護申請中")
      .thenRespondWith("保護申請完了。保護名: '%s'", "保護作成失敗")
  }

}
