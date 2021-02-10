package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

import cats.Functor
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState
import io.chrisdavenport.cats.effect.time.JavaTime

import scala.concurrent.duration.FiniteDuration

class FastDiggingEffectList(private val list: List[FastDiggingEffectTimings]) {

  import cats.implicits._

  def appendEffect[
    F[_] : JavaTime : Functor
  ](effect: FastDiggingEffect, duration: FiniteDuration): F[FastDiggingEffectList] = {
    JavaTime[F].getLocalDateTimeUTC.fmap { currentTime =>
      val timings = FastDiggingEffectTimings(currentTime, duration, effect)
      new FastDiggingEffectList(list.appended(timings))
    }
  }

  def filterInactive[F[_] : JavaTime : Functor]: F[FastDiggingEffectList] = {
    JavaTime[F].getLocalDateTimeUTC.fmap { currentTime =>
      new FastDiggingEffectList(list.filter(_.isActiveAt(currentTime)))
    }
  }

  def filteredList[F[_] : JavaTime : Functor]: F[List[FastDiggingEffectTimings]] = filterInactive[F].fmap(_.list)

  def totalEffectAmplifier[F[_] : JavaTime : Functor](suppressionSettings: FastDiggingEffectSuppressionState): F[Int] = {
    filteredList[F].map { list =>
      val totalAmplifier: Int =
        list
          .map(_.effect.amplifier)
          .sum
          .toInt

      (totalAmplifier - 1) min suppressionSettings.effectAmplifierCap
    }
  }
}

object FastDiggingEffectList {

  val empty: FastDiggingEffectList = new FastDiggingEffectList(List.empty)

}
