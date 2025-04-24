package com.github.unchama.targetedeffect.commandsender

import cats.data.Kleisli
import cats.effect.{IO, Sync}
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

object MessageEffect {
  def apply(string: String): TargetedEffect[CommandSender] =
    MessageEffectF[IO](string)

  def apply(stringList: List[String]): TargetedEffect[CommandSender] =
    MessageEffectF[IO](stringList)
}

/**
 * [[MessageEffect]]の一般化。 文脈が[[cats.effect.IO]]で固定されていない場合はこちらを使うと良い。
 *
 * NOTE: [[MessageEffectF]]の直後に[[CommandSender]]を適用する場合は、
 * implicit引数である`Sync[F]`の場所に渡しているとScala2コンパイラに認識されるため、apply等と書くと良い。
 * これがMessageEffectを残している理由である。Scala3に移行すればこの問題は解消される。
 */
object MessageEffectF {

  def apply[F[_]: Sync](string: String): Kleisli[F, CommandSender, Unit] =
    TargetedEffect.delay(_.sendMessage(string))

  def apply[F[_]: Sync](stringList: List[String]): Kleisli[F, CommandSender, Unit] =
    TargetedEffect.delay(_.sendMessage(stringList.mkString("\n")))

}
