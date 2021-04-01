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