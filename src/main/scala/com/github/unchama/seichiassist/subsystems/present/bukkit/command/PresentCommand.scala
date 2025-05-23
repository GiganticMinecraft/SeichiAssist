package com.github.unchama.seichiassist.subsystems.present.bukkit.command

import cats.data.Kleisli
import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, Sync}
import cats.implicits._
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.{
  BranchedExecutor,
  EchoExecutor,
  TraverseExecutor
}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.domain.actions.UuidToLastSeenName
import com.github.unchama.seichiassist.subsystems.present.domain.OperationResult.DeleteResult
import com.github.unchama.seichiassist.subsystems.present.domain._
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffectF}
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{ChatColor, Material}
import shapeless.HNil

/**
 * `/present` コマンドを定義する。
 *
 * `/present help`とこのファイルのドキュメントに矛盾がある場合、このファイルのドキュメントが優先する。
 *
 * 別に表記がない限り、この実装は以下の条件を満たす:
 *   - 存在しないプレゼントIDの指定は必ずエラーになる。
 *   - 操作が成功しなかったときは適切なメッセージを表示する。
 *
 * TODO: 型パラメータが関数のあちこちに散りばめられているが、classの型引数に集約できそう
 */
class PresentCommand {
  private val presentIdParser =
    Parsers.integer(MessageEffect("presentコマンドに与えるプレゼントIDは整数である必要があります。"))

  private val presentScopeModeParser = Parsers.fromOptionParser(
    { arg1: String =>
      arg1 match {
        // enum match
        case "player" | "all" => Some(arg1)
        case _                => None
      }
    },
    MessageEffect("presentコマンドで対象を指定する際のモードは、playerまたはallを指定してください。")
  )

  private def noPermissionMessage[F[_]: Sync] =
    MessageEffectF[F]("You don't have the permission.")

  private object SubCommands {
    object State {
      val help: EchoExecutor = EchoExecutor(
        MessageEffect(List("/present state", "    対象となっている全てのプレゼントを表示します"))
      )

      /**
       * 概要: 全てのプレゼントのうち、実行プレイヤーが対象となっているプレゼントの受け取り状況を表示する。 実行プレイヤーが対象ではないプレゼントは表示されない。
       *
       * 構文:
       *   - /present state
       */
      def executor[F[_]: ConcurrentEffect: NonServerThreadContextShift](
        implicit persistence: PresentPersistence[F, ItemStack]
      ): ContextualExecutor = playerCommandBuilder.buildWith { context =>
        val eff = for {
          // off-main-thread
          _ <- NonServerThreadContextShift[F].shift
          state <- persistence.fetchState(context.sender.getUniqueId)
        } yield {
          val presents = state
            .toList
            // 配布対象外のプレゼントを除外
            .filter { case (_, state) => state != PresentClaimingState.Unavailable }
            .map {
              case (id, state) =>
                s"ID=$id: ${decoratePresentState(state)}"
            }
            .filter(_.nonEmpty)

          val lines = if (presents.isEmpty) {
            List("対象のプレゼントが存在しません")
          } else {
            List(
              s"${ChatColor.GRAY}${ChatColor.UNDERLINE}対象のプレゼント一覧：${ChatColor.RESET}"
            ) ::: presents
          }

          MessageEffect(lines)
        }

        eff.toIO
      }
    }

    object ListSubCommand {
      val help: EchoExecutor = EchoExecutor(
        MessageEffect(List("/present list <ページ数>", "    全てのプレゼントをページに分けて表示します"))
      )

      /**
       * 概要: 実行プレイヤーと全てのプレゼントの受け取り状況をページネーションと共に表示する
       *
       * 構文:
       *
       *   - /present list &lt;page: PositiveInt&gt;
       */
      def executor[F[_]: ConcurrentEffect: NonServerThreadContextShift](
        implicit persistence: PresentPersistence[F, ItemStack]
      ): ContextualExecutor =
        playerCommandBuilder
          .thenParse(
            Parsers.closedRangeInt[Int Refined Positive](
              1,
              Int.MaxValue,
              MessageEffect("ページ数には1以上の数を指定してください。")
            )
          )
          .ifArgumentsMissing(help)
          .buildWith { context =>
            val perPage: Int Refined Positive = 10
            val page = context.args.parsed.head
            val player = context.sender.getUniqueId
            val eff = for {
              _ <- NonServerThreadContextShift[F].shift
              states <- persistence.fetchStateWithPagination(player, perPage, page)
              messageLine = states.fold(
                {
                  case PaginationRejectReason.TooLargePage(max) =>
                    List(s"ページ数が大きすぎます。${max}ページ以下にしてください")
                  case PaginationRejectReason.Empty =>
                    List(s"プレゼントが定義されていません。プレゼントを定義するには/present defineを使用してください。")
                },
                b =>
                  b.sortBy(_._1).map {
                    case (id, state) =>
                      s"ID=$id: ${decoratePresentState(state)}"
                  }
              )
            } yield {
              MessageEffect(messageLine)
            }
            eff.toIO
          }
    }

