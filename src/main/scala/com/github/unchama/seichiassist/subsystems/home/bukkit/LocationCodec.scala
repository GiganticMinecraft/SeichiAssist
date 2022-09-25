package com.github.unchama.seichiassist.subsystems.home.bukkit

import com.github.unchama.seichiassist.subsystems.home.domain.HomeLocation
import org.bukkit.{Bukkit, Location}

/**
 * [[org.bukkit.Location]] と [[HomeLocation]] との相互変換を実現するコーデック。
 *
 * [[HomeLocation]] は [[org.bukkit.Location]] よりも真に情報量が少ない(ワールドへの参照を持っていない)ため、
 * [[HomeLocation]] から [[org.bukkit.Location]] への変換は [[Option]] として返ってくる。
 */
object LocationCodec {

  def fromBukkitLocation(location: Location): HomeLocation = {
    HomeLocation(
      location.getWorld.getName,
      location.getX,
      location.getY,
      location.getZ,
      location.getPitch,
      location.getYaw
    )
  }

  def toBukkitLocation(location: HomeLocation): Option[Location] = {
    val world = Bukkit.getWorld(location.worldName)

    Option.when(world != null)(
      new Location(world, location.x, location.y, location.z, location.yaw, location.pitch)
    )
  }

}
