package com.github.unchama.seichiassist.subsystems.present.bukkit.command

import cats.Monad
import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, IO}
import cats.implicits._
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.domain.actions.UuidToLastSeenName
import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import com.github.unchama.seichiassist.subsystems.present.infrastructure.JdbcBackedPresentPersistence
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import org.bukkit.{ChatColor, Material}
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * `/present` コマンドを定義する。
 *
 * `/present help`とこのファイルのドキュメントに矛盾がある場合、このファイルのドキュメントが優先する。
 *
 * 別に表記がない限り、この実装は以下の条件を満たす:
 *   - 存在しないプレゼントIDの指定は必ずエラーになる。
 *   - 操作が成功しなかったときは適切なメッセージを表示する。
 */
object PresentCommand {
  private val presentIdParser = Parsers.integer(
    MessageEffect("presentコマンドに与えるプレゼントIDは整数である必要があります。")
  )

  private val presentScopeModeParser = Parsers.fromOptionParser({ arg1: String =>
    arg1 match {
      // enum match
      case "player" | "all" => Some(arg1)
      case _ => None
    }
  }, MessageEffect("presentコマンドで対象を指定する際のモードは、playerまたはallを指定してください。"))

  private val noPermissionMessage = MessageEffect("You don't have the permission.")

  /**
   * 概要: メインハンドに持っているアイテムをプレゼントとして定義する。
   * プレイヤーがプレゼントを受け取ることができるようになるには、必ずプレゼントを定義しなければならない。
   *
   * 権限ノード: `seichiassist.present.define`
   *
   * 構文:
   *   - /present define
   *
   * 出力: 定義が成功した場合は、割り振られたアイテムのIDを表示する。失敗した場合は、適切なエラーメッセージを表示する。
   */
  private def defineExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit persistence: JdbcBackedPresentPersistence[F]) =
    playerCommandBuilder
      .argumentsParsers(List())
      .execution { context =>
        val player = context.sender
        if (!player.hasPermission("seichiassist.present.define")) {
          IO.pure(noPermissionMessage)
        } else {
          val mainHandItem = player.getInventory.getItemInMainHand
          if (mainHandItem.getType eq Material.AIR) {
            // おそらくこれは意図した動作ではないのでエラーメッセージを表示する
            IO.pure(MessageEffect("メインハンドに何も持っていません。プレゼントを定義するためには、メインハンドに対象アイテムを持ってください。"))
          } else {
            val eff = for {
              _ <- NonServerThreadContextShift[F].shift
              presentID <- persistence.define(mainHandItem)
            } yield {
              MessageEffect(s"メインハンドに持ったアイテムをプレゼントとして定義しました。IDは${presentID}です。")
            }
            eff.toIO
          }
        }

      }
      .build()

  /**
   * 概要: 指定したプレゼントを消去する。対応が失われるため、このコマンドの実行が完了した後、プレイヤーはそのプレゼントを受け取ることができなくなる。
   *
   * 権限ノード: `seichiassist.present.delete`
   *
   * 出力: 操作の結果とそれに伴うメッセージ。
   */
  private def deleteExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit persistence: JdbcBackedPresentPersistence[F]) =
    playerCommandBuilder
      .argumentsParsers(List(presentIdParser))
      .execution { context =>
        if (!context.sender.hasPermission("seichiassist.present.delete")) {
          IO.pure(noPermissionMessage)
        } else {
          val presentId = context.args.parsed.head.asInstanceOf[Int]
          for {
            _ <- NonServerThreadContextShift[F].shift
            _ <- persistence.delete(presentId)
          } yield MessageEffect(s"IDが${presentId}のプレゼントの消去は正常に行われました。")
        }
      }
      .build()

  /**
   * 概要: プレイヤーが指定されたプレゼントを受け取れるようにする。
   *
   * 権限ノード: `seichiassist.present.grant`
   *
   * 出力: 操作の結果とそれに伴うメッセージ
   *
   * コマンド構文:
   *
   *   - /present grant &lt;presentId: PresentID&gt; player &lt;...players^†^: PlayerName&gt;
   *   - /present grant &lt;presentId: PresentID&gt; all
   *
   * 備考:
   *   - †: スペース区切り。
   */
  private def grantRightExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit persistence: JdbcBackedPresentPersistence[F], globalPlayerAccessor: UuidToLastSeenName[F]) =
    playerCommandBuilder
      .argumentsParsers(
        List(
          presentIdParser,
          presentScopeModeParser,
        )
      )
      .execution { context =>
        if (!context.sender.hasPermission("seichiassist.present.grant")) {
          IO.pure(noPermissionMessage)
        } else {
          // Parserを通した段階でargs[0]は "player" | "all" になっているのでこれでOK
          val List(_presentId, mode) = context.args.parsed
          val presentId = _presentId.asInstanceOf[Int]
          val isGlobal = mode.asInstanceOf[String] == "all"
          val eff = for {
            _ <- NonServerThreadContextShift[F].shift
            // TODO: 以下の処理は多分共通化できるがうまい方法が思いつかない
            globalUUID2Name <- globalPlayerAccessor.entries
            // 可変長引数には対応していないので`yetToBeParsed`を使う
            restArg = context.args
              // プレイヤー名は /[A-Za-z0-9_]{,16}/であるため空白が誤って解釈されることはない
              .yetToBeParsed
              // 連続した空白を消去
              .filter(_.nonEmpty)
            target = if (isGlobal)
              globalUUID2Name.keys
            else
              globalUUID2Name.filter { case (_, name) => restArg.contains(name) }.keys
            errorIfNobody = if (target.isEmpty) Some(MessageEffect("対象のプレイヤーが存在しません！")) else None
            _ <- persistence.grant(presentId, target.toSet)
          } yield errorIfNobody.getOrElse(MessageEffect(s"プレゼントIDが${presentId}のプレゼントを受け取れるプレイヤーを追加することに成功しました。"))

          eff.toIO
        }
      }
      .build()

  /**
   * 概要: プレイヤーがプレゼントを受け取れないようにする。
   *
   * 権限ノード: `seichiassist.present.revoke`
   *
   * 構文:
   *   - /present revoke &lt;presentId: PresentID&gt; player &lt;...players^✝^: PlayerName&gt;
   *   - /present revoke &lt;presentId: PresentID&gt; all
   *
   * 出力: 操作の結果とそれに伴うメッセージ。
   *
   * 備考:
   *   - ✝: スペース区切り。
   */
  private def revokeRightExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit persistence: JdbcBackedPresentPersistence[F], globalPlayerAccessor: UuidToLastSeenName[F]) =
    playerCommandBuilder
      .argumentsParsers(
        List(
          presentIdParser,
          presentScopeModeParser,
        )
      )
      .execution { context =>
        if (!context.sender.hasPermission("seichiassist.present.revoke")) {
          IO.pure(noPermissionMessage)
        } else {
          val args = context.args
          val presentId = args.parsed.head.asInstanceOf[Int]
          val isGlobal = args.parsed(1).asInstanceOf[String] == "all"
          val eff = for {
            _ <- NonServerThreadContextShift[F].shift
            globalUUID2Name <- globalPlayerAccessor.entries
            // 可変長引数には対応していないので`yetToBeParsed`を使う
            restArg = args
              // プレイヤー名は /[A-Za-z0-9_]{,16}/であるため空白が誤って解釈されることはない
              .yetToBeParsed
              // 連続した空白を消去
              .filter(_.nonEmpty)
            target = if (isGlobal)
              globalUUID2Name.keys
            else
              globalUUID2Name.filter { case (_, name) => restArg.contains(name) }.keys
            errorIfNobody = if (target.isEmpty) Some(MessageEffect("対象のプレイヤーが存在しません！")) else None
            _ <- persistence.revoke(presentId, target.toSet)
          } yield {
            errorIfNobody.getOrElse(
              MessageEffect(s"プレゼントIDが${presentId}のプレゼントを受け取ることができるプレイヤーの削除に成功しました。")
            )
          }
          eff.toIO
        }
      }
      .build()

  /**
   * 概要: 指定されたIDのプレゼントを受け取れるかどうかテストする。
   * 受け取れる場合は、プレイヤーのインベントリにアイテムを追加する。
   * 受け取れない場合は、エラーメッセージを表示する。
   *
   * 構文:
   *   - /present claim &lt;presentId: PresentID&gt;
   *
   * 出力: 受け取った場合は、その旨表示する。失敗した場合は、適切なエラーメッセージを表示する。
   */
  private def claimExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit persistence: JdbcBackedPresentPersistence[F]) =
    playerCommandBuilder
      .argumentsParsers(List(presentIdParser))
      .execution { context =>
        val player = context.sender.getUniqueId
        val presentId = context.args.parsed.head.asInstanceOf[Int]

        // この明示的な型変数の指定は必要
        val eff: F[TargetedEffect[Player]] = for {
          _ <- NonServerThreadContextShift[F].shift
          states <- persistence.fetchState(player)
          claimState = states.getOrElse(presentId, PresentClaimingState.Unavailable)
          effect <- claimState match {
            case PresentClaimingState.Claimed =>
              Monad[F].pure(MessageEffect(s"ID: ${presentId}のプレゼントはすでに受け取っています。"))
            case PresentClaimingState.NotClaimed =>
              for {
                _ <- persistence.markAsClaimed(presentId, player)
                item <- persistence.lookup(presentId)
              } yield {
                // 注釈: この明示的な型変数の指定は必要
                // see: https://discord.com/channels/237758724121427969/565935041574731807/823495317499805776
                item.fold[TargetedEffect[Player]](
                  MessageEffect(s"ID: ${presentId}のプレゼントは存在しません。IDをお確かめください。")
                ) { item =>
                  SequentialEffect(
                    Util.grantItemStacksEffect(item),
                    MessageEffect(s"ID: ${presentId}のプレゼントを付与しました。")
                  )
                }
              }
            case PresentClaimingState.Unavailable =>
              Monad[F].pure(MessageEffect(s"ID: ${presentId}のプレゼントは存在しないか、あるいは配布対象ではありません。"))
          }
        } yield effect

        eff.toIO
      }
      .build()

  /**
   * 概要: 全てのプレゼントのうち、実行プレイヤーが対象となっているプレゼントの受け取り状況を表示する。
   * 実行プレイヤーが対象ではないプレゼントは表示されない。
   *
   * 構文:
   *   - /present state
   */
  private def showStateExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit persistence: JdbcBackedPresentPersistence[F]) = playerCommandBuilder
    .execution { context =>
      val eff = for {
        // off-main-thread
        _ <- NonServerThreadContextShift[F].shift
        state <- persistence.fetchState(context.sender.getUniqueId)
      } yield {
        val mes = state
          .toList
          // 配布対象外のプレゼントを除外
          .filter { case (_, state) => state != PresentClaimingState.Unavailable }
          .map { case (id, state) =>
            s"ID=$id: ${decoratePresentState(state)}"
          }
          .filter(_.nonEmpty)
        MessageEffect(mes)
      }

      eff.toIO
    }
    .build()

  /**
   * 概要: 実行プレイヤーと全てのプレゼントの受け取り状況をページネーションと共に表示する
   *
   * 構文:
   *
   *   - /present list &lt;page: PositiveInt&gt;
   */
  private def listExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit persistence: JdbcBackedPresentPersistence[F]) =
    playerCommandBuilder
      .argumentsParsers(List(Parsers.closedRangeInt(1, Int.MaxValue, MessageEffect("ページ数には1以上の数を指定してください。"))))
      .execution { context =>
        val perPage: Int Refined Positive = 10
        val page = refineV[Positive](context.args.parsed.head.asInstanceOf[Int]) match {
          // argumentsParsersで1以上を指定しているのでここでコケることはないはず
          case Left(l) => throw new AssertionError(s"positive int: failed. message: $l")
          case Right(v) => v
        }
        val player = context.sender.getUniqueId
        val eff = for {
          _ <- NonServerThreadContextShift[F].shift
          states <- persistence.fetchStateWithPagination(player, perPage, page)
          messageLine = states
            .map { case (id, state) =>
              s"ID=$id: ${decoratePresentState(state)}"
            }
            .toList
        } yield {
          MessageEffect(messageLine)
        }
        eff.toIO
      }
      .build()

  /**
   * 概要: マニュアルを表示する。
   *
   * 引数:
   *   - /present help
   */
  def helpExecutor: ContextualExecutor = new EchoExecutor(
    MessageEffect(
      List(
        "/present define",
        "    プレゼントを定義します",
        "/present delete",
        "    プレゼントを削除します",
        "/present grant",
        "    プレゼントを受け取れるプレイヤーを追加します",
        "/present revoke",
        "    プレゼントを受け取れるプレイヤーを削除します",
        "/present claim",
        "    プレゼントを受け取ります",
        "/present list",
        "    全てのプレゼントを表示します",
        "/present state",
        "    対象となっている全てのプレゼントを表示します",
        "/present help",
        "    このメッセージを表示します"
      )
    )
  )

  def executor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit persistence: JdbcBackedPresentPersistence[F], globalPlayerAccessor: UuidToLastSeenName[F]): TabExecutor = BranchedExecutor(
    Map(
      "define" -> defineExecutor,
      "delete" -> deleteExecutor,
      "grant" -> grantRightExecutor,
      "revoke" -> revokeRightExecutor,
      "claim" -> claimExecutor,
      "list" -> listExecutor,
      "state" -> showStateExecutor,
      "help" -> helpExecutor,
    ),
    Some(helpExecutor),
    Some(helpExecutor)
  ).asNonBlockingTabExecutor()

  private def decoratePresentState(state: PresentClaimingState): String = state match {
    case PresentClaimingState.Claimed => s"${ChatColor.GOLD}受け取り済み"
    case PresentClaimingState.NotClaimed => s"${ChatColor.GREEN}受け取り可能"
    case PresentClaimingState.Unavailable => s"${ChatColor.GRAY}配布対象外"
  }
}
