package com.github.unchama.targetedeffect.syntax

import cats.kernel.Monoid
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
    def asCommandEffect(): TargetedEffect[Player] =
      targetedeffect.delay { p =>
        p.chat(s"/$string")
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
