package com.github.unchama.seichiassist.data.player.settings

import cats.effect.IO
import com.github.unchama.seichiassist.data.player.PlayerNickName
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import com.github.unchama.targetedeffect.UnfocusedEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

class PlayerSettings {
  import com.github.unchama.targetedeffect.MessageEffects._
  import com.github.unchama.targetedeffect.TargetedEffects._

  val fastDiggingEffectSuppression = new FastDiggingEffectSuppression()

  var autoMineStack = true

  //内訳メッセージを出すフラグ
  var receiveFastDiggingEffectStats = false

  //ガチャ受け取り方法設定
  var receiveGachaTicketEveryMinute = true

  //キルログ表示トグル
  var shouldDisplayDeathMessages = false

  //ワールドガード保護ログ表示トグル
  var shouldDisplayWorldGuardLogs = true

  var broadcastMutingSettings: BroadcastMutingSettings = BroadcastMutingSettings.MuteMessageAndSound

  //ハーフブロック破壊抑制用
  private var allowBreakingHalfBlocks = false

  //複数種類破壊トグル
  var multipleidbreakflag = false

  //PvPトグル
  var pvpflag = false

  var nickName = PlayerNickName(PlayerNickName.Style.Level, 0, 0, 0)

  var isExpBarVisible = false

  //region accessors and modifiers

  val toggleAutoMineStack: TargetedEffect[Any] =
      UnfocusedEffect {
        this.autoMineStack = !this.autoMineStack
      }

  val toggleWorldGuardLogEffect: TargetedEffect[Any] =
      UnfocusedEffect {
        this.shouldDisplayWorldGuardLogs = !this.shouldDisplayWorldGuardLogs
      }

  val toggleDeathMessageMutingSettings: TargetedEffect[Any] =
      UnfocusedEffect {
        this.shouldDisplayDeathMessages = !this.shouldDisplayDeathMessages
      }

  val getBroadcastMutingSettings: IO[BroadcastMutingSettings] = IO { broadcastMutingSettings }

  val toggleBroadcastMutingSettings: TargetedEffect[Any] = (_: Any) =>
    for {
      currentSettings <- getBroadcastMutingSettings
      nextSettings = currentSettings.next
    } yield { broadcastMutingSettings = nextSettings }

  val toggleHalfBreakFlag: TargetedEffect[Player] = deferredEffect(IO {
    allowBreakingHalfBlocks = !allowBreakingHalfBlocks

    val newStatus = if (allowBreakingHalfBlocks) s"${GREEN}破壊可能" else "${RED}破壊不可能"
    val responseMessage = s"現在ハーフブロックは$newStatus${RESET}です."

    responseMessage.asMessageEffect()
  })
}