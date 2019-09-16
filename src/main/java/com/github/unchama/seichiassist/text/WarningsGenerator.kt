package com.github.unchama.seichiassist.text

import com.github.unchama.seichiassist.asManagedWorld
import com.github.unchama.seichiassist.isSeichi
import org.bukkit.ChatColor.RED
import org.bukkit.entity.Player

/**
 * Created by karayuu on 2019/04/30
 * @param player 警告を表示する対象
 */
class WarningsGenerator(player: Player) {
  /**
   * 整地ワールド以外では建築量・ガチャ券が増加しないという警告.
   */
  val noRewardsOutsideSeichiWorld: List<String> =
    if (player.world.asManagedWorld()?.isSeichi == true)
      emptyList()
    else
      listOf(
          "${RED}整地ワールド以外では",
          "${RED}整地量とガチャ券は増えません"
      )
}
