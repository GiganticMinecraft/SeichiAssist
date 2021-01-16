package com.github.unchama.seichiassist.data

import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.enumeration.RelativeDirection
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.{Inventory, ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material}

import java.text.NumberFormat
import scala.collection.mutable

/**
 * 保護関連メニュー
 *
 * @author karayuu
 */
object RegionMenuData {
  sealed trait UnitQuantity {
    def value: Int
  }

  object UnitQuantity {
    case object ONE extends UnitQuantity {
      override def value = 1
    }

    case object TEN extends UnitQuantity {
      override def value = 10
    }

    case object ONE_HUNDRED extends UnitQuantity {
      override def value = 100
    }
  }
  private val config = SeichiAssist.seichiAssistConfig
  private val nfNum = NumberFormat.getNumberInstance
  private val CANNOT_EXPAND = s"$RED${UNDERLINE}これ以上拡張できません"
  private val CANNOT_SHRINK = s"$RED${UNDERLINE}これ以上縮小できません"
  val canClaim: mutable.Map[Player, Boolean] = new mutable.HashMap().withDefaultValue(true)
  val units: mutable.Map[Player, UnitQuantity] = new mutable.HashMap().withDefaultValue(UnitQuantity.ONE)

  def toggleUnit(player: Player): Unit = {
    units(player) = units(player) match {
      case UnitQuantity.ONE => UnitQuantity.TEN
      case UnitQuantity.TEN => UnitQuantity.ONE_HUNDRED
      case UnitQuantity.ONE_HUNDRED => UnitQuantity.ONE
    }
  }
  /**
   * グリッド式保護メニュを開きます。
   *
   * @param player
   * @return
   */
  def getGridWorldGuardMenu(player: Player): Inventory = {
    val pd = SeichiAssist.playermap(player.getUniqueId)
    val unitMap = pd.unitMap
    val directionMap = getPlayerDirectionString(player)
    val gridInv = Bukkit.createInventory(null, InventoryType.DISPENSER, s"${LIGHT_PURPLE}グリッド式保護設定メニュー")
    /* ディスペンサーの配置:
    +-+-+-+
    |0|1|2|
    +-+-+-+
    |3|4|5|
    +-+-+-+
    |6|7|8|
    +-+-+-+
    */

    Map(
      0 -> new IconItemStackBuilder(Material.STAINED_GLASS_PANE, 0)
        .amount(1)
        .title(s"${GREEN}拡張単位の変更")
        .lore(
          s"${GREEN}現在のユニット指定量",
          s"$AQUA${units(player).value}${GREEN}ユニット($AQUA${units(player).value * 15}${GREEN}ブロック)/1クリック",
          s"$RED${UNDERLINE}クリックで変更"
        ),
      1 -> {
        val lore = getGridLore(directionMap(RelativeDirection.AHEAD), unitMap(RelativeDirection.AHEAD))
        val err = if (!pd.canGridExtend(RelativeDirection.AHEAD, player.getWorld))
          Some(CANNOT_EXPAND)
        else if (!pd.canGridReduce(RelativeDirection.AHEAD))
          Some(CANNOT_SHRINK)
        else
          None

        new IconItemStackBuilder(Material.STAINED_GLASS_PANE, 14)
          .amount(1)
          .title(s"${DARK_GREEN}前に${units(player).value}ユニット増やす/減らす")
          .lore(lore :++ err)
      },

      2 -> new IconItemStackBuilder(Material.CHEST)
        .amount(1)
        .title(s"${GREEN}設定保存メニュー")
        .lore(s"$RED${UNDERLINE}クリックで開く"),

      3 -> {
        val lore = getGridLore(directionMap(RelativeDirection.LEFT), unitMap(RelativeDirection.LEFT))
        val err = if (!pd.canGridExtend(RelativeDirection.LEFT, player.getWorld))
          Some(CANNOT_EXPAND)
        else if (!pd.canGridReduce(RelativeDirection.LEFT))
          Some(CANNOT_SHRINK)
        else
          None

        new IconItemStackBuilder(Material.STAINED_GLASS_PANE, 10)
          .amount(1)
          .title(s"${DARK_GREEN}左に${units(player).value}ユニット増やす/減らす")
          .lore(lore :++ err)
      },

      4 -> {
        val sizeInfo = Map(
          RelativeDirection.AHEAD -> "前方向",
          RelativeDirection.BEHIND -> "後ろ方向",
          RelativeDirection.RIGHT -> "右方向",
          RelativeDirection.LEFT -> "左方向",
        ).map {
          case (rd, st) =>
            s"$GRAY$st：$AQUA${unitMap(rd)}${GRAY}ユニット($AQUA${nfNum.format(unitMap(rd) * 15)}${GRAY}ブロック)"
        }.toList

        new IconItemStackBuilder(Material.STAINED_GLASS_PANE, 11)
          .amount(1)
          .title(s"${DARK_GREEN}設定")
          .lore {
            (s"${GRAY}現在の設定" :: sizeInfo) :++ List (
              s"${GRAY}保護ユニット数：$AQUA${pd.gridChunkAmount}",
              s"${GRAY}保護ユニット上限値：$RED${config.getGridLimitPerWorld(player.getWorld.getName)}"
            )
          }
      },

      5 -> {
        val lore = getGridLore(directionMap(RelativeDirection.RIGHT), unitMap(RelativeDirection.RIGHT))
        val err = if (!pd.canGridExtend(RelativeDirection.RIGHT, player.getWorld))
          Some(CANNOT_EXPAND)
        else if (!pd.canGridReduce(RelativeDirection.RIGHT))
          Some(CANNOT_SHRINK)
        else
          None

        new IconItemStackBuilder(Material.STAINED_GLASS_PANE, 5)
          .amount(1)
          .title(s"${DARK_GREEN}右に${units(player).value}ユニット増やす/減らす")
          .lore(lore :++ err)
      },

      6 -> new IconItemStackBuilder(Material.STAINED_GLASS_PANE, 4)
        .amount(1)
        .title(s"${RED}全設定リセット")
        .lore(s"$RED${UNDERLINE}取扱注意！！"),

      7 -> {
        val lore = getGridLore(directionMap(RelativeDirection.BEHIND), unitMap(RelativeDirection.BEHIND))
        val err = if (!pd.canGridExtend(RelativeDirection.BEHIND, player.getWorld))
          Some(CANNOT_EXPAND)
        else if (!pd.canGridReduce(RelativeDirection.BEHIND))
          Some(CANNOT_SHRINK)
        else
          None

        new IconItemStackBuilder(Material.STAINED_GLASS_PANE, 13)
          .amount(1)
          .title(s"${DARK_GREEN}後ろに${units(player).value}ユニット増やす/減らす")
          .lore(lore :++ err)
      },

      8 -> {
        //8マス目
        val (lore, durability, titleColor) = if (!config.isGridProtectionEnabled(player.getWorld)) (
          List(
            s"$RED${UNDERLINE}このワールドでは保護を作成できません"
          ),
          14,
          RED
        ) else if (!canClaim(player)) (
          List(
            s"$RED${UNDERLINE}以下の原因により保護を作成できません",
            s"$RED・保護の範囲が他の保護と重複している",
            s"$RED・保護の作成上限に達している"
          ),
          14,
          RED
        ) else (
          List(
            s"${DARK_GREEN}保護作成可能です",
            s"$RED${UNDERLINE}クリックで作成"
          ),
          11,
          GREEN
        )
        new IconItemStackBuilder(Material.WOOL, durability.toShort)
          .amount(1)
          .title(s"${titleColor}保護作成")
          .lore(lore)
      }
    ).foreach { case (idx, stack) =>
      gridInv.setItem(idx, stack.flagged(ItemFlag.HIDE_ATTRIBUTES).build())
    }

    gridInv
  }

