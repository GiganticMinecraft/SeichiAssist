package com.github.unchama.seichiassist.subsystems.subhome.domain

import org.bukkit.Location

/**
 * サブホームオブジェクトのクラス
 */
case class SubHome(location: Location, name: String) {
  def getLocation = { // BukkitのLocationはミュータブルなのでコピーして返す必要がある
    location.clone
  }
}

case class SubHomeLocation(worldName: String, x: Int, y: Int, z: Int)

/**
 * サブホームオブジェクトのクラス
 */
case class SubHomeV2(name: String, location: SubHomeLocation)

object SubHome {
  type ID = Int
}