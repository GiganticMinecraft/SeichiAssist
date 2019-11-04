package com.github.unchama.contextualexecutor

import cats.effect.IO
import org.bukkit.command.{Command, CommandSender, TabExecutor}

/**
 * コマンド実行時に[TabExecutor]へ渡される情報をラップした[RawCommandContext]を用いて処理を行うオブジェクトへのtrait.
 */
trait ContextualExecutor {
  /**
   * [rawContext] に基づいて, 作用を計算する.
   *
   * 計算された作用はサーバーメインスレッド以外のコンテキストで実行されても良い.
   */
  // TODO rename to "executionWith"
  def executeWith(commandContext: RawCommandContext): IO[Unit]

  /**
   * [context] に基づいてTab補完の候補をListで返却する.
   */
  def tabCandidatesFor(context: RawCommandContext): List[String] = Nil
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
        val context = RawCommandContext(sender, ExecutedCommand(command, alias), args.toList)

        contextualExecutor.executeWith(context).unsafeRunAsync {
          case Left(error) =>
            println(s"Caught exception while executing ${command.getName} command.")
            error.printStackTrace()
          case Right(_) =>
        }

        // 非同期の操作を含むことを前提とするため, Bukkitへのコマンドの成否を必ず成功扱いにする
        true
      }

      import scala.jdk.CollectionConverters._

      override def onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array[String]): java.util.List[String] = {
        val context = RawCommandContext(sender, ExecutedCommand(command, alias), args.toList)

        contextualExecutor.tabCandidatesFor(context)
        }.asJava
    }
  }

}