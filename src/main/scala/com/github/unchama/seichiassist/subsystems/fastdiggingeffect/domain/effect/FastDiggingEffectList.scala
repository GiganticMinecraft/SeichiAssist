package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

import cats.{Applicative, Functor}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState
import io.chrisdavenport.cats.effect.time.JavaTime

import scala.concurrent.duration.FiniteDuration

class FastDiggingEffectList(private val list: List[FastDiggingEffectTimings]) {

  import cats.implicits._

  def filterInactive[F[_] : JavaTime : Functor]: F[FastDiggingEffectList] = {
    JavaTime[F].getLocalDateTimeUTC.fmap { currentTime =>
      new FastDiggingEffectList(list.filter(_.isActiveAt(currentTime)))
    }
  }

  def filteredList[F[_] : JavaTime : Functor]: F[List[FastDiggingEffectTimings]] = filterInactive[F].fmap(_.list)

  /**
   * 効果を追加し、不要になった効果を削除した新しいリストを作成する
   */
  def appendEffect[
    F[_] : JavaTime : Applicative
  ](effect: FastDiggingEffect, duration: FiniteDuration): F[FastDiggingEffectList] = {
    Applicative[F].map2(
      JavaTime[F].getLocalDateTimeUTC.fmap { currentTime =>
        FastDiggingEffectTimings(currentTime, duration, effect)
      },
      filteredList[F]
    ) { (timings, filteredList) =>
      new FastDiggingEffectList(filteredList.appended(timings))
    }
  }

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
