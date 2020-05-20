package com.github.unchama.buildassist.menu

import cats.data.NonEmptyList
import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.{ColorScheme, CommonButtons}
import org.bukkit.entity.Player

object MineStackMassCraftMenu {
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}

  type MineStackItemId = String

  case class MassCraftRecipe(ingredients: NonEmptyList[(MineStackItemId, Int)],
                             outputs: NonEmptyList[(MineStackItemId, Int)]) {
    /**
     * 押すとこのレシピのクラフトが実行されるようなボタンを計算する。
     *
     * プレーヤーが`requiredBuildLevel`に達していない場合にこのボタンを押すと、
     * クラフトは実行されず適切なメッセージがプレーヤーに通達される。
     *
     * @param requiredBuildLevel レシピの実行に必要な建築レベル
     * @param recipeScale レシピを実行する回数
     */
    def computeButton(player: Player, requiredBuildLevel: Int, recipeScale: Int): IO[Button] = {
      ???
    }
  }

  case class MassCraftRecipeBlock(recipe: MassCraftRecipe, recipeScales: List[Int], requiredBuildLevel: Int) {
    def toLayout(player: Player, beginIndex: Int): IO[List[(Int, Slot)]] = {
      import cats.implicits._

      recipeScales.zipWithIndex
        .traverse { case (scale, scaleIndex) =>
          for {
            button <- recipe.computeButton(player, requiredBuildLevel, scale)
          } yield (beginIndex + scaleIndex, button)
        }
    }
  }

  val recipeBlocks: Seq[List[(Int, MassCraftRecipeBlock)]] = {
    import eu.timepit.refined.auto._

    val oneToHundred: List[Int] = List(1, 10, 100)
    val oneToThousand: List[Int] = List(1, 10, 100, 1000)
    val oneToTenThousand: List[Int] = List(1, 10, 100, 1000, 10000)

    Seq(
      List(
        ChestSlotRef(0, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("stone", 10)),
            NonEmptyList.of(("step0", 20))
          ), oneToTenThousand, 1
        ),
        ChestSlotRef(1, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("stone", 10)),
            NonEmptyList.of(("smooth_brick0", 10))
          ), oneToTenThousand, 1
        ),
        ChestSlotRef(2, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("granite", 10)),
            NonEmptyList.of(("polished_granite", 10))
          ), oneToThousand, 2
        ),
        ChestSlotRef(2, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("diorite", 10)),
            NonEmptyList.of(("polished_diorite", 10))
          ), oneToThousand, 2
        ),
        ChestSlotRef(3, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("andesite", 10)),
            NonEmptyList.of(("polished_andesite", 10))
          ), oneToThousand, 2
        ),
        ChestSlotRef(3, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("quartz", 40)),
            NonEmptyList.of(("quartz_block", 10))
          ), oneToThousand, 2
        ),
        ChestSlotRef(4, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("brick_item", 40)),
            NonEmptyList.of(("brick", 10))
          ), oneToThousand, 2
        ),
        ChestSlotRef(4, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("nether_brick_item", 40)),
            NonEmptyList.of(("nether_brick", 10))
          ), oneToThousand, 2
        ),
      ),
      List(
        ChestSlotRef(0, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("snow_ball", 40)),
            NonEmptyList.of(("snow_block", 10))
          ), oneToThousand, 2
        ),
        ChestSlotRef(0, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("nether_stalk", 20), ("nether_brick_item", 20)),
            NonEmptyList.of(("red_nether_brick", 10))
          ), oneToThousand, 2
        ),
        ChestSlotRef(1, 1) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("iron_ore", 4), ("coal", 1)),
            NonEmptyList.of(("iron_ingot", 4))
          ), oneToHundred, 3
        ),
        ChestSlotRef(1, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("iron_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("iron_ingot", 50))
          ), oneToHundred, 3
        ),
        ChestSlotRef(2, 1) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("gold_ore", 4), ("coal", 1)),
            NonEmptyList.of(("gold_ingot", 4))
          ), oneToHundred, 3
        ),
        ChestSlotRef(2, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("gold_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("gold_ingot", 50))
          ), oneToHundred, 3
        ),
        ChestSlotRef(3, 1) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("sand", 4), ("coal", 1)),
            NonEmptyList.of(("glass", 4))
          ), oneToHundred, 3
        ),
        ChestSlotRef(3, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("sand", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("glass", 50))
          ), oneToHundred, 3
        ),
        ChestSlotRef(4, 1) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("netherrack", 4), ("coal", 1)),
            NonEmptyList.of(("nether_brick_item", 4))
          ), oneToHundred, 3
        ),
        ChestSlotRef(4, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("netherrack", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("nether_brick_item", 50))
          ), oneToHundred, 3
        ),
      ),
      List(
        ChestSlotRef(0, 1) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("clay_ball", 4), ("coal", 1)),
            NonEmptyList.of(("brick_item", 4))
          ), oneToHundred, 3
        ),
        ChestSlotRef(0, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("clay_ball", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("brick_item", 50))
          ), oneToHundred, 3
        ),
      )
    )
  }

  def apply(pageNumber: Int = 1): Menu = {
    import eu.timepit.refined.auto._

    val menuFrame = {
      import com.github.unchama.menuinventory.syntax._
      MenuFrame(6.chestRows, ColorScheme.purpleBold(s"MineStackブロック一括クラフト${pageNumber}"))
    }

    new Menu {
      override val frame: MenuFrame = menuFrame

      override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
        def buttonToTransferTo(newPageNumber: Int, skullOwnerReference: SkullOwnerReference): Button =
          CommonButtons.transferButton(
            new SkullItemStackBuilder(skullOwnerReference),
            s"${newPageNumber}ページ目へ",
            MineStackMassCraftMenu(newPageNumber)
          )

        val previousPageButtonSection =
          if (pageNumber > 1) {
            Map(ChestSlotRef(5, 7) -> buttonToTransferTo(pageNumber - 1, SkullOwners.MHF_ArrowUp))
          } else {
            Map()
          }

        val nextPageButtonSection =
          if (recipeBlocks.isDefinedAt(pageNumber)) {
            Map(ChestSlotRef(5, 8) -> buttonToTransferTo(pageNumber + 1, SkullOwners.MHF_ArrowDown))
          } else {
            Map()
          }

        val backToMenuButtonSection =
          Map(
            ChestSlotRef(5, 0) -> CommonButtons.transferButton(
              new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
              "ホームへ", BuildMainMenu
            )
          )

        import cats.implicits._

        for {
          recipeSectionBlocks <-
            recipeBlocks(pageNumber - 1)
              .traverse { case (beginIndex, recipeBlock) =>
                recipeBlock.toLayout(player, beginIndex)
              }

          recipeSection = recipeSectionBlocks.flatten.toMap
        } yield {
          MenuSlotLayout(
            recipeSection ++ previousPageButtonSection ++ nextPageButtonSection ++ backToMenuButtonSection
          )
        }
      }
    }
  }
}