  private def getGridLore(direction: String, unit: Int) = List(
    s"$RESET${GREEN}左クリックで増加",
    s"$RESET${RED}右クリックで減少",
    s"$RESET$GRAY$GRAY---------------",
    s"${GRAY}方向：$AQUA$direction",
    s"${GRAY}現在の指定方向ユニット数：$AQUA$unit$GRAY($AQUA${nfNum.format(unit * 15)}${GRAY}ブロック)"
  )

  private def getPlayerDirectionString(player: Player): Map[RelativeDirection, String] = {
    var rotation = (player.getLocation.getYaw + 180) % 360
    if (rotation < 0) rotation += 360
    import com.github.unchama.seichiassist.util.enumeration.Direction
    import com.github.unchama.seichiassist.util.enumeration.Direction._
    val theMap = if (0.0 <= rotation && rotation < 45.0) { //前が北(North)
      Map(
        RelativeDirection.BEHIND -> SOUTH,
        RelativeDirection.AHEAD -> NORTH,
        RelativeDirection.LEFT -> WEST,
        RelativeDirection.RIGHT -> EAST,
      )
    }
    else if (45.0 <= rotation && rotation < 135.0) { //前が東(East)
      Map(
        RelativeDirection.RIGHT -> SOUTH,
        RelativeDirection.LEFT -> NORTH,
        RelativeDirection.BEHIND -> WEST,
        RelativeDirection.AHEAD -> EAST,
      )
    }
    else if (135.0 <= rotation && rotation < 225.0) { //前が南(South)
      Map(
        RelativeDirection.AHEAD -> SOUTH,
        RelativeDirection.BEHIND -> NORTH,
        RelativeDirection.RIGHT -> WEST,
        RelativeDirection.LEFT -> EAST,
      )
    }
    else if (225.0 <= rotation && rotation < 315.0) { //前が西(West)
      Map(
        RelativeDirection.LEFT -> SOUTH,
        RelativeDirection.RIGHT -> NORTH,
        RelativeDirection.AHEAD -> WEST,
        RelativeDirection.BEHIND -> EAST,
      )
    }
    else if (315.0 <= rotation && rotation < 360.0) {
      Map(
        RelativeDirection.BEHIND -> SOUTH,
        RelativeDirection.AHEAD -> NORTH,
        RelativeDirection.LEFT -> WEST,
        RelativeDirection.RIGHT -> EAST,
      )
    } else {
      Map()
    }
    theMap.map { case (rd, d) =>
      (rd, d match {
        case SOUTH => Some("南(South)")
        case NORTH => Some("北(North)")
        case WEST => Some("西(West)")
        case EAST => Some("東(East)")
        case _ => None
      })
    }.filter(_._2.nonEmpty).map { case(rd, d) =>
      (rd, d.get)
    }
  }

