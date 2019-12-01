package com.github.unchama.targetedeffect.syntax

import cats.data.Kleisli
import cats.effect.IO
import cats.kernel.Monoid
import com.github.unchama.concurrent.{BukkitSyncExecutionContext, Execution}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

trait TargetedEffectCombineAll {
  implicit class TargetedEffectFold[T](val effects: List[TargetedEffect[T]]) {
    import cats.implicits._

    def asSequentialEffect(): TargetedEffect[T] = Monoid[TargetedEffect[T]].combineAll(effects)
  }
}

trait StringTargetedEffectSyntax {
  implicit class StringToCommandEffect(val string: String) {
    def asCommandEffect()(implicit context: BukkitSyncExecutionContext): TargetedEffect[Player] =
      Kleisli { p =>
        // 非同期スレッドからchatを呼ぶとコマンドがそのスレッドで実行される(Spigot 1.12.2)。
        // コマンドの実行は基本的に同期スレッドで行ってほしいのでメインスレッドまで処理を飛ばす。
        Execution.onServerMainThread(IO { p.chat(s"/$string") })
      }
  }

  implicit class StringMessageEffect(val string: String) {
    def asMessageEffect(): TargetedEffect[CommandSender] =
      targetedeffect.delay(_.sendMessage(string))
  }

  implicit class StringListMessageEffect(val stringList: List[String]) {
    def asMessageEffect(): TargetedEffect[CommandSender] =
      targetedeffect.delay(_.sendMessage(stringList.toArray))
  }
}

trait AllSyntax
  extends TargetedEffectCombineAll
  with StringTargetedEffectSyntax
