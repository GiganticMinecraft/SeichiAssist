package com.github.unchama.seichiassist.commands.legacy

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor._
import org.bukkit.command.{Command, CommandExecutor, CommandSender}

class VoteCommand extends CommandExecutor {
  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    val databaseGateway: DatabaseGateway = SeichiAssist.databaseGateway

    def notifyRecordCommandUsage(): Unit = {
      sender.sendMessage(Array(
        s"$RED/vote record <プレイヤー名>",
        "投票特典配布用コマンドです(マルチ鯖対応済)"
      ))
    }

    def printHelp(): Unit = {
      notifyRecordCommandUsage()
    }

    if (args.length == 0) {
      printHelp()
    } else if (args(0).equalsIgnoreCase("record")) {
      if (args.length != 2) { //引数が2つでない時の処理
        notifyRecordCommandUsage()
      } else {
        //引数が2つの時の処理
        val lowerCasePlayerName = Util.getName(args(1))
        //プレイヤーオンライン、オフラインにかかわらずsqlに送信(マルチ鯖におけるコンフリクト防止の為)
        sender.sendMessage(s"$YELLOW${lowerCasePlayerName}の投票特典配布処理開始…")
        //mysqlにも書き込んどく
        databaseGateway.playerDataManipulator.incrementVotePoint(lowerCasePlayerName)

        if (databaseGateway.playerDataManipulator.addChainVote(lowerCasePlayerName))
          sender.sendMessage(s"${GREEN}連続投票数の記録に成功")
        else
          sender.sendMessage(s"${RED}連続投票数の記録に失敗")
      }
    } else {
      printHelp()
    }

    true
  }
}
