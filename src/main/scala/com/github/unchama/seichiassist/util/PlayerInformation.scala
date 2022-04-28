package com.github.unchama.seichiassist.util

import org.bukkit.entity.Player

object PlayerInformation {
  def getPlayerDirection(player: Player): AbsoluteDirection = {
    var rotation = ((player.getLocation.getYaw + 180) % 360).toDouble

    if (rotation < 0) rotation += 360.0

    // 0,360:south 90:west 180:north 270:east
    if (0.0 <= rotation && rotation < 45.0) AbsoluteDirection.NORTH
    else if (45.0 <= rotation && rotation < 135.0) AbsoluteDirection.EAST
    else if (135.0 <= rotation && rotation < 225.0) AbsoluteDirection.SOUTH
    else if (225.0 <= rotation && rotation < 315.0) AbsoluteDirection.WEST
    else AbsoluteDirection.NORTH
  }
}
