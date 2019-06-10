package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.messaging.EmptyMessage
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util
import kotlinx.coroutines.delay
import org.bukkit.ChatColor
import org.bukkit.Difficulty

object GiganticFeverCommand {
  val executor = ContextualExecutorBuilder.beginConfiguration()
      .execution {
        val config = SeichiAssist.config

        Util.sendEveryMessage("${ChatColor.AQUA}フィーバー！この時間MOBたちは踊りに出かけてるぞ！今が整地時だ！")
        Util.sendEveryMessage("${ChatColor.AQUA}(${config.giganticFeverDisplayTime}間)")

        Util.setDifficulty(SeichiAssist.seichiWorldList, Difficulty.PEACEFUL)

        delay(config.giganticFeverMinutes * 60L * 1000L)

        Util.setDifficulty(SeichiAssist.seichiWorldList, Difficulty.HARD)
        Util.sendEveryMessage("${ChatColor.AQUA}フィーバー終了！MOBたちは戻ってきたぞ！")

        EmptyMessage
      }
      .build()
      .asNonBlockingTabExecutor()
}
