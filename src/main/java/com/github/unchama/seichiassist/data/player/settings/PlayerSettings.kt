package com.github.unchama.seichiassist.data.player.settings

import com.github.unchama.seichiassist.data.player.PlayerNickName
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.UnfocusedEffect
import com.github.unchama.targetedeffect.asMessageEffect
import com.github.unchama.targetedeffect.unfocusedEffect
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class PlayerSettings {
  val fastDiggingEffectSuppression = FastDiggingEffectSuppression()

  var autoMineStack = true

  //内訳メッセージを出すフラグ
  var receiveFastDiggingEffectStats = false

  //ガチャ受け取り方法設定
  var receiveGachaTicketEveryMinute = true

  //キルログ表示トグル
  var shouldDisplayDeathMessages = false

  //ワールドガード保護ログ表示トグル
  var shouldDisplayWorldGuardLogs = true

  var broadcastMutingSettings: BroadcastMutingSettings = BroadcastMutingSettings.MUTE_MESSAGE_AND_SOUND

  //ハーフブロック破壊抑制用
  private var allowBreakingHalfBlocks = false

  //複数種類破壊トグル
  var multipleidbreakflag = false

  //PvPトグル
  var pvpflag = false

  var nickName = PlayerNickName(PlayerNickName.Style.Level, 0, 0, 0)

  var isExpBarVisible = false

  //region accessors and modifiers

  val toggleAutoMineStack: UnfocusedEffect =
      unfocusedEffect {
        this.autoMineStack = !this.autoMineStack
      }

  val toggleWorldGuardLogEffect: UnfocusedEffect =
      unfocusedEffect {
        this.shouldDisplayWorldGuardLogs = !this.shouldDisplayWorldGuardLogs
      }

  val toggleDeathMessageMutingSettings: UnfocusedEffect =
      unfocusedEffect {
        this.shouldDisplayDeathMessages = !this.shouldDisplayDeathMessages
      }

  @Suppress("RedundantSuspendModifier")
  suspend fun getBroadcastMutingSettings(): BroadcastMutingSettings = broadcastMutingSettings

  val toggleBroadcastMutingSettings
    get() = unfocusedEffect {
      broadcastMutingSettings = getBroadcastMutingSettings().nextSettingsOption()
    }

  @Suppress("RedundantSuspendModifier")
  suspend fun toggleHalfBreakFlag(): TargetedEffect<Player> {
    allowBreakingHalfBlocks = !allowBreakingHalfBlocks

    val newStatus = if (allowBreakingHalfBlocks) "${ChatColor.GREEN}破壊可能" else "${ChatColor.RED}破壊不可能"
    val responseMessage = "現在ハーフブロックは$newStatus${ChatColor.RESET}です."

    return responseMessage.asMessageEffect()
  }
}