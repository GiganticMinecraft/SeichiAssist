package com.github.unchama.seichiassist.data.subhome

import org.bukkit.Location

case class SubHome(location: Location, name: String) {
  def getLocation: Location = location.clone // BukkitのLocationはミュータブルなのでコピーして返す必要がある
}
