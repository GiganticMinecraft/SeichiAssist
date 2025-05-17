package com.github.unchama.seichiassist.menus

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.{
  ClickEventFilter,
  FilteredButtonEffect
}
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.menus.gridregion.GridRegionMenu
import com.github.unchama.seichiassist.subsystems.gridregion.GridRegionAPI
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.{CommandEffect, FocusedSoundEffect}
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.{Location, Material, Sound}

object RegionMenu extends Menu {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
  import com.github.unchama.targetedeffect._
  import com.github.unchama.targetedeffect.player.PlayerEffects._

  class Environment(
    implicit val ioCanOpenGridRegionMenu: IO CanOpen GridRegionMenu.type,
    implicit val gridRegionAPI: GridRegionAPI[IO, Player, Location]
  )

  override val frame: MenuFrame = MenuFrame(Right(InventoryType.HOPPER), s"${BLACK}保護メニュー")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    val constantButtons = ConstantButtons(environment)
    val computations = ButtonComputations(environment)(player)
    import constantButtons._
    import computations._

    for {
      buttonToClaimRegion <- computeButtonToClaimRegion
    } yield {
      menuinventory
        .MenuSlotLayout(
          0 -> summonWandButton,
          1 -> buttonToClaimRegion,
          2 -> displayOpenerRegionButton,
          4 -> openGridRegionMenuButton
        )
        .merge(
          // ヴァルハラサーバー(`serverNum = 3`) では RegionGUI が利用できない (2022/08/06現在) ので表示しない
          if (SeichiAssist.seichiAssistConfig.getServerNum != 3)
            MenuSlotLayout(3 -> openRegionGUIButton)
          else MenuSlotLayout.emptyLayout
        )
    }
  }

  private case class ButtonComputations(environment: Environment)(player: Player) {

    import player._
    import environment._

    val computeButtonToClaimRegion: IO[Button] = for {
      regionCount <- gridRegionAPI.regionCount(player)
    } yield {
      val session = WorldEdit.getInstance().getSessionManager.get(BukkitAdapter.adapt(player))
      val world = BukkitAdapter.adapt(player.getWorld)
      val isSelected = session.isSelectionDefined(world)
      val playerHasPermission = player.hasPermission("worldguard.region.claim")

      val selectionOpt = Option.when(isSelected)(session.getSelection(world))
      val selectionHasEnoughSpace =
        selectionOpt.exists(selection => selection.getLength >= 10 && selection.getWidth >= 10)

      val canMakeRegion = playerHasPermission && !isSelected && selectionHasEnoughSpace

      val iconItemStack = {
        val lore = {
          if (!playerHasPermission) {
            Seq(s"${RED}このワールドでは", s"${RED}保護を作成できません")
          } else if (!isSelected) {
            Seq(s"${RED}範囲指定されていません", s"${RED}先に木の斧で2か所クリックしてネ")
          } else if (!selectionHasEnoughSpace) {
            Seq(s"${RED}選択された範囲が狭すぎます", s"${RED}一辺当たり最低10ブロック以上にしてネ")
          } else {
            Seq(s"$DARK_GREEN${UNDERLINE}範囲指定されています", s"$DARK_GREEN${UNDERLINE}クリックすると保護を作成します")
          }
        } ++ {
          if (playerHasPermission)
            Seq(
              s"${GRAY}Y座標は自動で全範囲保護されます",
              s"${YELLOW}A new region has been claimed",
              s"${YELLOW}named '${getName}_${regionCount.value}'.",
              s"${GRAY}と出れば保護設定完了です",
              s"${RED}赤色で別の英文が出た場合",
              s"${GRAY}保護の設定に失敗しています",
              s"$GRAY・別の保護と被っていないか",
              s"$GRAY・保護数上限に達していないか",
              s"${GRAY}確認してください"
            )
          else Seq()
        }

        import scala.util.chaining._
        new IconItemStackBuilder(Material.GOLDEN_AXE)
          .tap { b => if (canMakeRegion) b.enchanted() }
          .title(s"$YELLOW$UNDERLINE${BOLD}保護の作成")
          .lore(lore.toList)
          .build()
      }

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ =>
          if (!playerHasPermission)
            MessageEffect(s"${RED}このワールドでは保護を作成できません")
          else if (!isSelected)
            SequentialEffect(
              MessageEffect(s"${RED}先に木の斧で範囲を指定してからこのボタンを押してください"),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
            )
          else if (!selectionHasEnoughSpace)
            SequentialEffect(
              MessageEffect(s"${RED}指定された範囲が狭すぎます。1辺当たり最低10ブロック以上にしてください"),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5f)
            )
          else
            SequentialEffect(
              CommandEffect("/expand vert"),
              CommandEffect(s"rg claim ${player.getName}_${regionCount.value}"),
              gridRegionAPI.createAndClaimRegionSelectedOnWorldGuard,
              CommandEffect("/sel"),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
            )
        )
      )
    }
  }

  private case class ConstantButtons(environment: Environment) {

    val summonWandButton: Button = {
      val wandUsage = List(
        s"$GREEN①召喚された斧を手に持ちます",
        s"$GREEN②保護したい領域の一方の角を${YELLOW}左${GREEN}クリック",
        s"$GREEN③もう一方の対角線上の角を${RED}右${GREEN}クリック",
        s"$GREEN④メニューの${YELLOW}金の斧${GREEN}をクリック"
      )

      val iconItemStack = new IconItemStackBuilder(Material.WOODEN_AXE)
        .title(s"$YELLOW$UNDERLINE${BOLD}保護設定用の木の斧を召喚")
        .lore(
          wandUsage ++ List(
            s"$DARK_RED${UNDERLINE}クリックで召喚",
            s"$DARK_GREEN$UNDERLINE※インベントリを空けておこう",
            s"${DARK_GRAY}command=>[//wand]"
          )
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ =>
          SequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            CommandEffect("/wand"),
            MessageEffect(wandUsage)
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
          s"$RED$UNDERLINE/rg removemember 保護名 プレイヤー名",
          s"${GRAY}該当保護の指定メンバーを削除",
          s"${DARK_GRAY}その他のコマンドはwikiを参照",
          s"${DARK_GRAY}command=>[/rg list]"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ =>
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            closeInventoryEffect,
            ComputedEffect(player => CommandEffect(s"rg list -p ${player.getName}"))
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
          s"${DARK_GRAY}command=>[/land]"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ => CommandEffect("land"))
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
          s"${AQUA}保護が作成されます。"
        )
        .build()

      Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(_ =>
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1f, 1f),
            environment.ioCanOpenGridRegionMenu.open(GridRegionMenu)
          )
        )
      )
    }

  }

}
