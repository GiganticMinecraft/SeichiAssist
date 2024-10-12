package com.github.unchama.seichiassist.menus

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.targetedeffect._
import com.github.unchama.targetedeffect.player.PlayerEffects._
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * サーバ間移動メニュー
 *
 * Created by karayuu on 2019/12/11
 */
object ServerSwitchMenu extends Menu {

  import enumeratum._
  import eu.timepit.refined.auto._

  /**
   * UI上のサーバを表すオブジェクト.
   *
   * @param uiLabel
   *   UI上で表示される際のサーバ名
   * @param identifier
   *   /server を使って移動する際に指定するサーバの識別子
   * @param chestSlotRef
   *   UI上でのButtonの表示位置
   * @param material
   *   UI上でのButtonのMaterial
   */
  sealed abstract class Server(
    val uiLabel: String,
    val identifier: String,
    val chestSlotRef: Int,
    val material: Material
  ) extends EnumEntry

  case object Server extends Enum[Server] {

    case object ARCADIA
        extends Server(
          s"$YELLOW${BOLD}アルカディア",
          "s1",
          ChestSlotRef(0, 0),
          Material.DIAMOND_PICKAXE
        )

    case object EDEN
        extends Server(s"$YELLOW${BOLD}エデン", "s2", ChestSlotRef(0, 1), Material.DIAMOND_SHOVEL)

    case object VALHALLA
        extends Server(s"$YELLOW${BOLD}ヴァルハラ", "s3", ChestSlotRef(0, 2), Material.DIAMOND_AXE)

    case object PUBLIC
        extends Server(s"$GREEN${BOLD}公共施設", "s7", ChestSlotRef(0, 8), Material.DIAMOND)

    case object SEICHI
        extends Server(s"$YELLOW${BOLD}整地専用", "s5", ChestSlotRef(0, 4), Material.IRON_PICKAXE)

    val values: IndexedSeq[Server] = findValues

  }

  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread

  class Environment(
    implicit val ioCanOpenStickMenu: IO CanOpen FirstPage.type,
    implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
  )

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(2.chestRows, s"$DARK_RED${BOLD}サーバーを選択してください")

  val serverButtonLayout: MenuSlotLayout = {
    val layoutMap = Server
      .values
      .map { server =>
        val slotIndex = server.chestSlotRef
        val iconItemStack = new IconItemStackBuilder(server.material)
          .title(server.uiLabel + "サーバー")
          .lore {
            // 整地専用サーバーの場合は上級者向けのサーバーである旨を通知
            if (server.identifier == "s5")
              List("上級者向けのサーバー", "始めたての頃は他のサーバーがおすすめ").map { str => s"$RESET$BLUE$str" }
            else Nil
          }
          .enchanted()
          .build()
        val button = Button(
          iconItemStack,
          LeftClickButtonEffect {
            ComputedEffect(_ => {
              connectToServerEffect(server.identifier)
            })
          }
        )
        slotIndex -> button
      }
      .toMap

    MenuSlotLayout(layoutMap)
  }

  /**
   * @return
   *   `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] =
    IO.pure {
      import environment._

      MenuSlotLayout(ChestSlotRef(1, 0) -> CommonButtons.openStickMenu)
        .merge(serverButtonLayout)
    }
}
