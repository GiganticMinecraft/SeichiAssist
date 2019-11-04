package com.github.unchama.seichiassist.text

import com.github.unchama.seichiassist.ManagedWorld
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

/**
 * Created by karayuu on 2019/04/30
 *
 * @param player 警告を表示する対象
 */
class WarningsGenerator(player: Player) {
  /**
   * 整地ワールド以外では建築量・ガチャ券が増加しないという警告.
   */
  val noRewardsOutsideSeichiWorld: List[String] =
    if (ManagedWorld.fromBukkitWorld(player.getWorld).exists(_.isSeichi))
      Nil
    else
      List(
        s"${RED}整地ワールド以外では",
        s"${RED}整地量とガチャ券は増えません"
      )
}