    object Claim {
      val help: EchoExecutor = EchoExecutor(
        MessageEffect(List("/present claim <プレゼントID>", "    プレゼントを受け取ります"))
      )

      /**
       * 概要: 指定されたIDのプレゼントを受け取れるかどうかテストする。 受け取れる場合は、プレイヤーのインベントリにアイテムを追加する。
       * 受け取れない場合は、エラーメッセージを表示する。
       *
       * 構文:
       *   - /present claim &lt;presentId: PresentID&gt;
       *
       * 出力: 受け取った場合は、その旨表示する。失敗した場合は、適切なエラーメッセージを表示する。
       */
      def executor[
        F[_]: ConcurrentEffect: NonServerThreadContextShift: OnMinecraftServerThread
      ](implicit persistence: PresentPersistence[F, ItemStack]): ContextualExecutor =
        playerCommandBuilder
          .thenParse(presentIdParser)
          .ifArgumentsMissing(help)
          .buildWithExecutionCSEffect { context =>
            val player = context.sender.getUniqueId
            val presentId = context.args.parsed.head

            (for {
              _ <- Kleisli.liftF(NonServerThreadContextShift[F].shift)
              states <- Kleisli.liftF(persistence.fetchState(player))
            } yield {
              val claimState = states.getOrElse(presentId, PresentClaimingState.Unavailable)

              claimState match {
                case PresentClaimingState.Claimed =>
                  MessageEffectF[F](s"ID: ${presentId}のプレゼントはすでに受け取っています。")
                case PresentClaimingState.NotClaimed =>
                  Kleisli
                    .liftF(for {
                      _ <- persistence.markAsClaimed(presentId, player)
                      item <- persistence.lookup(presentId)
                    } yield item)
                    .flatMap { item =>
                      item.fold[TargetedEffectF[F, Player]](
                        MessageEffectF[F](s"ID: ${presentId}のプレゼントは存在しません。IDをお確かめください。")
                      ) { item =>
                        SequentialEffect(
                          InventoryOperations.grantItemStacksEffect[F](item),
                          MessageEffectF[F](s"ID: ${presentId}のプレゼントを付与しました。")
                        )
                      }
                    }
                case PresentClaimingState.Unavailable =>
                  MessageEffectF[F](s"ID: ${presentId}のプレゼントは存在しないか、あるいは配布対象ではありません。")
              }
            }).flatten
          }
    }

    object Define {
      val help: EchoExecutor = EchoExecutor(
        MessageEffect(List("/present define", "    プレゼントを手に持っているアイテムで定義します"))
      )

      /**
       * 概要: メインハンドに持っているアイテムをプレゼントとして定義する。 プレイヤーがプレゼントを受け取ることができるようになるには、必ずプレゼントを定義しなければならない。
       *
       * 権限ノード: `seichiassist.present.define`
       *
       * 構文:
       *   - /present define
       *
       * 出力: 定義が成功した場合は、割り振られたアイテムのIDを表示する。失敗した場合は、適切なエラーメッセージを表示する。
       */
      def executor[F[_]: ConcurrentEffect: NonServerThreadContextShift](
        implicit persistence: PresentPersistence[F, ItemStack]
      ): ContextualExecutor =
        playerCommandBuilder.buildWithExecutionCSEffect { context =>
          val player = context.sender
          if (player.hasPermission("seichiassist.present.define")) {
            val mainHandItem = player.getInventory.getItemInMainHand
            if (mainHandItem.getType == Material.AIR) {
              // おそらくこれは意図した動作ではないのでエラーメッセージを表示する
              MessageEffectF[F]("メインハンドに何も持っていません。プレゼントを定義するためには、メインハンドに対象アイテムを持ってください。")
            } else {
              (for {
                _ <- Kleisli.liftF(NonServerThreadContextShift[F].shift)
                presentID <- Kleisli.liftF(persistence.define(mainHandItem))
              } yield {
                MessageEffectF(s"メインハンドに持ったアイテムをプレゼントとして定義しました。IDは${presentID}です。")
              }).flatten
            }
          } else {
            noPermissionMessage
          }
        }
    }

    object Delete {
      val help: EchoExecutor = EchoExecutor(
        MessageEffect(List("/present delete <プレゼントID>", "    プレゼントを削除します"))
      )

