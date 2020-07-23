package com.github.unchama.seichiassist.mebius.controller.listeners

import com.github.unchama.seichiassist.SeichiAssist
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// TODO cleanup
object MebiusListener {

  import scala.jdk.CollectionConverters._

  /** 初期の名前 */
  private val DEFNAME = "MEBIUS"

  private val LOREFIRST2 = List(
    s"$RESET",
    s"$RESET${ChatColor.AQUA}初心者をサポートする不思議なヘルメット。",
    s"$RESET${ChatColor.AQUA}整地により成長する。",
    ""
  )
  private val LV = 4
  private val NAMEHEAD = s"$RESET${ChatColor.GOLD}${ChatColor.BOLD}"
  private val ILHEAD = s"$RESET$RED${ChatColor.BOLD}アイテムLv. "

  // PlayerData取得
  private def getPlayerData(player: Player) = SeichiAssist.playermap.apply(player.getUniqueId)

  // ItemStackがMebiusか
  private def isMebius(item: ItemStack): Boolean = {
    val meta = item.getItemMeta

    meta.hasLore && {
      val lore = meta.getLore.asScala
      LOREFIRST2.forall(lore.contains)
    }
  }

  /** Mebiusを装備しているか */
  def isEquip(player: Player): Boolean =
    try isMebius(player.getInventory.getHelmet)
    catch {
      case _: NullPointerException => false
    }

  /** MebiusのDisplayNameを設定 */
  def setName(player: Player, name: String): Boolean = {
    if (isEquip(player)) {
      val mebius = player.getInventory.getHelmet
      val meta = mebius.getItemMeta
      meta.setDisplayName(s"$NAMEHEAD$name")
      player.sendMessage(s"${getName(mebius)}${RESET}に命名しました。")
      mebius.setItemMeta(meta)
      player.getInventory.setHelmet(mebius)
      getPlayerData(player).mebius.speakForce(s"わーい、ありがとう！今日から僕は$NAMEHEAD$name${RESET}だ！")
      return true
    }
    false
  }

  /** MebiusのDisplayNameを取得 */
  def getName(mebius: ItemStack): String = {
    try if (isMebius(mebius)) return mebius.getItemMeta.getDisplayName
    catch {
      case _: NullPointerException =>
    }
    s"$NAMEHEAD$DEFNAME"
  }

  def setNickname(player: Player, name: String): Boolean = if (!isEquip(player)) false
  else {
    val mebius = player.getInventory.getHelmet
    val nbtItem = new NBTItem(mebius)
    nbtItem.setString("nickname", name)
    player.getInventory.setHelmet(nbtItem.getItem)
    getPlayerData(player).mebius.speakForce("わーい、ありがとう！今日から君のこと" + ChatColor.GREEN + name + RESET + "って呼ぶね！")
    true
  }

  // FIXME あの！ここはListenerクラスですよ！！
  def getNickname(player: Player): String =
    if (!isEquip(player)) null
    else {
      val mebius = player.getInventory.getHelmet
      val nbtItem = new NBTItem(mebius)
      if (nbtItem.getString("nickname").isEmpty) {
        nbtItem.setString("nickname", player.getName)
        player.getName
      }
      else nbtItem.getString("nickname")
    }

  // MebiusのLvを取得
  def getMebiusLevel(mebius: ItemStack): Int = {
    mebius.getItemMeta.getLore.get(LV).replace(ILHEAD, "").toInt
  }

}
