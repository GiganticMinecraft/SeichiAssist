package com.github.unchama.buildassist.menu

import cats.data.{Kleisli, NonEmptyList}
import cats.effect.IO
import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, ReloadingButton}
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.{BuildMainMenu, ColorScheme, CommonButtons}
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.text.NumberFormat
import java.util.Locale

object MineStackMassCraftMenu {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{
    layoutPreparationContext,
    onMainThread
  }

  /**
   * ソース上で定義されているIDの一覧は[[com.github.unchama.seichiassist.MineStackObjectList]]を参照。
   */
  type MineStackItemId = String

  class Environment(
    implicit val canOpenBuildMainMenu: CanOpen[IO, BuildMainMenu.type],
    val canOpenItself: CanOpen[IO, MineStackMassCraftMenu],
    val mineStackAPI: MineStackAPI[IO, Player, ItemStack]
  )

  case class MassCraftRecipe(
    ingredients: NonEmptyList[(MineStackItemId, Int)],
    products: NonEmptyList[(MineStackItemId, Int)]
  ) {

    /**
     * このレシピのクラフトを `scale` 回実行するような新たなレシピを作成する。
     */
    def scaleBy(scale: Int): MassCraftRecipe = {
      def scaleChunk(chunk: (MineStackItemId, Int)): (MineStackItemId, Int) =
        chunk match {
          case (id, amount) => (id, amount * scale)
        }

      MassCraftRecipe(ingredients.map(scaleChunk), products.map(scaleChunk))
    }

    /**
     * 押すとこのレシピのクラフトが実行されるようなボタンを計算する。
     *
     * プレーヤーが`requiredBuildLevel`に達していない場合にこのボタンを押すと、 クラフトは実行されず適切なメッセージがプレーヤーに通達される。
     *
     * @param requiredMassCraftLevel
     *   レシピの実行に必要なクラフトレベル
     * @param menuPageNumber
     *   このボタンが表示される一括クラフト画面のページ番号
     */
    def computeButton(player: Player, requiredMassCraftLevel: Int, menuPageNumber: Int)(
      implicit environment: Environment
    ): IO[Button] = {
      import cats.implicits._

      def queryAmountOf(mineStackObj: MineStackObject[ItemStack]): IO[Long] =
        environment.mineStackAPI.mineStackRepository.getStackedAmountOf(player, mineStackObj)

      def toMineStackObjectChunk(
        chunk: (MineStackItemId, Int)
      ): (MineStackObject[ItemStack], Int) =
        chunk.leftMap(id =>
          environment.mineStackAPI.mineStackObjectList.findByName(id).unsafeRunSync().get
        )

      def enumerateChunkDetails(
        chunks: NonEmptyList[(MineStackObject[ItemStack], Int)]
      ): String =
        chunks.map { case (obj, amount) => s"${obj.uiName.get}${amount}個" }.mkString_("+")

      val requiredBuildLevel =
        BuildAssist.config.getMinestackBlockCraftlevel(requiredMassCraftLevel)

      val ingredientObjects = ingredients.map(toMineStackObjectChunk)
      val productObjects = products.map(toMineStackObjectChunk)

      val iconComputation = {
        val title = {
          def enumerateChunkNames(
            chunks: NonEmptyList[(MineStackObject[ItemStack], Int)]
          ): String =
            chunks.map(_._1.uiName.get).mkString_("と")

          s"$YELLOW$UNDERLINE$BOLD" +
            s"${enumerateChunkNames(ingredientObjects)}を${enumerateChunkNames(productObjects)}に変換します"
        }

        val loreHeading = {
          s"$RESET$GRAY" +
            s"${enumerateChunkDetails(ingredientObjects)}→${enumerateChunkDetails(productObjects)}"
        }

        for {
          possessionDisplayBlock <-
            (ingredientObjects.toList ++ productObjects.toList).map(_._1).traverse { obj =>
              queryAmountOf(obj).map { amount =>
                s"$RESET$GRAY${obj.uiName.get}の数：${NumberFormat.getNumberInstance(Locale.US).format(amount)}"
              }
            }
        } yield {
          val lore = List(
            List(loreHeading),
            possessionDisplayBlock,
            List(
              s"$RESET${GRAY}建築Lv${requiredBuildLevel}以上で利用可能",
              s"$RESET$DARK_RED${UNDERLINE}クリックで変換"
            )
          ).flatten

          // MineStackObjectから直接メタ等のスタック情報を受け継ぐべきなのでビルダを使わずメタを直接書き換える
          val productStack = productObjects.head._1.itemStack

          productStack.setItemMeta {
            import scala.jdk.javaapi.CollectionConverters.asJava

            val meta = productStack.getItemMeta
            meta.setDisplayName(title)
            meta.setLore(asJava(lore))
            meta
          }

          // 対数オーダーをアイコンのスタック数にする
          productStack.setAmount(products.head._2.toString.length)
          productStack
        }

      }

      val buttonEffect = for {
        buildLevel <- BuildAssist.instance.buildAmountDataRepository(player).read.toIO
        allIngredientsAmount <- ingredientObjects.traverse {
          case (obj, _) =>
            environment.mineStackAPI.mineStackRepository.getStackedAmountOf(player, obj)
        }
        allIngredientsAvailable = (allIngredientsAmount zip ingredientObjects.map(_._2))
          .forall { case (mineStackAmount, requireAmount) => mineStackAmount >= requireAmount }
        isLowerBuildLevel = buildLevel.levelCorrespondingToExp.level < requiredBuildLevel
        errorEffect =
          if (isLowerBuildLevel) {
            MessageEffect(s"${RED}建築Lvが足りません")
          } else if (!allIngredientsAvailable) {
            MessageEffect(s"${RED}クラフト材料が足りません")
          } else emptyEffect
        craftEffect = ingredientObjects.traverse {
          case (mineStackObject, amount) =>
            environment
              .mineStackAPI
              .mineStackRepository
              .subtractStackedAmountOf(player, mineStackObject, amount)
        } >> productObjects.traverse {
          case (mineStackObject, amount) =>
            environment
              .mineStackAPI
              .mineStackRepository
              .addStackedAmountOf(player, mineStackObject, amount)
        } >> {
          val successMessage = s"$GREEN${enumerateChunkDetails(ingredientObjects)}→" +
            s"${enumerateChunkDetails(productObjects)}変換"

          MessageEffectF[IO](successMessage).apply(player)
        }
      } yield {
        val effect =
          if (!isLowerBuildLevel && allIngredientsAvailable)
            Kleisli { _: Player => craftEffect }
          else errorEffect

        LeftClickButtonEffect(
          SequentialEffect(
            effect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
          )
        )
      }

      for {
        icon <- iconComputation
        effect <- buttonEffect
      } yield {
        val button = Button(icon, effect)
        val reloadTargetMenu = MineStackMassCraftMenu(menuPageNumber)

        ReloadingButton(reloadTargetMenu)(button)
      }
    }
  }

