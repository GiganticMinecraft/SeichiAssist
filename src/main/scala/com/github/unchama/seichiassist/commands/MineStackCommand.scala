package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.ParserResponse.{failWith, succeedWith}
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.menus.minestack.CategorizedMineStackMenu
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackObjectCategory
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

object MineStackCommand {
  def executor(
    implicit ioCanOpenCategorizedMenu: IO CanOpen CategorizedMineStackMenu
  ): TabExecutor =
    BranchedExecutor(
      Map(
        "on" -> ChildExecutors.setAutoCollectionExecutor(true),
        "off" -> ChildExecutors.setAutoCollectionExecutor(false),
        "open" -> ChildExecutors.openCategorizedMineStackMenu,
        "store-all" -> ChildExecutors.storeEverythingInInventory
      )
    ).asNonBlockingTabExecutor()

  object ChildExecutors {

    def setAutoCollectionExecutor(autoMineStack: Boolean): ContextualExecutor =
      playerCommandBuilder
        .execution { context =>
          IO {
            val sender = context.sender
            val pd = SeichiAssist.playermap(sender.getUniqueId).settings
            SequentialEffect(
              UnfocusedEffect {
                pd.autoMineStack = autoMineStack
              },
              if (autoMineStack)
                MessageEffect("mineStack自動収集をonにしました。")
              else
                MessageEffect("mineStack自動収集をoffにしました。")
            )
          }
        }
        .build()

    def openCategorizedMineStackMenu(
      implicit ioCanOpenCategorizedMenu: IO CanOpen CategorizedMineStackMenu
    ): ContextualExecutor =
      playerCommandBuilder
        .argumentsParsers(
          List(
            Parsers
              .closedRangeInt(1, Int.MaxValue, MessageEffect("カテゴリは正の値を指定してください。"))
              .andThen(_.flatMap { categoryValue =>
                MineStackObjectCategory
                  .fromSerializedValue(categoryValue.asInstanceOf[Int] - 1) match {
                  case Some(category) => succeedWith(category)
                  case None           => failWith("指定されたカテゴリは存在しません。")
                }
              }),
            Parsers.closedRangeInt(1, Int.MaxValue, MessageEffect("ページ数は正の値を指定してください。"))
          )
        )
        .execution { context =>
          IO.pure(
            ioCanOpenCategorizedMenu.open(
              new CategorizedMineStackMenu(
                context.args.parsed.head.asInstanceOf[MineStackObjectCategory],
                context.args.parsed(1).toString.toInt - 1
              )
            )
          )
        }
        .build()

    def storeEverythingInInventory: ContextualExecutor =
      playerCommandBuilder
        .execution { context =>
          IO {
            val player = context.sender
            val inventory = player.getInventory
            SequentialEffect(
              UnfocusedEffect {
                inventory.getContents.toList.zipWithIndex.foreach {
                  case (itemStack, index) =>
                    if (
                      itemStack != null && BreakUtil.tryAddItemIntoMineStack(player, itemStack)
                    )
                      inventory.clear(index)
                }
              },
              MessageEffect(s"${YELLOW}インベントリの中身をすべてマインスタックに収納しました。")
            )
          }
        }
        .build()

  }

}
