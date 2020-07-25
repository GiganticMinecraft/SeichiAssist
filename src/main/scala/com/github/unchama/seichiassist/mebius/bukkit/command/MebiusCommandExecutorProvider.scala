package com.github.unchama.seichiassist.mebius.bukkit.command

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.contextualexecutor.{ContextualExecutor, PartiallyParsedArgs}
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.bukkit.command.MebiusCommandExecutorProvider.Messages
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechGateway, MebiusSpeechStrength}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect, UnfocusedEffect}
import org.bukkit.ChatColor.{GREEN, RED, RESET}
import org.bukkit.command.{CommandSender, TabExecutor}
import org.bukkit.entity.Player

class MebiusCommandExecutorProvider(implicit gatewayRepository: PlayerDataRepository[MebiusSpeechGateway[IO]]) {

  import ChildExecutors._

  val executor: TabExecutor = {

    BranchedExecutor(
      Map(
        "nickname" -> ChildExecutors.NicknameCommand.executor,
        "naming" -> namingExecutor
      ), whenArgInsufficient = Some(printDescriptionExecutor), whenBranchNotFound = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()
  }

  object ChildExecutors {
    val printDescriptionExecutor: ContextualExecutor = ContextualExecutorBuilder.beginConfiguration()
      .execution { _ => IO(Messages.commandDescription) }
      .build()
    val namingExecutor: ContextualExecutor = playerCommandBuilder
      .argumentsParsers(List(Parsers.identity))
      .execution { context =>
        val newName = concatHeadAndRemainingArgs(context.args)
        val player = context.sender

        MebiusInteractionTemplate(
          MessageEffect(s"${RED}命名はMEBIUSを装着して行ってください."),
          _.copy(mebiusName = newName),
          newProperty => {
            val newDisplayName = BukkitMebiusItemStackCodec.displayNameOfMaterializedItem(newProperty)
            SequentialEffect(
              MessageEffect(s"$newDisplayName${RESET}に命名しました。"),
              Kleisli.liftF {
                gatewayRepository(player).makeSpeechIgnoringBlockage(
                  newProperty,
                  MebiusSpeech(
                    s"わーい、ありがとう！今日から僕は$newDisplayName${RESET}だ！",
                    MebiusSpeechStrength.Loud
                  )
                )
              }
            )
          }
        ).effectOn(player)
      }
      .build()

    private def concatHeadAndRemainingArgs(args: PartiallyParsedArgs): String =
      s"${args.parsed.head.toString} ${args.yetToBeParsed.mkString(" ")}"

    private case class MebiusInteractionTemplate(effectIfMebiusIsNotWorn: TargetedEffect[Player],
                                                 propertyModifier: MebiusProperty => MebiusProperty,
                                                 additionalEffectsOnModification: MebiusProperty => TargetedEffect[Player]) {

      def effectOn(player: Player): IO[TargetedEffect[Player]] =
        for {
          helmet <- IO {
            player.getInventory.getHelmet
          }
          effect <- IO.pure {
            BukkitMebiusItemStackCodec
              .decodeMebiusProperty(helmet)
              .map(propertyModifier) match {
              case Some(newProperty) =>
                SequentialEffect(
                  UnfocusedEffect {
                    player.getInventory.setHelmet {
                      BukkitMebiusItemStackCodec.materialize(newProperty, damageValue = helmet.getDurability)
                    }
                  },
                  additionalEffectsOnModification(newProperty)
                )
              case None => effectIfMebiusIsNotWorn
            }
          }
        } yield effect
    }

    object NicknameCommand {
      private val resetNicknameExecutor = playerCommandBuilder
        .execution { context =>
          val player = context.sender
          setNicknameOverrideOnMebiusOn(
            player,
            player.getName,
            newName => s"${GREEN}メビウスからの呼び名を${newName}にリセットしました.",
            s"${RED}呼び名のリセットはMEBIUSを装着して行ってください."
          )
        }
        .build()

      private val setNicknameExecutor = playerCommandBuilder
        .argumentsParsers(List(Parsers.identity), onMissingArguments = printDescriptionExecutor)
        .execution { context =>
          val player = context.sender
          setNicknameOverrideOnMebiusOn(
            player,
            concatHeadAndRemainingArgs(context.args),
            newName => s"${GREEN}メビウスからの呼び名を${newName}にセットしました.",
            s"${RED}呼び名の設定はMEBIUSを装着して行ってください."
          )
        }
        .build()

      private def setNicknameOverrideOnMebiusOn(player: Player,
                                                name: String,
                                                successMessage: String => String,
                                                errorMessage: String): IO[TargetedEffect[Player]] = {

        MebiusInteractionTemplate(
          MessageEffect(errorMessage),
          _.copy(ownerNicknameOverride = Some(name)),
          newProperty => SequentialEffect(
            MessageEffect(successMessage(name)),
            Kleisli.liftF {
              gatewayRepository(player).makeSpeechIgnoringBlockage(
                newProperty,
                MebiusSpeech(
                  s"わーい、ありがとう！今日から君のこと$GREEN$name${RESET}って呼ぶね！",
                  MebiusSpeechStrength.Loud
                )
              )
            }
          )
        ).effectOn(player)
      }

      val executor: BranchedExecutor = BranchedExecutor(Map(
        "reset" -> resetNicknameExecutor,
        "set" -> setNicknameExecutor
      ), whenArgInsufficient = Some(checkNicknameExecutor), whenBranchNotFound = Some(checkNicknameExecutor))
      private val checkNicknameExecutor = playerCommandBuilder
        .execution { context =>
          IO(MessageEffect {
            BukkitMebiusItemStackCodec
              .decodeMebiusProperty(context.sender.getInventory.getHelmet)
              .map(_.ownerNickname)
              .fold {
                s"${RED}呼び名の確認はMEBIUSを装着して行ってください."
              } { name =>
                s"${GREEN}現在のメビウスからの呼び名 : $name"
              }
          })
        }
        .build()
    }

  }

}

object MebiusCommandExecutorProvider {

  object Messages {
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

}