  case class MassCraftRecipeBlock(
    recipe: MassCraftRecipe,
    recipeScales: List[Int],
    requiredBuildLevel: Int
  ) {
    def toLayout(player: Player, beginIndex: Int, pageNumber: Int)(
      implicit environment: Environment
    ): IO[List[(Int, Slot)]] = {
      import cats.implicits._

      recipeScales.zipWithIndex.traverse {
        case (scale, scaleIndex) =>
          for {
            button <- recipe
              .scaleBy(scale)
              .computeButton(player, requiredBuildLevel, pageNumber)
          } yield (beginIndex + scaleIndex, button)
      }
    }
  }

  /**
   * 各メニューページのレシピブロックを持つ`Seq`。
   *
   * `Seq`の`i`番目の`List`には、 メニュー`(i+1)`番目のインベントリ内のスロットへの参照とレシピブロックの組が格納されている。
   */
  val recipeBlocks: Seq[List[(Int, MassCraftRecipeBlock)]] = {
    import eu.timepit.refined.auto._

    val oneToHundred: List[Int] = List(1, 10, 100)
    val oneToThousand: List[Int] = List(1, 10, 100, 1000)
    val oneToTenThousand: List[Int] = List(1, 10, 100, 1000, 10000)

    Seq(
      List(
        ChestSlotRef(0, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("stone", 10)), NonEmptyList.of(("step0", 20))),
          oneToTenThousand,
          1
        ),
        ChestSlotRef(1, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("stone", 10)),
            NonEmptyList.of(("smooth_brick0", 10))
          ),
          oneToTenThousand,
          1
        ),
        ChestSlotRef(2, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("granite", 10)),
            NonEmptyList.of(("polished_granite", 10))
          ),
          oneToThousand,
          2
        ),
        ChestSlotRef(2, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("diorite", 10)),
            NonEmptyList.of(("polished_diorite", 10))
          ),
          oneToThousand,
          2
        ),
        ChestSlotRef(3, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("andesite", 10)),
            NonEmptyList.of(("polished_andesite", 10))
          ),
          oneToThousand,
          2
        ),
        ChestSlotRef(3, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("quartz", 40)),
            NonEmptyList.of(("quartz_block", 10))
          ),
          oneToThousand,
          2
        ),
        ChestSlotRef(4, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("brick_item", 40)), NonEmptyList.of(("brick", 10))),
          oneToThousand,
          2
        ),
        ChestSlotRef(4, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("nether_brick_item", 40)),
            NonEmptyList.of(("nether_brick", 10))
          ),
          oneToThousand,
          2
        )
      ),
      List(
        ChestSlotRef(0, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("snow_ball", 40)),
            NonEmptyList.of(("snow_block", 10))
          ),
          oneToThousand,
          2
        ),
        ChestSlotRef(0, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("nether_stalk", 20), ("nether_brick_item", 20)),
            NonEmptyList.of(("red_nether_brick", 10))
          ),
          oneToThousand,
          2
        ),
        ChestSlotRef(1, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("iron_ore", 40), ("coal", 10)),
            NonEmptyList.of(("iron_ingot", 40))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(1, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("iron_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("iron_ingot", 50))
          ),
          oneToHundred,
          3
        ),
        ChestSlotRef(2, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("gold_ore", 40), ("coal", 10)),
            NonEmptyList.of(("gold_ingot", 40))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("gold_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("gold_ingot", 50))
          ),
          oneToHundred,
          3
        ),
        ChestSlotRef(3, 1) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("sand", 4), ("coal", 1)),
            NonEmptyList.of(("glass", 4))
          ),
          oneToHundred,
          3
        ),
        ChestSlotRef(3, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("sand", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("glass", 50))
          ),
          oneToHundred,
          3
        ),
        ChestSlotRef(4, 1) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("netherrack", 4), ("coal", 1)),
            NonEmptyList.of(("nether_brick_item", 4))
          ),
          oneToHundred,
          3
        ),
        ChestSlotRef(4, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("netherrack", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("nether_brick_item", 50))
          ),
          oneToHundred,
          3
        )
      ),
      List(
        ChestSlotRef(0, 1) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("clay_ball", 4), ("coal", 1)),
            NonEmptyList.of(("brick_item", 4))
          ),
          oneToHundred,
          3
        ),
        ChestSlotRef(0, 6) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("clay_ball", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("brick_item", 50))
          ),
          oneToHundred,
          3
        ),
        ChestSlotRef(1, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("ender_stone", 4)),
            NonEmptyList.of(("end_bricks", 4))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(1, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("coal", 9)), NonEmptyList.of(("coal_block", 1))),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("iron_ingot", 9)),
            NonEmptyList.of(("iron_block", 1))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("gold_ingot", 9)),
            NonEmptyList.of(("gold_block", 1))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(3, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("lapis_lazuli", 9)),
            NonEmptyList.of(("lapis_block", 1))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(3, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("redstone", 9)),
            NonEmptyList.of(("redstone_block", 1))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(4, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("emerald", 9)),
            NonEmptyList.of(("emerald_block", 1))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(4, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("diamond", 9)),
            NonEmptyList.of(("diamond_block", 1))
          ),
          oneToThousand,
          3
        )
      ),
      List(
        ChestSlotRef(0, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("clay", 1)), NonEmptyList.of(("clay_ball", 4))),
          oneToThousand,
          3
        ),
        ChestSlotRef(0, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("magma", 8), ("bucket", 1)),
            NonEmptyList.of(("lava_bucket", 1))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(1, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("quartz_ore", 4), ("coal", 1)),
            NonEmptyList.of(("quartz", 4))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(1, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("quartz_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("quartz", 50))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("quartz_block", 1)),
            NonEmptyList.of(("quartz_block1", 1))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("quartz_block", 2)),
            NonEmptyList.of(("quartz_block2", 1))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(3, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("redstone_ore", 4), ("coal", 1)),
            NonEmptyList.of(("redstone", 4))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(3, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("redstone_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("redstone", 50))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(4, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("lapis_ore", 4), ("coal", 1)),
            NonEmptyList.of(("lapis_lazuli", 4))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(4, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("lapis_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("lapis_lazuli", 50))
          ),
          oneToThousand,
          3
        )
      ),
      List(
        ChestSlotRef(0, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("emerald_ore", 4), ("coal", 1)),
            NonEmptyList.of(("emerald", 4))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(0, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("emerald_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("emerald", 50))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(1, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("diamond_ore", 4), ("coal", 1)),
            NonEmptyList.of(("diamond", 4))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(1, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("diamond_ore", 50), ("lava_bucket", 1)),
            NonEmptyList.of(("diamond", 50))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("iron_ingot", 3)), NonEmptyList.of(("bucket", 1))),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("sandstone", 4), ("coal", 1)),
            NonEmptyList.of(("sandstone2", 4))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(3, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("chorus_fruit", 4), ("coal", 1)),
            NonEmptyList.of(("popped_chorus_fruit", 4))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(3, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("popped_chorus_fruit", 4)),
            NonEmptyList.of(("purpur_block", 4))
          ),
          oneToThousand,
          3
        )
      ),
      // フェンス
      List(
        ChestSlotRef(0, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("wood", 4), ("stick", 2)),
            NonEmptyList.of(("fence", 3))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(1, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("wood_1", 4), ("stick", 2)),
            NonEmptyList.of(("spruce_fence", 3))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("wood_2", 4), ("stick", 2)),
            NonEmptyList.of(("birch_fence", 3))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(3, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("wood_3", 4), ("stick", 2)),
            NonEmptyList.of(("jungle_fence", 3))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(4, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("wood_4", 4), ("stick", 2)),
            NonEmptyList.of(("acacia_fence", 3))
          ),
          oneToThousand,
          3
        ),
        ChestSlotRef(0, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(
            NonEmptyList.of(("wood_5", 4), ("stick", 2)),
            NonEmptyList.of(("dark_oak_fence", 3))
          ),
          oneToThousand,
          3
        )
      ),
      // 木材ハーフ
      List(
        ChestSlotRef(0, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("wood", 3)), NonEmptyList.of(("wood_step0", 6))),
          oneToThousand,
          3
        ),
        ChestSlotRef(1, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("wood_1", 3)), NonEmptyList.of(("wood_step1", 6))),
          oneToThousand,
          3
        ),
        ChestSlotRef(2, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("wood_2", 3)), NonEmptyList.of(("wood_step2", 6))),
          oneToThousand,
          3
        ),
        ChestSlotRef(3, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("wood_3", 3)), NonEmptyList.of(("wood_step3", 6))),
          oneToThousand,
          3
        ),
        ChestSlotRef(4, 0) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("wood_4", 3)), NonEmptyList.of(("wood_step4", 6))),
          oneToThousand,
          3
        ),
        ChestSlotRef(0, 5) -> MassCraftRecipeBlock(
          MassCraftRecipe(NonEmptyList.of(("wood_5", 3)), NonEmptyList.of(("wood_step5", 6))),
          oneToThousand,
          3
        )
      )
    )
  }
}

