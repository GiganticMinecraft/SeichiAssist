package com.github.unchama.seichiassist.mebius.controller.command

import cats.effect.IO
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.controller.listeners.MebiusListener
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor.{GREEN, RED, RESET}
import org.bukkit.command.{CommandSender, TabExecutor}
import org.bukkit.entity.Player

object MebiusCommand {

  import ChildExecutors._

  val executor: TabExecutor = {

    BranchedExecutor(
      Map(
        "nickname" -> ChildExecutors.NicknameCommand.executor,
        "naming" -> namingExecutor
      ), whenArgInsufficient = Some(printDescriptionExecutor), whenBranchNotFound = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()
  }

  private object Messages {
    val commandDescription: TargetedEffect[CommandSender] =
      MessageEffect {
        List(
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
        )
      }
  }

  object ChildExecutors {
    val printDescriptionExecutor: ContextualExecutor = ContextualExecutorBuilder.beginConfiguration()
      .execution { _ => IO(Messages.commandDescription) }
      .build()

    val namingExecutor: ContextualExecutor = playerCommandBuilder
      .argumentsParsers(List(Parsers.identity))
      .execution { context =>
        // TODO cleanup / do not use Boolean
        def setName(player: Player, name: String): Boolean = {
          val updatedProperty = ItemStackMebiusCodec
            .decodeMebiusProperty(player.getInventory.getHelmet)
            .map {
              _.copy(mebiusName = name)
            }

          updatedProperty.foreach { newProperty =>
            val newDisplayName = ItemStackMebiusCodec.displayNameOfMaterializedItem(newProperty)

            player.sendMessage(s"$newDisplayName${RESET}に命名しました。")
            SeichiAssist.playermap.apply(player.getUniqueId).mebius
              .speakForce(s"わーい、ありがとう！今日から僕は$newDisplayName${RESET}だ！")

            player.getInventory.setHelmet(ItemStackMebiusCodec.materialize(newProperty))
          }

          updatedProperty.nonEmpty
        }

        val newName = s"${context.args.parsed.head.asInstanceOf[String]} ${context.args.yetToBeParsed.mkString(" ")}"

        if (!setName(context.sender, newName)) {
          IO(MessageEffect(s"${RED}命名はMEBIUSを装着して行ってください."))
        } else IO(emptyEffect)
      }
      .build()

    object NicknameCommand {
      // TODO cleanup / do not use Boolean
      private def setNickname(player: Player, name: String): Boolean = {
        val updatedProperty = ItemStackMebiusCodec
          .decodeMebiusProperty(player.getInventory.getHelmet)
          .map {
            _.copy(ownerNickname = Some(name))
          }

        updatedProperty.foreach { newProperty =>
          player.getInventory.setHelmet(ItemStackMebiusCodec.materialize(newProperty))
          SeichiAssist.playermap.apply(player.getUniqueId).mebius
            .speakForce(s"わーい、ありがとう！今日から君のこと$GREEN$name${RESET}って呼ぶね！")
        }

        updatedProperty.nonEmpty
      }

      private val checkNicknameExecutor = playerCommandBuilder
        .execution { context =>
          IO(MessageEffect {
            MebiusListener.getNickname(context.sender)
              .fold {
                s"${RED}呼び名の確認はMEBIUSを装着して行ってください."
              } { name =>
                s"${GREEN}現在のメビウスからの呼び名 : $name"
              }
          })
        }
        .build()

      private val resetNicknameExecutor = playerCommandBuilder
        .execution { context =>
          val message = if (setNickname(context.sender, context.sender.getName)) {
            s"${GREEN}メビウスからの呼び名を${context.sender.getName}にリセットしました."
          } else {
            s"${RED}呼び名のリセットはMEBIUSを装着して行ってください."
          }

          IO(MessageEffect(message))
        }
        .build()

      private val setNicknameExecutor = playerCommandBuilder
        .argumentsParsers(List(Parsers.identity), onMissingArguments = printDescriptionExecutor)
        .execution { context =>
          val newName = s"${context.args.parsed.head.asInstanceOf[String]} ${context.args.yetToBeParsed.mkString(" ")}"
          val message = if (!setNickname(context.sender, newName)) {
            s"${RED}呼び名の設定はMEBIUSを装着して行ってください."
          } else {
            s"${GREEN}メビウスからの呼び名を${newName}にセットしました."
          }

          IO(MessageEffect(message))
        }
        .build()

      val executor: BranchedExecutor = BranchedExecutor(Map(
        "reset" -> resetNicknameExecutor,
        "set" -> setNicknameExecutor
      ), whenArgInsufficient = Some(checkNicknameExecutor), whenBranchNotFound = Some(checkNicknameExecutor))
    }

  }

}