      /**
       * 概要: 指定したプレゼントを消去する。 対応が失われるため、このコマンドの実行が完了した後、プレイヤーはそのプレゼントを受け取ることができなくなる。
       *
       * 権限ノード: `seichiassist.present.delete`
       *
       * 出力: 操作の結果とそれに伴うメッセージ。
       */
      def executor[F[_]: ConcurrentEffect: NonServerThreadContextShift](
        implicit persistence: PresentPersistence[F, ItemStack]
      ): ContextualExecutor =
        ContextualExecutorBuilder
          .beginConfiguration
          .thenParse(presentIdParser)
          .ifArgumentsMissing(help)
          .buildWithExecutionCSEffect { context =>
            {
              val presentId = context.args.parsed.head
              if (!context.sender.hasPermission("seichiassist.present.delete")) {
                noPermissionMessage
              } else {
                (for {
                  _ <- Kleisli.liftF(NonServerThreadContextShift[F].shift)
                  result <- Kleisli.liftF(persistence.delete(presentId))
                } yield result match {
                  case DeleteResult.Done =>
                    MessageEffectF(s"IDが${presentId}のプレゼントの消去は正常に行われました。")
                  case DeleteResult.NotFound =>
                    MessageEffectF(s"IDが${presentId}のプレゼントは存在しませんでした。")
                }).flatten
              }
            }
          }
    }

    object Grant {
      val help: EchoExecutor = EchoExecutor(
        MessageEffect(
          List(
            "/present grant <プレゼントID> all|(player <...プレーヤー名>)",
            "    プレゼントを受け取れるプレイヤーを追加します"
          )
        )
      )

      /**
       * 概要: プレイヤーが指定されたプレゼントを受け取れるようにする。
       *
       * 権限ノード: `seichiassist.present.grant`
       *
       * 出力: 操作の結果とそれに伴うメッセージ
       *
       * コマンド構文:
       *
       *   - /present grant &lt;presentId: PresentID&gt; player &lt;...players^†^:
       *     PlayerName&gt;
       *   - /present grant &lt;presentId: PresentID&gt; all
       *
       * 備考:
       *   - †: スペース区切り。
       */
      def executor[F[_]: ConcurrentEffect: NonServerThreadContextShift](
        implicit persistence: PresentPersistence[F, ItemStack],
        globalPlayerAccessor: UuidToLastSeenName[F]
      ): ContextualExecutor =
        ContextualExecutorBuilder
          .beginConfiguration
          .thenParse(presentIdParser)
          .thenParse(presentScopeModeParser)
          .ifArgumentsMissing(help)
          .buildWithExecutionCSEffect { context =>
            if (context.sender.hasPermission("seichiassist.present.grant")) {
              import shapeless.::
              val presentId :: mode :: HNil = context.args.parsed
              // Parserを通した段階でargs[0]は "player" | "all" になっているのでこれでOK
              val isGlobal = mode == "all"
              (for {
                _ <- Kleisli.liftF(NonServerThreadContextShift[F].shift)
                // TODO: 以下の処理は多分共通化できるがうまい方法が思いつかない
                globalUUID2Name <- Kleisli.liftF(globalPlayerAccessor.entries)
                // 可変長引数には対応していないので`yetToBeParsed`を使う
                restArg = context
                  .args
                  // プレイヤー名は /[A-Za-z0-9_]{,16}/であるため空白が誤って解釈されることはない
                  .yetToBeParsed
                  // 連続した空白を消去
                  .filter(_.nonEmpty)
                target =
                  if (isGlobal)
                    globalUUID2Name.keys
                  else
                    globalUUID2Name.filter { case (_, name) => restArg.contains(name) }.keys
                errorIfNobody = Option.when(target.isEmpty) {
                  MessageEffectF("対象のプレイヤーが存在しません！")
                }
                grantError <- Kleisli.liftF(persistence.grant(presentId, target.toSet))
              } yield errorIfNobody.getOrElse(
                grantError
                  .map {
                    case GrantRejectReason.NoSuchPresentID =>
                      MessageEffectF("指定されたプレゼントIDは存在しません！")
                  }
                  .getOrElse(MessageEffectF(s"プレゼント(id: $presentId)を受け取れるプレイヤーを追加しました。"))
              )).flatten
            } else {
              noPermissionMessage
            }
          }
    }

    object Revoke {
      val help: EchoExecutor = EchoExecutor(
        MessageEffect(
          List(
            "/present revoke <プレゼントID> all|(player <...プレーヤー名>)",
            "    プレゼントを受け取れるプレイヤーを削除します"
          )
        )
      )

