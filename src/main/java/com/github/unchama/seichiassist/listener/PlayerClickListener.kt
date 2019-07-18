package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.*
import com.github.unchama.seichiassist.data.GachaPrize
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.seichiassist.menus.stickmenu.firstPage
import com.github.unchama.seichiassist.task.AsyncEntityRemover
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.sequentialEffect
import com.github.unchama.targetedeffect.unfocusedEffect
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownExpBottle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

class PlayerClickListener : Listener {
  internal var plugin = SeichiAssist.instance
  internal var playermap = SeichiAssist.playermap
  internal var gachadatalist = SeichiAssist.gachadatalist
  //アクティブスキル処理
  @EventHandler
  fun onPlayerActiveSkillEvent(event: PlayerInteractEvent) {
    //プレイヤー型を取得
    val player = event.player
    //プレイヤーが起こしたアクションを取得
    val action = event.action
    //使った手を取得
    val equipmentslot = event.hand
    //UUIDを取得
    val uuid = player.uniqueId
    //プレイヤーデータを取得
    val playerdata = playermap[uuid] ?: return

    //playerdataがない場合はreturn
    if (equipmentslot == null) {
      return
    }
    //オフハンドから実行された時処理を終了
    if (equipmentslot == EquipmentSlot.OFF_HAND) {
      return
    }

    if (player.isSneaking) {
      return
    }
    //サバイバルでない時　または　フライ中の時終了
    if (player.gameMode != GameMode.SURVIVAL || player.isFlying) {
      return
    }
    //アクティブスキルフラグがオフの時処理を終了
    if (playerdata.activeskilldata.mineflagnum == 0 || playerdata.activeskilldata.skillnum == 0) {
      return
    }

    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) {
      return
    }


    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
      //アサルトアーマー使用中の時は終了左クリックで判定
      if (playerdata.activeskilldata.assaulttype != 0) {
        return
      }
      //クールダウンタイム中は処理を終了
      if (!playerdata.activeskilldata.skillcanbreakflag) {
        //SEを再生
        player.playSound(player.location, Sound.BLOCK_DISPENSER_FAIL, 0.5.toFloat(), 1f)
        return
      }


