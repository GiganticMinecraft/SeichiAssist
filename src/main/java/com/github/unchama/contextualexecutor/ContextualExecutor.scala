package com.github.unchama.contextualexecutor

import org.bukkit.command.{Command, CommandSender, TabExecutor}

/**
 * コマンド実行時に[TabExecutor]へ渡される情報をラップした[RawCommandContext]を用いて処理を行うオブジェクトへのinterface.
 */
trait ContextualExecutor {

  /**
   * [rawContext] に基づいて, 作用を発生させる.
   *
   * このメソッドは**サーバーメインスレッド上のコルーチンで実行する必要性はない**.
   * また, 実行時例外が発生することはない.
   */
  def executeWith(rawContext: RawCommandContext)

  /**
   * [context] に基づいてTab補完の候補をListで返却する.
   */
  def tabCandidatesFor(context: RawCommandContext): List[String] = null
}

object ContextualExecutor {

  implicit class ContextualTabExecutor(val contextualExecutor: ContextualExecutor) {
    /**
     * この[ContextualExecutor]を[TabExecutor]オブジェクトへ変換する.
     *
     * この関数から得られる[TabExecutor]は[ContextualExecutor.executeWith]を非同期スレッドから発火するため,
     * 同期的な実行を期待する場合には[ContextualExecutor.executeWith]側で実行するコンテキストを指定せよ.
     */
    def asNonBlockingTabExecutor(): TabExecutor = new TabExecutor {
      override def onCommand(sender: CommandSender, command: Command, alias: String, args: Array[String]): Boolean = {
        val context = RawCommandContext(sender, ExecutedCommand(command, alias), args.toList())

        unsafe {
          runNonBlocking({
            fx {
              !effect {
                executeWith(context)
              }
            }
          }) {
            when(it) {
              is Either
              .Left -> it.a.printStackTrace()
            }
          }
        }

        // 非同期の操作を含むことを前提とするため, Bukkitへのコマンドの成否を必ず成功扱いにする
        return true
      }

      override def onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array[String]): List[String] = {
        val context = RawCommandContext (sender, ExecutedCommand (command, alias), args.toList)

        tabCandidatesFor (context)
      }
    }
  }
}