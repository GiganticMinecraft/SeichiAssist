package com.github.unchama.seichiassist.commands

import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, IO, Sync}
import cats.implicits._
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.{BukkitObjectInputStream, BukkitObjectOutputStream}
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID
import scala.util.Using

// FIXME: 標準のexecutor形式に変換する
class PresentCommand[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit effectEnvironment: EffectEnvironment) {
  private val repo = new Operator()
  /**
   * 概要: メインハンドに持っているアイテムをプレゼントとして受け取れるように追加する。
   *
   * 権限ノード: `seichiassist.present.add`
   *
   * 出力: 追加が成功した場合は、アイテムのIDを返却する。
   *
   * コマンド構文: /present add [...players^†^: PlayerName]
   *
   * 備考:
   * <ul>
   *   <li>†: スペース区切り。省略時は全てのプレイヤーが指定されたものとみなす</li>
   * </ul>
   */
  private val addExec = playerCommandBuilder
    .execution { context =>
      if (!context.sender.hasPermission("seichiassist.present.add")) {
        IO {
          MessageEffect("You don't have the permission.")
        }
      } else {
        val item = context.sender.getInventory.getItemInMainHand

        val eff = for {
          _ <- NonServerThreadContextShift[F].shift
          // 可変長引数には対応していないので`yetToBeParsed`を使う
          restParam = context.args.yetToBeParsed.filter(_.nonEmpty)

          uuids <- repo.getUUIDs
          sy = if (restParam.isEmpty) uuids else restParam.map(Bukkit.getOfflinePlayer).map(_.getUniqueId)
          optItemId <- repo.addPresent(item, sy.toSeq)
        } yield {
          val message = optItemId match {
            case Some(id) => s"メインハンドのアイテムをID: ${id}として登録しました。"
            case None => "アイテムの登録に失敗しました。再度お試しください。"
          }
          MessageEffect(message)
        }

        eff.toIO
      }
    }
    .build()

  /**
   * 概要: 指定されたIDのプレゼントを受け取る。
   * このコマンドを実行した際、プレイヤーにアイテムを追加する。
   *
   * コマンド構文: /present claim &lt;id: int&gt;
   */
  private val claimExec = playerCommandBuilder
    .argumentsParsers(List(Parsers.integer(MessageEffect("/present claimの第一引数には整数を入力してください。"))))
    .execution { context =>
      val player = context.sender
      val presentId = context.args.parsed.head.asInstanceOf[Int]
      val eff = for {
        states <- repo.fetchPresentsState(player)
        claimState = states.get(presentId)
      } yield {
        claimState match {
          case Some(state) =>
            state match {
              case PresentClaimingState.Claimed =>
                val show = MessageEffect(s"ID: ${presentId}のプレゼントはすでに受け取っています。")
                IO.pure(show)
              case PresentClaimingState.Unclaimed =>
                val showType = for {
                  _ <- repo.claimPresent(player, presentId)
                  items <- repo.getAllPresent
                } yield {
                  SequentialEffect(
                    Util.grantItemStacksEffect(items(presentId)),
                    MessageEffect(s"ID: ${presentId}のプレゼントを付与しました。")
                  )
                }
                val showType2 = showType.toIO
                showType2
            }
          case None =>
            IO.pure(MessageEffect(s"ID: ${presentId}のプレゼントは存在しないか、あるいは配布対象ではありません"))
        }
      }

      eff.toIO.flatten
    }
    .build()

  /**
   * 全てのプレゼントを総覧し、ステータス (未受取/受け取り済み) を表示する
   *
   * コマンド構文: /present state
   */
  private val stateExec = playerCommandBuilder
    .execution { context =>
      val eff = for {
        // off-main-thread
        _ <- NonServerThreadContextShift[F].shift
        state <- repo.fetchPresentsState(context.sender)
      } yield {
        val mes = state
          .toList
          .map(x => s"ID: ${x._1} ---> ${
            x._2 match {
              case PresentClaimingState.Claimed => "受け取り済み"
              case PresentClaimingState.Unclaimed => "受取可能"
            }
          }")
        MessageEffect(mes)
      }

      eff.toIO
    }
    .build()

