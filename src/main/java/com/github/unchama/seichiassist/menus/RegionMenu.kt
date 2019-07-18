package com.github.unchama.seichiassist.menus

import arrow.core.Right
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.Menu
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.player.asCommandEffect
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

/**
 * Created by karayuu on 2019/06/23
 */
object RegionMenu: Menu {
  private object Buttons {
    val summonWandButton: Button = run {
      val usage = listOf(
          "${GREEN}①召喚された斧を手に持ちます\n",
          "${GREEN}②保護したい領域の一方の角を${YELLOW}左${GREEN}クリック\n",
          "${GREEN}③もう一方の対角線上の角を${RED}右${GREEN}クリック\n",
          "${GREEN}④メニューの${YELLOW}金の斧${GREEN}をクリック\n"
      )

      val buttonLore = run {
        val summonInfo = listOf(
            "$DARK_RED${UNDERLINE}クリックで召喚",
            "$DARK_GREEN${UNDERLINE}※インベントリを空けておこう"
        )

        val commandInfo = "${DARK_GRAY}command->[//wand]"

        summonInfo + usage + commandInfo
      }

      val leftClickEffect = sequentialEffect(
          TargetedEffect { it.closeInventory() },
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          "wand".asCommandEffect(),
          usage.asMessageEffect()
      )

      Button(
          IconItemStackBuilder(Material.WOOD_AXE)
              .title("$YELLOW$UNDERLINE${BOLD}保護設定用の木の斧を召喚")
              .lore(buttonLore)
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, leftClickEffect)
      )
    }

    suspend fun Player.computeClaimRegionButton(): Button {
      val player = this
      val openerData = SeichiAssist.playermap[uniqueId]!!
      val selection = ExternalPlugins.getWorldEdit().getSelection(player)

      val playerHasPermission = player.hasPermission("worldguard.region.claim")
      val isSelectionNull = selection == null
      val selectionHasEnoughSpace = selection.length >= 10 && selection.width >= 10

      val canMakeRegion = playerHasPermission && !isSelectionNull && selectionHasEnoughSpace

      val buttonLore = run {
        val baseLore = if (!playerHasPermission) {
          listOf(
              "${RED}このワールドでは",
              "${RED}保護を申請できません"
          )
        } else if (isSelectionNull) {
          listOf(
              "${RED}範囲指定されていません",
              "${RED}先に木の斧で2か所クリックしてネ"
          )
        } else if (selectionHasEnoughSpace) {
          listOf(
              "${RED}選択された範囲が狭すぎます",
              "${RED}一辺当たり最低10ブロック以上にしてネ"
          )
        } else {
          listOf(
              "$DARK_GREEN${UNDERLINE}範囲指定されています",
              "$DARK_GREEN${UNDERLINE}クリックすると保護を申請します"
          )
        }

        val infoLore = if (playerHasPermission) {
          listOf(
              "${GRAY}Y座標は自動で全範囲保護されます",
              "${YELLOW}A new region has been claimed",
              "${YELLOW}named '${player.name}_${openerData.rgnum}'.",
              "${GRAY}と出れば保護設定完了です",
              "${RED}赤色で別の英文が出た場合",
              "${GRAY}保護の設定に失敗しています",
              "${GRAY}・別の保護と被っていないか",
              "${GRAY}・保護数上限に達していないか",
              "${GRAY}確認してください"
          )
        } else {
          listOf()
        }

        baseLore + infoLore
      }


      val leftClickEffect = if (canMakeRegion) {
        sequentialEffect(
            "/expand vert".asCommandEffect(),
            "rg claim ${player.name}_${openerData.rgnum}".asCommandEffect(),
            deferredEffect { openerData.incrementRegionNumber },
            "/sel".asCommandEffect(),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        )
      } else {
        EmptyEffect
      }

      val baseIconBuilder = IconItemStackBuilder(Material.GOLD_AXE)
          .title("$YELLOW$UNDERLINE${BOLD}保護の申請")
          .lore(buttonLore)
      val buttonEffect = FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, leftClickEffect)

      return if (canMakeRegion) {
        Button(
            baseIconBuilder
                .enchanted()
                .build(),
            buttonEffect
        )
      } else {
        Button(
            baseIconBuilder.build(),
            buttonEffect
        )
      }
    }

    val displayOpenerRegionButton: Button = run {
      val buttonLore = listOf(
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
          "${DARK_GRAY}command->[/rg list]"
      )

      val leftClickEffect = sequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          TargetedEffect { it.closeInventory() },
          TargetedEffect { it.chat("/rg list -p ${it.name}") }
      )

      Button(
          IconItemStackBuilder(Material.STONE_AXE)
              .title("$YELLOW$UNDERLINE${BOLD}保護一覧を表示")
              .lore(buttonLore)
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, leftClickEffect)
      )
    }
  }

  private val regionGUIButton: Button = run {
    val buttonLore = listOf(
        "$DARK_RED${UNDERLINE}クリックで開く",
        "${RED}保護の管理が超簡単に！",
        "${YELLOW}自分の所有する保護内でクリックすると",
        "${YELLOW}保護の各種設定や削除が行えます",
        "${DARK_GRAY}command->[/land]"
    )

    val leftClickEffect = "land".asCommandEffect()

    Button(
        IconItemStackBuilder(Material.DIAMOND_AXE)
            .title("$YELLOW$UNDERLINE${BOLD}RegionGUI機能")
            .lore(buttonLore)
            .build(),
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, leftClickEffect)
    )
  }

  private val gridRegionMenuOpenButton: Button = run {
    val buttonLore = listOf(
        "$DARK_RED${UNDERLINE}クリックで開く",
        "${RED}グリッド式保護の作成ができます",
        "${YELLOW}グリッド式保護とは...",
        "${GRAY}保護をユニット単位で管理するシステムのこと",
        "${AQUA}15ブロック=1ユニットとして",
        "${AQUA}保護が作成されます。"
    )


    val leftClickEffect = sequentialEffect(
        FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    //TODO: ここにメニューを開く処理を加える。
    )

    Button(
        IconItemStackBuilder(Material.IRON_AXE)
            .title("$YELLOW$UNDERLINE${BOLD}グリッド式保護作成画面へ")
            .lore(buttonLore)
            .build(),
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, leftClickEffect)
    )
  }

  private suspend fun Player.computeMenuLayout(): IndexedSlotLayout = with(Buttons) {
    IndexedSlotLayout(
        0 to summonWandButton,
        1 to computeClaimRegionButton(),
        2 to displayOpenerRegionButton,
        3 to regionGUIButton,
        4 to gridRegionMenuOpenButton
    )
  }

  override val open: TargetedEffect<Player> = TargetedEffect {
    val view = MenuInventoryView(Right(InventoryType.HOPPER), "${BLACK}保護メニュー", it.computeMenuLayout())
    view.createNewSession().open
  }
}
