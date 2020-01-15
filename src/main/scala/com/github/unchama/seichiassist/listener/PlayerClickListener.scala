package com.github.unchama.seichiassist.listener

import cats.effect.IO
import com.github.unchama.seichiassist
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.activeskill.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.activeskill.effect.{ActiveSkillNormalEffect, ActiveSkillPremiumEffect}
import com.github.unchama.seichiassist.data.GachaPrize
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import net.md_5.bungee.api.chat.{HoverEvent, TextComponent}
import org.bukkit.ChatColor._
import org.bukkit.entity.ThrownExpBottle
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.{EquipmentSlot, ItemStack}
import org.bukkit.{GameMode, Material, Sound}

class PlayerClickListener extends Listener {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sync
  import com.github.unchama.seichiassist.util.ops.ItemStackOps._
  import com.github.unchama.targetedeffect._
  import com.github.unchama.util.syntax._

  import scala.jdk.CollectionConverters._

  private val plugin = SeichiAssist.instance
  private val playerMap = SeichiAssist.playermap
  private val gachaDataList = SeichiAssist.gachadatalist

  //アクティブスキル処理
  @EventHandler
  def onPlayerActiveSkillEvent(event: PlayerInteractEvent): Unit = {
    //プレイヤー型を取得
    val player = event.getPlayer
    //プレイヤーが起こしたアクションを取得
    val action = event.getAction
    //使った手を取得
    val equipmentslot = event.getHand
    //UUIDを取得
    val uuid = player.getUniqueId
    //プレイヤーデータを取得
    val playerdata = playerMap.getOrElse(uuid, return)

    if (equipmentslot == null) return

    //オフハンドから実行された時処理を終了
    if (equipmentslot == EquipmentSlot.OFF_HAND) return

    if (player.isSneaking) return

    //サバイバルでない時　または　フライ中の時終了
    if (player.getGameMode != GameMode.SURVIVAL || player.isFlying) return

    //アクティブスキルフラグがオフの時処理を終了
    if (playerdata.activeskilldata.mineflagnum == 0 || playerdata.activeskilldata.skillnum == 0) return

    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) return

    action match {
      case Action.LEFT_CLICK_BLOCK | Action.LEFT_CLICK_AIR =>
        //アサルトアーマーをどっちも使用していない時終了
        if (playerdata.activeskilldata.assaulttype == 0) return
      case Action.RIGHT_CLICK_BLOCK | Action.RIGHT_CLICK_AIR =>
        //アサルトアーマー使用中の時は終了左クリックで判定
        if (playerdata.activeskilldata.assaulttype != 0) return
      case Action.PHYSICAL =>
        // クリック以外のInteractEventを無視する
        return
    }

    //クールダウンタイム中は処理を終了
    if (!playerdata.activeskilldata.skillcanbreakflag) {
      //SEを再生
      player.playSound(player.getLocation, Sound.BLOCK_DISPENSER_FAIL, 0.5.toFloat, 1f)
      return
    }