      /**
       * 概要: プレイヤーがプレゼントを受け取れないようにする。
       *
       * 権限ノード: `seichiassist.present.revoke`
       *
       * 構文:
       *   - /present revoke &lt;presentId: PresentID&gt; player &lt;...players^✝^:
       *     PlayerName&gt;
       *   - /present revoke &lt;presentId: PresentID&gt; all
       *
       * 出力: 操作の結果とそれに伴うメッセージ。
       *
       * 備考:
       *   - ✝: スペース区切り。
       */
      def executor[F[_]: ConcurrentEffect: NonServerThreadContextShift](
        implicit persistence: PresentPersistence[F, ItemStack],
        globalPlayerAccessor: UuidToLastSeenName[F]
      ): ContextualExecutor =
        ContextualExecutorBuilder
          .beginConfiguration
          .thenParse(presentIdParser)
          .thenParse(presentScopeModeParser)
          .ifArgumentsMissing(help)
          .buildWithExecutionCSEffect { context =>
            if (context.sender.hasPermission("seichiassist.present.revoke")) {
              import shapeless.::
              val args = context.args
              val presentId :: presentScope :: HNil = args.parsed
              val isGlobal = presentScope == "all"
              (for {
                _ <- Kleisli.liftF(NonServerThreadContextShift[F].shift)
                globalUUID2Name <- Kleisli.liftF(globalPlayerAccessor.entries)
                // 可変長引数には対応していないので`yetToBeParsed`を使う
                restArg = args
                  // プレイヤー名は /[A-Za-z0-9_]{,16}/であるため空白が誤って解釈されることはない
                  .yetToBeParsed
                  // 連続した空白を消去
                  .filter(_.nonEmpty)
                target =
                  if (isGlobal)
                    globalUUID2Name.keys
                  else
                    globalUUID2Name.filter { case (_, name) => restArg.contains(name) }.keys
                errorIfNobody =
                  if (target.isEmpty) Some(MessageEffectF("対象のプレイヤーが存在しません！")) else None
                warning <- Kleisli.liftF(persistence.revoke(presentId, target.toSet))
              } yield {
                errorIfNobody.getOrElse {
                  warning
                    .map {
                      case RevokeWarning.NoSuchPresentID =>
                        MessageEffectF("そのようなプレゼントIDはありません！")
                      case RevokeWarning.NoPlayers => MessageEffectF("対象となるプレイヤーが存在しません！")
                    }
                    .getOrElse {
                      MessageEffectF(s"プレゼント(id: $presentId)を受け取れるプレイヤーを削除しました。")
                    }
                }
              }).flatten
            } else {
              noPermissionMessage
            }
          }
    }

    object Help {

      /**
       * 概要: マニュアルを表示する。
       *
       * 引数:
       *   - /present help
       */
      def executor: ContextualExecutor = {
        TraverseExecutor(
          List(
            SubCommands.State.help,
            SubCommands.ListSubCommand.help,
            SubCommands.Claim.help,
            EchoExecutor(
              MessageEffect(
                List(
                  "/present help",
                  "    このメッセージを表示します",
                  s"${ChatColor.GRAY}==== [管理者用コマンド] ===="
                )
              )
            ),
            SubCommands.Define.help,
            SubCommands.Delete.help,
            SubCommands.Grant.help,
            SubCommands.Revoke.help
          )
        )
      }
    }
  }

  def executor[F[_]: ConcurrentEffect: NonServerThreadContextShift: OnMinecraftServerThread](
    implicit persistence: PresentPersistence[F, ItemStack],
    globalPlayerAccessor: UuidToLastSeenName[F]
  ): TabExecutor = BranchedExecutor(
    Map(
      "define" -> SubCommands.Define.executor,
      "delete" -> SubCommands.Delete.executor,
      "grant" -> SubCommands.Grant.executor,
      "revoke" -> SubCommands.Revoke.executor,
      "claim" -> SubCommands.Claim.executor,
      "list" -> SubCommands.ListSubCommand.executor,
      "state" -> SubCommands.State.executor,
      "help" -> SubCommands.Help.executor
    ),
    Some(SubCommands.Help.executor),
    Some(SubCommands.Help.executor)
  ).asNonBlockingTabExecutor()

  private def decoratePresentState(state: PresentClaimingState): String = state match {
    case PresentClaimingState.Claimed     => s"${ChatColor.GOLD}受け取り済み"
    case PresentClaimingState.NotClaimed  => s"${ChatColor.GREEN}受け取り可能"
    case PresentClaimingState.Unavailable => s"${ChatColor.GRAY}配布対象外"
  }
}
