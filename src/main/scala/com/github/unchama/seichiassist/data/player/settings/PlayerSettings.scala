package com.github.unchama.seichiassist.data.player.settings

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.seichiassist.data.player.{NicknameStyle, PlayerNickname}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

class PlayerSettings {

  import com.github.unchama.targetedeffect._
  // キルログ表示トグル
  var shouldDisplayDeathMessages = false
  // ワールドガード保護ログ表示トグル
  var shouldDisplayWorldGuardLogs = true

  // region accessors and modifiers
  var broadcastMutingSettings: BroadcastMutingSettings =
    BroadcastMutingSettings.MuteMessageAndSound

  // 複数種類破壊トグル
  var performMultipleIDBlockBreakWhenOutsideSeichiWorld: Boolean = false

  // スキルでのネザー水晶類ブロックの破壊トグル
  var allowBreakNetherQuartzBlock: Boolean = true

  // PvPトグル
  var pvpflag = false
  var nickname: PlayerNickname = PlayerNickname(NicknameStyle.Level)
  // ハーフブロック破壊抑制用
  private var allowBreakingHalfBlocks = false

  val toggleWorldGuardLogEffect: TargetedEffect[Any] =
    UnfocusedEffect {
      this.shouldDisplayWorldGuardLogs = !this.shouldDisplayWorldGuardLogs
    }
  val toggleDeathMessageMutingSettings: TargetedEffect[Any] =
    UnfocusedEffect {
      this.shouldDisplayDeathMessages = !this.shouldDisplayDeathMessages
    }
  val getBroadcastMutingSettings: IO[BroadcastMutingSettings] = IO {
    broadcastMutingSettings
  }
  val toggleBroadcastMutingSettings: TargetedEffect[Any] = Kleisli.liftF(for {
    currentSettings <- getBroadcastMutingSettings
    nextSettings = currentSettings.next
  } yield {
    broadcastMutingSettings = nextSettings
  })
  val toggleHalfBreakFlag: TargetedEffect[Player] = DeferredEffect(IO {
    allowBreakingHalfBlocks = !allowBreakingHalfBlocks

    val newStatus = if (allowBreakingHalfBlocks) s"${GREEN}破壊可能" else "${RED}破壊不可能"
    val responseMessage = s"現在ハーフブロックは$newStatus${RESET}です."

    MessageEffect(responseMessage)
  })

  /**
   * 複数ブロック同時破壊のON/OFFを切り替える[UnforcedEffect]
   */
  val toggleMultipleIdBreakFlag: TargetedEffect[Player] = UnfocusedEffect {
    performMultipleIDBlockBreakWhenOutsideSeichiWorld =
      !performMultipleIDBlockBreakWhenOutsideSeichiWorld
  }

  /**
   * スキルでのネザー水晶類ブロックの破壊のON/OFFを切り替える[UnforcedEffect]
   */
  val toggleAllowNetherQuartzBlockBreakFlag: TargetedEffect[Player] = UnfocusedEffect {
    allowBreakNetherQuartzBlock = !allowBreakNetherQuartzBlock
  }
}
