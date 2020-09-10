package com.github.unchama.seichiassist.data.subhome

import org.bukkit.Location
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable


/**
 * サブホームオブジェクトのクラス
 */
// TODO Scalaize and make this case class
class SubHome(val location: Location, val name: String) {
  def getLocation: Location = location.clone // BukkitのLocationはミュータブルなのでコピーして返す必要がある
}
