package com.github.unchama.seichiassist.database.manipulators

import com.github.unchama.seichiassist.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.util.ActionStatus
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.{Inventory, ItemStack}

class DonateDataManipulator(private val gateway: DatabaseGateway) {

  private val tableReference: String
    get() = gateway.databaseName + "." + DatabaseConstants.DONATEDATA_TABLENAME

  def addPremiumEffectBuy(playerdata: PlayerData,
                          effect: ActiveSkillPremiumEffect): ActionStatus {
    val command = ("insert into " + tableReference
        + " (playername,playeruuid,effectnum,effectname,usepoint,date) "
        + "value("
        + "'" + playerdata.lowercaseName + "',"
        + "'" + playerdata.uuid.toString() + "',"
        + effect.num + ","
        + "'" + effect.getsqlName() + "',"
        + effect.usePoint + ","
        + "cast( now() as datetime )"
        + ")")

    return gateway.executeUpdate(command)
  }

  def addDonate(name: String, point: Int): ActionStatus {
    val command = ("insert into " + tableReference
        + " (playername,getpoint,date) "
        + "value("
        + "'" + name + "',"
        + point + ","
        + "cast( now() as datetime )"
        + ")")
    return gateway.executeUpdate(command)
  }

  def loadDonateData(playerdata: PlayerData, inventory: Inventory): Boolean {
    var itemstack: ItemStack
    var itemmeta: ItemMeta
    var material: Material
    var lore2: List[String]
    var count = 0
    val effect = ActiveSkillPremiumEffect.values()

    val command = "select * from " + tableReference + " where playername = '" + playerdata.lowercaseName + "'"
    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        //ポイント購入の処理
        val getPoint = lrs.getInt("getpoint")
        val usePoint = lrs.getInt("usepoint")
        if (getPoint > 0) {
          itemstack = ItemStack(Material.DIAMOND)
          lore2 = List(ChatColor.RESET.toString() + "" + ChatColor.GREEN + "" + "金額：" + getPoint * 100,
              "" + ChatColor.RESET + ChatColor.GREEN + "プレミアムエフェクトポイント：+" + getPoint,
              "" + ChatColor.RESET + ChatColor.GREEN + "日時：" + lrs.getString("date")
          )
          itemstack.itemMeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND).apply {
            displayName = "" + ChatColor.AQUA + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付"
            lore = lore2
          }
          inventory.setItem(count, itemstack)
        } else if (usePoint > 0) {
          val num = lrs.getInt("effectnum") - 1
          material = effect[num].material
          itemstack = ItemStack(material)

          lore2 = List("" + ChatColor.RESET + ChatColor.GOLD + "プレミアムエフェクトポイント： -" + usePoint,
              "" + ChatColor.RESET + ChatColor.GOLD + "日時：" + lrs.getString("date")
          )
          itemstack.itemMeta = Bukkit.getItemFactory().getItemMeta(material).apply {
            displayName = "" + ChatColor.RESET.toString() + ChatColor.YELLOW + "購入エフェクト：" + effect[num].desc
            lore = lore2
          }
          inventory.setItem(count, itemstack)
        }
        count++
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return false
    }

    return true
  }
}
