package com.github.unchama.seichiassist.subsystems.gacha.bukkit.command

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, Sync, SyncIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrizeId,
  GachaPrizesDataOperations
}
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.GachaTicketPersistence
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

import java.util.UUID

class GachaCommand[F[_]: NonServerThreadContextShift: Sync: ConcurrentEffect](
  implicit gachaTicketPersistence: GachaTicketPersistence[F],
  gachaPrizesDataOperations: GachaPrizesDataOperations[F],
  syncUuidRepository: UuidRepository[SyncIO]
) {

  private val printDescriptionExecutor = EchoExecutor(
    MessageEffect(
      List(
        s"$YELLOW$BOLD/gachaコマンドの使い方",
        s"$RED/gacha give <all/プレイヤー名> <個数>",
        "ガチャ券配布コマンドです。allを指定で全員に配布(マルチ鯖対応済)",
        s"$RED/gacha get <ID> (<名前>)",
        "指定したガチャリストのIDを入手 (所有者付きにもできます) IDを0に指定するとガチャリンゴを入手できます",
        s"$RED/gacha add <確率>",
        "現在のメインハンドをガチャリストに追加。確率は1.0までで指定",
        s"$RED/gacha addms2 <確率> <名前> <レベル>",
        "現在のメインハンドをMineStack用ガチャリストに追加。確率は1.0までで指定",
        s"$RED/gacha addms <名前> <レベル> <ID>",
        "指定したガチャリストのIDを指定した名前とレベル(実際のレベルではないことに注意)でMineStack用ガチャリストに追加",
        s"$DARK_GRAY※ゲーム内でのみ実行できます",
        s"$RED/gacha list",
        "現在のガチャリストを表示",
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
        s"$RED/gacha move <番号> <移動先番号>",
        "リスト該当番号のガチャ景品の並び替えを行う",
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
        s"$RED/gacha demo <回数>",
        "現在のガチャリストで指定回数試行し結果を表示。100万回まで"
      )
    )
  )

  val executor: TabExecutor =
    BranchedExecutor(
      Map("give" -> ChildExecutors.giveGachaTickets),
      whenBranchNotFound = Some(printDescriptionExecutor),
      whenArgInsufficient = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()

  object ChildExecutors {

    val giveGachaTickets: ContextualExecutor = ContextualExecutorBuilder
      .beginConfiguration()
      .argumentsParsers(
        List(
          Parsers.fromOptionParser(
            value =>
              value.toLowerCase match {
                case "all" => Some("all")
                case _ =>
                  val uuid = syncUuidRepository.getUuid(value).unsafeRunSync()
                  if (uuid.nonEmpty)
                    uuid.flatMap(uuid => Some(uuid.toString))
                  else None
              },
            MessageEffect("指定されたプレイヤー名が見つかりませんでした。")
          ),
          Parsers.closedRangeInt(1, Int.MaxValue, MessageEffect("配布するガチャ券の枚数は正の値を指定してください。"))
        )
      )
      .executionCSEffect { context =>
        val args = context.args.parsed
        val amount = args(1).asInstanceOf[Int]
        if (args.head.toString == "all") {
          Kleisli
            .liftF(gachaTicketPersistence.add(amount))
            .flatMap(_ => MessageEffectF(s"${GREEN}全プレイヤーへガチャ券${amount}枚加算成功"))
        } else {
          // Parserによりallじゃなかった場合はUUIDであることが確定している
          Kleisli
            .liftF(gachaTicketPersistence.add(amount, UUID.fromString(args.head.toString)))
            .flatMap(_ => MessageEffectF(s"${GREEN}ガチャ券${amount}枚加算成功"))
        }
      }
      .build()

    val giveItem: ContextualExecutor =
      playerCommandBuilder
        .argumentsParsers(
          List(Parsers.integer(MessageEffect("IDは整数値で指定してください。")), Parsers.identity)
        )
        .execution { context =>
          gachaPrizesDataOperations.getGachaPrize(
            GachaPrizeId(context.args.parsed.head.asInstanceOf[Int])
          )
        }

  }

}