  /**
   * グリッド式保護設定保存メニューを取得します。
   *
   * @param player プレイヤー
   * @return グリッド式保護・設定保存Inventory
   */
  def getGridTemplateInventory(player: Player): Inventory = {
    val inv = Bukkit.createInventory(null, 9 * (getRow + 1), s"${LIGHT_PURPLE}グリッド式保護・設定保存")
    (0 until config.getTemplateKeepAmount)
      .map(getGridtempMenuicon(_, player))
      .zipWithIndex
      .foreach { case (icon, i) =>
        inv.setItem(i, icon)
      }
    //戻るボタン
    val retIcon = new IconItemStackBuilder(Material.BARRIER)
      .amount(1)
      .title(s"${RED}グリッド式保護メニューに戻る")
      .lore(s"$RED${UNDERLINE}クリックで戻る")
      .flagged(ItemFlag.HIDE_ATTRIBUTES)
      .build()
    inv.setItem(getRow * 9, retIcon)
    inv
  }

  /**
   * テンプレートメニュー用。
   *
   * @return グリッド式保護テンプレート保存メニューの縦の数
   */
  private def getRow: Int = (config.getTemplateKeepAmount / 9.0).ceil.toInt

  /**
   * テンプレートメニュー用。メニューアイコン作成
   *
   * @param i      アイコン番号
   * @param player プレイヤー
   * @return メニューアイコン
   */
  private def getGridtempMenuicon(i: Int, player: Player): ItemStack = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val num = i + 1
    val (mat, lore, additionalText) = playerData.templateMap.get(i) match {
      case Some(template) =>
        (
          Material.CHEST,
          List(
            s"${GREEN}設定内容",
            s"${GRAY}前方向：$AQUA${template.aheadAmount}${GRAY}ユニット",
            s"${GRAY}後ろ方向：$AQUA${template.behindAmount}${GRAY}ユニット",
            s"${GRAY}右方向：$AQUA${template.rightAmount}${GRAY}ユニット",
            s"${GRAY}左方向：$AQUA${template.leftAmount}${GRAY}ユニット",
            s"${GREEN}左クリックで設定を読み込み",
            s"${RED}右クリックで現在の設定で上書き"
          ),
          "(設定済)"
        )
      case None =>
        (
          Material.PAPER,
          List(
            s"${GREEN}未設定",
            s"${RED}左クリックで現在の設定を保存"
          ),
          ""
        )
    }
    new IconItemStackBuilder(mat)
      .amount(1)
      .title(s"${GREEN}テンプレNo.$num$additionalText")
      .lore(lore)
      .flagged(ItemFlag.HIDE_ATTRIBUTES)
      .build()
  }
}