case class MineStackMassCraftMenu(pageNumber: Int = 1) extends Menu {
  override type Environment = MineStackMassCraftMenu.Environment

  override val frame: MenuFrame = {
    import com.github.unchama.menuinventory.syntax._
    MenuFrame(6.chestRows, ColorScheme.purpleBold(s"MineStackブロック一括クラフト$pageNumber"))
  }

  import eu.timepit.refined.auto._

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._

    def buttonToTransferTo(
      newPageNumber: Int,
      skullOwnerReference: SkullOwnerReference
    ): Button =
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
      if (MineStackMassCraftMenu.recipeBlocks.isDefinedAt(pageNumber)) {
        Map(ChestSlotRef(5, 8) -> buttonToTransferTo(pageNumber + 1, SkullOwners.MHF_ArrowDown))
      } else {
        Map()
      }

    val backToMenuButtonSection =
      Map(
        ChestSlotRef(5, 0) -> CommonButtons.transferButton(
          new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
          "ホームへ",
          BuildMainMenu
        )
      )

    import cats.implicits._

    for {
      recipeSectionBlocks <-
        MineStackMassCraftMenu.recipeBlocks(pageNumber - 1).traverse {
          case (beginIndex, recipeBlock) =>
            recipeBlock.toLayout(player, beginIndex, pageNumber)
        }

      recipeSection = recipeSectionBlocks.flatten.toMap
    } yield {
      MenuSlotLayout(
        recipeSection ++ previousPageButtonSection ++ nextPageButtonSection ++ backToMenuButtonSection
      )
    }
  }
}
