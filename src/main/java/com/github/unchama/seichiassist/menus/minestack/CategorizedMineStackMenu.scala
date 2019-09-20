package com.github.unchama.seichiassist.menus.minestack

import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import org.bukkit.entity.Player

object CategorizedMineStackMenu {
  private const val mineStackObjectPerPage = 9 * 5

  private suspend def Player.computeMenuLayout(category: MineStackObjectCategory, page: Int): IndexedSlotLayout {
    val categoryItemList = MineStackObjectList.minestacklist!!.filter { it.category() === category }
    val totalNumberOfPages = ceil(categoryItemList.size / 45.0).toInt()

    // オブジェクトリストが更新されるなどの理由でpageが最大値を超えてしまった場合、最後のページを計算する
    if (page >= totalNumberOfPages) return computeMenuLayout(category, totalNumberOfPages - 1)

    // カテゴリ内のMineStackアイテム取り出しボタンを含むセクション
    val categorizedItemSection =
        IndexedSlotLayout(
            categoryItemList.drop(mineStackObjectPerPage * page).take(mineStackObjectPerPage)
                .withIndex()
                .mapValues { with (MineStackButtons) { getMineStackItemButtonOf(it) } }
        )

    // ページ操作等のボタンを含むレイアウトセクション
    val uiOperationSection = run {
      def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference) = Button(
          SkullItemStackBuilder(skullOwnerReference)
              .title(s"$YELLOW$UNDERLINE${BOLD}MineStack${pageIndex + 1}ページ目へ")
              .lore(List(s"$RESET$DARK_RED${UNDERLINE}クリックで移動"))
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                CommonSoundEffects.menuTransitionFenceSound,
                forCategory(category, pageIndex).open
            )
          }
      )

      val mineStackMainMenuButtonSection = run {
        val mineStackMainMenuButton = Button(
            SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
                .title(s"$YELLOW$UNDERLINE${BOLD}MineStackメインメニューへ")
                .lore(List(s"$RESET$DARK_RED${UNDERLINE}クリックで移動"))
                .build(),
            FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
              sequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  MineStackMainMenu.open
              )
            }
        )

        singleSlotLayout { (9 * 5) to mineStackMainMenuButton }
      }

      val previousPageButtonSection = if (page > 0) {
        singleSlotLayout { 9 * 5 + 7 to buttonToTransferTo(page - 1, SkullOwners.MHF_ArrowUp) }
      } else emptyLayout

      val nextPageButtonSection = if (page + 1 < totalNumberOfPages) {
        singleSlotLayout { 9 * 5 + 8 to buttonToTransferTo(page + 1, SkullOwners.MHF_ArrowDown) }
      } else emptyLayout

      combinedLayout(
          mineStackMainMenuButtonSection,
          previousPageButtonSection,
          nextPageButtonSection
      )
    }

    // 自動スタック機能トグルボタンを含むセクション
    val autoMineStackToggleButtonSection = singleSlotLayout {
      (9 * 5 + 4) to with(MineStackButtons) { computeAutoMineStackToggleButton() }
    }

    return combinedLayout(
        categorizedItemSection,
        uiOperationSection,
        autoMineStackToggleButtonSection
    )
  }

  /**
   * カテゴリ別マインスタックメニューで [pageIndex] + 1 ページ目の[Menu]
   */
  def forCategory(category: MineStackObjectCategory, pageIndex: Int = 0): Menu = object: Menu {
    override val open: TargetedEffect[Player] = computedEffect { player =>
      val session = MenuInventoryView(
          6.rows(),
          s"$DARK_BLUE${BOLD}MineStack(${category.uiLabel})"
      ).createNewSession()

      sequentialEffect(
          session.openEffectThrough(Schedulers.sync),
          UnfocusedEffect { session.overwriteViewWith(player.computeMenuLayout(category, pageIndex)) }
      )
    }
  }
}