    if (MaterialSets.breakMaterials.contains(event.getMaterial)) {
      if (playerdata.activeskilldata.skilltype == ActiveSkill.ARROW.gettypenum()) {
        //クールダウン処理
        val cooldown = ActiveSkill.ARROW.getCoolDown(playerdata.activeskilldata.skillnum)
        if (cooldown > 5) {
          new CoolDownTask(player, false, true, false).runTaskLater(plugin, cooldown)
        } else {
          new CoolDownTask(player, false, false, false).runTaskLater(plugin, cooldown)
        }

        val projectArrowEffect =
          if (playerdata.activeskilldata.effectnum == 0) {
            // エフェクトが指定されていないとき
            ArrowEffects.normalArrowEffect
          } else if (playerdata.activeskilldata.effectnum <= 100) {
            // 通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
            val skilleffect = ActiveSkillNormalEffect.values
            skilleffect(playerdata.activeskilldata.effectnum - 1).arrowEffect(player)
          } else {
            // スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
            val premiumeffect = ActiveSkillPremiumEffect.values
            premiumeffect(playerdata.activeskilldata.effectnum - 1 - 100).arrowEffect(player)
          }

        seichiassist.unsafe.runAsyncTargetedEffect(player)(projectArrowEffect, "ArrowEffectを非同期で実行する")
      }
    }
  }


  //プレイヤーが右クリックした時に実行(ガチャを引く部分の処理)
  @EventHandler
  def onPlayerRightClickGachaEvent(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val uuid = player.getUniqueId
    val playerData = playerMap.getOrElse(uuid, return)

    //もしサバイバルでなければ処理を終了
    if (player.getGameMode != GameMode.SURVIVAL) return

    val clickedItemStack = event.getItem.ifNull {
      return
    }

    //ガチャ用の頭でなければ終了
    if (!Util.isGachaTicket(clickedItemStack)) return

    event.setCancelled(true)

    //連打防止クールダウン処理
    if (!playerData.gachacooldownflag) return

    //連打による負荷防止の為クールダウン処理
    new CoolDownTask(player, false, false, true).runTaskLater(plugin, 4)

    //オフハンドから実行された時処理を終了
    if (event.getHand == EquipmentSlot.OFF_HAND) return

    //ガチャシステムメンテナンス中は処理を終了
    if (SeichiAssist.gachamente) {
      player.sendMessage("現在ガチャシステムはメンテナンス中です。\nしばらく経ってからもう一度お試しください")
      return
    }

    //ガチャデータが設定されていない場合
    if (gachaDataList.isEmpty) {
      player.sendMessage("ガチャが設定されていません")
      return
    }

    val action = event.getAction
    if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return

    val count =
      if (player.isSneaking) {
        val amount = clickedItemStack.getAmount
        player.sendMessage(s"$AQUA${amount}回ガチャを回しました。")
        amount
      }
      else 1

    if (!Util.removeItemfromPlayerInventory(player.getInventory, clickedItemStack, count)) {
      player.sendMessage(RED.toString + "ガチャ券の数が不正です。")
      return
    }

    (1 to count).foreach { _ =>
      //プレゼント用ガチャデータ作成
      val present = GachaPrize.runGacha()

      val probabilityOfItem = present.probability
      val givenItem = present.itemStack

      //ガチャ実行
      if (probabilityOfItem < 0.1) {
        givenItem.appendOwnerInformation(player)
      }

      //メッセージ設定
      val additionalMessage =
        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, givenItem)
          ""
        } else {
          Util.dropItem(player, givenItem)
          s"${AQUA}プレゼントがドロップしました。"
        }

      //確率に応じてメッセージを送信
      if (probabilityOfItem < 0.001) {
        Util.sendEverySoundWithoutIgnore(Sound.ENTITY_ENDERDRAGON_DEATH, 0.5.toFloat, 2f)

        {
          playerData.settings.getBroadcastMutingSettings
            .flatMap(settings =>
              IO {
                if (!settings.shouldMuteMessages) {
                  player.playSound(player.getLocation, Sound.ENTITY_ENDERDRAGON_DEATH, 0.5.toFloat, 2f)
                }
              }
            )
          }.unsafeRunSync()

        val loreWithoutOwnerName = givenItem.getItemMeta.getLore.asScala.toList
          .filterNot {
            _ == s"§r§2所有者：${player.getName}"
          }

        val localizedEnchantmentList = givenItem.getItemMeta.getEnchants.asScala.toSeq
          .map { case (enchantment, level) =>
            s"$GRAY${Util.getEnchantName(enchantment.getName, level)}"
          }

        val message =
          new TextComponent().modify { c =>
            import c._
            setText(s"$AQUA${givenItem.getItemMeta.getDisplayName}${GOLD}を引きました！おめでとうございます！")
            setHoverEvent {
              new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Array(
                  new TextComponent(
                    s" ${givenItem.getItemMeta.getDisplayName}\n" +
                      Util.getDescFormat(localizedEnchantmentList.toList) +
                      Util.getDescFormat(loreWithoutOwnerName)
                  )
                )
              )
            }
          }

        player.sendMessage(s"${RED}おめでとう！！！！！Gigantic☆大当たり！$additionalMessage")
        Util.sendEveryMessageWithoutIgnore(s"$GOLD${player.getDisplayName}がガチャでGigantic☆大当たり！")
        Util.sendEveryMessageWithoutIgnore(message)
      } else if (probabilityOfItem < 0.01) {
        player.playSound(player.getLocation, Sound.ENTITY_WITHER_SPAWN, 0.8.toFloat, 1f)
        player.sendMessage(s"${GOLD}おめでとう！！大当たり！$additionalMessage")
      } else if (probabilityOfItem < 0.1) {
        player.sendMessage(s"${YELLOW}おめでとう！当たり！$additionalMessage")
      } else {
        if (count == 1) {
          player.sendMessage(s"${WHITE}はずれ！また遊んでね！$additionalMessage")
        }
      }
    }
    player.playSound(player.getLocation, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.1.toFloat)
  }

  //スキル切り替えのイベント
  @EventHandler
  def onPlayerActiveSkillToggleEvent(event: PlayerInteractEvent): Unit = {
    //プレイヤーを取得
    val player = event.getPlayer
    //プレイヤーの起こしたアクションの取得
    val action = event.getAction
    //アクションを起こした手を取得
    val equipmentslot = event.getHand
    val currentItem = player.getInventory.getItemInMainHand.getType
    if (currentItem == Material.STICK || currentItem == Material.SKULL_ITEM) {
      return
    }
    //UUIDを取得
    val uuid = player.getUniqueId
    //playerdataを取得
    val playerdata = playerMap(uuid).ifNull {
      return
    }
    //playerdataがない場合はreturn


    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) {
      return
    }

    //アクティブスキルを発動できるレベルに達していない場合処理終了
    if (playerdata.level < SeichiAssist.seichiAssistConfig.getDualBreaklevel) {
      return
    }

    //クールダウンタイム中は処理を終了
    if (!playerdata.activeskilldata.skillcanbreakflag) {
      //SEを再生
      //player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, (float)0.5, 1);
      return
    }

    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {

      val mainhandflag = MaterialSets.breakMaterials.contains(currentItem)
      val offhandflag = MaterialSets.breakMaterials.contains(player.getInventory.getItemInOffHand.getType)

      var activemineflagnum = playerdata.activeskilldata.mineflagnum
      //どちらにも対応したアイテムを持っていない場合終了
      if (!mainhandflag && !offhandflag) {
        return
      }
      //アクション実行されたブロックがある場合の処理
      if (action == Action.RIGHT_CLICK_BLOCK) {
        //クリックされたブロックの種類を取得
        val cmaterial = event.getClickedBlock.getType
        //cancelledmateriallistに存在すれば処理終了
        if (MaterialSets.cancelledMaterials.contains(cmaterial)) {
          return
        }
      }

      if (mainhandflag && equipmentslot == EquipmentSlot.HAND) {
        //メインハンドで指定ツールを持っていた時の処理
        //スニークしていないかつアサルトタイプが選択されていない時処理を終了
        if (!player.isSneaking && playerdata.activeskilldata.assaulttype == 0) {
          return
        }

        //設置をキャンセル
        event.setCancelled(true)
        val skillTypeId = playerdata.activeskilldata.skilltype
        val skillNumber = playerdata.activeskilldata.skillnum
        if (skillTypeId == ActiveSkill.BREAK.gettypenum() && skillNumber == 1 || skillTypeId == ActiveSkill.BREAK.gettypenum() && skillNumber == 2) {

          activemineflagnum = (activemineflagnum + 1) % 3
          val status = activemineflagnum match {
            case 0 => "：OFF"
            case 1 => ":ON-Above(上向き）"
            case 2 => ":ON-Under(下向き）"
            case _ => throw new RuntimeException("This branch should not be reached")
          }
          player.sendMessage(GOLD.toString + ActiveSkill.getActiveSkillName(skillTypeId, skillNumber) + status)
          playerdata.activeskilldata.updateSkill(skillTypeId, skillNumber, activemineflagnum)
          player.playSound(player.getLocation, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        } else if (skillTypeId > 0 && skillNumber > 0
          && skillTypeId < 4) {
          activemineflagnum = (activemineflagnum + 1) % 2
          activemineflagnum match {
            case 0 => player.sendMessage(GOLD.toString + ActiveSkill.getActiveSkillName(skillTypeId, skillNumber) + "：OFF")
            case 1 => player.sendMessage(GOLD.toString + ActiveSkill.getActiveSkillName(skillTypeId, skillNumber) + ":ON")
          }
          playerdata.activeskilldata.updateSkill(skillTypeId, skillNumber, activemineflagnum)
          player.playSound(player.getLocation, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        }
      }

      if (MaterialSets.breakMaterials(player.getInventory.getItemInOffHand.getType) && equipmentslot == EquipmentSlot.OFF_HAND) {
        //オフハンドで指定ツールを持っていた時の処理

        //設置をキャンセル
        event.setCancelled(true)
        val assaultNumber = playerdata.activeskilldata.assaultnum
        val assaultTypeId = playerdata.activeskilldata.assaulttype

        if (assaultNumber >= 4 && assaultTypeId >= 4) {
          //メインハンドでも指定ツールを持っていたらフラグは変えない
          if (!mainhandflag || playerdata.activeskilldata.skillnum == 0) {
            activemineflagnum = (activemineflagnum + 1) % 2
          }
          if (activemineflagnum == 0) {
            player.sendMessage(GOLD.toString + ActiveSkill.getActiveSkillName(assaultTypeId, assaultNumber) + ":OFF")
          } else {
            player.sendMessage(GOLD.toString + ActiveSkill.getActiveSkillName(assaultTypeId, assaultNumber) + ":ON")
          }
          playerdata.activeskilldata.updateAssaultSkill(player, assaultTypeId, assaultNumber, activemineflagnum)
          player.playSound(player.getLocation, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        }
      }
    }
  }

  //棒メニューを開くイベント
  @EventHandler
  def onPlayerMenuEvent(event: PlayerInteractEvent): Unit = {
    //プレイヤーを取得
    val player = event.getPlayer
    //プレイヤーが起こしたアクションを取得
    val action = event.getAction

    if (player.getInventory.getItemInMainHand.getType != Material.STICK) return

    event.setCancelled(true)

    // 右クリックの処理ではない
    if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return
    if (event.getHand == EquipmentSlot.OFF_HAND) return

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

    seichiassist.unsafe.runAsyncTargetedEffect(player)(
      sequentialEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        StickMenu.firstPage.open
      ),
      "棒メニューの1ページ目を開く"
    )
  }

  //プレイヤーの拡張インベントリを開くイベント
  @EventHandler
  def onPlayerOpenInventorySkillEvent(event: PlayerInteractEvent): Unit = {
    //プレイヤーを取得
    val player = event.getPlayer
    //プレイヤーが起こしたアクションを取得
    val action = event.getAction
    //使った手を取得
    val equipmentslot = event.getHand

    if (event.getMaterial == Material.ENDER_PORTAL_FRAME) {
      //設置をキャンセル
      event.setCancelled(true)
      //UUIDを取得
      val uuid = player.getUniqueId
      //playerdataを取得
      val playerdata = playerMap(uuid)
      //念のためエラー分岐
      if (playerdata == null) {
        Util.sendPlayerDataNullMessage(player)
        plugin.getLogger.warning(player.getName + " => PlayerData not found.")
        plugin.getLogger.warning("PlayerClickListener.onPlayerOpenInventorySkillEvent")
        return
      }
      //パッシブスキル[4次元ポケット]（PortalInventory）を発動できるレベルに達していない場合処理終了
      if (playerdata.level < SeichiAssist.seichiAssistConfig.getPassivePortalInventorylevel) {
        player.sendMessage(GREEN.toString + "4次元ポケットを入手するには整地レベルが" + SeichiAssist.seichiAssistConfig.getPassivePortalInventorylevel + "以上必要です。")
        return
      }
      if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
        //オフハンドから実行された時処理を終了
        if (equipmentslot == EquipmentSlot.OFF_HAND) {
          return
        }
        //開く音を再生
        player.playSound(player.getLocation, Sound.BLOCK_ENDERCHEST_OPEN, 1f, 0.1.toFloat)
        //インベントリを開く
        player.openInventory(playerdata.pocketInventory)
      }
    }
  }

  //　経験値瓶を持った状態でのShift右クリック…一括使用
  @EventHandler
  def onPlayerRightClickExpBottleEvent(event: PlayerInteractEvent): Unit = {
    // 経験値瓶を持った状態でShift右クリックをした場合
    if (event.getPlayer.isSneaking && event.getPlayer.getInventory.getItemInMainHand.getType == Material.EXP_BOTTLE
      && (event.getAction == Action.RIGHT_CLICK_AIR || event.getAction == Action.RIGHT_CLICK_BLOCK)) {
      event.setCancelled(true)

      (0 until event.getItem.getAmount).foreach { _ =>
        event.getPlayer.launchProjectile(classOf[ThrownExpBottle])
      }

      event.getPlayer.getInventory.setItemInMainHand(new ItemStack(Material.AIR))
    }
  }

  //頭の即時回収
  @EventHandler
  def onPlayerRightClickMineHeadEvent(e: PlayerInteractEvent): Unit = {

    val p = e.getPlayer
    val useItem = p.getInventory.getItemInMainHand
    //専用アイテムを持っていない場合無視
    if (!Util.isMineHeadItem(useItem)) {
      return
    }

    val action = e.getAction
    //ブロックの左クリックじゃない場合無視
    if (action != Action.LEFT_CLICK_BLOCK) {
      return
    }

    val targetBlock = e.getClickedBlock
    //頭じゃない場合無視
    if (targetBlock.getType != Material.SKULL) {
      return
    }

    //壊せない場合無視
    if (!BreakUtil.canBreak(p, Some.apply(targetBlock))) {
      return
    }

    //インベントリに空がない場合無視
    if (Util.isPlayerInventoryFull(p)) {
      p.sendMessage(RED.toString + "インベントリがいっぱいです")
      return
    }

    //頭を付与
    p.getInventory.addItem(Util.getSkullDataFromBlock(targetBlock))
    //ブロックを空気で置き換える
    targetBlock.setType(Material.AIR)
    //音を鳴らしておく
    p.playSound(p.getLocation, Sound.ENTITY_ITEM_PICKUP, 2.0f, 1.0f)
  }
}
