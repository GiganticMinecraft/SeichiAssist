package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent

class PlayerPickupItemListener : Listener {
  internal var playermap = SeichiAssist.playermap
  private val config = SeichiAssist.seichiAssistConfig

  @EventHandler
  fun onPickupMineStackItem(event: PlayerPickupItemEvent) {
    val player = event.player

    if (player.gameMode != GameMode.SURVIVAL) return

    val playerData = playermap[player.uniqueId] ?: return

    if (playerData.level < config.getMineStacklevel(1)) return

    if (!playerData.minestackflag) return

    val item = event.item
    val itemstack = item.itemStack
    val amount = itemstack.amount
    val material = itemstack.type
    if (SeichiAssist.DEBUG) {
      player.sendMessage(ChatColor.RED.toString() + "pick:" + itemstack.toString())
      player.sendMessage(ChatColor.RED.toString() + "pickDurability:" + itemstack.durability)
    }

    var i = 0
    while (i < MineStackObjectList.minestacklist!!.size) {
      val mineStackObj = MineStackObjectList.minestacklist!![i]
      if (material == mineStackObj.material && itemstack.durability.toInt() == mineStackObj.durability) {
        //この時点でIDとサブIDが一致している
        if (!mineStackObj.hasNameLore && !itemstack.itemMeta.hasLore() && !itemstack.itemMeta.hasDisplayName()) {//名前と説明文が無いアイテム
          if (playerData.level < config.getMineStacklevel(mineStackObj.level)) {
            //レベルを満たしていない
            return
          } else {
            playerData.minestack.addStackedAmountOf(mineStackObj, amount.toLong())
            break
          }
        } else if (mineStackObj.hasNameLore && itemstack.itemMeta.hasDisplayName() && itemstack.itemMeta.hasLore()) {
          //名前・説明文付き
          val meta = itemstack.itemMeta
          //この時点で名前と説明文がある
          if (mineStackObj.gachaType == -1) { //ガチャ以外のアイテム(がちゃりんご)
            if (meta.displayName != StaticGachaPrizeFactory.getGachaRingoName() || meta.lore != StaticGachaPrizeFactory.getGachaRingoLore()) {
              return
            }
            if (playerData.level < config.getMineStacklevel(mineStackObj.level)) {
              //レベルを満たしていない
              return
            } else {
              playerData.minestack.addStackedAmountOf(mineStackObj, amount.toLong())
              break
            }
          } else {
            //ガチャ品
            val g = SeichiAssist.msgachadatalist[mineStackObj.gachaType]
            val name = playerData.name //プレイヤーのネームを見る
            if (g.probability < 0.1) { //カタログギフト券を除く(名前があるアイテム)
              if (!Util.ItemStackContainsOwnerName(itemstack, name)) {
                //所有者の名前が無ければreturn
                return
              }
            }

            if (g.itemStackEquals(itemstack)) { //中身が同じ場合のみここに入る
              if (playerData.level < config.getMineStacklevel(mineStackObj.level)) {
                return
              } else {
                playerData.minestack.addStackedAmountOf(mineStackObj, amount.toLong())
                break
              }
            }
          }
        }
      }
      i++
    }

    if (i == MineStackObjectList.minestacklist!!.size) {
      return
    }

    event.isCancelled = true
    player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
    item.remove()
  }
}
