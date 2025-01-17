package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.ManagedWorld._
import org.bukkit.block.data.`type`.Slab
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.{EventHandler, Listener}

object YMinus59DoubleSlabCanceller extends Listener {

  /**
   * 以下の条件をすべて満たすときにブロックの設置をキャンセルし、その旨を示すメッセージを送出する
   *   - プレイヤーが対象座標にブロックを設置できる
   *   - プレイヤーが手に持っているブロックが焼き石のハーフブロックである
   *   - 対象座標の改変後ブロックが焼き石の二段重ねハーフブロックである
   *   - 対象座標が整地ワールドを指している
   *   - 対象ブロックのY座標が-59である
   * @see
   *   https://github.com/GiganticMinecraft/SeichiAssist/issues/775
   * @param event
   *   対象イベント
   */
  @EventHandler
  def onPlaceDoubleSlabAtYMinus59(event: BlockPlaceEvent): Unit = {
    if (!event.canBuild) return
    if (!event.getBlockPlaced.getWorld.isSeichi) return
    if (event.getBlockPlaced.getY != -59) return
    event.getBlockPlaced.getBlockData match {
      case slab: Slab if slab.getType == Slab.Type.DOUBLE =>
        event.setCancelled(true)
        event.getPlayer.sendMessage("Y-59のハーフブロックを二段重ねにすることはできません。")
      case _ =>
    }
  }
}
