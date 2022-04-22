package com.github.unchama.seichiassist.subsystems.subhome.bukkit

import com.github.unchama.seichiassist.subsystems.subhome.domain.SubHomeLocation
import org.bukkit.{Bukkit, Location}

/**
 * [[org.bukkit.Location]] と [[SubHomeLocation]] との相互変換を実現するコーデック。
 *
 * [[SubHomeLocation]] は [[org.bukkit.Location]] よりも真に情報量が少ない(ワールドへの参照を持っていない)ため、
 * [[SubHomeLocation]] から [[org.bukkit.Location]] への変換は [[Option]] として返ってくる。
 */
object LocationCodec {

  def fromBukkitLocation(location: Location): SubHomeLocation = {
    SubHomeLocation(
      location.getWorld.getName,
      location.getX,
      location.getY,
      location.getZ,
      location.getPitch,
      location.getYaw
    )
  }

  def toBukkitLocation(location: SubHomeLocation): Option[Location] = {
    val world = Bukkit.getWorld(location.worldName)

    Option.when(world != null)(
      new Location(world, location.x, location.y, location.z, location.yaw, location.pitch)
    )
  }

}
