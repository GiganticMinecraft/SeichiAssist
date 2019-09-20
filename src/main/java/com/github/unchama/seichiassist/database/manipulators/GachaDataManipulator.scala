package com.github.unchama.seichiassist.database.manipulators

import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}

class GachaDataManipulator(private val gateway: DatabaseGateway) {

  private val tableReference: String
    get() = gateway.databaseName + "." + DatabaseConstants.GACHADATA_TABLENAME

  //ガチャデータロード
  def loadGachaData(): Boolean {
    val prizes = ArrayList[GachaPrize]()

    val command = s"select * from $tableReference"
    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        val restoredInventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"))
        val restoredItemStack = restoredInventory.getItem(0)

        val prize = GachaPrize(
            restoredItemStack, lrs.getDouble("probability")
        )

        prizes += prize
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

    SeichiAssist.gachadatalist.clear()
    SeichiAssist.gachadatalist.addAll(prizes)
    return true

  }

  //ガチャデータセーブ
  def saveGachaData(): Boolean {

    //まずmysqlのガチャテーブルを初期化(中身全削除)
    var command = s"truncate table $tableReference"
    if (gateway.executeUpdate(command) == Fail) {
      return false
    }

    //次に現在のgachadatalistでmysqlを更新
    for (gachadata in SeichiAssist.gachadatalist) {
      //Inventory作ってガチャのitemstackに突っ込む
      val inventory = Bukkit.getServer().createInventory(null, 9 * 1)
      inventory.setItem(0, gachadata.itemStack)

      command = ("insert into " + tableReference + " (probability, itemstack)"
          + " values"
          + "(" + gachadata.probability + "," +
          "'" + BukkitSerialization.toBase64(inventory) + "')")
      if (gateway.executeUpdate(command) == Fail) {
        return false
      }
    }
    return true
  }


}
