package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.application.actions

import com.github.unchama.seichiassist._
import com.github.unchama.targetedeffect.player.ActionBarMessageEffect
import com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain.BreakSkillTriggerConfigKey
import org.bukkit.ChatColor.RED
import org.bukkit.entity.Player

object BreakSkillTriggerSettings{

   /**
   * ブロック破壊時、「マナ切れブロック破壊停止設定」を取得する。
   * @param player マナ切れブロック破壊停止設定を取得するプレイヤー
   */
  def isBreakBlockManaFullyConsumed(
    player: Player, 
    ): Boolean = {

    val isBreakBlockManaFullyConsumed = SeichiAssist
      .instance
      .breakSkillTriggerConfigSystem
      .api
      .breakSkillTriggerConfig(player, BreakSkillTriggerConfigKey.ManaFullyConsumed)
      .unsafeRunSync()
    
      if(isBreakBlockManaFullyConsumed){
        ActionBarMessageEffect(s"${RED}マナ切れでブロック破壊を止めるスキルは有効化されています").run(player).unsafeRunSync()
      }
      isBreakBlockManaFullyConsumed
  }
}