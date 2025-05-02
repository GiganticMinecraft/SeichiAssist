package com.github.unchama.seichiassist.listener

import cats.effect.{IO, SyncIO}
import com.github.unchama.generic.effect.concurrent.TryableFiber
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.stickmenu.{FirstPage, StickMenu}
import com.github.unchama.seichiassist.seichiskill.ActiveSkill
import com.github.unchama.seichiassist.seichiskill.ActiveSkillRange.RemoteArea
import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.Disabled
import com.github.unchama.seichiassist.seichiskill.assault.AssaultRoutine
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.util._
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.external.ExternalPlugins
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.{GameMode, Material, Sound}

class PlayerClickListener(
  implicit effectEnvironment: EffectEnvironment,
  manaApi: ManaApi[IO, SyncIO, Player],
  ioCanOpenStickMenu: IO CanOpen FirstPage.type,
  ioOnMainThread: OnMinecraftServerThread[IO]
) extends Listener {

  import ManagedWorld._
  import com.github.unchama.generic.ContextCoercion._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{asyncShift, timer}
  import com.github.unchama.targetedeffect._

  private val plugin = SeichiAssist.instance

  import plugin.activeSkillAvailability

  private val playerMap = SeichiAssist.playermap

  // アクティブスキル処理
  @EventHandler
  def onPlayerActiveSkillEvent(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val action = event.getAction

    val equipmentSlot = event.getHand

    if (equipmentSlot == null || equipmentSlot == EquipmentSlot.OFF_HAND) return
    if (player.isSneaking || player.getGameMode != GameMode.SURVIVAL) return

    val playerData = playerMap(player.getUniqueId)
    val skillState = playerData.skillState.get.unsafeRunSync()

    if (skillState.usageMode == Disabled) return
    if (!player.getWorld.isSeichiSkillAllowed) return

    action match {
      case Action.LEFT_CLICK_BLOCK | Action.LEFT_CLICK_AIR =>
        if (skillState.assaultSkill.isEmpty) return
      case Action.RIGHT_CLICK_BLOCK | Action.RIGHT_CLICK_AIR =>
        if (skillState.assaultSkill.nonEmpty) return
      // クリック以外のInteractEventを無視する
      case Action.PHYSICAL => return
    }

    // クールダウンタイム中は処理を終了
    if (!activeSkillAvailability(player).get.unsafeRunSync()) {
      // SEを再生
      player.playSound(player.getLocation, Sound.BLOCK_DISPENSER_FAIL, 0.5f, 1f)
      return
    }

    if (MaterialSets.breakToolMaterials.contains(event.getMaterial)) {
      skillState.activeSkill match {
        case Some(ActiveSkill(_, _, RemoteArea(_), coolDownOption, _, _)) =>
          import cats.implicits._
          import com.github.unchama.concurrent.syntax._

          // クールダウン処理
          val coolDownTicks = coolDownOption.getOrElse(0)
          val soundEffectAfterCoolDown =
            if (coolDownTicks > 5)
              FocusedSoundEffect(Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 0.1f)(player)
            else
              IO.unit

          val controlSkillAvailability =
            activeSkillAvailability(player).set(false).coerceTo[IO] >>
              IO.sleep(coolDownTicks.ticks) >>
              activeSkillAvailability(player).set(true).coerceTo[IO] >>
              soundEffectAfterCoolDown

          val arrowEffect = playerData.skillEffectState.selection.arrowEffect

          effectEnvironment.unsafeRunEffectAsync("スキルのクールダウンの状態を戻す", controlSkillAvailability)
          effectEnvironment.unsafeRunEffectAsync(
            "ArrowEffectを非同期で実行する",
            arrowEffect.run(player)
          )
        case _ =>
      }
    }
  }

  // スキル切り替えのイベント
  @EventHandler
  def onPlayerActiveSkillToggleEvent(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val action = event.getAction

    val equipmentSlot = event.getHand

    val currentItem = player.getInventory.getItemInMainHand.getType
    if (currentItem == Material.STICK || currentItem == Material.PLAYER_HEAD) return

    val playerData = playerMap(player.getUniqueId)
    val playerLevel = SeichiAssist
      .instance
      .breakCountSystem
      .api
      .seichiAmountDataRepository(player)
      .read
      .unsafeRunSync()
      .levelCorrespondingToExp
      .level

    if (playerLevel < SeichiAssist.seichiAssistConfig.getDualBreaklevel) return
    if (!player.getWorld.isSeichiSkillAllowed) return
    if (!activeSkillAvailability(player).get.unsafeRunSync()) return

    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
      val hasToolInMainHand = MaterialSets.breakToolMaterials.contains(currentItem)

      if (
        action == Action.RIGHT_CLICK_BLOCK &&
        MaterialSets.cancelledMaterials.contains(event.getClickedBlock.getType)
      ) return

      val skillState = playerData.skillState.get.unsafeRunSync()

      if (equipmentSlot == EquipmentSlot.HAND && hasToolInMainHand) {
        // メインハンドで指定ツールを持っていた時の処理
        // スニークしていないかつアサルトタイプが選択されていない時処理を終了
        if (!player.isSneaking && skillState.assaultSkill.isEmpty) return

        // 設置をキャンセル
        event.setCancelled(true)

        skillState.activeSkill match {
          case Some(skill) =>
            val toggledMode = skillState.usageMode.nextMode(skill)

            playerData.skillState.set(skillState.copy(usageMode = toggledMode)).unsafeRunSync()
            player.sendMessage(s"$GOLD${skill.name}：${toggledMode.modeString(skill)}")
            player.playSound(player.getLocation, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
          case None =>
        }
      } else if (equipmentSlot == EquipmentSlot.OFF_HAND) {
        skillState.assaultSkill match {
          case Some(skill) =>
            // オフハンドで指定ツールを持っていた時の処理

            event.setCancelled(true)

            import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sleepAndRoutineContext

            SeichiAssist
              .instance
              .assaultSkillRoutines(player)
              .flipState(TryableFiber.start(AssaultRoutine.tryStart(player, skill)))
              .as(())
              .unsafeRunSync()

            player.playSound(player.getLocation, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
          case None =>
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  def onPlayerOpenMenuWithSomething(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val action = event.getAction
    val inventory = player.getInventory

    if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return

    if (
      inventory
        .getItemInMainHand
        .getType == Material.STICK && inventory.getItemInOffHand != null
    )
      event.setCancelled(true)
  }

  // 棒メニューを開くイベント
  @EventHandler
  def onPlayerMenuEvent(event: PlayerInteractEvent): Unit = {
    // プレイヤーを取得
    val player = event.getPlayer
    // プレイヤーが起こしたアクションを取得
    val action = event.getAction

    if (player.getInventory.getItemInMainHand.getType != Material.STICK) return

    // 右クリックの処理ではない
    if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return
    if (event.getHand == EquipmentSlot.OFF_HAND) return
    event.setCancelled(true)

    effectEnvironment.unsafeRunAsyncTargetedEffect(player)(
      SequentialEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        ioCanOpenStickMenu.open(StickMenu.firstPage)
      ),
      "棒メニューの1ページ目を開く"
    )
  }

  // 頭の即時回収
  @EventHandler
  def onPlayerRightClickMineHeadEvent(e: PlayerInteractEvent): Unit = {

    val p = e.getPlayer
    val useItem = p.getInventory.getItemInMainHand
    // 専用アイテムを持っていない場合無視
    if (!ItemInformation.isMineHeadItem(useItem)) {
      return
    }

    val action = e.getAction
    // ブロックの左クリックじゃない場合無視
    if (action != Action.LEFT_CLICK_BLOCK) {
      return
    }

    val targetBlock = e.getClickedBlock
    // 頭じゃない場合無視
    if (targetBlock.getType != Material.PLAYER_HEAD) {
      return
    }

    // 壊せない場合無視
    if (!BreakUtil.canBreak(p, targetBlock)) {
      return
    }

    // インベントリに空がない場合無視
    if (InventoryOperations.isPlayerInventoryFull(p)) {
      p.sendMessage(RED.toString + "インベントリがいっぱいです")
      return
    }

    // 頭を付与
    ItemInformation.getSkullDataFromBlock(targetBlock) match {
      case Some(itemStack) => p.getInventory.addItem(itemStack)
      case None            =>
    }
    if (!ExternalPlugins.getCoreProtectWrapper.queueBlockRemoval(p, targetBlock)) {
      SeichiAssist
        .instance
        .getLogger
        .warning(
          s"Logging in skull break: Failed Location: ${targetBlock.getLocation}, Player:$p"
        )
    }
    // ブロックを空気で置き換える
    targetBlock.setType(Material.AIR)
    // 音を鳴らしておく
    p.playSound(p.getLocation, Sound.ENTITY_ITEM_PICKUP, 2.0f, 1.0f)
  }
}
