package com.github.unchama.seichiassist.mebius.controller.listeners

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// TODO cleanup
object MebiusListener {
  private val defaultMebiusName = "MEBIUS"
  private val displayNamePrefix = s"$RESET$GOLD$BOLD"

  // PlayerData取得
  private def getPlayerData(player: Player) = SeichiAssist.playermap.apply(player.getUniqueId)

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
    if (isEquip(player)) {
      val mebius = player.getInventory.getHelmet
      val meta = mebius.getItemMeta
      meta.setDisplayName(s"$displayNamePrefix$name")
      player.sendMessage(s"${getName(mebius)}${RESET}に命名しました。")
      mebius.setItemMeta(meta)
      player.getInventory.setHelmet(mebius)
      getPlayerData(player).mebius.speakForce(s"わーい、ありがとう！今日から僕は$displayNamePrefix$name${RESET}だ！")
      return true
    }
    false
  }

  def setNickname(player: Player, name: String): Boolean =
    if (!isEquip(player)) {
      false
    } else {
      val mebius = player.getInventory.getHelmet
      val nbtItem = new NBTItem(mebius)
      nbtItem.setString("nickname", name)
      player.getInventory.setHelmet(nbtItem.getItem)
      getPlayerData(player).mebius.speakForce(s"わーい、ありがとう！今日から君のこと$GREEN$name${RESET}って呼ぶね！")
      true
    }

  // FIXME あの！ここはListenerクラスですよ！！
  def getNickname(player: Player): String =
    ItemStackMebiusCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .map(_.ownerNickname.getOrElse(player.getDisplayName))
      .orNull
}
