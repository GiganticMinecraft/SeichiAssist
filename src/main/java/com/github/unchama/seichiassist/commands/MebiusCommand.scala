package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.listener.MebiusListener
import com.github.unchama.targetedeffect.EmptyEffect
import org.bukkit.ChatColor._

object MebiusCommand {
  private object Messages {
    val commandDescription = List(
      s"${RED}[Usage]",
      s"${RED}/mebius naming [name]",
      s"${RED}  現在頭に装着中のMEBIUSに[name]を命名します。",
        "",
      s"${RED}/mebius nickname",
      s"${RED}  MEBIUSから呼ばれる名前を表示します",
        "",
      s"${RED}/mebius nickname set [name]",
      s"${RED}  MEBIUSから呼ばれる名前を[name]に変更します",
        "",
      s"${RED}/mebius nickname reset",
      s"${RED}  MEBIUSからの呼び名をプレイヤー名(初期設定)に戻します",
        ""
    ).asMessageEffect()

    val permissionWarning = s"${RED}このコマンドは権限者のみが実行可能です.".asMessageEffect()
  }

  private object ChildExecutors {
    val printDescriptionExecutor = ContextualExecutorBuilder.beginConfiguration()
        .execution { Messages.commandDescription }
        .build()

    val getExecutor = playerCommandBuilder
        .execution { context =>
          if (!context.sender.isOp) Messages.permissionWarning else {
            MebiusListener.debugGive(context.sender)
            EmptyEffect
          }
        }
        .build()

    val reloadExecutor = playerCommandBuilder
        .execution { context =>
          if (!context.sender.isOp) Messages.permissionWarning else {
            MebiusListener.reload()
            EmptyEffect
          }
        }
        .build()

    val debugExecutor = playerCommandBuilder
        .execution { context =>
          if (!context.sender.isOp) Messages.permissionWarning else {
            MebiusListener.debug(context.sender)
            EmptyEffect
          }
        }
        .build()

    object NickNameCommand {
      private val checkNickNameExecutor = playerCommandBuilder
          .execution { context =>
            val message = MebiusListener.nickname(context.sender)
              ?.let { s"${GREEN}現在のメビウスからの呼び名 : $it" }
              ?: s"${RED}呼び名の確認はMEBIUSを装着して行ってください."

            message.asMessageEffect()
          }
          .build()

      private val resetNickNameExecutor = playerCommandBuilder
          .execution { context =>
            val message = if (MebiusListener.setNickname(context.sender, context.sender.name)) {
              s"${GREEN}メビウスからの呼び名を${context.sender.name}にリセットしました."
            } else {
              s"${RED}呼び名のリセットはMEBIUSを装着して行ってください."
            }

            message.asMessageEffect()
          }
          .build()

      private val setNickNameExecutor = playerCommandBuilder
          .argumentsParsers(List(Parsers.identity), onMissingArguments = printDescriptionExecutor)
          .execution { context =>
            val newName = s"${context.args.parsed[0].asInstanceOf[String]} ${context.args.yetToBeParsed.joinToString(" ")}"
            val message = if (!MebiusListener.setNickname(context.sender, newName)) {
              s"${RED}呼び名の設定はMEBIUSを装着して行ってください."
            } else {
              s"${GREEN}メビウスからの呼び名を${newName}にセットしました."
            }

            message.asMessageEffect()
          }
          .build()

      val executor = BranchedExecutor(mapOf(
          "reset" to resetNickNameExecutor,
          "set" to setNickNameExecutor
      ), whenArgInsufficient = checkNickNameExecutor, whenBranchNotFound = checkNickNameExecutor)
    }

    val namingExecutor = playerCommandBuilder
        .argumentsParsers(List(Parsers.identity))
        .execution { context =>
          val newName = s"${context.args.parsed[0].asInstanceOf[String]} ${context.args.yetToBeParsed.joinToString(" ")}"

          if (!MebiusListener.setName(context.sender, newName)) {
            s"${RED}命名はMEBIUSを装着して行ってください.".asMessageEffect()
          } else EmptyEffect
        }
        .build()
  }

  val executor = with(ChildExecutors) {
    BranchedExecutor(
        mapOf(
            "get" to getExecutor,
            "reload" to reloadExecutor,
            "debug" to debugExecutor,
            "nickname" to ChildExecutors.NickNameCommand.executor,
            "naming" to namingExecutor
        ), whenArgInsufficient = printDescriptionExecutor, whenBranchNotFound = printDescriptionExecutor
    ).asNonBlockingTabExecutor()
  }
}