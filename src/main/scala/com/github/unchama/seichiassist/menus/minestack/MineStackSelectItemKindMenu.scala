package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import cats.implicits.toTraverseOps
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectWithKindVariants
import eu.timepit.refined.auto._
import org.bukkit.ChatColor.{BOLD, DARK_BLUE}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object MineStackSelectItemKindMenu {

  class Environment(
    implicit val canOpenCategorizedMineStackMenu: CanOpen[IO, CategorizedMineStackMenu],
    implicit val mineStackAPI: MineStackAPI[IO, Player, ItemStack],
    implicit val gachaPrizeAPI: GachaPrizeAPI[IO, ItemStack, Player]
  )

}

case class MineStackSelectItemKindMenu(
  group: MineStackObjectWithKindVariants[ItemStack],
  oldPage: Int
) extends Menu {

  import com.github.unchama.menuinventory.syntax._

  override type Environment = MineStackSelectItemKindMenu.Environment
  override val frame: MenuFrame =
    MenuFrame(6.chestRows, s"$DARK_BLUE${BOLD}MineStack(アイテム種類選択)")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._
    val buttonMapping = (List(group.representative) ++ group.kindVariants).zipWithIndex.map {
      case (inListMineStackObj, index) =>
        index -> MineStackButtons(player).getMineStackObjectButtonOf(inListMineStackObj)
    } ++ List(
      ChestSlotRef(5, 0) -> IO(
        CommonButtons.transferButton(
          new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp),
          s"MineStack${oldPage + 1}ページ目へ",
          CategorizedMineStackMenu(group.category, oldPage)
        )
      )
    )
    for {
      mapping <- buttonMapping.traverse(_.sequence)
    } yield MenuSlotLayout(mapping: _*)

  }

}