  val executor: TabExecutor = BranchedExecutor(
    Map(
      "add" -> addExec,
      "claim" -> claimExec,
      "state" -> stateExec
    )
  ).asNonBlockingTabExecutor()
}

import scalikejdbc._
class Operator[F[_] : Sync] {
  def getUUIDs: F[Set[UUID]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT uuid from seichiassist.playerdata;"
        // mapがないとキレる
        .map { rs =>
          UUID.fromString(rs.string("uuid"))
        }
        .list()
        .apply()
        .toSet
    }
  }

  /**
   *
   * @param itemstack 追加するアイテム
   * @param players 配るプレイヤー
   * @return 成功した場合新たに取得した`F[Some[Int]]`、失敗した場合`F[None]`
   */
  def addPresent(itemstack: ItemStack, players: Seq[UUID]): F[Option[Int]] = {
    Sync[F].delay {
      val next = DB.readOnly { implicit session =>
        sql"""SELECT MAX(present_id) as max FROM presents"""
          .map { _.int("max") }
          .first()
          .apply()
      }.map { _ + 1 }.getOrElse(1)
      val wasSuccessful = DB.localTx { implicit session =>
        sql"""INSERT INTO present VALUES ($next, '${ItemStackBlobProxy.itemStackToBlob(itemstack)}')"""
          .execute()
          .apply()

        val initQuery =
          players.map { uuid => (uuid.toString, next, false) }.map { t =>
            s"('${t._1}', ${t._2}, ${t._3})"
          }.mkString(",\n")
        sql"""INSERT INTO present_state VALUES $initQuery"""
          .execute()
          .apply()
      }
      Option.when(wasSuccessful) {
        next
      }
    }
  }

  def claimPresent(player: Player, presentId: Int): F[Unit] = {
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"""UPDATE present_state SET claimed = TRUE WHERE uuid = '${player.getUniqueId}' AND present_id = $presentId;"""
          .update()
          .apply()
      }
    }
  }

  def getAllPresent: F[Map[Int, ItemStack]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT present_id, itemstack FROM presents;"""
          .map { rs => (
            rs.int("present_id"),
            ItemStackBlobProxy.blobToItemStack(rs.string("itemstack"))
          )}
          .list()
          .apply()
          .toMap
      }
    }
  }
  
  def fetchPresentsState(player: Player): F[Map[Int, PresentClaimingState]] = {
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT present_id, claimed FROM present_state WHERE uuid = '${player.getUniqueId}'"""
          .map { x =>
            val claimState = if (x.boolean("claimed"))
              PresentClaimingState.Claimed
            else
              PresentClaimingState.Unclaimed
            (x.int("present_id"), claimState)
          }
          .list()
          .apply()
          .toMap
      }
    }
  }
}

sealed trait PresentClaimingState
object PresentClaimingState {
  case object Unclaimed extends PresentClaimingState
  case object Claimed extends PresentClaimingState
}

/**
 * ItemStackとデータベースのBlob (正確にはその文字列表現) を互いに変換する。
 */
object ItemStackBlobProxy {
  type Base64 = String
  def itemStackToBlob(stack: ItemStack): Base64 = {
    Using.resource(new ByteArrayOutputStream()) { baos =>
      Using.resource(new BukkitObjectOutputStream(baos)) { bos =>
        bos.writeObject(stack)
      }
      Base64Coder.encodeLines(baos.toByteArray)
    }
  }

  def blobToItemStack(data: Base64): ItemStack = {
    Using.resource(new ByteArrayInputStream(Base64Coder.decodeLines(data))) { bais =>
      Using.resource(new BukkitObjectInputStream(bais)) { boi =>
        boi.readObject().asInstanceOf[ItemStack]
      }
    }
  }
}
