package com.github.unchama.seichiassist.subsystems.subhome.bukkit.command

import cats.Monad
import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, Effect, IO}
import com.github.unchama.chatinterceptor.CancellationReason.Overridden
import com.github.unchama.chatinterceptor.ChatInterceptionScope
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.subhome.bukkit.{LocationCodec, TeleportEffect}
import com.github.unchama.seichiassist.subsystems.subhome.domain.OperationResult.RenameResult
import com.github.unchama.seichiassist.subsystems.subhome.domain.{SubHome, SubHomeId}
import com.github.unchama.seichiassist.subsystems.subhome.{
  SubHomeAPI,
  SubHomeReadAPI,
  SubHomeWriteAPI
}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

object SubHomeCommand {

  import cats.implicits._

  private val printDescriptionExecutor = new EchoExecutor(
    MessageEffect(
      List(
        s"$GREEN/subhome コマンドの使い方",
        s"${GREEN}移動する場合",
        s"$GREEN/subhome warp [移動したいサブホームの番号]",
        s"${GREEN}セットする場合",
        s"$GREEN/subhome set [セットしたいサブホームの番号]",
        s"${GREEN}名前変更する場合",
        s"$GREEN/subhome name [名前変更したいサブホームの番号]"
      )
    )
  )

  private val subHomeMax = SeichiAssist.seichiAssistConfig.getSubHomeMax

  private val argsAndSenderConfiguredBuilder = playerCommandBuilder.argumentsParsers(
    List(
      Parsers.closedRangeInt(
        1,
        subHomeMax,
        failureMessage = MessageEffect(s"サブホームの番号を1～${subHomeMax}の間で入力してください")
      )
    ),
    onMissingArguments = printDescriptionExecutor
  )

  private def subHomeNotSetMessage: List[String] = List(s"${YELLOW}指定されたサブホームポイントが設定されていません。")

  def executor[F[
    _
  ]: SubHomeAPI: ConcurrentEffect: NonServerThreadContextShift: OnMinecraftServerThread](
    implicit scope: ChatInterceptionScope
  ): TabExecutor = BranchedExecutor(
    Map("warp" -> warpExecutor, "set" -> setExecutor, "name" -> nameExecutor),
    whenArgInsufficient = Some(printDescriptionExecutor),
    whenBranchNotFound = Some(printDescriptionExecutor)
  ).asNonBlockingTabExecutor()

  private def warpExecutor[F[
    _
  ]: ConcurrentEffect: NonServerThreadContextShift: OnMinecraftServerThread: SubHomeReadAPI] =
    argsAndSenderConfiguredBuilder
      .execution { context =>
        val subHomeId = SubHomeId(context.args.parsed.head.asInstanceOf[Int])
        val player = context.sender

        val eff = for {
          _ <- NonServerThreadContextShift[F].shift
          subHomeLocation <- SubHomeReadAPI[F].get(player.getUniqueId, subHomeId)
        } yield {
          subHomeLocation match {
            case None => MessageEffect(s"サブホームポイント${subHomeId}が設定されてません")
            case Some(SubHome(_, location)) =>
              LocationCodec.toBukkitLocation(location) match {
                case Some(bukkitLocation) =>
                  TeleportEffect.to[F](bukkitLocation).mapK(Effect.toIOK[F]) >>
                    MessageEffect(s"サブホームポイント${subHomeId}にワープしました")
                case None =>
                  MessageEffect(
                    List(s"${RED}サブホームポイントへのワープに失敗しました", s"${RED}登録先のワールドが削除された可能性があります")
                  )
              }
          }
        }

        eff.toIO
      }
      .build()

  private def setExecutor[F[
    _
  ]: ConcurrentEffect: NonServerThreadContextShift: SubHomeWriteAPI] =
    argsAndSenderConfiguredBuilder
      .execution { context =>
        val subHomeId = SubHomeId(context.args.parsed.head.asInstanceOf[Int])
        val player = context.sender

        val subHomeLocation = LocationCodec.fromBukkitLocation(player.getLocation)

        val eff = for {
          _ <- NonServerThreadContextShift[F].shift
          _ <- SubHomeWriteAPI[F].upsertLocation(player.getUniqueId, subHomeId)(subHomeLocation)
        } yield MessageEffect(s"現在位置をサブホームポイント${subHomeId}に設定しました")

        eff.toIO
      }
      .build()

  private def nameExecutor[F[_]: ConcurrentEffect: NonServerThreadContextShift: SubHomeAPI](
    implicit scope: ChatInterceptionScope
  ) = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = SubHomeId(context.args.parsed.head.asInstanceOf[Int])

      val player = context.sender
      val uuid = player.getUniqueId

      val instruction = List(
        s"サブホームポイント${subHomeId}に設定する名前をチャットで入力してください",
        s"$YELLOW※入力されたチャット内容は他のプレイヤーには見えません"
      )

      def doneMessage(inputName: String) =
        List(s"${GREEN}サブホームポイント${subHomeId}の名前を", s"$GREEN${inputName}に更新しました")

      val cancelledInputMessage = List(s"${YELLOW}入力がキャンセルされました。")

      for {
        _ <- Monad[IO].ifM(SubHomeReadAPI[F].configured(uuid, subHomeId).toIO)(
          MessageEffect(instruction)(player) >>
            scope.interceptFrom(uuid).flatMap {
              case Left(newName) =>
                SubHomeWriteAPI[F].rename(uuid, subHomeId)(newName).toIO.flatMap {
                  case RenameResult.Done =>
                    MessageEffect(doneMessage(newName))(player)
                  case RenameResult.NotFound =>
                    MessageEffect(subHomeNotSetMessage)(player)
                }
              case Right(Overridden) => MessageEffect(cancelledInputMessage)(player)
              case Right(_)          => IO.unit
            },
          MessageEffect(subHomeNotSetMessage)(player)
        )
      } yield TargetedEffect.emptyEffect
    }
    .build()
}
