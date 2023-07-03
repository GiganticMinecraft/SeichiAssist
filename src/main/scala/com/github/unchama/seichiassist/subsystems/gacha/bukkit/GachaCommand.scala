package com.github.unchama.seichiassist.subsystems.gacha.bukkit

import cats.data.Kleisli
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.ParserResponse.{failWith, succeedWith}
import com.github.unchama.contextualexecutor.builder.{
  ContextualExecutorBuilder,
  Parsers,
  SingleArgumentParser
}
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.CloneableBukkitItemStack.instance
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.BukkitGrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.PlayerName
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.{
  GachaTicketAmount,
  GrantResultOfGachaTicketFromAdminTeam
}
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain._
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventName
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.{
  GachaPrize,
  GachaPrizeId
}
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.targetedeffect.DeferredEffect
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{Interval, NonNegative, Positive}
import eu.timepit.refined.auto._
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, TabExecutor}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import shapeless.HNil

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

class GachaCommand[
  F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect
](
  implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
  canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack],
  gachaTicketAPI: GachaTicketAPI[F],
  mineStackAPI: MineStackAPI[F, Player, ItemStack]
) {

  import cats.implicits._

  private val printDescriptionExecutor = EchoExecutor(
    MessageEffect(
      List(
        s"$YELLOW$BOLD/gachaコマンドの使い方",
        s"$RED/gacha give <all/プレイヤー名/UUID> <個数>",
        "ガチャ券配布コマンドです。allを指定で全員に配布(マルチ鯖対応済)",
        s"$RED/gacha get <ID> (<名前>)",
        "指定したガチャリストのIDを入手 (所有者付きにもできます) IDを0に指定するとガチャリンゴを入手できます",
        s"$RED/gacha add <確率> (<イベント名>)",
        "現在のメインハンドを指定されたイベントのガチャリストに追加。確率は1.0までで指定",
        s"$DARK_GRAY※イベント名を入力しなかった場合は通常排出アイテムとして登録されます。",
        s"$RED/gacha list (<イベント>)",
        "指定したイベントのガチャリストを表示",
        s"$DARK_GRAY※イベント名を指定しなかった場合は通常排出アイテムを表示します",
        s"$RED/gacha remove <番号>",
        "リスト該当番号のガチャ景品を削除",
        s"$RED/gacha setamount <番号> <個数>",
        "リスト該当番号のガチャ景品の個数変更。64まで",
        s"$RED/gacha setprob <番号> <確率>",
        "リスト該当番号のガチャ景品の確率変更",
        s"$RED/gacha clear",
        "ガチャリストを全消去する。取扱注意",
        s"$RED/gacha save",
        "コマンドによるガチャリストへの変更をmysqlに送信",
        s"$RED/gacha reload",
        "ガチャリストをmysqlから読み込む",
        s"$DARK_GRAY※onEnable時と同じ処理",
        s"$RED/gacha create-event <イベント名(30字以内、日本語可)> <開始日> <終了日>",
        "日付はyyyy-MM-ddの形式で指定をしてください。",
        s"$DARK_GRAY※通常排出のガチャ景品リストがコピーされます。",
        s"${DARK_GRAY}置き換えたいアイテムを置き換えてイベント排出景品を設定してください。",
        s"$RED/gacha delete-event <イベント名>",
        "イベントを削除します。(間違ってイベントを作成した時以外は使わないでください。)",
        s"$RED/gacha list-event",
        "イベントの一覧を表示します。"
      )
    )
  )

  val executor: TabExecutor = {
    import ChildExecutors._
    BranchedExecutor(
      Map(
        "give" -> giveGachaTickets,
        "get" -> giveItem,
        "add" -> add,
        "remove" -> remove,
        "list" -> list,
        "setamount" -> setAmount,
        "setprob" -> setProbability,
        "clear" -> clear,
        "save" -> save,
        "reload" -> reload,
        "create-event" -> createEvent,
        "delete-event" -> deleteEvent,
        "list-event" -> eventList
      ),
      whenBranchNotFound = Some(printDescriptionExecutor),
      whenArgInsufficient = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()
  }

  object ChildExecutors {

    Parsers
      .closedRangeInt[Int Refined NonNegative](
        0,
        Int.MaxValue,
        MessageEffect("IDは0以上の整数を指定してください。")
      )
      .andThen(_.flatMap { id =>
        val intId = id
        if (gachaPrizeAPI.existsGachaPrize(GachaPrizeId(intId)).toIO.unsafeRunSync())
          succeedWith(intId)
        else
          failWith("指定されたIDのアイテムは存在しません！")
      })

    private val gachaPrizeIdExistsParser: SingleArgumentParser[GachaPrizeId] =
      Parsers
        .closedRangeInt[Int Refined Positive](
          1,
          Int.MaxValue,
          MessageEffect("IDは正の値を指定してください。")
        )
        .andThen(_.flatMap { intId =>
          val id = GachaPrizeId(intId)
          if (gachaPrizeAPI.existsGachaPrize(id).toIO.unsafeRunSync()) {
            succeedWith(id)
          } else {
            failWith("指定されたIDのアイテムは存在しません！")
          }
        })

    private val probabilityParser: SingleArgumentParser[Double] =
      Parsers.double(MessageEffect("確率は小数点数で指定してください。")).andThen {
        _.flatMap { doubleNum =>
          if (doubleNum <= 1.0 && doubleNum >= 0.0) {
            succeedWith(doubleNum)
          } else {
            failWith("確率は正の数かつ1.0以下で指定してください。")
          }
        }
      }

    val giveGachaTickets: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration
      .thenParse(arg => {
        val uuidRegex =
          "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r

        val x: GiveGachaSelector = if (uuidRegex.matches(arg)) {
          ByUUID(UUID.fromString(arg))
        } else if (arg == "all") {
          GiveAll
        } else {
          ByName(arg)
        }

        Right(x)
      })
      .thenParse(
        Parsers.closedRangeInt[Int Refined Positive](
          1,
          Int.MaxValue,
          MessageEffect("配布するガチャ券の枚数は正の値を指定してください。")
        )
      )
      .buildWith { context =>
        import shapeless.::
        import com.github.unchama.seichiassist.subsystems.gacha.bukkit.{ByName, ByUUID, GiveAll}
        val selector :: amount :: HNil = context.args.parsed
        val gachaTicketAmount = GachaTicketAmount(amount)
        val eff = selector match {
          case GiveAll =>
            Kleisli
              .liftF(gachaTicketAPI.addToAllKnownPlayers(gachaTicketAmount).toIO)
              .flatMap(_ => MessageEffectF(s"${GREEN}全プレイヤーへガチャ券${amount}枚加算成功"))
          case ByUUID(uuid) =>
            Kleisli
              .liftF[IO, CommandSender, GrantResultOfGachaTicketFromAdminTeam](
                gachaTicketAPI.addByUUID(gachaTicketAmount, uuid).toIO
              )
              .flatMap {
                case GrantResultOfGachaTicketFromAdminTeam.Success =>
                  MessageEffectF(s"${GREEN}ガチャ券${amount}枚加算成功")
                case GrantResultOfGachaTicketFromAdminTeam.NotExists =>
                  MessageEffectF(s"${RED}プレイヤーが存在しません。")
                case GrantResultOfGachaTicketFromAdminTeam.GrantedToMultiplePlayers =>
                  MessageEffectF(s"${RED}加算は成功しましたが、複数プレイヤーが存在しました。")
              }
          case ByName(playerLogin) =>
            Kleisli
              .liftF[IO, CommandSender, GrantResultOfGachaTicketFromAdminTeam](
                gachaTicketAPI.addByPlayerName(gachaTicketAmount, PlayerName(playerLogin)).toIO
              )
              .flatMap {
                case GrantResultOfGachaTicketFromAdminTeam.Success =>
                  MessageEffectF(s"${GREEN}ガチャ券${amount}枚加算成功")
                case GrantResultOfGachaTicketFromAdminTeam.NotExists =>
                  MessageEffectF(s"${RED}プレイヤーが存在しません。")
                case GrantResultOfGachaTicketFromAdminTeam.GrantedToMultiplePlayers =>
                  MessageEffectF(s"${RED}加算は成功しましたが、複数プレイヤーが存在しました。")
              }
        }

        IO { eff }
      }

    val giveItem: ContextualExecutor =
      playerCommandBuilder
        .thenParse(
          Parsers.closedRangeInt[Int Refined NonNegative](
            0,
            Int.MaxValue,
            MessageEffect("IDは0以上の整数を指定してください。")
          )
        )
        .thenParse(Parsers.identity)
        .buildWithExecutionF { context =>
          import shapeless.::
          val gachaPrizeId :: owner :: shapeless.HNil = context.args.parsed
          val ownerName = Some(owner)

          for {
            gachaPrize <- gachaPrizeAPI.fetch(GachaPrizeId(gachaPrizeId))
            existsGachaPrize = gachaPrize.nonEmpty
            _ <- new BukkitGrantGachaPrize[F]()
              .insertIntoPlayerInventoryOrDrop(gachaPrize.get, ownerName)(context.sender)
              .whenA(existsGachaPrize)
          } yield {
            if (existsGachaPrize)
              MessageEffect("ガチャアイテムを付与しました。")
            else
              MessageEffect("指定されたIDのガチャ景品は存在しません。")
          }
        }

    val add: ContextualExecutor =
      playerCommandBuilder.thenParse(probabilityParser).thenParse(eventName => Right(GachaEventName(eventName))).buildWith {
        context =>
          import shapeless.::
          val player = context.sender
          val probability :: eventName :: HNil = context.args.parsed
          val mainHandItem = player.getInventory.getItemInMainHand
          val eff =
            for {
              _ <- gachaPrizeAPI.addGachaPrize(
                GachaPrize(
                  mainHandItem,
                  GachaProbability(probability),
                  probability < 0.1,
                  _,
                  Some(eventName)
                )
              )
            } yield MessageEffect(
              List("ガチャアイテムを追加しました！", "ガチャアイテムを保存するためには/gacha saveを実行してください。")
            )

          eff.toIO
      }

    val list: ContextualExecutor =
      ContextualExecutorBuilder.beginConfiguration.buildWithExecutionF { context =>
        val eventName = context.args.yetToBeParsed.headOption.map(GachaEventName)
        val eff = for {
          gachaPrizes <- gachaPrizeAPI.allGachaPrizeList
        } yield {
          val gachaPrizeInformation = gachaPrizes
            .filter { gachaPrize =>
              if (eventName.isEmpty) gachaPrize.gachaEventName.isEmpty
              else
                gachaPrize.gachaEventName.nonEmpty && gachaPrize.gachaEventName == eventName
            }
            .sortBy(_.id.id)
            .map { gachaPrize =>
              val itemStack = gachaPrize.itemStack
              val probability = gachaPrize.probability.value

              s"${gachaPrize.id.id}|${itemStack.getType.toString}/${itemStack
                  .getItemMeta
                  .getDisplayName}$RESET|${itemStack.getAmount}|$probability(${probability * 100}%)"
            }
            .toList

          val totalProbability = gachaPrizes.map(_.probability.value).sum
          MessageEffect(
            List(s"${RED}アイテム番号|アイテム名|アイテム数|出現確率") ++ gachaPrizeInformation ++ List(
              s"${RED}合計確率: $totalProbability(${totalProbability * 100}%)",
              s"${RED}合計確率は100%以内に収まるようにしてください。"
            )
          )
        }

        eff.toIO
      }

    val remove: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration
      .thenParse(
        Parsers.closedRangeInt[Int Refined Positive](
          1,
          Int.MaxValue,
          MessageEffect("IDは正の値を指定してください。")
        )
      )
      .buildWithExecutionF { context =>
        val gachaId = GachaPrizeId(context.args.parsed.head)
        for {
          existsGachaPrize <- gachaPrizeAPI.existsGachaPrize(gachaId)
          _ <- gachaPrizeAPI.removeByGachaPrizeId(gachaId).whenA(existsGachaPrize)
        } yield {
          if (existsGachaPrize)
            MessageEffect(List("ガチャアイテムを削除しました", "ガチャアイテム削除を保存するためには/gacha saveを実行してください。"))
          else
            MessageEffect("指定されたIDのガチャ景品は存在しません。")
        }
      }

    val setAmount: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration
        .thenParse(gachaPrizeIdExistsParser)
        .thenParse(
          Parsers.closedRangeInt[Int Refined Interval.Closed[1, 64]](
            1,
            64,
            MessageEffect("数は1～64で指定してください。")
          )
        )
        .buildWith { context =>
          import shapeless.::
          val t :: amount :: HNil = context.args.parsed
          val targetId = GachaPrizeId(t)
          val eff = for {
            existingGachaPrize <- gachaPrizeAPI.fetch(targetId)
            existsGachaPrize = existingGachaPrize.nonEmpty
            _ <- gachaPrizeAPI.removeByGachaPrizeId(targetId).whenA(existsGachaPrize)
            itemStack = existingGachaPrize.map(_.itemStack)
            _ <- gachaPrizeAPI
              .addGachaPrize(_ =>
                existingGachaPrize
                  .get
                  .copy(itemStack = itemStack.get.tap {
                    _.setAmount(amount)
                  })
              )
              .whenA(existsGachaPrize)
          } yield {
            if (existsGachaPrize)
              MessageEffect(
                s"${targetId.id}|${itemStack.get.getType.toString}/${itemStack.get.getItemMeta.getDisplayName}${RESET}のアイテム数を${amount}個に変更しました。"
              )
            else
              MessageEffect("指定されたIDのガチャ景品は存在しません。")
          }

          eff.toIO
        }

    val setProbability: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration
      .thenParse(gachaPrizeIdExistsParser)
      .thenParse(probabilityParser)
      .buildWith { context =>
        import shapeless.::
        val target :: newProb :: HNil = context.args.parsed
        val targetId = GachaPrizeId(target)
        val eff = for {
          existingGachaPrize <- gachaPrizeAPI.fetch(targetId)
          existsGachaPrize = existingGachaPrize.nonEmpty
          _ <- gachaPrizeAPI.removeByGachaPrizeId(targetId).whenA(existsGachaPrize)
          _ <- gachaPrizeAPI
            .addGachaPrize(_ =>
              existingGachaPrize.get.copy(probability = GachaProbability(newProb))
            )
            .whenA(existsGachaPrize)
          itemStack = existingGachaPrize.get.itemStack
        } yield {
          if (existsGachaPrize)
            MessageEffect(s"${targetId.id}|${itemStack.getType.toString}/${itemStack
                .getItemMeta
                .getDisplayName}${RESET}の確率を$newProb(${newProb * 100}%)に変更しました。")
          else
            MessageEffect("指定されたIDのガチャ景品は存在しません。")
        }
        eff.toIO
      }

    val clear: ContextualExecutor =
      ContextualExecutorBuilder.beginConfiguration.buildWithEffectAsExecution {
        DeferredEffect(
          for {
            _ <- gachaPrizeAPI.clear.toIO
          } yield MessageEffect(
            List(
              "すべて削除しました。",
              "/gacha saveを実行するとmysqlのデータも全削除されます。",
              "削除を取り消すには/gacha reloadコマンドを実行します。"
            )
          )
        )
      }

    val save: ContextualExecutor =
      ContextualExecutorBuilder.beginConfiguration.buildWithEffectAsExecution {
        val eff = DeferredEffect(for {
          gachaPrizes <- gachaPrizeAPI.allGachaPrizeList
          _ <- gachaPrizeAPI.replace(gachaPrizes)
        } yield MessageEffect("ガチャデータをmysqlに保存しました。"))

        eff.toIO
      }

    val reload: ContextualExecutor =
      ContextualExecutorBuilder.beginConfiguration.buildWithEffectAsExecution {
        DeferredEffect(for {
          _ <- gachaPrizeAPI.load.toIO
        } yield MessageEffect("ガチャデータをmysqlから読み込みました。"))
      }

    val createEvent: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration
        .thenParse(Parsers.identity)
        .thenParse(Parsers.identity)
        .thenParse(Parsers.identity)
        .buildWith { context =>
          import shapeless.::
          val e :: startDate :: endDate :: HNil = context.args.parsed
          val eventName = GachaEventName(e)

          val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
          val dateRegex = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])".r

          if (!dateRegex.matches(startDate) || !dateRegex.matches(endDate)) {
            IO(MessageEffect(s"${RED}開始日/終了日はyyyy-MM-ddの形式で指定してください。"))
          } else if (eventName.name.length > 30) {
            IO(MessageEffect(s"${RED}イベント名は30字以内で指定してください。"))
          } else {
            val eff = for {
              existsEvent <- gachaPrizeAPI.existsGachaEvent(eventName)
              _ <- gachaPrizeAPI
                .createGachaEvent(
                  GachaEvent(
                    eventName,
                    LocalDate.parse(startDate, dateTimeFormatter),
                    LocalDate.parse(endDate, dateTimeFormatter)
                  )
                )
                .unlessA(existsEvent)

            } yield {
              if (existsEvent) MessageEffect(s"${RED}指定された名前のイベントが既に存在します。")
              else MessageEffect(s"${AQUA}イベントを作成しました。")
            }

            eff.toIO
          }
        }

    val deleteEvent: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration
        .thenParse(Parsers.identity)
        .buildWithExecutionF { context =>
          val eventName = GachaEventName(context.args.parsed.head)
          for {
            _ <- gachaPrizeAPI.deleteGachaEvent(eventName)
          } yield MessageEffect(s"ガチャイベント: ${eventName.name}を削除しました。")
        }

    val eventList: ContextualExecutor =
      ContextualExecutorBuilder.beginConfiguration.buildWith { _ =>
        val eff = for {
          events <- gachaPrizeAPI.createdGachaEvents
        } yield {
          val messages = "イベント名 | 開始日 | 終了日" +: events.map { event =>
            s"${event.eventName} | ${event.getStartDateString} | ${event.getEndDateString}"
          }
          MessageEffect(messages.toList)
        }

        eff.toIO
      }
  }

}