      if (MaterialSets.breakMaterials.contains(event.material)) {
        if (playerdata.activeskilldata.skilltype == ActiveSkill.ARROW.gettypenum()) {
          //クールダウン処理
          val cooldown = ActiveSkill.ARROW.getCoolDown(playerdata.activeskilldata.skillnum)
          if (cooldown > 5) {
            CoolDownTask(player, false, true, false).runTaskLater(plugin, cooldown)
          } else {
            CoolDownTask(player, false, false, false).runTaskLater(plugin, cooldown)
          }
          //エフェクトが指定されていないときの処理
          if (playerdata.activeskilldata.effectnum == 0) {
            runArrowSkill(player, Arrow::class.java)
          } else if (playerdata.activeskilldata.effectnum <= 100) {
            val skilleffect = ActiveSkillEffect.values()
            skilleffect[playerdata.activeskilldata.effectnum - 1].runArrowEffect(player)
          } else if (playerdata.activeskilldata.effectnum > 100) {
            val premiumeffect = ActiveSkillPremiumEffect.values()
            premiumeffect[playerdata.activeskilldata.effectnum - 1 - 100].runArrowEffect(player)
          }//エフェクトが指定されているときの処理
        }
      }
    } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
      //アサルトアーマーをどっちも使用していない時終了
      if (playerdata.activeskilldata.assaulttype == 0) {
        return
      }

      //クールダウンタイム中は処理を終了
      if (!playerdata.activeskilldata.skillcanbreakflag) {
        //SEを再生
        player.playSound(player.location, Sound.BLOCK_DISPENSER_FAIL, 0.5.toFloat(), 1f)
        return
      }


      if (MaterialSets.breakMaterials.contains(event.material)) {
        if (playerdata.activeskilldata.skilltype == ActiveSkill.ARROW.gettypenum()) {
          //クールダウン処理
          val cooldown = ActiveSkill.ARROW.getCoolDown(playerdata.activeskilldata.skillnum)
          if (cooldown > 5) {
            CoolDownTask(player, false, true, false).runTaskLater(plugin, cooldown)
          } else {
            CoolDownTask(player, false, false, false).runTaskLater(plugin, cooldown)
          }
          //エフェクトが指定されていないときの処理
          if (playerdata.activeskilldata.effectnum == 0) {
            runArrowSkill(player, Arrow::class.java)
          } else if (playerdata.activeskilldata.effectnum <= 100) {
            val skilleffect = ActiveSkillEffect.values()
            skilleffect[playerdata.activeskilldata.effectnum - 1].runArrowEffect(player)
          } else if (playerdata.activeskilldata.effectnum > 100) {
            val premiumeffect = ActiveSkillPremiumEffect.values()
            premiumeffect[playerdata.activeskilldata.effectnum - 1 - 100].runArrowEffect(player)
          }//スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
          //通常エフェクトが指定されているときの処理(100以下の番号に割り振る）

        }
      }
    }
  }

  private fun <T : org.bukkit.entity.Projectile> runArrowSkill(player: Player, clazz: Class<T>) {
    //プレイヤーの位置を取得
    val ploc = player.location

    //発射する音を再生する.
    player.playSound(ploc, Sound.ENTITY_ARROW_SHOOT, 1f, 1f)

    //スキルを実行する処理
    val loc = player.location
    loc.add(loc.direction).add(0.0, 1.6, 0.0)
    val vec = loc.direction
    val k = 1.0
    vec.x = vec.x * k
    vec.y = vec.y * k
    vec.z = vec.z * k
    val proj = player.world.spawn(loc, clazz)
    proj.shooter = player
    proj.setGravity(false)
    //読み込み方法
    /*
		 * Projectile proj = event.getEntity();
			if ( proj instanceof Arrow && proj.hasMetadata("ArrowSkill") ) {
			}
		 */
    proj.setMetadata("ArrowSkill", FixedMetadataValue(plugin, true))
    proj.velocity = vec

    //矢を消去する処理
    AsyncEntityRemover(proj).runTaskLater(plugin, 100)
  }


  //プレイヤーが右クリックした時に実行(ガチャを引く部分の処理)
  @EventHandler
  fun onPlayerRightClickGachaEvent(event: PlayerInteractEvent) {
    //プレイヤー型を取得
    val player = event.player
    //UUIDを取得
    val uuid = player.uniqueId
    //プレイヤーデータを取得
    val playerdata = playermap[uuid] ?: return
    //playerdataがない場合はreturn

    val name = playerdata.name
    //プレイヤーが起こしたアクションを取得
    val action = event.action
    //使った手を取得
    val equipmentslot = event.hand
    //もしサバイバルでなければ処理を終了
    if (player.gameMode != GameMode.SURVIVAL) {
      return
    }
    //使ったアイテムを取得
    if (event.item == null) {
      return
    }
    val itemstack = event.item
    //ガチャ用の頭でなければ終了
    if (!Util.isGachaTicket(itemstack)) {
      return
    }
    event.isCancelled = true

    //以下サバイバル時のガチャ券の処理↓

    //連打防止クールダウン処理
    if (!playerdata.gachacooldownflag) {
      return
    } else {
      //連打による負荷防止の為クールダウン処理
      CoolDownTask(player, false, false, true).runTaskLater(plugin, 4)
    }

    //オフハンドから実行された時処理を終了
    if (equipmentslot == EquipmentSlot.OFF_HAND) {
      return
    }
    //ガチャシステムメンテナンス中は処理を終了
    if (SeichiAssist.gachamente) {
      player.sendMessage("現在ガチャシステムはメンテナンス中です。\nしばらく経ってからもう一度お試しください")
      return
    }
    //ガチャデータが設定されていない場合
    if (gachadatalist.isEmpty()) {
      player.sendMessage("ガチャが設定されていません")
      return
    }

    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
      var count = 1
      if (player.isSneaking) {
        count = itemstack.amount
        player.sendMessage(ChatColor.AQUA.toString() + "" + count + "回ガチャを回しました。")
      }

      if (!Util.removeItemfromPlayerInventory(player.inventory, itemstack, count)) {
        player.sendMessage(ChatColor.RED.toString() + "ガチャ券の数が不正です。")
        return
      }
      for (c in 0 until count) {
        //プレゼント用ガチャデータ作成
        val present: GachaPrize = GachaPrize.runGacha()
        //ガチャ実行
        if (present.probability < 0.1) {
          present.appendOwnerLore(name)
        }
        //メッセージ設定
        var str = ""

        //プレゼントを格納orドロップ
        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, present.itemStack)
        } else {
          Util.dropItem(player, present.itemStack)
          str += ChatColor.AQUA.toString() + "プレゼントがドロップしました。"
        }

        //確率に応じてメッセージを送信
        if (present.probability < 0.001) {
          Util.sendEverySoundWithoutIgnore(Sound.ENTITY_ENDERDRAGON_DEATH, 0.5.toFloat(), 2f)
          if (!playerdata.everysoundflag) {
            player.playSound(player.location, Sound.ENTITY_ENDERDRAGON_DEATH, 0.5.toFloat(), 2f)
          }
          val enchantname = ArrayList<String>()
          val lore = present.itemStack.itemMeta.lore
          val enchantment = present.itemStack.itemMeta.enchants

          for (enchant in enchantment.keys) {
            enchantname.add(ChatColor.GRAY.toString() + Util.getEnchantName(enchant.name, enchantment[enchant]!!))
          }
          lore.remove("§r§2所有者：" + player.name)

          val message = TextComponent()
          message.text = ChatColor.AQUA.toString() + present.itemStack.itemMeta.displayName + ChatColor.GOLD + "を引きました！おめでとうございます！"
          message.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(" " + present.itemStack.itemMeta.displayName + "\n" + Util.getDescFormat(enchantname) + Util.getDescFormat(lore)).create())

          player.sendMessage(ChatColor.RED.toString() + "おめでとう！！！！！Gigantic☆大当たり！" + str)
          Util.sendEveryMessageWithoutIgnore(ChatColor.GOLD.toString() + player.displayName + "がガチャでGigantic☆大当たり！")
          Util.sendEveryMessageWithoutIgnore(message)
        } else if (present.probability < 0.01) {
          //大当たり時にSEを鳴らす(自分だけ)
          player.playSound(player.location, Sound.ENTITY_WITHER_SPAWN, 0.8.toFloat(), 1f)
          //ver 0.3.1以降 大当たり時の全体通知を削除
          player.sendMessage(ChatColor.GOLD.toString() + "おめでとう！！大当たり！" + str)

        } else if (present.probability < 0.1) {
          player.sendMessage(ChatColor.YELLOW.toString() + "おめでとう！当たり！" + str)
        } else {
          if (count == 1) {
            player.sendMessage(ChatColor.WHITE.toString() + "はずれ！また遊んでね！" + str)
          }
        }
      }
      player.playSound(player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.1.toFloat())
    }
  }

  //スキル切り替えのイベント
  @EventHandler
  fun onPlayerActiveSkillToggleEvent(event: PlayerInteractEvent) {
    //プレイヤーを取得
    val player = event.player
    //プレイヤーの起こしたアクションの取得
    val action = event.action
    //アクションを起こした手を取得
    val equipmentslot = event.hand
    if (player.inventory.itemInMainHand.type == Material.STICK || player.inventory.itemInMainHand.type == Material.SKULL_ITEM) {
      return
    }
    //UUIDを取得
    val uuid = player.uniqueId
    //playerdataを取得
    val playerdata = playermap[uuid] ?: return
    //playerdataがない場合はreturn


    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) {
      return
    }

    //アクティブスキルを発動できるレベルに達していない場合処理終了
    if (playerdata.level < SeichiAssist.seichiAssistConfig.dualBreaklevel) {
      return
    }

    //クールダウンタイム中は処理を終了
    if (!playerdata.activeskilldata.skillcanbreakflag) {
      //SEを再生
      //player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, (float)0.5, 1);
      return
    }

    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {

      val mainhandflag = MaterialSets.breakMaterials.contains(player.inventory.itemInMainHand.type)
      val offhandflag = MaterialSets.breakMaterials.contains(player.inventory.itemInOffHand.type)

      var activemineflagnum = playerdata.activeskilldata.mineflagnum
      //どちらにも対応したアイテムを持っていない場合終了
      if (!mainhandflag && !offhandflag) {
        return
      }
      //アクション実行されたブロックがある場合の処理
      if (action == Action.RIGHT_CLICK_BLOCK) {
        //クリックされたブロックの種類を取得
        val cmaterial = event.clickedBlock.type
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
        event.isCancelled = true

        if (playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum() && playerdata.activeskilldata.skillnum == 1 || playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum() && playerdata.activeskilldata.skillnum == 2) {

          activemineflagnum = (activemineflagnum + 1) % 3
          when (activemineflagnum) {
            0 -> player.sendMessage(ChatColor.GOLD.toString() + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) + "：OFF")
            1 -> player.sendMessage(ChatColor.GOLD.toString() + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) + ":ON-Above(上向き）")
            2 -> player.sendMessage(ChatColor.GOLD.toString() + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) + ":ON-Under(下向き）")
          }
          playerdata.activeskilldata.updateSkill(player, playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum, activemineflagnum)
          player.playSound(player.location, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        } else if (playerdata.activeskilldata.skilltype > 0 && playerdata.activeskilldata.skillnum > 0
            && playerdata.activeskilldata.skilltype < 4) {
          activemineflagnum = (activemineflagnum + 1) % 2
          when (activemineflagnum) {
            0 -> player.sendMessage(ChatColor.GOLD.toString() + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) + "：OFF")
            1 -> player.sendMessage(ChatColor.GOLD.toString() + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) + ":ON")
          }
          playerdata.activeskilldata.updateSkill(player, playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum, activemineflagnum)
          player.playSound(player.location, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        }
      }

      if (MaterialSets.breakMaterials.contains(player.inventory.itemInOffHand.type) && equipmentslot == EquipmentSlot.OFF_HAND) {
        //オフハンドで指定ツールを持っていた時の処理

        //設置をキャンセル
        event.isCancelled = true


        if (playerdata.activeskilldata.assaultnum >= 4 && playerdata.activeskilldata.assaulttype >= 4) {
          //メインハンドでも指定ツールを持っていたらフラグは変えない
          if (!mainhandflag || playerdata.activeskilldata.skillnum == 0) {
            activemineflagnum = (activemineflagnum + 1) % 2
          }
          if (activemineflagnum == 0) {
            player.sendMessage(ChatColor.GOLD.toString() + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.assaulttype, playerdata.activeskilldata.assaultnum) + ":OFF")
          } else {
            player.sendMessage(ChatColor.GOLD.toString() + ActiveSkill.getActiveSkillName(playerdata.activeskilldata.assaulttype, playerdata.activeskilldata.assaultnum) + ":ON")
          }
          playerdata.activeskilldata.updateAssaultSkill(player, playerdata.activeskilldata.assaulttype, playerdata.activeskilldata.assaultnum, activemineflagnum)
          player.playSound(player.location, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        }
      }
    }
  }

  //棒メニューを開くイベント
  @EventHandler
  fun onPlayerMenuEvent(event: PlayerInteractEvent) {
    //プレイヤーを取得
    val player = event.player
    //プレイヤーが起こしたアクションを取得
    val action = event.action
    //アクションを起こした手を取得
    val equipmentslot = event.hand

    if (player.inventory.itemInMainHand.type != Material.STICK) return

    // 右クリックの処理ではない
    if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return
    if (equipmentslot == EquipmentSlot.OFF_HAND) return

    val effect = sequentialEffect(
        unfocusedEffect { event.isCancelled = true },
        FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
        StickMenu.firstPage.open
    )

    runBlocking {
      effect.runFor(player)
    }
  }

  //プレイヤーの拡張インベントリを開くイベント
  @EventHandler
  fun onPlayerOpenInventorySkillEvent(event: PlayerInteractEvent) {
    //プレイヤーを取得
    val player = event.player
    //プレイヤーが起こしたアクションを取得
    val action = event.action
    //使った手を取得
    val equipmentslot = event.hand

    if (event.material == Material.ENDER_PORTAL_FRAME) {
      //設置をキャンセル
      event.isCancelled = true
      //UUIDを取得
      val uuid = player.uniqueId
      //playerdataを取得
      val playerdata = playermap[uuid]
      //念のためエラー分岐
      if (playerdata == null) {
        Util.sendPlayerDataNullMessage(player)
        plugin.logger.warning(player.name + " -> PlayerData not found.")
        plugin.logger.warning("PlayerClickListener.onPlayerOpenInventorySkillEvent")
        return
      }
      //パッシブスキル[4次元ポケット]（PortalInventory）を発動できるレベルに達していない場合処理終了
      if (playerdata.level < SeichiAssist.seichiAssistConfig.passivePortalInventorylevel) {
        player.sendMessage(ChatColor.GREEN.toString() + "4次元ポケットを入手するには整地レベルが" + SeichiAssist.seichiAssistConfig.passivePortalInventorylevel + "以上必要です。")
        return
      }
      if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
        //オフハンドから実行された時処理を終了
        if (equipmentslot == EquipmentSlot.OFF_HAND) {
          return
        }
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_ENDERCHEST_OPEN, 1f, 0.1.toFloat())
        //インベントリを開く
        player.openInventory(playerdata.inventory)
      }
    }
  }

  //　経験値瓶を持った状態でのShift右クリック…一括使用
  @EventHandler
  fun onPlayerRightClickExpBottleEvent(event: PlayerInteractEvent) {
    // 経験値瓶を持った状態でShift右クリックをした場合
    if (event.player.isSneaking && event.player.inventory.itemInMainHand.type == Material.EXP_BOTTLE
        && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
      event.isCancelled = true
      val num = event.item.amount
      for (cnt in 0 until num) {
        event.player.launchProjectile(ThrownExpBottle::class.java)
      }
      event.player.inventory.itemInMainHand = ItemStack(Material.AIR)
    }
  }

  //頭の即時回収
  @EventHandler
  fun onPlayerRightClickMineHeadEvent(e: PlayerInteractEvent) {

    val p = e.player
    val useItem = p.inventory.itemInMainHand
    if (!Util.isMineHeadItem(useItem)) {
      return
    }              //専用アイテムを持っていない場合無視

    if (Util.isPlayerInventoryFull(p)) {
      return
    }

    val action = e.action
    if (action != Action.LEFT_CLICK_BLOCK) {
      return
    }        //ブロックの左クリックじゃない場合無視

    val targetBlock = e.clickedBlock
    if (targetBlock.type != Material.SKULL) {
      return
    }      //頭じゃない場合無視

    if (!BreakUtil.canBreak(p, targetBlock)) {
      return
    }          //壊せない場合無視

    if (Util.isPlayerInventoryFull(p)) {                  //インベントリに空がない場合無視
      p.sendMessage(ChatColor.RED.toString() + "インベントリがいっぱいです")
      return
    }

    //頭を付与
    p.inventory.addItem(Util.getSkullDataFromBlock(targetBlock))
    //ブロックを空気で置き換える
    targetBlock.type = Material.AIR
    //音を鳴らしておく
    p.playSound(p.location, Sound.ENTITY_ITEM_PICKUP, 2.0f, 1.0f)
  }
}
