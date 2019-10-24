package com.github.unchama.seichiassist.database.manipulators

import java.io.IOException
import java.sql.SQLException

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.MineStackGachaData
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.seichiassist.util.BukkitSerialization
import com.github.unchama.util.ActionStatus
import org.bukkit.Bukkit

import scala.collection.mutable.ArrayBuffer

class MineStackGachaDataManipulator(private val gateway: DatabaseGateway) {

  import com.github.unchama.util.syntax.ResultSetSyntax._

  private val tableReference: String = gateway.databaseName + "." + DatabaseConstants.MINESTACK_GACHADATA_TABLENAME

  //MineStack用ガチャデータロード
  def loadMineStackGachaData(): Boolean = {
    val gachadatalist = ArrayBuffer[MineStackGachaData]()

    val command = s"select * from $tableReference"
    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        val savedInventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"))
        val itemStack = savedInventory.getItem(0)

        val gachaData = new MineStackGachaData(
          lrs.getString("obj_name"), itemStack, lrs.getDouble("probability"), lrs.getInt("level")
        )

        gachadatalist += gachaData
      }
    } catch {
      case e@(_: SQLException | _: IOException) =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return false
    }

    SeichiAssist.msgachadatalist.clear()
    SeichiAssist.msgachadatalist.addAll(gachadatalist)
    true
  }

  //MineStack用ガチャデータセーブ
  def saveMineStackGachaData(): Boolean = {


    //まずmysqlのガチャテーブルを初期化(中身全削除)
    var command = s"truncate table $tableReference"
    if (gateway.executeUpdate(command) == ActionStatus.Fail) {
      return false
    }

    //次に現在のgachadatalistでmysqlを更新
    for {gachadata <- SeichiAssist.msgachadatalist} {
      //Inventory作ってガチャのitemstackに突っ込む
      val inventory = Bukkit.getServer.createInventory(null, 9 * 1)
      inventory.setItem(0, gachadata.itemStack)

      command = ("insert into " + tableReference + " (probability,level,obj_name,itemstack)"
        + " values"
        + "(" + gachadata.probability
        + "," + gachadata.level
        + ",'" + gachadata.objName + "'"
        + ",'" + BukkitSerialization.toBase64(inventory) + "'"
        + ")")

      if (gateway.executeUpdate(command) == ActionStatus.Fail) {
        return false
      }
    }
    true
  }
}
