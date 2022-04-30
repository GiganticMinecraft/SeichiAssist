package com.github.unchama.seichiassist.commands.legacy

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.util.ActionStatus.Fail
import org.bukkit.ChatColor._
import org.bukkit.command.{Command, CommandExecutor, CommandSender}

class DonationCommand extends CommandExecutor {
  override def onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array[String]
  ): Boolean = {
    def printRecordCommandUsage(): Unit = {
      sender.sendMessage(
        Array(s"$RED/donation record <プレイヤー名> <ポイント数>", "寄付者用プレミアムエフェクトポイント配布コマンドです(マルチ鯖対応済)")
      )
    }

    def printHelp(): Unit = {
      printRecordCommandUsage()
    }

    val databaseGateway: DatabaseGateway = SeichiAssist.databaseGateway

    if (args.length == 0) {
      printHelp()
    } else if (args(0).equalsIgnoreCase("record")) {
      if (args.length != 3) {
        // 引数が3でない時の処理
        printRecordCommandUsage()
      } else {
        // 引数が3の時の処理

        val name = args(1).toLowerCase
        // 配布ポイント数取得
        val num = args(2).toInt

        if (databaseGateway.donateDataManipulator.addDonate(name, num) eq Fail)
          sender.sendMessage(s"${RED}失敗")
        else
          sender.sendMessage(s"${GREEN}成功")
      }
    } else {
      printHelp()
    }

    true
  }
}
