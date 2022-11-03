package com.github.unchama.seichiassist.subsystems.gacha.bukkit

import cats.data.Kleisli
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.ParserResponse.{failWith, succeedWith}
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.SeichiAssist.databaseGateway
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.data.MineStackGachaData
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.BukkitGrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.PlayerName
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.{GachaTicketAmount, GrantResultOfGachaTicketFromAdminTeam}
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizePrizePrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain._
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{GachaEvent, GachaEventName}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.{GachaPrize, GachaPrizeId}
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, TabExecutor}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

class GachaCommand[
  F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect
](
   implicit gachaPersistence: GachaPrizeListPersistence[F, ItemStack],
   gachaAPI: GachaPrizePrizePrizeAPI[F, ItemStack, Player],
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
        "現在のメインハンドをガチャリストに追加。確率は1.0までで指定",
        s"$DARK_GRAY※イベント名を入力しなかった場合は通常排出アイテムとして登録されます。",
        s"$RED/gacha addms2 <確率> <名前>",
        "現在のメインハンドをMineStack用ガチャリストに追加。確率は1.0までで指定",
        s"$RED/gacha addms <名前>  <ID>",
        "指定したガチャリストのIDを指定した名前でMineStack用ガチャリストに追加",
        s"$DARK_GRAY※ゲーム内でのみ実行できます",
        s"$RED/gacha list (<イベント>)",
        "指定したイベントのガチャリストを表示",
        s"$DARK_GRAY※イベント名を指定しなかった場合は通常排出アイテムを表示します",
        s"$RED/gacha listms",
        "現在のMineStack用ガチャリストを表示",
        s"$RED/gacha remove <番号>",
        "リスト該当番号のガチャ景品を削除",
        s"$RED/gacha removems",
        "リスト一番下のMineStackガチャ景品を削除(追加失敗した場合の修正用)",
        s"$RED/gacha setamount <番号> <個数>",
        "リスト該当番号のガチャ景品の個数変更。64まで",
        s"$RED/gacha setprob <番号> <確率>",
        "リスト該当番号のガチャ景品の確率変更",
        s"$RED/gacha clear",
        "ガチャリストを全消去する。取扱注意",
        s"$RED/gacha save",
        "コマンドによるガチャリストへの変更をmysqlに送信",
        s"$RED/gacha savems",
        "コマンドによるMineStack用ガチャリストへの変更をmysqlに送信",
        s"$DARK_RED※変更したら必ずsaveコマンドを実行(セーブされません)",
        s"$RED/gacha reload",
        "ガチャリストをmysqlから読み込む",
        s"$DARK_GRAY※onEnable時と同じ処理",
        s"$RED/gacha create-event <イベント名> <開始日> <終了日>",
        "日付はyyyy-MM-ddの形式で指定をしてください。",
        s"$DARK_GRAY※通常排出のガチャ景品リストがコピーされます。",
        s"${DARK_GRAY}置き換えたいアイテムを置き換えてイベント排出景品を設定してください。",
        s"$RED/gacha delete-event <イベント名>",
        "イベントを削除します。(間違ってイベントを作成した時以外に使わないでください。)",
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
        "list-event" -> eventList,
        "addms" -> addms,
        "addms2" -> addms2,
        "listms" -> listms,
        "removems" -> removems,
        "savems" -> savems
      ),
      whenBranchNotFound = Some(printDescriptionExecutor),
      whenArgInsufficient = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()
  }

  object ChildExecutors {

    val gachaPrizeIdExistsParser: String => Either[TargetedEffect[CommandSender], Any] = Parsers
      .closedRangeInt(1, Int.MaxValue, MessageEffect("IDは正の値を指定してください。"))
      .andThen(_.flatMap { id =>
        val intId = id.asInstanceOf[Int]
        if (gachaAPI.existsGachaPrize(GachaPrizeId(intId)).toIO.unsafeRunSync()) {
          succeedWith(intId)
        } else {
          failWith("指定されたIDのアイテムは存在しません！")
        }
      })

    val probabilityParser: String => Either[TargetedEffect[CommandSender], Any] =
      Parsers.double(MessageEffect("確率は小数点数で指定してください。")).andThen {
        _.flatMap { num =>
          val doubleNum = num.asInstanceOf[Double]
          if (doubleNum <= 1.0 && doubleNum >= 0.0) {
            succeedWith(doubleNum)
          } else {
            failWith("確率は正の数かつ1.0以下で指定してください。")
          }
        }
      }

    val giveGachaTickets: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration()
      .argumentsParsers(
        List(
          Parsers.identity,
          Parsers.closedRangeInt(1, Int.MaxValue, MessageEffect("配布するガチャ券の枚数は正の値を指定してください。"))
        )
      )
      .executionCSEffect { context =>
        val args = context.args.parsed
        val amount = args(1).asInstanceOf[Int]
        val gachaTicketAmount = GachaTicketAmount(amount)
        args.head.toString match {
          case "all" =>
            Kleisli
              .liftF(gachaTicketAPI.addToAllKnownPlayers(gachaTicketAmount))
              .flatMap(_ => MessageEffectF(s"${GREEN}全プレイヤーへガチャ券${amount}枚加算成功"))
          case value =>
            val uuidRegex =
              "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r

            (if (uuidRegex.matches(value)) {
               Kleisli.liftF[F, CommandSender, GrantResultOfGachaTicketFromAdminTeam](
                 gachaTicketAPI.addByUUID(gachaTicketAmount, UUID.fromString(value))
               )
             } else {
               Kleisli.liftF[F, CommandSender, GrantResultOfGachaTicketFromAdminTeam](
                 gachaTicketAPI.addByPlayerName(gachaTicketAmount, PlayerName(value))
               )
             }).flatMap {
              case GrantResultOfGachaTicketFromAdminTeam.Success =>
                MessageEffectF(s"${GREEN}ガチャ券${amount}枚加算成功")
              case GrantResultOfGachaTicketFromAdminTeam.NotExists =>
                MessageEffectF(s"${RED}プレイヤーが存在しません。")
              case GrantResultOfGachaTicketFromAdminTeam.GrantedToMultiplePlayers =>
                MessageEffectF(s"${RED}加算は成功しましたが、複数プレイヤーが存在しました。")
            }
        }
      }
      .build()

    val giveItem: ContextualExecutor =
      playerCommandBuilder
        .argumentsParsers(List(gachaPrizeIdExistsParser))
        .execution { context =>
          val eff = for {
            gachaPrize <- gachaAPI.fetch(
              GachaPrizeId(context.args.parsed.head.asInstanceOf[Int])
            )
            _ <- new BukkitGrantGachaPrize[F]().grantGachaPrize(gachaPrize.get)(context.sender)
          } yield MessageEffect("ガチャアイテムを付与しました。")

          eff.toIO
        }
        .build()

    val add: ContextualExecutor =
      playerCommandBuilder
        .argumentsParsers(List(probabilityParser, Parsers.identity))
        .execution { context =>
          val player = context.sender
          val args = context.args.parsed
          val probability = args.head.asInstanceOf[Double]
          val eventName =
            Option.when(args(1).toString != null)(GachaEventName(args(1).toString))
          val mainHandItem = player.getInventory.getItemInMainHand
          val eff = for {
            _ <- gachaAPI.addGachaPrize(
              GachaPrize(
                mainHandItem,
                GachaProbability(probability),
                probability < 0.1,
                _,
                eventName
              )
            )
          } yield MessageEffect(
            List("ガチャアイテムを追加しました！", "ガチャアイテムを保存するためには/gacha saveを実行してください。")
          )

          eff.toIO
        }
        .build()

    val list: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .execution { context =>
          val eventName = context.args.yetToBeParsed.headOption.map(GachaEventName)
          val eff = for {
            gachaPrizes <- gachaAPI.list
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
        .build()

    val remove: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration()
      .argumentsParsers(List(gachaPrizeIdExistsParser))
      .execution { context =>
        val eff = for {
          _ <- gachaAPI.removeByGachaPrizeId(
            GachaPrizeId(context.args.parsed.head.asInstanceOf[Int])
          )
        } yield MessageEffect(
          List("ガチャアイテムを削除しました", "ガチャアイテム削除を永続化するためには/gacha saveを実行してください。")
        )

        eff.toIO

      }
      .build()

    val setAmount: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .argumentsParsers(
          List(
            gachaPrizeIdExistsParser,
            Parsers.closedRangeInt(1, 64, MessageEffect("数は1～64で指定してください。"))
          )
        )
        .execution { context =>
          val targetId = GachaPrizeId(context.args.parsed.head.asInstanceOf[Int])
          val amount = context.args.parsed(1).asInstanceOf[Int]
          val eff = for {
            existingGachaPrize <- gachaAPI.fetch(targetId)
            _ <- gachaAPI.removeByGachaPrizeId(targetId)
            itemStack = existingGachaPrize.get.itemStack
            _ <- gachaAPI.addGachaPrize(_ =>
              existingGachaPrize
                .get
                .copy(itemStack = itemStack.tap {
                  _.setAmount(amount)
                })
            )
          } yield MessageEffect(
            s"${targetId.id}|${itemStack.getType.toString}/${itemStack.getItemMeta.getDisplayName}${RESET}のアイテム数を${amount}個に変更しました。"
          )

          eff.toIO
        }
        .build()

    val setProbability: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration()
      .argumentsParsers(List(gachaPrizeIdExistsParser, probabilityParser))
      .execution { context =>
        val args = context.args.parsed
        val targetId = GachaPrizeId(args.head.asInstanceOf[Int])
        val newProb = args(1).asInstanceOf[Double]
        val eff = for {
          existingGachaPrize <- gachaAPI.fetch(targetId)
          _ <- gachaAPI.removeByGachaPrizeId(targetId)
          _ <- gachaAPI.addGachaPrize(_ =>
            existingGachaPrize.get.copy(probability = GachaProbability(newProb))
          )
          itemStack = existingGachaPrize.get.itemStack
        } yield MessageEffect(s"${targetId.id}|${itemStack.getType.toString}/${itemStack
            .getItemMeta
            .getDisplayName}${RESET}の確率を$newProb(${newProb * 100}%)に変更しました。")

        eff.toIO
      }
      .build()

    val clear: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .execution { _ =>
          val eff = for {
            _ <- gachaAPI.clear
          } yield MessageEffect(
            List(
              "すべて削除しました。",
              "/gacha saveを実行するとmysqlのデータも全削除されます。",
              "削除を取り消すには/gacha reloadコマンドを実行します。"
            )
          )
          eff.toIO

        }
        .build()

    val save: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .execution { _ =>
          val eff = for {
            gachaPrizes <- gachaAPI.list
            _ <- gachaPersistence.set(gachaPrizes)
          } yield MessageEffect("ガチャデータをmysqlに保存しました。")

          eff.toIO
        }
        .build()

    val reload: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration()
      .execution { _ =>
        val eff = for {
          _ <- gachaAPI.load
        } yield MessageEffect("ガチャデータをmysqlから読み込みました。")

        eff.toIO
      }
      .build()

    val createEvent: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .argumentsParsers {
          List(Parsers.identity, Parsers.identity, Parsers.identity)
        }
        .execution { values =>
          val parsedArgs = values.args.parsed
          val eventName = GachaEventName(parsedArgs.head.toString)

          val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
          val startDate = parsedArgs(1).toString
          val endDate = parsedArgs.last.toString
          val dateRegex = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])".r

          if (!dateRegex.matches(startDate) || !dateRegex.matches(endDate)) {
            IO(MessageEffect(s"${RED}開始日/終了日はyyyy-MM-ddの形式で指定してください。"))
          } else {
            val eff = for {
              isExistsEvent <- gachaAPI.isExistsGachaEvent(eventName)
              _ <- gachaAPI
                .createGachaEvent(
                  GachaEvent(
                    eventName,
                    LocalDate.parse(startDate, dateTimeFormatter),
                    LocalDate.parse(endDate, dateTimeFormatter)
                  )
                )
                .whenA(!isExistsEvent)

            } yield {
              if (isExistsEvent) MessageEffect(s"${RED}指定されたイベント名のイベントが存在します。")
              else MessageEffect(s"${AQUA}イベントを作成しました。")
            }

            eff.toIO
          }
        }
        .build()

    val deleteEvent: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .argumentsParsers(List(Parsers.identity))
        .execution { values =>
          val eventName = GachaEventName(values.args.parsed.head.toString)
          val eff = for {
            _ <- gachaAPI.deleteGachaEvent(eventName)
          } yield MessageEffect(s"ガチャイベント: ${eventName.name}を削除しました。")

          eff.toIO
        }
        .build()

    val eventList: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .execution { _ =>
          val eff = for {
            events <- gachaAPI.createdGachaEvents
          } yield {
            val messages = "イベント名 | 開始日 | 終了日" +: events.map { event =>
              s"${event.eventName} | ${event.getStartDateString} | ${event.getEndDateString}"
            }
            MessageEffect(messages.toList)
          }

          eff.toIO
        }
        .build()

    // TODO: ここから下のコマンドの実装はMineStackシステムがレガシーのときに行われているため、旧実装をそのままなぞらえて実装している。
    //  そのため、MineStackシステムがsubsystemsに含まれるときが来たら書き換えることが望ましい。
    //  というかそもそもこの実装はMineStack側で行われるべきであるかもしれない。

    val addms: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration()
      .argumentsParsers(List(Parsers.identity, gachaPrizeIdExistsParser))
      .execution { context =>
        val args = context.args.parsed
        val eff = for {
          gachaPrize <- gachaAPI.fetch(GachaPrizeId(args(1).asInstanceOf[Int]))
        } yield {
          val _gachaPrize = gachaPrize.get // ParserによりGachaPrizeの存在は確認されている
          val mineStackGachaData = new MineStackGachaData(
            args.head.toString,
            _gachaPrize.itemStack,
            _gachaPrize.probability.value,
            1
          )
          SeichiAssist.msgachadatalist.addOne(mineStackGachaData)
          MessageEffect(
            List(
              s"ガチャID:${_gachaPrize.id}のデータを変数名:${args.head.toString}でMineStack用ガチャリストに追加しました。",
              "/gacha savemsでmysqlに保存してください"
            )
          )
        }

        eff.toIO
      }
      .build()

    val addms2: ContextualExecutor =
      playerCommandBuilder
        .argumentsParsers(List(probabilityParser, Parsers.identity))
        .execution { context =>
          val args = context.args.parsed
          val mainHand = context.sender.getInventory.getItemInMainHand
          val probability = args.head.asInstanceOf[Double]
          val mineStackGachaData =
            new MineStackGachaData(args(1).toString, mainHand, probability, 1)
          SeichiAssist.msgachadatalist.addOne(mineStackGachaData)
          IO(
            MessageEffect(
              List(
                s"${mainHand.getType.toString}/${mainHand.getItemMeta.getDisplayName}$RESET${mainHand.getAmount}個を確率${probability}としてMineStack用ガチャリストに追加しました。",
                "/gacha savemsでmysqlに保存してください。"
              )
            )
          )
        }
        .build()

    val listms: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .execution { _ =>
          IO {
            val gachaDataListInformation = SeichiAssist.msgachadatalist.zipWithIndex.map {
              case (gachaData, index) =>
                s"$index|${gachaData.level}|${gachaData.objName}|${gachaData.itemStack.getType.toString}/${gachaData.itemStack.getItemMeta.getDisplayName}$RESET|${gachaData
                    .itemStack
                    .getAmount}|${gachaData.probability}(${gachaData.probability * 100}%)"
            }
            MessageEffect(
              List(s"${RED}アイテム番号|レベル|変数名|アイテム名|アイテム数|出現確率") ++ gachaDataListInformation
            )
          }
        }
        .build()

    val removems: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .execution { _ =>
          IO {
            if (SeichiAssist.msgachadatalist.isEmpty) {
              MessageEffect("MineStack用ガチャリストが空です。")
            } else {
              val size = SeichiAssist.msgachadatalist.size
              val mineStackGachaData = SeichiAssist.msgachadatalist(size - 1)
              SeichiAssist.msgachadatalist.remove(size - 1)
              MessageEffect(
                List(
                  s"$size|${mineStackGachaData.level}|${mineStackGachaData.objName}|${mineStackGachaData.itemStack.getType.toString}/${mineStackGachaData
                      .itemStack
                      .getItemMeta
                      .getDisplayName}$RESET|${mineStackGachaData.itemStack.getAmount}|${mineStackGachaData.probability}を削除しました。",
                  "/gacha savemsでmysqlに保存してください。"
                )
              )
            }
          }
        }
        .build()

    val savems: ContextualExecutor =
      ContextualExecutorBuilder
        .beginConfiguration()
        .execution { _ =>
          IO {
            if (!databaseGateway.mineStackGachaDataManipulator.saveMineStackGachaData)
              MessageEffect("mysqlにMineStack用ガチャデータを保存できませんでした")
            else MessageEffect("mysqlにMineStack用ガチャデータを保存しました")
          }
        }
        .build()

  }

}
