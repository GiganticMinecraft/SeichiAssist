package com.github.unchama.seichiassist.menus.skill

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.database.manipulators.DonateDataManipulator._
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import net.md_5.bungee.api.ChatColor._
import org.bukkit.ChatColor.{GOLD, GREEN, RESET}
import org.bukkit.Material
import org.bukkit.entity.Player

object PremiumPointTransactionHistoryMenu {
  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}
  import eu.timepit.refined.auto._

  def buttonToTransferTo(number: Int, skullOwnerReference: SkullOwnerReference): Button =
    CommonButtons.transferButton(
      new SkullItemStackBuilder(skullOwnerReference),
      s"${number}ページ目へ",
      apply(number)
    )

  def apply(pageNumber: Int): Menu = new Menu {
    override val frame: MenuFrame = MenuFrame(4.chestRows, s"$BLUE${BOLD}プレミアムエフェクト購入履歴")

    def computeDynamicParts(player: Player): IO[List[(Int, Button)]] = {
      for {
        history <- SeichiAssist.databaseGateway.donateDataManipulator.loadTransactionHistoryFor(player)
      } yield {
        val entriesPerPage = 3 * 9
        val slicedHistory = history.slice((pageNumber - 1) * entriesPerPage, pageNumber * entriesPerPage)

        val historySection =
          slicedHistory.zipWithIndex.map { case (transaction, index) =>
            val itemStack =
              transaction match {
                case Obtained(amount, date) =>
                  new IconItemStackBuilder(Material.DIAMOND)
                    .title(s"$AQUA$UNDERLINE${BOLD}寄付")
                    .lore(List(
                      s"${RESET.toString}${GREEN}金額：${amount * 100}",
                      s"$RESET${GREEN}プレミアムエフェクトポイント：+$amount",
                      s"$RESET${GREEN}日時：$date"
                    ))
                    .build()
                case Used(amount, date, forPurchaseOf) => {
                  forPurchaseOf match {
                    case Left(unknownEffectName) =>
                      new IconItemStackBuilder(Material.BEDROCK)
                        .title(s"$RESET${YELLOW}購入エフェクト：未定義($unknownEffectName)")
                    case Right(skill) =>
                      new IconItemStackBuilder(skill.materialOnUI)
                        .title(s"$RESET${YELLOW}購入エフェクト：${skill.nameOnUI}")
                  }
                }.lore(
                  s"$RESET${GOLD}プレミアムエフェクトポイント： -$amount",
                  s"$RESET${GOLD}日時：$date"
                ).build()
              }

            (index, Button(itemStack, Nil))
          }

        val previousPageButtonSection =
          if (pageNumber > 1)
            Seq(ChestSlotRef(3, 7) -> buttonToTransferTo(pageNumber - 1, SkullOwners.MHF_ArrowUp))
          else
            Seq()

        val nextPageButtonSection =
          if (history.drop(pageNumber * entriesPerPage).nonEmpty)
            Seq(ChestSlotRef(3, 8) -> buttonToTransferTo(pageNumber + 1, SkullOwners.MHF_ArrowDown))
          else
            Seq()

        historySection ++ previousPageButtonSection ++ nextPageButtonSection
      }
    }

    val constantParts: Map[Int, Button] = {
      val moveBackButton =
        CommonButtons.transferButton(
          new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
          "整地スキルエフェクト選択メニューへ",
          ActiveSkillEffectMenu,
        )

      Map(
        ChestSlotRef(3, 0) -> moveBackButton
      )
    }

    /**
     * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
     */
    override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
      for {
        dynamicParts <- computeDynamicParts(player)
      } yield MenuSlotLayout(constantParts ++ dynamicParts)
    }
  }
}
