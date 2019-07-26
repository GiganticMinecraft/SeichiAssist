package com.github.unchama.seichiassist.database.manipulators

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.MineStackGachaData
import com.github.unchama.seichiassist.database.DatabaseConstants
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.seichiassist.task.recordIteration
import com.github.unchama.seichiassist.util.BukkitSerialization
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

import java.io.IOException
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList

import com.github.unchama.util.ActionStatus.Fail

class MineStackGachaDataManipulator(private val gateway: DatabaseGateway) {

  private val tableReference: String
    get() = gateway.databaseName + "." + DatabaseConstants.MINESTACK_GACHADATA_TABLENAME

  //MineStack用ガチャデータロード
  fun loadMineStackGachaData(): Boolean {
    val gachadatalist = ArrayList<MineStackGachaData>()

    val command = "select * from $tableReference"
    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        val savedInventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"))
        val itemStack = savedInventory.getItem(0)

        val gachaData = MineStackGachaData(
            lrs.getString("obj_name"), itemStack, lrs.getDouble("probability"), lrs.getInt("level")
        )

        gachadatalist += gachaData
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return false
    } catch (e: IOException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return false
    }

    SeichiAssist.msgachadatalist.clear()
    SeichiAssist.msgachadatalist.addAll(gachadatalist)
    return true
  }

  //MineStack用ガチャデータセーブ
  fun saveMineStackGachaData(): Boolean {


    //まずmysqlのガチャテーブルを初期化(中身全削除)
    var command = "truncate table $tableReference"
    if (gateway.executeUpdate(command) == Fail) {
      return false
    }

    //次に現在のgachadatalistでmysqlを更新
    for (gachadata in SeichiAssist.msgachadatalist) {
      //Inventory作ってガチャのitemstackに突っ込む
      val inventory = Bukkit.getServer().createInventory(null, 9 * 1)
      inventory.setItem(0, gachadata.itemStack)

      command = ("insert into " + tableReference + " (probability,level,obj_name,itemstack)"
          + " values"
          + "(" + gachadata.probability
          + "," + gachadata.level
          + ",'" + gachadata.objName + "'"
          + ",'" + BukkitSerialization.toBase64(inventory) + "'"
          + ")")

      if (gateway.executeUpdate(command) == Fail) {
        return false
      }
    }
    return true
  }
}
