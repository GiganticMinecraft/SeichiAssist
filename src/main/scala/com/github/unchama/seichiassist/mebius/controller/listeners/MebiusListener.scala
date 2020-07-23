package com.github.unchama.seichiassist.mebius.controller.listeners

import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

// TODO cleanup
object MebiusListener {
  private val defaultMebiusName = "MEBIUS"
  private val displayNamePrefix = s"$RESET$GOLD$BOLD"

  /** MebiusのDisplayNameを取得 */
  def getName(mebius: ItemStack): String = {
    displayNamePrefix +
      ItemStackMebiusCodec.decodeMebiusProperty(mebius)
        .map(_.mebiusName)
        .getOrElse(defaultMebiusName)
  }
}
