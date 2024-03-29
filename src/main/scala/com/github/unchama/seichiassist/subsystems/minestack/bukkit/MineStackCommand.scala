package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import cats.effect.IO
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.ParserResponse.{failWith, succeedWith}
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.menus.minestack.{
  CategorizedMineStackMenu,
  MineStackMainMenu
}
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectCategory
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{DeferredEffect, SequentialEffect}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import shapeless.HNil

object MineStackCommand {
  def executor(
    implicit ioCanOpenCategorizedMenu: IO CanOpen CategorizedMineStackMenu,
    ioCanOpenMinestackMainMenu: IO CanOpen MineStackMainMenu.type,
    mineStackAPI: MineStackAPI[IO, Player, ItemStack]
  ): TabExecutor =
    BranchedExecutor(
      Map(
        "on" -> ChildExecutors.setAutoCollectionExecutor(isItemCollectedAutomatically = true),
        "off" -> ChildExecutors.setAutoCollectionExecutor(isItemCollectedAutomatically = false),
        "open" -> ChildExecutors.openCategorizedMineStackMenu,
        "store-all" -> ChildExecutors.storeEverythingInInventory
      )
    ).asNonBlockingTabExecutor()

  object ChildExecutors {

    def setAutoCollectionExecutor(
      isItemCollectedAutomatically: Boolean
    )(implicit mineStackAPI: MineStackAPI[IO, Player, ItemStack]): ContextualExecutor =
      playerCommandBuilder.buildWithEffectAsExecution(
        SequentialEffect(
          DeferredEffect {
            IO(mineStackAPI.setAutoMineStack(isItemCollectedAutomatically))
          },
          if (isItemCollectedAutomatically)
            MessageEffect("MineStack自動収集をONにしました。")
          else
            MessageEffect("MineStack自動収集をOFFにしました。")
        )
      )

    def openCategorizedMineStackMenu(
      implicit ioCanOpenCategorizedMenu: IO CanOpen CategorizedMineStackMenu,
      ioCanOpenMinestackMainMenu: IO CanOpen MineStackMainMenu.type
    ): ContextualExecutor =
      playerCommandBuilder
        .thenParse(
          Parsers
            .closedRangeInt[Int Refined Positive](
              1,
              Int.MaxValue,
              MessageEffect("カテゴリは正の値を指定してください。")
            )
            .andThen(_.flatMap { categoryValue =>
              MineStackObjectCategory.fromSerializedValue(categoryValue - 1) match {
                case Some(category) =>
                  succeedWith(Option.unless(categoryValue.value == 0)(category))
                case None => failWith("指定されたカテゴリは存在しません。")
              }
            })
        )
        .thenParse(
          Parsers.closedRangeInt[Int Refined Positive](
            1,
            Int.MaxValue,
            MessageEffect("ページ数は正の値を指定してください。")
          )
        )
        .buildWith { context =>
          import shapeless.::
          val categoryOpt :: page :: HNil = context.args.parsed

          IO.pure {
            categoryOpt.fold(ioCanOpenMinestackMainMenu.open(MineStackMainMenu)) { category =>
              ioCanOpenCategorizedMenu.open(new CategorizedMineStackMenu(category, page - 1))
            }
          }
        }

    import cats.implicits._

    def storeEverythingInInventory(
      implicit mineStackAPI: MineStackAPI[IO, Player, ItemStack]
    ): ContextualExecutor =
      playerCommandBuilder.buildWith { context =>
        for {
          player <- IO(context.sender)
          inventory <- IO(player.getInventory)
          targetIndexes <- inventory.getContents.toList.zipWithIndex.traverse {
            case (itemStack, index) if itemStack != null =>
              mineStackAPI
                .mineStackRepository
                .tryIntoMineStack(player, itemStack, itemStack.getAmount)
                .map(Option.when(_)(index))
            case _ => IO.pure(None)
          }
          _ <- IO(targetIndexes.foreach(_.foreach(index => inventory.clear(index))))
        } yield MessageEffect(s"${YELLOW}インベントリの中身をすべてマインスタックに収納しました。")
      }

  }

}
