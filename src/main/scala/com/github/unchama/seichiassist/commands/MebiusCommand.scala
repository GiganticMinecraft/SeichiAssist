package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.listener.MebiusListener
import com.github.unchama.targetedeffect.syntax._
import com.github.unchama.targetedeffect.{TargetedEffect, emptyEffect}
import com.github.unchama.util.syntax.Nullability._
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, TabExecutor}

object MebiusCommand {

  val executor: TabExecutor = {
    import com.github.unchama.seichiassist.commands.MebiusCommand.ChildExecutors._

    BranchedExecutor(
      Map(
        "get" -> getExecutor,
        "debug" -> debugExecutor,
        "nickname" -> ChildExecutors.NicknameCommand.executor,
        "naming" -> namingExecutor
      ), whenArgInsufficient = Some(printDescriptionExecutor), whenBranchNotFound = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()
  }

  private object Messages {
    val commandDescription: TargetedEffect[CommandSender] = List(
      s"$RED[Usage]",
      s"$RED/mebius naming [name]",
      s"$RED  現在頭に装着中のMEBIUSに[name]を命名します。",
      "",
      s"$RED/mebius nickname",
      s"$RED  MEBIUSから呼ばれる名前を表示します",
      "",
      s"$RED/mebius nickname set [name]",
      s"$RED  MEBIUSから呼ばれる名前を[name]に変更します",
      "",
      s"$RED/mebius nickname reset",
      s"$RED  MEBIUSからの呼び名をプレイヤー名(初期設定)に戻します",
      ""
    ).asMessageEffect()

    val permissionWarning: TargetedEffect[CommandSender] = s"${RED}このコマンドは権限者のみが実行可能です.".asMessageEffect()
  }

  private object ChildExecutors {
    val printDescriptionExecutor: ContextualExecutor = ContextualExecutorBuilder.beginConfiguration()
      .execution { _ => IO(Messages.commandDescription) }
      .build()

    val getExecutor: ContextualExecutor = playerCommandBuilder
      .execution { context =>
        if (!context.sender.isOp) {
          IO(Messages.permissionWarning)
        } else {
          MebiusListener.debugGive(context.sender)
          IO(emptyEffect)
        }
      }
      .build()

    val debugExecutor: ContextualExecutor = playerCommandBuilder
      .execution { context =>
        if (!context.sender.isOp) {
          IO(Messages.permissionWarning)
        } else {
          MebiusListener.debug(context.sender)
          IO(emptyEffect)
        }
      }
      .build()
    val namingExecutor: ContextualExecutor = playerCommandBuilder
      .argumentsParsers(List(Parsers.identity))
      .execution { context =>
        val newName = s"${context.args.parsed(0).asInstanceOf[String]} ${context.args.yetToBeParsed.mkString(" ")}"

        if (!MebiusListener.setName(context.sender, newName)) {
          IO(s"${RED}命名はMEBIUSを装着して行ってください.".asMessageEffect())
        } else IO(emptyEffect)
      }
      .build()

    object NicknameCommand {
      private val checkNicknameExecutor = playerCommandBuilder
        .execution { context =>
          val message = MebiusListener.getNickname(context.sender)
            .ifNotNull(name => s"${GREEN}現在のメビウスからの呼び名 : $name")
            .ifNull(s"${RED}呼び名の確認はMEBIUSを装着して行ってください.")

          IO(message.asMessageEffect())
        }
        .build()

      private val resetNicknameExecutor = playerCommandBuilder
        .execution { context =>
          val message = if (MebiusListener.setNickname(context.sender, context.sender.getName)) {
            s"${GREEN}メビウスからの呼び名を${context.sender.getName}にリセットしました."
          } else {
            s"${RED}呼び名のリセットはMEBIUSを装着して行ってください."
          }

          IO(message.asMessageEffect())
        }
        .build()

      private val setNicknameExecutor = playerCommandBuilder
        .argumentsParsers(List(Parsers.identity), onMissingArguments = printDescriptionExecutor)
        .execution { context =>
          val newName = s"${context.args.parsed(0).asInstanceOf[String]} ${context.args.yetToBeParsed.mkString(" ")}"
          val message = if (!MebiusListener.setNickname(context.sender, newName)) {
            s"${RED}呼び名の設定はMEBIUSを装着して行ってください."
          } else {
            s"${GREEN}メビウスからの呼び名を${newName}にセットしました."
          }

          IO(message.asMessageEffect())
        }
        .build()

      val executor = BranchedExecutor(Map(
        "reset" -> resetNicknameExecutor,
        "set" -> setNicknameExecutor
      ), whenArgInsufficient = Some(checkNicknameExecutor), whenBranchNotFound = Some(checkNicknameExecutor))
    }
  }
}
