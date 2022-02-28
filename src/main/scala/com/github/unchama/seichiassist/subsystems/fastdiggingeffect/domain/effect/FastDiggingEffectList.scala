package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

import cats.{Applicative, Functor}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState
import io.chrisdavenport.cats.effect.time.JavaTime

import scala.concurrent.duration.FiniteDuration

class FastDiggingEffectList(private val list: List[FastDiggingEffectTimings]) {

  import cats.implicits._

  def filterInactive[F[_]: JavaTime: Functor]: F[FastDiggingEffectList] = {
    JavaTime[F].getLocalDateTimeUTC.fmap { currentTime =>
      new FastDiggingEffectList(list.filter(_.isActiveAt(currentTime)))
    }
  }

  def filteredList[F[_]: JavaTime: Functor]: F[List[FastDiggingEffectTimings]] =
    filterInactive[F].fmap(_.list)

  /**
   * 効果を追加し、不要になった効果を削除した新しいリストを作成する作用。
   */
  def appendEffect[F[_]: JavaTime: Applicative](
    effect: FastDiggingEffect,
    duration: FiniteDuration
  ): F[FastDiggingEffectList] = {
    Applicative[F].map2(
      JavaTime[F].getLocalDateTimeUTC.fmap { currentTime =>
        FastDiggingEffectTimings(currentTime, duration, effect)
      },
      filteredList[F]
    ) { (timings, filteredList) => new FastDiggingEffectList(filteredList.appended(timings)) }
  }

  /**
   * [[FastDiggingEffectSuppressionState]] を考慮した、 現在有効な採掘速度上昇効果の合計値をMinecraftのポーション効果値として得る作用。
   *
   * 合計値が1未満だった場合、[[None]]が得られる。
   */
  def totalPotionAmplifier[F[_]: JavaTime: Functor](
    suppressionSettings: FastDiggingEffectSuppressionState
  ): F[Option[Int]] = {
    filteredList[F].map { list =>
      val totalAmplifier: FastDiggingAmplifier = list.map(_.effect.amplifier).combineAll
      val capped =
        FastDiggingAmplifier.order.min(totalAmplifier, suppressionSettings.effectAmplifierCap)

      capped.toMinecraftPotionAmplifier
    }
  }
}

object FastDiggingEffectList {

  val empty: FastDiggingEffectList = new FastDiggingEffectList(List.empty)

}
