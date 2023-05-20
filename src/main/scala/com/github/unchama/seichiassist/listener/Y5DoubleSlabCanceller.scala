package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.ManagedWorld._
import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.{EventHandler, Listener}

object Y5DoubleSlabCanceller extends Listener {

  /**
   * 以下の条件をすべて満たすときにブロックの設置をキャンセルし、その旨を示すメッセージを送出する
   *   - プレイヤーが対象座標にブロックを設置できる
   *   - プレイヤーが手に持っているブロックが焼き石のハーフブロックである
   *   - 対象座標の改変後ブロックが焼き石の二段重ねハーフブロックである
   *   - 対象座標が整地ワールドを指している
   *   - 対象ブロックのY座標が5である
   * @see
   *   https://github.com/GiganticMinecraft/SeichiAssist/issues/775
   * @param event
   *   対象イベント
   */
  @EventHandler
  def onPlaceDoubleSlabAtY5(event: BlockPlaceEvent): Unit = {
    if (!event.canBuild) return
    if (event.getItemInHand.getType ne Material.STEP) return
    if (event.getItemInHand.getDurability != 0) return
    if (event.getBlockPlaced.getType ne Material.DOUBLE_STEP) return
    if (event.getBlockPlaced.getData != 0) return
    if (!event.getBlockPlaced.getWorld.isSeichi) return
    if (event.getBlockPlaced.getY != 5) return
    event.setCancelled(true)
    event.getPlayer.sendMessage("Y5のハーフブロックを二段重ねにすることはできません。")
  }
}
