package com.github.unchama.targetedeffect.player

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.concurrent.{BukkitSyncIOShift, Execution}
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

object CommandEffect {
  def apply(string: String)(implicit context: BukkitSyncIOShift): TargetedEffect[Player] =
    Kleisli { p =>
      // 非同期スレッドからchatを呼ぶとコマンドがそのスレッドで実行される(Spigot 1.12.2)。
      // コマンドの実行は基本的に同期スレッドで行ってほしいのでメインスレッドまで処理を飛ばす。
      Execution.onServerMainThread(IO { p.chat(s"/$string") })
    }
}
