package com.github.unchama.seichiassist.subsystems.home.bukkit.command

import cats.Monad
import cats.data.Kleisli
import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, Effect, IO, SyncEffect}
import com.github.unchama.chatinterceptor.CancellationReason.Overridden
import com.github.unchama.chatinterceptor.ChatInterceptionScope
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.buildcount.BuildCountAPI
import com.github.unchama.seichiassist.subsystems.home.bukkit.{LocationCodec, TeleportEffect}
import com.github.unchama.seichiassist.subsystems.home.{HomeAPI, HomeReadAPI, HomeWriteAPI}
import com.github.unchama.seichiassist.subsystems.home.domain.{Home, HomeId}
import com.github.unchama.seichiassist.subsystems.home.domain.OperationResult.RenameResult
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.ContentModelContainer
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class HomeCommand[F[
  _
]: OnMinecraftServerThread: ConcurrentEffect: NonServerThreadContextShift: HomeAPI, G[
  _
]: SyncEffect: ContextCoercion[*[_], F]](
  implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player],
  buildCountReadAPI: BuildCountAPI[F, G, Player]
) {

  import cats.implicits._

  private val printDescriptionExecutor = EchoExecutor(
    MessageEffect(
      List(
        s"$GREEN/home コマンドの使い方",
        s"${GREEN}移動する場合",
        s"$GREEN/home warp [移動したいホームの番号]",
        s"${GREEN}セットする場合",
        s"$GREEN/home set [セットしたいホームの番号]",
        s"${GREEN}名前変更する場合",
        s"$GREEN/home name [名前変更したいホームの番号]",
        s"${GREEN}削除する場合",
        s"$GREEN/home remove [削除したいホームの番号]"
      )
    )
  )

  private val argsAndSenderConfiguredBuilder = playerCommandBuilder.argumentsParsers(
    List(
      Parsers.closedRangeInt(
        HomeId.minimumNumber,
        HomeId.maxNumber,
        failureMessage =
          MessageEffect(s"ホームの番号を${HomeId.minimumNumber}～${HomeId.maxNumber}の間で入力してください")
      )
    ),
    onMissingArguments = printDescriptionExecutor
  )

  private def homeNotSetMessage: List[String] = List(s"${YELLOW}指定されたホームポイントが設定されていません。")

  def executor[F[
    _
  ]: HomeAPI: ConcurrentEffect: NonServerThreadContextShift: OnMinecraftServerThread](
    implicit scope: ChatInterceptionScope
  ): TabExecutor = BranchedExecutor(
    Map(
      "warp" -> warpExecutor,
      "set" -> setExecutor,
      "name" -> nameExecutor,
      "remove" -> removeExecutor
    ),
    whenArgInsufficient = Some(printDescriptionExecutor),
    whenBranchNotFound = Some(printDescriptionExecutor)
  ).asNonBlockingTabExecutor()

  private def removeExecutor[F[
    _
  ]: ConcurrentEffect: NonServerThreadContextShift: OnMinecraftServerThread: HomeWriteAPI] =
    argsAndSenderConfiguredBuilder
      .executionCSEffect { context =>
        val homeId = HomeId(context.args.parsed.head.asInstanceOf[Int])
        val player = context.sender

        Kleisli
          .liftF(HomeWriteAPI[F].remove(player.getUniqueId, homeId))
          .flatMap(_ => MessageEffectF(s"ホームポイント${homeId}を削除しました。"))
      }
      .build()

  private def warpExecutor[F[
    _
  ]: ConcurrentEffect: NonServerThreadContextShift: OnMinecraftServerThread: HomeReadAPI] =
    argsAndSenderConfiguredBuilder
      .execution { context =>
        val homeId = HomeId(context.args.parsed.head.asInstanceOf[Int])
        val player = context.sender

        val eff = for {
          _ <- NonServerThreadContextShift[F].shift
          homeLocation <- HomeReadAPI[F].get(player.getUniqueId, homeId)
        } yield {
          homeLocation match {
            case None => MessageEffect(s"ホームポイント${homeId}が設定されてません")
            case Some(Home(_, location)) =>
              LocationCodec.toBukkitLocation(location) match {
                case Some(bukkitLocation) =>
                  TeleportEffect.to[F](bukkitLocation).mapK(Effect.toIOK[F]) >>
                    MessageEffect(s"ホームポイント${homeId}にワープしました")
                case None =>
                  MessageEffect(
                    List(s"${RED}ホームポイントへのワープに失敗しました", s"${RED}登録先のワールドが削除された可能性があります")
                  )
              }
          }
        }

        eff.toIO
      }
      .build()

  private def setExecutor[F[_]: ConcurrentEffect: NonServerThreadContextShift: HomeWriteAPI] =
    argsAndSenderConfiguredBuilder
      .execution { context =>
        val homeId = HomeId(context.args.parsed.head.asInstanceOf[Int])
        val player = context.sender

        val homeLocation = LocationCodec.fromBukkitLocation(player.getLocation)

        // APIから整地量、建築量を取得しレベルからホームポイントに使用できるidの最大値を取得する
        // 取得結果と引数のhomeIdを比較し処理分岐を行う
        val eff = for {
          seichiAmount <- ContextCoercion(
            breakCountReadAPI.seichiAmountDataRepository(player).read
          )
          buildAmount <- ContextCoercion(
            buildCountReadAPI.playerBuildAmountRepository(player).read
          )
          homeIdLimit = Home.maxHomePerPlayer + HomeId.maxNumberByPlayerOf(
            seichiAmount,
            buildAmount
          )
//          canUseHomeId = homeIdLimit <= homeId.value
          _ <- MessageEffectF[F](s"このホーム番号は現在のレベルでは使用できません").apply(player)
          _ <- {
            NonServerThreadContextShift[F].shift >> HomeWriteAPI[F].upsertLocation(
              player.getUniqueId,
              homeId
            )(homeLocation) >> MessageEffectF[F](s"現在位置をホームポイント${homeId}に設定しました").apply(player)
          }
        } yield TargetedEffect.emptyEffect

        eff.toIO
      }
      .build()

  private def nameExecutor[F[_]: ConcurrentEffect: NonServerThreadContextShift: HomeAPI](
    implicit scope: ChatInterceptionScope
  ) = argsAndSenderConfiguredBuilder
    .execution { context =>
      val homeId = HomeId(context.args.parsed.head.asInstanceOf[Int])

      val player = context.sender
      val uuid = player.getUniqueId

      val instruction =
        List(s"ホームポイント${homeId}に設定する名前をチャットで入力してください", s"$YELLOW※入力されたチャット内容は他のプレイヤーには見えません")

      def doneMessage(inputName: String): List[String] =
        List(s"${GREEN}ホームポイント${homeId}の名前を", s"$GREEN${inputName}に更新しました")

      val cancelledInputMessage = List(s"${YELLOW}入力がキャンセルされました。")

      for {
        _ <- Monad[IO].ifM(HomeReadAPI[F].configured(uuid, homeId).toIO)(
          MessageEffect(instruction)(player) >>
            scope.interceptFrom(uuid).flatMap {
              case Left(newName) =>
                HomeWriteAPI[F].rename(uuid, homeId)(newName).toIO.flatMap {
                  case RenameResult.Done =>
                    MessageEffect(doneMessage(newName))(player)
                  case RenameResult.NotFound =>
                    MessageEffect(homeNotSetMessage)(player)
                }
              case Right(Overridden) => MessageEffect(cancelledInputMessage)(player)
              case Right(_)          => IO.unit
            },
          MessageEffect(homeNotSetMessage)(player)
        )
      } yield TargetedEffect.emptyEffect
    }
    .build()
}
