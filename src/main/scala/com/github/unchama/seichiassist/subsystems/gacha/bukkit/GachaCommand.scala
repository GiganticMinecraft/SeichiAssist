package com.github.unchama.seichiassist.subsystems.gacha.bukkit

import cats.data.Kleisli
import cats.effect.ConcurrentEffect
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
import com.github.unchama.seichiassist.subsystems.gacha.domain.PlayerName
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.{
  GachaTicketAmount,
  GrantResultOfGachaTicketFromAdminTeam
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain._
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventName
}
import com.github.unchama.seichiassist.subsystems.gachaprize.{GachaPrizeAPI, domain}
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.{Interval, NonNegative, Positive}
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, TabExecutor}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import shapeless.HNil

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

class GachaCommand[F[_]: OnMinecraftServerThread: ConcurrentEffect](
  implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
  canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack],
  gachaTicketAPI: GachaTicketAPI[F]
) {

  import cats.implicits._
  import cats.effect.implicits._

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
        "create-event" -> createEvent,
        "delete-event" -> deleteEvent,
        "list-event" -> eventList
      ),
      whenBranchNotFound = Some(printDescriptionExecutor),
      whenArgInsufficient = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()
  }

  object ChildExecutors {

    private val gachaPrizeIdExistsParser: SingleArgumentParser[GachaPrizeId] =
      Parsers
        .closedRangeInt[Int Refined Positive](
          1,
          Int.MaxValue,
          MessageEffect("IDは正の値を指定してください。")
        )
        .andThen(_.flatMap { intId =>
          val id = GachaPrizeId(intId)

          // FIXME: パーサーは effectful であってよいかもしれない
          //        (この unsafeRunSync() は Parser の型定義に IO が乗っていればなくてよい。そうするべきでは？)
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
      .thenParse(Parsers.identity)
      .thenParse(s =>
        Parsers
          .closedRangeInt[Int Refined Positive](
            1,
            Int.MaxValue,
            MessageEffect("配布するガチャ券の枚数は正の値を指定してください。")
          )
          .apply(s)
          .map(r => GachaTicketAmount(r.value))
      )
      .buildWithExecutionCSEffect { context =>
        import shapeless.::
        val selector :: amount :: HNil = context.args.parsed
        selector match {
          case "all" =>
            Kleisli
              .liftF(gachaTicketAPI.addToAllKnownPlayers(amount))
              .flatMap(_ => MessageEffectF(s"${GREEN}全プレイヤーへガチャ券${amount.value}枚加算成功"))
          case value =>
            val uuidRegex =
              "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r

            Kleisli
              .liftF {
                if (uuidRegex.matches(value)) {
                  gachaTicketAPI.addByUUID(amount, UUID.fromString(value))
                } else {
                  gachaTicketAPI.addByPlayerName(amount, PlayerName(value))
                }
              }
              .flatMap {
                case GrantResultOfGachaTicketFromAdminTeam.Success =>
                  MessageEffectF(s"${GREEN}ガチャ券${amount.value}枚加算成功")
                case GrantResultOfGachaTicketFromAdminTeam.NotExists =>
                  MessageEffectF(s"${RED}プレイヤーが存在しません。")
                case GrantResultOfGachaTicketFromAdminTeam.GrantedToMultiplePlayers =>
                  MessageEffectF(s"${RED}加算は成功しましたが、複数プレイヤーが存在しました。")
              }
        }
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
        .buildWithExecutionCSEffect { context =>
          import shapeless.::
          val gachaPrizeId :: shapeless.HNil = context.args.parsed
          // optional
          val ownerName = context.args.yetToBeParsed.headOption

          Kleisli
            .liftF {
              for {
                gachaPrize <- gachaPrizeAPI.fetch(GachaPrizeId(gachaPrizeId))
                _ <- gachaPrize.traverse { gachaPrize =>
                  val itemStack = ownerName match {
                    case Some(name) => gachaPrize.materializeWithOwnerSignature(name)
                    case None       => gachaPrize.itemStack
                  }

                  InventoryOperations.grantItemStacksEffect(itemStack).apply(context.sender)
                }
              } yield gachaPrize
            }
            .flatMap {
              case Some(_) => MessageEffectF("ガチャアイテムを付与しました。")
              case None    => MessageEffectF("指定されたIDのガチャ景品は存在しません。")
            }
        }

    val add: ContextualExecutor =
      playerCommandBuilder.thenParse(probabilityParser).buildWithExecutionCSEffect { context =>
        import shapeless.::

        val player = context.sender
        val probability :: HNil = context.args.parsed
        val eventName = context.args.yetToBeParsed.headOption.map(GachaEventName)
        val mainHandItem = player.getInventory.getItemInMainHand

        Kleisli
          .liftF[F, CommandSender, Unit] {
            for {
              events <- gachaPrizeAPI.createdGachaEvents
              _ <- gachaPrizeAPI.addGachaPrize(
                domain.GachaPrizeTableEntry(
                  mainHandItem,
                  GachaProbability(probability),
                  probability < 0.1,
                  _,
                  events.find(gachaEvent => eventName.contains(gachaEvent.eventName))
                )
              )
            } yield ()
          }
          .productR(MessageEffectF("ガチャアイテムを追加しました！"))
      }

    val list: ContextualExecutor =
      ContextualExecutorBuilder.beginConfiguration.buildWithExecutionCSEffect { context =>
        val eventName = context.args.yetToBeParsed.headOption.map(GachaEventName)
        Kleisli.liftF(gachaPrizeAPI.allGachaPrizeList).flatMap { gachaPrizes =>
          val gachaPrizeInformation = gachaPrizes
            .filter { gachaPrize =>
              if (eventName.isEmpty) gachaPrize.nonGachaEventItem
              else
                gachaPrize.gachaEvent.map(_.eventName) == eventName
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
          MessageEffectF(
            List(s"${RED}アイテム番号|アイテム名|アイテム数|出現確率") ++ gachaPrizeInformation ++ List(
              s"${RED}合計確率: $totalProbability(${totalProbability * 100}%)",
              s"${RED}合計確率は100%以内に収まるようにしてください。"
            )
          )
        }
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
      .buildWithExecutionCSEffect { context =>
        val gachaId = GachaPrizeId(context.args.parsed.head)
        Kleisli.liftF(gachaPrizeAPI.removeByGachaPrizeId(gachaId)).flatMap {
          didRemoveGachaPrize =>
            if (didRemoveGachaPrize) {
              MessageEffectF("ガチャアイテムを削除しました")
            } else {
              MessageEffectF("指定されたIDのガチャ景品が存在しないため、ガチャアイテムを削除できませんでした。")
            }
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
        .buildWithExecutionCSEffect { context =>
          import shapeless.::
          val targetId :: amount :: HNil = context.args.parsed

          Kleisli
            .liftF(for {
              currentGachaPrize <- gachaPrizeAPI.fetch(targetId)
              oldItemStack <- currentGachaPrize.traverse { prize =>
                gachaPrizeAPI
                  .upsertGachaPrize(
                    prize.copy(itemStack = prize.itemStack.tap(_.setAmount(amount)))
                  )
                  .as(Some(prize.itemStack))
              }
            } yield oldItemStack)
            .flatMap {
              case Some(itemStack) =>
                MessageEffectF(
                  s"${targetId.id}|${itemStack.get.getType.toString}/${itemStack.get.getItemMeta.getDisplayName}${RESET}のアイテム数を${amount}個に変更しました。"
                )
              case None =>
                MessageEffectF("指定されたIDのガチャ景品が存在しないため、アイテム数が変更できませんでした。")
            }
        }

    val setProbability: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration
      .thenParse(gachaPrizeIdExistsParser)
      .thenParse(probabilityParser)
      .buildWithExecutionCSEffect { context =>
        import shapeless.::
        val targetId :: newProb :: HNil = context.args.parsed

        for {
          currentGachaPrize <- Kleisli.liftF(gachaPrizeAPI.fetch(targetId))
          probabilityChange <- Kleisli.liftF(currentGachaPrize.traverse { gachaPrize =>
            gachaPrizeAPI
              .upsertGachaPrize(gachaPrize.copy(probability = GachaProbability(newProb)))
          })
          itemStack = currentGachaPrize.map(_.itemStack)
        } yield {
          if (probabilityChange.nonEmpty) {
            MessageEffectF(
              s"${targetId.id}|${itemStack.get.getType.toString}/${itemStack.get.getItemMeta.getDisplayName}${RESET}の確率を$newProb(${newProb * 100}%)に変更しました。"
            )
          } else {
            MessageEffectF("指定されたIDのガチャ景品は存在しません。")
          }
        }

      }

    val createEvent: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration
        .thenParse(Parsers.identity)
        .thenParse(Parsers.identity)
        .thenParse(Parsers.identity)
        .buildWithExecutionCSEffect { context =>
          import shapeless.::
          val e :: startDate :: endDate :: HNil = context.args.parsed
          val eventName = GachaEventName(e)

          val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
          val dateRegex = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])".r

          if (!dateRegex.matches(startDate) || !dateRegex.matches(endDate)) {
            MessageEffectF(s"${RED}開始日/終了日はyyyy-MM-ddの形式で指定してください。")
          } else if (eventName.name.length > 30) {
            MessageEffectF(s"${RED}イベント名は30字以内で指定してください。")
          } else {
            for {
              existsEvent <- Kleisli.liftF(gachaPrizeAPI.existsGachaEvent(eventName))
              _ <- Kleisli.liftF(
                gachaPrizeAPI
                  .createGachaEvent(
                    GachaEvent(
                      eventName,
                      LocalDate.parse(startDate, dateTimeFormatter),
                      LocalDate.parse(endDate, dateTimeFormatter)
                    )
                  )
                  .unlessA(existsEvent)
              )

            } yield {
              if (existsEvent) MessageEffectF(s"${RED}指定された名前のイベントが既に存在します。")
              else MessageEffectF(s"${AQUA}イベントを作成しました。")
            }
          }.flatten

        }

    val deleteEvent: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration
        .thenParse(Parsers.identity)
        .buildWithExecutionCSEffect { context =>
          val eventName = GachaEventName(context.args.parsed.head)

          Kleisli.liftF(gachaPrizeAPI.deleteGachaEvent(eventName)).flatMap { _ =>
            MessageEffectF(s"ガチャイベント: ${eventName.name}を削除しました。")
          }
        }

    private def toTimeString(localDate: LocalDate): String = {
      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      dateTimeFormatter.format(localDate)
    }

    val eventList: ContextualExecutor =
      ContextualExecutorBuilder.beginConfiguration.buildWithExecutionCSEffect { _ =>
        Kleisli.liftF(gachaPrizeAPI.createdGachaEvents).flatMap { events =>
          val messages = "イベント名 | 開始日 | 終了日" +: events.map { event =>
            s"${event.eventName.name} | ${toTimeString(event.startDate)} | ${toTimeString(event.endDate)}"
          }

          MessageEffectF(messages.toList)
        }
      }
  }

}
