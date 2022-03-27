package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ParserResponse.{failWith, succeedWith}
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.menus.minestack.CategorizedMineStackMenu
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.command.TabExecutor

object MsCommand {

  def executor(
    implicit categorizedMineStackMenuEnvironment: CategorizedMineStackMenu.Environment,
    layoutPreparationContext: LayoutPreparationContext
  ): TabExecutor = playerCommandBuilder
    .argumentsParsers(
      List(
        Parsers.identity,
        category => {
          category.toIntOption match {
            case Some(categoryValue) =>
              MineStackObjectCategory.fromSerializedValue(categoryValue) match {
                case Some(_) => succeedWith(categoryValue)
                case None    => failWith("指定されたカテゴリは存在しません。")
              }
            case None => failWith("カテゴリは数字で入力してください。")
          }
        }
      )
    )
    .execution { context =>
      val args = context.args.parsed
      if (args.length != 2) {
        IO(MessageEffect("不正な引数です。"))
      } else if (args(1).toString.toIntOption.isEmpty) {
        IO(MessageEffect("ページの指定が不正です。"))
      }
      val category =
        MineStackObjectCategory.fromSerializedValue(args.head.asInstanceOf[Int]).get
      IO.pure(CategorizedMineStackMenu(category, args(1).asInstanceOf[Int]).open)
    }
    .build()
    .asNonBlockingTabExecutor()

}
