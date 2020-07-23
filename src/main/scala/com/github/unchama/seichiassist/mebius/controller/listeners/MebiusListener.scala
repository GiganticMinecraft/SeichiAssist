package com.github.unchama.seichiassist.mebius.controller.listeners

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// TODO cleanup
object MebiusListener {
  private val defaultMebiusName = "MEBIUS"
  private val displayNamePrefix = s"$RESET$GOLD$BOLD"

  /** Mebiusを装備しているか */
  def isEquip(player: Player): Boolean = ItemStackMebiusCodec.isMebius(player.getInventory.getHelmet)

  /** MebiusのDisplayNameを取得 */
  def getName(mebius: ItemStack): String = {
    displayNamePrefix +
      ItemStackMebiusCodec.decodeMebiusProperty(mebius)
        .map(_.mebiusName)
        .getOrElse(defaultMebiusName)
  }

  /** MebiusのDisplayNameを設定 */
  def setName(player: Player, name: String): Boolean = {
    val updatedProperty = ItemStackMebiusCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .map {
        _.copy(mebiusName = name)
      }

    updatedProperty.foreach { newProperty =>
      player.sendMessage(s"$displayNamePrefix$name${RESET}に命名しました。")
      player.getInventory.setHelmet(ItemStackMebiusCodec.materialize(newProperty))
      SeichiAssist.playermap.apply(player.getUniqueId).mebius
        .speakForce(s"わーい、ありがとう！今日から僕は$displayNamePrefix$name${RESET}だ！")
    }

    updatedProperty.nonEmpty
  }

  def setNickname(player: Player, name: String): Boolean = {
    val updatedProperty = ItemStackMebiusCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .map {
        _.copy(ownerNickname = Some(name))
      }

    updatedProperty.foreach { newProperty =>
      player.getInventory.setHelmet(ItemStackMebiusCodec.materialize(newProperty))
      SeichiAssist.playermap.apply(player.getUniqueId).mebius
        .speakForce(s"わーい、ありがとう！今日から君のこと$GREEN$name${RESET}って呼ぶね！")
    }

    updatedProperty.nonEmpty
  }

  def getNickname(player: Player): Option[String] =
    ItemStackMebiusCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .map(_.ownerNickname.getOrElse(player.getDisplayName))
}
