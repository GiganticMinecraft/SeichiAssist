package com.github.unchama.buildassist

import com.github.unchama.seichiassist.util.TypeConverter
import org.bukkit.ChatColor
import org.bukkit.ChatColor._
import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import org.bukkit.entity.Player

final class FlyCommand extends CommandExecutor {
  override def onCommand(sender: CommandSender, cmd: Command, label: String, args: Array[String]): Boolean = {
    //プレイヤーからの送信でない時処理終了
    val player = sender match {
      case sender: Player => sender
      case _ =>
        sender.sendMessage(s"${GREEN}このコマンドはゲーム内から実行してください。")
        return true
    }

    if (args.length == 0) {
      sender.sendMessage(s"${GREEN}fly機能を利用したい場合は、末尾に「利用したい時間(分単位)」の数値を、")
      sender.sendMessage(s"${GREEN}fly機能を中断したい場合は、末尾に「finish」を記入してください。")
      return true
    }

    if (args.length == 1) {
      //UUIDを取得
      val uuid = player.getUniqueId

      //playerdataを取得
      val playerData = BuildAssist.playermap.getOrElse(uuid, return false)

      val expman = new ExperienceManager(player)
      var flytime = playerData.flytime
      val endlessFly = playerData.endlessfly
      val query = args(0)

      if (query.equalsIgnoreCase("finish")) {
        playerData.flyflag = false
        playerData.flytime = 0
        playerData.endlessfly = false
        player.setAllowFlight(false)
        player.setFlying(false)
        sender.sendMessage(s"${GREEN}fly効果を停止しました。")
      } else if (query.equalsIgnoreCase("endless")) {
        if (!expman.hasExp(BuildAssist.config.getFlyExp)) {
          sender.sendMessage(s"${GREEN}所持している経験値が、必要経験値量(${BuildAssist.config.getFlyExp})に達していません。")
        } else {
          playerData.flyflag = true
          playerData.endlessfly = true
          playerData.flytime = 0
          player.setAllowFlight(true)
          player.setFlying(true)
          sender.sendMessage(s"${GREEN}無期限でfly効果をONにしました。")
        }
      } else if (TypeConverter.isParsableToInteger(query)) {
        val minutes = query.toInt
        if (minutes <= 0) {
          sender.sendMessage(s"${GREEN}時間指定の数値は「1」以上の整数で行ってください。")
          return true
        } else if (!expman.hasExp(BuildAssist.config.getFlyExp)) {
          sender.sendMessage(s"${GREEN}所持している経験値が、必要経験値量(${BuildAssist.config.getFlyExp})に達していません。")
        } else {
          if (endlessFly) {
            sender.sendMessage(s"${GREEN}無期限飛行モードは解除されました。")
          }

          flytime += minutes
          playerData.flyflag = true
          playerData.flytime = flytime
          playerData.endlessfly = false
          sender.sendMessage(s"$YELLOW【flyコマンド認証】効果の残り時間はあと${flytime}分です。")
          player.setAllowFlight(true)
          player.setFlying(true)
        }
      } else {
        sender.sendMessage(s"${GREEN}fly機能を利用したい場合は、末尾に「利用したい時間(分単位)」の数値を、")
        sender.sendMessage(s"${GREEN}fly機能を中断したい場合は、末尾に「finish」を記入してください。")
      }
      return true
    }
    false
  }
}