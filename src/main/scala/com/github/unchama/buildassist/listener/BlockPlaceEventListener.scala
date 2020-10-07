package com.github.unchama.buildassist.listener

import java.math.BigDecimal
import java.util.UUID

import com.github.unchama.buildassist.{BuildAssist, PlayerData, Util}
import com.github.unchama.seichiassist.ManagedWorld._
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent

import scala.collection.mutable

/**
 * Created by karayuu on 2020/10/07
 */
class BlockPlaceEventListener extends TypedEventListener[BlockPlaceEvent] {
  private val playermap: mutable.HashMap[UUID, PlayerData] = BuildAssist.playermap

  @EventHandler
  override def onEvent(event: BlockPlaceEvent): Unit = {
    val player = event.getPlayer
    if (!player.getWorld.isTrackedBuildBlockWorld) return

    //設置がキャンセルされていたら終了
    if (event.isCancelled) return

    val uuid = player.getUniqueId
    val playerdata = playermap.getOrElse(uuid, () => null)

    //プレイヤーデータが無い場合は処理終了
    if (playerdata == null) return

    Util.addBuild1MinAmount(player, BigDecimal.ONE)
  }
}
