package com.github.unchama.seichiassist.commands

import cats.effect.{ConcurrentEffect, IO, Sync}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
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

/*
class PresentCommand[
  F[_] : ConcurrentEffect : NonServerThreadContextShift
](implicit environment: EffectEnvironment) extends TabExecutor {
  override def onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array[String]): util.List[String] =
    null

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    import cats.implicits._
    val program = for {
      _ <- NonServerThreadContextShift[F].shift
      uuids <- Operator.getUUIDs
    } yield ()

    environment.runEffectAsync("一連の動作", program)
    true
  }
}
*/

// FIXME: 標準のexecutor形式に変換する
class PresentCommand[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit effectEnvironment: EffectEnvironment) {
  private val dbs = new Operator()
  private val addExec = playerCommandBuilder
    .execution { context =>
      val item = context.sender.getInventory.getItemInMainHand
      IO {
        import cats.implicits._
        for {
          _ <- NonServerThreadContextShift[F].shift
          // TODO: プレイヤーを選択
          _ <- dbs.addPresent(item, context.args.parsed.head.asInstanceOf[String].split(','))
        } yield ()
        MessageEffect("アイテムを追加しました！")
      }
    }
    .build()

  private val claimExec = playerCommandBuilder
    .build()

  // 全てのプレゼントを総覧し、ステータス (未受取/受け取り済み/対象外) を表示する
  private val listExec = playerCommandBuilder
    .build()

  private val stateExec = playerCommandBuilder
    .build()

  val executor: TabExecutor = BranchedExecutor(
    Map(
      "add" -> addExec,
      "claim" -> claimExec,
      "list" -> listExec,
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

  def addPresent(itemstack: ItemStack, players: Seq[String]): F[Unit] = {
    Sync[F].delay {
      val next = DB.readOnly { implicit session =>
        sql"""SELECT MAX(present_id) as max FROM presents"""
          .map { _.int("max") }
          .first()
          .apply()
      }.map { _ + 1 }.getOrElse(1)
      DB.localTx { implicit session =>
        sql"""INSERT INTO present VALUES ($next, '${ItemStackBlobProxy.itemStackToBlob(itemstack)}')"""
          .update()
          .apply()

        val initQuery = Sync[F].fmap(Sync[F].delay {
          players.map(Bukkit.getOfflinePlayer).map(_.getUniqueId)
        }) { x =>
          x.map { uuid => (uuid.toString, next, false) }.map { t =>
            s"('${t._1}', ${t._2}, ${t._3})"
          }.mkString(",\n")
        }
        
        sql"""INSERT INTO present_state VALUES $initQuery"""
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
