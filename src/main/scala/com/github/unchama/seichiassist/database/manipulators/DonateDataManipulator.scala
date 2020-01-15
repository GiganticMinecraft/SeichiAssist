package com.github.unchama.seichiassist.database.manipulators

import java.sql.SQLException

import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillPremiumEffect
import com.github.unchama.util.ActionStatus
import org.bukkit.ChatColor._
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.{Bukkit, Material}

class DonateDataManipulator(private val gateway: DatabaseGateway) {

  import com.github.unchama.util.syntax.ResultSetSyntax._

  import scala.jdk.CollectionConverters._

  def addPremiumEffectBuy(playerdata: PlayerData,
                          effect: ActiveSkillPremiumEffect): ActionStatus = {
    val command = ("insert into " + tableReference
      + " (playername,playeruuid,effectnum,effectname,usepoint,date) "
      + "value("
      + "'" + playerdata.lowercaseName + "',"
      + "'" + playerdata.uuid.toString + "',"
      + effect.num + ","
      + "'" + effect.getsqlName + "',"
      + effect.usePoint + ","
      + "cast( now() as datetime )"
      + ")")

    gateway.executeUpdate(command)
  }

  def addDonate(name: String, point: Int): ActionStatus = {
    val command = ("insert into " + tableReference
      + " (playername,getpoint,date) "
      + "value("
      + "'" + name + "',"
      + point + ","
      + "cast( now() as datetime )"
      + ")")
    gateway.executeUpdate(command)
  }

  private def tableReference: String = gateway.databaseName + "." + DatabaseConstants.DONATEDATA_TABLENAME

  def loadDonateData(playerdata: PlayerData, inventory: Inventory): Boolean = {
    var itemstack: ItemStack = null
    var material: Material = null
    var lore2: List[String] = null
    val effect = ActiveSkillPremiumEffect.values
    // TODO: ほんとうにStarSelectじゃなきゃだめ?
    val command = "select * from " + tableReference + " where playername = '" + playerdata.lowercaseName + "'"
    try {
      var count = 0
      gateway.executeQuery(command).recordIteration { lrs =>
        //ポイント購入の処理
        val getPoint = lrs.getInt("getpoint")
        val usePoint = lrs.getInt("usepoint")
        if (getPoint > 0) {
          itemstack = new ItemStack(Material.DIAMOND)
          lore2 = List(RESET.toString + "" + GREEN + "" + "金額：" + getPoint * 100,
            "" + RESET + GREEN + "プレミアムエフェクトポイント：+" + getPoint,
            "" + RESET + GREEN + "日時：" + lrs.getString("date")
          )
          itemstack.setItemMeta({
            val meta = Bukkit.getItemFactory.getItemMeta(Material.DIAMOND)
            meta.setDisplayName("" + AQUA + UNDERLINE + "" + BOLD + "寄付")
            meta.setLore(lore2.asJava)
            meta
          })
          inventory.setItem(count, itemstack)
        } else if (usePoint > 0) {
          val num = lrs.getInt("effectnum") - 1
          material = effect(num).material
          itemstack = new ItemStack(material)

          lore2 = List("" + RESET + GOLD + "プレミアムエフェクトポイント： -" + usePoint,
            "" + RESET + GOLD + "日時：" + lrs.getString("date")
          )
          itemstack.setItemMeta({
            val meta = Bukkit.getItemFactory.getItemMeta(material)
            meta.setDisplayName("" + RESET.toString + YELLOW + "購入エフェクト：" + effect(num).desc)
            meta.setLore(lore2.asJava)
            meta
          })
          inventory.setItem(count, itemstack)
        }
        count += 1
      }
    } catch {
      case e: SQLException =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return false
    }

    true
  }
}
