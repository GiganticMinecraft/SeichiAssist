package com.github.unchama.seichiassist.menus

import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.data.RegionMenuData
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

object RegionMenu : Menu {

  private object ConstantButtons {

    val summonWandButton: Button = run {
      val wandUsage = arrayOf(
          "${GREEN}①召喚された斧を手に持ちます",
          "${GREEN}②保護したい領域の一方の角を${YELLOW}左${GREEN}クリック",
          "${GREEN}③もう一方の対角線上の角を${RED}右${GREEN}クリック",
          "${GREEN}④メニューの${YELLOW}金の斧${GREEN}をクリック"
      )

      val iconItemStack = IconItemStackBuilder(Material.WOOD_AXE)
          .title("$YELLOW$UNDERLINE${BOLD}保護設定用の木の斧を召喚")
          .lore(
              *wandUsage,
              "$DARK_RED${UNDERLINE}クリックで召喚",
              "$DARK_GREEN${UNDERLINE}※インベントリを空けておこう",
              "${DARK_GRAY}command->[//wand]")
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK,
              sequentialEffect(
                  TargetedEffect { it.closeInventory() },
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                  "/wand".asCommandEffect(),
                  wandUsage.toList().asMessageEffect()
              ))
      )
    }

    val displayOpenerRegionButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.STONE_AXE)
          .title("$YELLOW$UNDERLINE${BOLD}保護一覧を表示")
          .lore(
              "$DARK_RED${UNDERLINE}クリックで表示",
              "${GRAY}今いるワールドで",
              "${GRAY}あなたが保護している",
              "${GRAY}土地の一覧を表示します",
              "$RED$UNDERLINE/rg info 保護名",
              "${GRAY}該当保護の詳細情報を表示",
              "$RED$UNDERLINE/rg rem 保護名",
              "${GRAY}該当保護を削除する",
              "$RED$UNDERLINE/rg addmem 保護名 プレイヤー名",
              "${GRAY}該当保護に指定メンバーを追加",
              "$RED$UNDERLINE/rg removemenber 保護名 プレイヤー名",
              "${GRAY}該当保護の指定メンバーを削除",
              "${DARK_GRAY}その他のコマンドはwikiを参照",
              "${DARK_GRAY}command->[/rg list]")
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK,
              sequentialEffect(
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                  TargetedEffect { it.closeInventory() },
                  TargetedEffect { it.chat("/rg list -p ${it.name}") }
              ))
      )
    }

    val openRegionGUIButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.DIAMOND_AXE)
          .title("$YELLOW$UNDERLINE${BOLD}RegionGUI機能")
          .lore(
              "$DARK_RED${UNDERLINE}クリックで開く",
              "${RED}保護の管理が超簡単に！",
              "${YELLOW}自分の所有する保護内でクリックすると",
              "${YELLOW}保護の各種設定や削除が行えます",
              "${DARK_GRAY}command->[/land]")
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, "land".asCommandEffect())
      )
    }

    val openGridRegionMenuButton: Button = run {
      val iconItemStack = IconItemStackBuilder(Material.IRON_AXE)
          .title("$YELLOW$UNDERLINE${BOLD}グリッド式保護作成画面へ")
          .lore(
              "$DARK_RED${UNDERLINE}クリックで開く",
              "${RED}グリッド式保護の作成ができます",
              "${YELLOW}グリッド式保護とは...",
              "${GRAY}保護をユニット単位で管理するシステムのこと",
              "${AQUA}15ブロック=1ユニットとして",
              "${AQUA}保護が作成されます。")
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK,
              sequentialEffect(
                  FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1f, 1f),
                  TargetedEffect { it.openInventory(RegionMenuData.getGridWorldGuardMenu(it)) }
              )
          )
      )
    }

  }

  private object ButtonComputations {

    suspend def Player.computeButtonToClaimRegion(): Button = run {
      val openerData = SeichiAssist.playermap[uniqueId]!!
      val selection = ExternalPlugins.getWorldEdit().getSelection(player)

      val playerHasPermission = player.hasPermission("worldguard.region.claim")
      val isSelectionNull = selection == null
      val selectionHasEnoughSpace =
          if (!isSelectionNull)
            selection.length >= 10 && selection.width >= 10
          else false

      val canMakeRegion = playerHasPermission && !isSelectionNull && selectionHasEnoughSpace

      val iconItemStack = IconItemStackBuilder(Material.GOLD_AXE)
          .run { if (canMakeRegion) enchanted() else this }
          .title("$YELLOW$UNDERLINE${BOLD}保護の申請")
          .lore(
              *when {
                !playerHasPermission -> arrayOf(
                    "${RED}このワールドでは",
                    "${RED}保護を申請できません")
                isSelectionNull -> arrayOf(
                    "${RED}範囲指定されていません",
                    "${RED}先に木の斧で2か所クリックしてネ")
                !selectionHasEnoughSpace -> arrayOf(
                    "${RED}選択された範囲が狭すぎます",
                    "${RED}一辺当たり最低10ブロック以上にしてネ")
                else -> arrayOf(
                    "$DARK_GREEN${UNDERLINE}範囲指定されています",
                    "$DARK_GREEN${UNDERLINE}クリックすると保護を申請します")
              },
              *if (playerHasPermission) arrayOf(
                  "${GRAY}Y座標は自動で全範囲保護されます",
                  "${YELLOW}A new region has been claimed",
                  "${YELLOW}named '${name}_${openerData.regionCount}'.",
                  "${GRAY}と出れば保護設定完了です",
                  "${RED}赤色で別の英文が出た場合",
                  "${GRAY}保護の設定に失敗しています",
                  "${GRAY}・別の保護と被っていないか",
                  "${GRAY}・保護数上限に達していないか",
                  "${GRAY}確認してください"
              ) else emptyArray())
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK,
              when {
                !playerHasPermission -> "${RED}このワールドでは保護を申請できません".asMessageEffect()
                isSelectionNull -> sequentialEffect(
                    "${RED}先に木の斧で範囲を指定してからこのボタンを押してください".asMessageEffect(),
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                )
                !selectionHasEnoughSpace -> sequentialEffect(
                    "${RED}指定された範囲が狭すぎます。1辺当たり最低10ブロック以上にしてください".asMessageEffect(),
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
                )
                else -> sequentialEffect(
                    "/expand vert".asCommandEffect(),
                    "rg claim ${player.name}_${openerData.regionCount}".asCommandEffect(),
                    deferredEffect { openerData.incrementRegionNumber },
                    "/sel".asCommandEffect(),
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
                )
              }
          )
      )
    }

  }

  private suspend def Player.computeMenuLayout(): IndexedSlotLayout =
      with(ConstantButtons) {
        with(ButtonComputations) {
          IndexedSlotLayout(
              0 to summonWandButton,
              1 to computeButtonToClaimRegion(),
              2 to displayOpenerRegionButton,
              3 to openRegionGUIButton,
              4 to openGridRegionMenuButton
          )
        }
      }

  override val open: TargetedEffect<Player> = computedEffect { player ->
    val session = MenuInventoryView(Right(InventoryType.HOPPER), "${BLACK}保護メニュー").createNewSession()

    sequentialEffect(
        session.openEffectThrough(Schedulers.sync),
        UnfocusedEffect { session.overwriteViewWith(player.computeMenuLayout()) }
    )
  }

}