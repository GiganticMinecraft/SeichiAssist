package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.{Config, SeichiAssist}
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.enumeration.DirectionType
import org.bukkit.ChatColor._
import org.bukkit.block.BlockFace
import org.bukkit.{Bukkit, Material}
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.{Inventory, ItemStack}

import java.text.NumberFormat
import java.util
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

/**
 * 保護関連メニュー
 *
 * @author karayuu
 */
object RegionMenuData {
  private val config: Config = SeichiAssist.seichiAssistConfig
  private val nfNum: NumberFormat = NumberFormat.getNumberInstance
  private val CANNOT_EXPAND = s"$RED${UNDERLINE}これ以上拡張できません"
  private val CANNOT_SHRINK = s"$RED${UNDERLINE}これ以上縮小できません"
  /**
   * グリッド式保護メニュを開きます。
   *
   * @param player
   * @return
   */
  def getGridWorldGuardMenu(player: Player): Inventory = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val unitMap = playerData.unitMap
    val directionMap = getPlayerDirectionString(player)
    val gridInv: Inventory = Bukkit.createInventory(null, InventoryType.DISPENSER, s"${LIGHT_PURPLE}グリッド式保護設定メニュー")
    //0マス目
    val lore0 = List(
      s"${GREEN}現在のユニット指定量",
      s"$AQUA${playerData.unitPerClick}${GREEN}ユニット($AQUA${playerData.unitPerClick * 15}${GREEN}ブロック)/1クリック",
      s"$RED${UNDERLINE}クリックで変更"
    )
    gridInv.setItem(0, Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 0, s"${GREEN}拡張単位の変更", lore0, true))
    //1マス目
    val lore1 = getGridLore(directionMap(DirectionType.AHEAD), unitMap(DirectionType.AHEAD))
    if (!playerData.canGridExtend(DirectionType.AHEAD, player.getWorld.getName)) lore1.add(CANNOT_EXPAND)
    else if (!playerData.canGridReduce(DirectionType.AHEAD)) lore1.add(CANNOT_SHRINK)
    val menuicon1 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 14, s"${DARK_GREEN}前に${playerData.unitPerClick}ユニット増やす/減らす", lore1.asScala.toList, true)
    gridInv.setItem(1, menuicon1)
    //2マス目
    val lore2 = new util.ArrayList[String]
    lore2.add(s"$RED${UNDERLINE}クリックで開く")
    val menuicon2 = Util.getMenuIcon(Material.CHEST, 1, s"${GREEN}設定保存メニュー", lore2.asScala.toList, true)
    gridInv.setItem(2, menuicon2)
    //3マス目
    val lore3: util.List[String] = getGridLore(directionMap(DirectionType.LEFT), unitMap(DirectionType.LEFT))
    if (!playerData.canGridExtend(DirectionType.LEFT, player.getWorld.getName)) lore3.add(CANNOT_EXPAND)
    else if (!playerData.canGridReduce(DirectionType.LEFT)) lore3.add(CANNOT_SHRINK)
    val menuicon3 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 10, s"${DARK_GREEN}左に${playerData.unitPerClick}ユニット増やす/減らす", lore3.asScala.toList, true)
    gridInv.setItem(3, menuicon3)
    //4マス目
    // このエントリ群はOrderedで、順序を変えるとUIの表示が変わる為避けること
    val lore4Map = Map(
      DirectionType.AHEAD -> "前方向",
      DirectionType.BEHIND -> "後ろ方向",
      DirectionType.RIGHT -> "右方向",
      DirectionType.LEFT -> "左方向",
    )

    val lore4b = lore4Map
      .map(tp => s"$GRAY${lore4Map(tp._1)}：$AQUA${unitMap(tp._1)}${GRAY}ユニット($AQUA${nfNum.format(unitMap(tp._1) * 15)}${GRAY}ブロック)")
    val lore4 = List(
      s"${GRAY}現在の設定",
    ) ::: lore4b.toList ::: List (
      s"${GRAY}保護ユニット数：$AQUA${playerData.gridChunkAmount}",
      s"${GRAY}保護ユニット上限値：$RED${config.getGridLimitPerWorld(player.getWorld.getName)}"
    )
    val menuicon4 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 11, s"${DARK_GREEN}設定", lore4, true)
    gridInv.setItem(4, menuicon4)
    //5マス目
    val lore5 = getGridLore(directionMap(DirectionType.RIGHT), unitMap(DirectionType.RIGHT))
    if (!playerData.canGridExtend(DirectionType.RIGHT, player.getWorld.getName)) lore5.add(CANNOT_EXPAND)
    else if (!playerData.canGridReduce(DirectionType.RIGHT)) lore5.add(CANNOT_SHRINK)
    val menuicon5 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 5, s"${DARK_GREEN}右に${playerData.unitPerClick}ユニット増やす/減らす", lore5.asScala.toList, true)
    gridInv.setItem(5, menuicon5)
    //6マス目
    val lore6 = List(
      s"$RED${UNDERLINE}取扱注意！！"
    )
    val menuicon6 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 4, s"${RED}全設定リセット", lore6, true)
    gridInv.setItem(6, menuicon6)
    //7マス目
    val lore7 = getGridLore(directionMap(DirectionType.BEHIND), unitMap(DirectionType.BEHIND))
    if (!playerData.canGridExtend(DirectionType.BEHIND, player.getWorld.getName)) lore7.add(CANNOT_EXPAND)
    else if (!playerData.canGridReduce(DirectionType.BEHIND)) lore7.add(CANNOT_SHRINK)
    val menuicon7 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 13, s"${DARK_GREEN}後ろに${playerData.unitPerClick}ユニット増やす/減らす", lore7.asScala.toList, true)
    gridInv.setItem(7, menuicon7)
    //8マス目
    val menuicon8 = if (!config.isGridProtectionEnabled(player.getWorld)) {
      val lore8 = List(
        s"$RED${UNDERLINE}このワールドでは保護を作成できません"
      )
      Util.getMenuIcon(Material.WOOL, 1, 14, s"${RED}保護作成", lore8, true)
    } else if (!playerData.canCreateRegion) {
      val lore8 = List(
        s"$RED${UNDERLINE}以下の原因により保護を作成できません",
        s"$RED・保護の範囲が他の保護と重複している",
        s"$RED・保護の作成上限に達している"
      )
      Util.getMenuIcon(Material.WOOL, 1, 14, s"${RED}保護作成", lore8, true)
    } else {
      val lore8 = List(
        s"${DARK_GREEN}保護作成可能です",
        s"$RED${UNDERLINE}クリックで作成"
      )
      Util.getMenuIcon(Material.WOOL, 1, 11, s"${GREEN}保護作成", lore8, true)
    }
    gridInv.setItem(8, menuicon8)
    gridInv
  }

  // TODO: immutable
  private def getGridLore(direction: String, unit: Int): util.List[String] = {
    ArrayBuffer.from(List(
      s"$RESET${GREEN}左クリックで増加",
      s"$RESET${RED}右クリックで減少",
      s"$RESET$GRAY$GRAY---------------", 
      s"${GRAY}方向：$AQUA$direction", 
      s"${GRAY}現在の指定方向ユニット数：$AQUA$unit$GRAY($AQUA${nfNum.format(unit * 15)}${GRAY}ブロック)"
    )).asJava
  }

  private def getPlayerDirectionString(player: Player): Map[DirectionType, String] = {
    var rotation = (player.getLocation.getYaw + 180) % 360
    if (rotation < 0) rotation += 360
    import org.bukkit.block.BlockFace._
    val theMap = if (0.0 <= rotation && rotation < 45.0) { //前が北(North)
      Map(
        DirectionType.BEHIND -> SOUTH,
        DirectionType.AHEAD -> NORTH,
        DirectionType.LEFT -> WEST,
        DirectionType.RIGHT -> EAST,
      )
    }
    else if (45.0 <= rotation && rotation < 135.0) { //前が東(East)
      Map(
        DirectionType.RIGHT -> SOUTH,
        DirectionType.LEFT -> NORTH,
        DirectionType.BEHIND -> WEST,
        DirectionType.AHEAD -> EAST,
      )
    }
    else if (135.0 <= rotation && rotation < 225.0) { //前が南(South)
      Map(
        DirectionType.AHEAD -> SOUTH,
        DirectionType.BEHIND -> NORTH,
        DirectionType.RIGHT -> WEST,
        DirectionType.LEFT -> EAST,
      )
    }
    else if (225.0 <= rotation && rotation < 315.0) { //前が西(West)
      Map(
        DirectionType.LEFT -> SOUTH,
        DirectionType.RIGHT -> NORTH,
        DirectionType.AHEAD -> WEST,
        DirectionType.BEHIND -> EAST,
      )
    }
    else if (315.0 <= rotation && rotation < 360.0) {
      Map(
        DirectionType.BEHIND -> SOUTH,
        DirectionType.AHEAD -> NORTH,
        DirectionType.LEFT -> WEST,
        DirectionType.RIGHT -> EAST,
      )
    } else {
      Map()
    }
    theMap.map((tp: (DirectionType, BlockFace)) => {
      (tp._1, tp._2 match {
        case SOUTH => Some("南(South)")
        case NORTH => Some("北(North)")
        case WEST => Some("西(West)")
        case EAST => Some("東(East)")
        case _ => None
      })
    }).filter(_._2.nonEmpty).map(tp => (tp._1, tp._2.get))
  }

  /**
   * グリッド式保護設定保存メニューを取得します。
   *
   * @param player プレイヤー
   * @return グリッド式保護・設定保存Inventory
   */
  def getGridTemplateInventory(player: Player): Inventory = {
    val inv: Inventory = Bukkit.createInventory(null, 9 * (getAisleAmount + 1), s"${LIGHT_PURPLE}グリッド式保護・設定保存")
    for (i <- 0 until config.getTemplateKeepAmount) {
      inv.setItem(i, getGridtempMenuicon(i, player))
    }
    //戻るボタン
    val lore = List(s"$RED${UNDERLINE}クリックで戻る")
    val retIcon = Util.getMenuIcon(Material.BARRIER, 1, s"${RED}グリッド式保護メニューに戻る", lore, true)
    inv.setItem(getAisleAmount * 9, retIcon)
    inv
  }

  /**
   * テンプレートメニュー用。
   *
   * @return グリッド式保護テンプレート保存メニューの縦の数
   */
  private def getAisleAmount: Int = config.getTemplateKeepAmount / 9 + 1

  /**
   * テンプレートメニュー用。メニューアイコン作成
   *
   * @param i      アイコン番号
   * @param player プレイヤー
   * @return メニューアイコン
   */
  private def getGridtempMenuicon(i: Int, player: Player): ItemStack = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    playerData.templateMap.get(i) match {
      case Some(template) =>
        val lore = List(
          s"${GREEN}設定内容",
          s"${GRAY}前方向：$AQUA${template.getAheadAmount}${GRAY}ユニット",
          s"${GRAY}後ろ方向：$AQUA${template.getBehindAmount}${GRAY}ユニット",
          s"${GRAY}右方向：$AQUA${template.getRightAmount}${GRAY}ユニット",
          s"${GRAY}左方向：$AQUA${template.getLeftAmount}${GRAY}ユニット",
          s"${GREEN}左クリックで設定を読み込み",
          s"${RED}右クリックで現在の設定で上書き"
        )
        Util.getMenuIcon(
          Material.CHEST,
          1,
          s"${GREEN}テンプレNo.${i + 1}(設定済)",
          lore,
          true
        )
      case None =>
        val lore = List(
          s"${GREEN}未設定",
          s"${RED}左クリックで現在の設定を保存"
        )
        Util.getMenuIcon(
          Material.PAPER,
          1,
          s"${RED}テンプレNo.${i + 1}",
          lore,
          true
        )
    }
  }
}