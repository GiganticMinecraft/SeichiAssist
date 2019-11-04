package com.github.unchama.seichiassist.database.manipulators

import java.io.IOException
import java.sql.SQLException

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.GachaPrize
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.seichiassist.util.BukkitSerialization
import com.github.unchama.util.ActionStatus
import org.bukkit.Bukkit

import scala.collection.mutable.ArrayBuffer

class GachaDataManipulator(private val gateway: DatabaseGateway) {

  import com.github.unchama.util.syntax.ResultSetSyntax._

  private val tableReference: String = gateway.databaseName + "." + DatabaseConstants.GACHADATA_TABLENAME

  //ガチャデータロード
  def loadGachaData(): Boolean = {
    val prizes = ArrayBuffer[GachaPrize]()

    val command = s"select * from $tableReference"
    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        val restoredInventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"))
        val restoredItemStack = restoredInventory.getItem(0)

        val prize = new GachaPrize(restoredItemStack, lrs.getDouble("probability"))

        prizes.append(prize)
      }
    } catch {
      case e@(_: SQLException | _: IOException) =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return false
    }

    SeichiAssist.gachadatalist.clear()
    SeichiAssist.gachadatalist.addAll(prizes)
    true
  }

  //ガチャデータセーブ
  def saveGachaData(): Boolean = {

    //まずmysqlのガチャテーブルを初期化(中身全削除)
    var command = s"truncate table $tableReference"
    if (gateway.executeUpdate(command) == ActionStatus.Fail) {
      return false
    }

    //次に現在のgachadatalistでmysqlを更新
    for {gachadata <- SeichiAssist.gachadatalist} {
      //Inventory作ってガチャのitemstackに突っ込む
      val inventory = Bukkit.getServer.createInventory(null, 9 * 1)
      inventory.setItem(0, gachadata.itemStack)

      command = ("insert into " + tableReference + " (probability, itemstack)"
        + " values"
        + "(" + gachadata.probability + "," +
        "'" + BukkitSerialization.toBase64(inventory) + "')")
      if (gateway.executeUpdate(command) == ActionStatus.Fail) {
        return false
      }
    }
    true
  }


}
