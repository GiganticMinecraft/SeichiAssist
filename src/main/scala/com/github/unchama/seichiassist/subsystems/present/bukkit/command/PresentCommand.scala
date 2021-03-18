package com.github.unchama.seichiassist.subsystems.present.bukkit.command

import cats.implicits._
import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.present.domain.PresentClaimingState
import com.github.unchama.seichiassist.subsystems.present.infrastructure.JdbcBackedPresentRepository
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.{Bukkit, ChatColor}
import org.bukkit.command.TabExecutor

object PresentCommand {
  /**
   * 概要: メインハンドに持っているアイテムをプレゼントとして受け取れるように追加する。
   *
   * 権限ノード: `seichiassist.present.add`
   *
   * 出力: 追加が成功した場合は、アイテムのIDを返却する。失敗した場合は、適切なエラーメッセージを表示する。
   *
   * コマンド構文:
   * <ul>
   * <li>/present add player &lt;...players^†^: PlayerName&gt;</li>
   * <li>/present add all</li>
   * </ul>
   * 備考:
   * <ul>
   *   <li>†: スペース区切り。</li>
   * </ul>
   */
  private def addingExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit repo: JdbcBackedPresentRepository[F]) = playerCommandBuilder
    .argumentsParsers(List(Parsers.fromOptionParser({ arg0: String =>
      arg0 match {
        // enum match
        case "player" | "all" => Some(arg0)
        case _ => None
      }
    }, MessageEffect("presentコマンドのモード指定は、playerまたはallを指定してください。"))))
    .execution { context =>
      if (!context.sender.hasPermission("seichiassist.present.add")) {
        IO.pure(MessageEffect("You don't have the permission."))
      } else {
        // Parserを通した段階でargs[0]は "player" | "all" になっているのでこれでOK
        val isGlobal = context.args.parsed.head.asInstanceOf[String] == "all"
        val item = context.sender.getInventory.getItemInMainHand
        val eff = for {
          _ <- NonServerThreadContextShift[F].shift
          uuids <- repo.getUUIDs
          // 可変長引数には対応していないので`yetToBeParsed`を使う
          target = if (isGlobal)
            uuids
          else context.args
            // プレイヤー名は /[A-Za-z0-9_]{,16}/であるため空白が誤って解釈されることはない
            .yetToBeParsed
            // 連続した空白を消去
            .filter(_.nonEmpty)
            .map(Bukkit.getOfflinePlayer)
            .map(_.getUniqueId)
          errorIfNobody = if (target.isEmpty) MessageEffect("対象のプレイヤーが存在しません！") else TargetedEffect.emptyEffect
          optItemId <- repo.performAddPresent(item, target.toSeq)
        } yield {
          val message = optItemId match {
            case Some(id) => s"メインハンドのアイテムをID: ${id}として登録しました。"
            case None => "アイテムの登録に失敗しました。再度お試しください。"
          }
          SequentialEffect(
            MessageEffect(message),
            errorIfNobody
          )
        }

        eff.toIO
      }
    }
    .build()

  /**
   * 概要: 指定されたIDのプレゼントを受け取る。
   * このコマンドを実行した際、プレイヤーにアイテムを追加する。
   *
   * 構文: /present claim &lt;id: int&gt;
   */
  private def claimingExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit repo: JdbcBackedPresentRepository[F]) = playerCommandBuilder
    .argumentsParsers(List(Parsers.integer(MessageEffect("/present claimの第一引数には整数を入力してください。"))))
    .execution { context =>
      val player = context.sender
      val presentId = context.args.parsed.head.asInstanceOf[Int]
      val eff = for {
        _ <- NonServerThreadContextShift[F].shift
        states <- repo.fetchPresentsState(player)
        claimState = states.get(presentId)
      } yield {
        claimState match {
          case Some(state) =>
            state match {
              case PresentClaimingState.Claimed =>
                IO.pure(MessageEffect(s"ID: ${presentId}のプレゼントはすでに受け取っています。"))
              case PresentClaimingState.NotClaimed =>
                val eff = for {
                  _ <- repo.claimPresent(player, presentId)
                  items <- repo.getAllPresent
                } yield {
                  SequentialEffect(
                    Util.grantItemStacksEffect(items(presentId)),
                    MessageEffect(s"ID: ${presentId}のプレゼントを付与しました。")
                  )
                }
                eff.toIO
            }
          case None =>
            IO.pure(MessageEffect(s"ID: ${presentId}のプレゼントは存在しないか、あるいは配布対象ではありません"))
        }
      }

      eff.toIO.flatten
    }
    .build()

  /**
   * 実行プレイヤーが対象となっている全てのプレゼントを総覧し、受け取りステータスを表示する
   *
   * 構文: /present state
   */
  private def showingStateExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit repo: JdbcBackedPresentRepository[F]) = playerCommandBuilder
    .execution { context =>
      val eff = for {
        // off-main-thread
        _ <- NonServerThreadContextShift[F].shift
        state <- repo.fetchPresentsState(context.sender)
      } yield {
        val mes = state
          .toList
          .map { case (id, state) =>
            s"ID=$id: ${
              state match {
                case PresentClaimingState.Claimed => s"${ChatColor.GOLD}受け取り済み"
                case PresentClaimingState.NotClaimed => s"${ChatColor.GREEN}受け取り可能"
              }
            }"
          }
          .filter(_.nonEmpty)
        MessageEffect(mes)
      }

      eff.toIO
    }
    .build()

  /**
   * 実行プレイヤーと対応するプレゼントの状況をページネーションと共に表示する
   * 構文: /present list &lt;page: PositiveInt&gt;
   */
  private def listingExecutor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit repo: JdbcBackedPresentRepository[F]) = playerCommandBuilder
    .argumentsParsers(List(Parsers.closedRangeInt(1, Int.MaxValue, MessageEffect("ページ数には1以上の数を指定してください。"))))
    .execution { context =>
      val perPage = 10
      val page = context.args.parsed.head.asInstanceOf[Int]
      val player = context.sender
      val eff = for {
        _ <- NonServerThreadContextShift[F].shift
        state <- repo.fetchPresentsState(player)
        ids = state.keys.toBuffer.sorted.slice((page - 1) * perPage, page * perPage - 1)
        id2state = ids.map { id => (id, state(id)) }
      } yield {
        MessageEffect(id2state.map { case (id, state) =>
          s"ID=$id: ${
            state match {
              case PresentClaimingState.Claimed => s"${ChatColor.GOLD}受け取り済み"
              case PresentClaimingState.NotClaimed => s"${ChatColor.GREEN}受け取り可能"
              case PresentClaimingState.Unavailable => s"${ChatColor.GRAY}対象外"
            }
          }${ChatColor.RESET}"
        }.toList)
      }
      eff.toIO
    }
    .build()
  def executor[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit repo: JdbcBackedPresentRepository[F]): TabExecutor = BranchedExecutor(
    Map(
      "add" -> addingExecutor,
      "claim" -> claimingExecutor,
      "state" -> showingStateExecutor,
      "list" -> listingExecutor,
    )
  ).asNonBlockingTabExecutor()
}
