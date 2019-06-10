package com.github.unchama.contextualexecutor

import arrow.effects.extensions.io.fx.fx
import arrow.effects.extensions.io.unsafeRun.runNonBlocking
import arrow.unsafe
import com.github.unchama.messaging.MessageToSender
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

/**
 * コマンド実行時に[TabExecutor]へ渡される情報をラップした[RawCommandContext]を用いて処理を行うオブジェクトへのinterface.
 */
interface ContextualExecutor {

  /**
   * [rawContext] に基づいて, 作用を発生させる.
   *
   * このメソッドは**サーバーメインスレッド上のコルーチンで実行する必要性はない**.
   * また, 実行時例外が発生することはない.
   *
   * @return 発生させた作用に対応する[MessageToSender]. 作用の発生源である[CommandSender]にこのオブジェクトが転送されることが想定されている.
   */
  suspend fun executeWith(rawContext: RawCommandContext): MessageToSender

  /**
   * [context] に基づいてTab補完の候補をListで返却する.
   */
  fun tabCandidatesFor(context: RawCommandContext): List<String>? = null

}

/**
 * この[ContextualExecutor]を[TabExecutor]オブジェクトへ変換する.
 *
 * この関数から得られる[TabExecutor]は[ContextualExecutor.executeWith]を非同期スレッドから発火するため,
 * 同期的な実行を期待する場合には[ContextualExecutor.executeWith]側で実行するコンテキストを指定せよ.
 */
fun ContextualExecutor.asNonBlockingTabExecutor(): TabExecutor = object : TabExecutor {
  override fun onCommand(sender: CommandSender, command: Command, alias: String, args: Array<out String>): Boolean {
    val context = RawCommandContext(sender, ExecutedCommand(command, alias), args.toList())

    unsafe {
      runNonBlocking({
        fx {
          !effect {
            val message = executeWith(context)

            message.transmitTo(sender)
          }
        }
      }) { }
    }

    // 非同期の操作を含むことを前提とするため, Bukkitへのコマンドの成否を必ず成功扱いにする
    return true
  }

  override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
    val context = RawCommandContext(sender, ExecutedCommand(command, alias), args.toList())

    return tabCandidatesFor(context)
  }
}
