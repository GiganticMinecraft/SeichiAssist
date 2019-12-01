package com.github.unchama.seichiassist.menus

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.RegionMenuData
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.{Material, Sound}

object RegionMenu extends Menu {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sync
  import com.github.unchama.targetedeffect._
  import com.github.unchama.targetedeffect.player.PlayerEffects.{closeInventoryEffect, _}
  import com.github.unchama.targetedeffect.syntax._

  override val frame: MenuFrame =
    MenuFrame(Right(InventoryType.HOPPER), s"${BLACK}保護メニュー")

  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    import ConstantButtons._
    val computations = ButtonComputations(player)
    import computations._

    for {
      buttonToClaimRegion <- computeButtonToClaimRegion
    } yield {
      menuinventory.MenuSlotLayout(
        0 -> summonWandButton,
        1 -> buttonToClaimRegion,
        2 -> displayOpenerRegionButton,
        3 -> openRegionGUIButton,
        4 -> openGridRegionMenuButton
      )
    }
  }

  private case class ButtonComputations(player: Player) {

    import player._

    val computeButtonToClaimRegion: IO[Button] = IO {
      val openerData = SeichiAssist.playermap(player.getUniqueId)
      val selection = ExternalPlugins.getWorldEdit.getSelection(player)

      val playerHasPermission = player.hasPermission("worldguard.region.claim")
      val isSelectionNull = selection == null
      val selectionHasEnoughSpace =
        if (!isSelectionNull)
          selection.getLength >= 10 && selection.getWidth >= 10
        else false

      val canMakeRegion = playerHasPermission && !isSelectionNull && selectionHasEnoughSpace

      val iconItemStack = {
        import com.github.unchama.util.syntax._

        val lore = {
          if (!playerHasPermission)
            Seq(
              s"${RED}このワールドでは",
              s"${RED}保護を申請できません"
            )
          else if (isSelectionNull)
            Seq(
              s"${RED}範囲指定されていません",
              s"${RED}先に木の斧で2か所クリックしてネ"
            )
          else if (!selectionHasEnoughSpace)
            Seq(
              s"${RED}選択された範囲が狭すぎます",
              s"${RED}一辺当たり最低10ブロック以上にしてネ"
            )
          else Seq(
            s"$DARK_GREEN${UNDERLINE}範囲指定されています",
            s"$DARK_GREEN${UNDERLINE}クリックすると保護を申請します"
          )
        } ++ {
          if (playerHasPermission)
            Seq(
              s"${GRAY}Y座標は自動で全範囲保護されます",
              s"${YELLOW}A new region has been claimed",
              s"${YELLOW}named '${getName}_${openerData.regionCount}'.",
              s"${GRAY}と出れば保護設定完了です",
              s"${RED}赤色で別の英文が出た場合",
              s"${GRAY}保護の設定に失敗しています",
              s"$GRAY・別の保護と被っていないか",
              s"$GRAY・保護数上限に達していないか",
              s"${GRAY}確認してください"
            ) else Seq()
        }

        new IconItemStackBuilder(Material.GOLD_AXE)
          .modify { b => if (canMakeRegion) b.enchanted() }
          .title(s"$YELLOW$UNDERLINE${BOLD}保護の申請")
          .lore(lore.toList)
          .build()
      }

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ =>
          if (!playerHasPermission)
            s"${RED}このワールドでは保護を申請できません".asMessageEffect()
          else if (isSelectionNull)
            sequentialEffect(
              s"${RED}先に木の斧で範囲を指定してからこのボタンを押してください".asMessageEffect(),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
            )
          else if (!selectionHasEnoughSpace)
            sequentialEffect(
              s"${RED}指定された範囲が狭すぎます。1辺当たり最低10ブロック以上にしてください".asMessageEffect(),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
            )
          else
            sequentialEffect(
              "/expand vert".asCommandEffect(),
              s"rg claim ${player.getName}_${openerData.regionCount}".asCommandEffect(),
              openerData.incrementRegionNumber,
              "/sel".asCommandEffect(),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
            )
        )
      )
    }
  }

  private object ConstantButtons {

    val summonWandButton: Button = {
      val wandUsage = Array(
        s"$GREEN①召喚された斧を手に持ちます",
        s"$GREEN②保護したい領域の一方の角を${YELLOW}左${GREEN}クリック",
        s"$GREEN③もう一方の対角線上の角を${RED}右${GREEN}クリック",
        s"$GREEN④メニューの${YELLOW}金の斧${GREEN}をクリック"
      )

      val iconItemStack = new IconItemStackBuilder(Material.WOOD_AXE)
        .title(s"$YELLOW$UNDERLINE${BOLD}保護設定用の木の斧を召喚")
        .lore(
          (wandUsage ++ Array(
            s"$DARK_RED${UNDERLINE}クリックで召喚",
            s"$DARK_GREEN$UNDERLINE※インベントリを空けておこう",
            s"${DARK_GRAY}command=>[//wand]")).toList
        ).build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ =>
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "/wand".asCommandEffect(),
            wandUsage.toList.asMessageEffect()
          )
        )
      )
    }

    val displayOpenerRegionButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.STONE_AXE)
        .title(s"$YELLOW$UNDERLINE${BOLD}保護一覧を表示")
        .lore(
          s"$DARK_RED${UNDERLINE}クリックで表示",
          s"${GRAY}今いるワールドで",
          s"${GRAY}あなたが保護している",
          s"${GRAY}土地の一覧を表示します",
          s"$RED$UNDERLINE/rg info 保護名",
          s"${GRAY}該当保護の詳細情報を表示",
          s"$RED$UNDERLINE/rg rem 保護名",
          s"${GRAY}該当保護を削除する",
          s"$RED$UNDERLINE/rg addmem 保護名 プレイヤー名",
          s"${GRAY}該当保護に指定メンバーを追加",
          s"$RED$UNDERLINE/rg removemenber 保護名 プレイヤー名",
          s"${GRAY}該当保護の指定メンバーを削除",
          s"${DARK_GRAY}その他のコマンドはwikiを参照",
          s"${DARK_GRAY}command=>[/rg list]")
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ =>
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            closeInventoryEffect,
            computedEffect(player => s"rg list -p ${player.getName}".asCommandEffect())
          )
        )
      )
    }

    val openRegionGUIButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.DIAMOND_AXE)
        .title(s"$YELLOW$UNDERLINE${BOLD}RegionGUI機能")
        .lore(
          s"$DARK_RED${UNDERLINE}クリックで開く",
          s"${RED}保護の管理が超簡単に！",
          s"${YELLOW}自分の所有する保護内でクリックすると",
          s"${YELLOW}保護の各種設定や削除が行えます",
          s"${DARK_GRAY}command=>[/land]")
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ => "land".asCommandEffect())
      )
    }

    val openGridRegionMenuButton: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.IRON_AXE)
        .title(s"$YELLOW$UNDERLINE${BOLD}グリッド式保護作成画面へ")
        .lore(
          s"$DARK_RED${UNDERLINE}クリックで開く",
          s"${RED}グリッド式保護の作成ができます",
          s"${YELLOW}グリッド式保護とは...",
          s"${GRAY}保護をユニット単位で管理するシステムのこと",
          s"${AQUA}15ブロック=1ユニットとして",
          s"${AQUA}保護が作成されます。")
        .build()

      Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ =>
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1f, 1f),
            // TODO メニューに置き換える
            computedEffect(p => openInventoryEffect(RegionMenuData.getGridWorldGuardMenu(p)))
          )
        )
      )
    }

  }

}