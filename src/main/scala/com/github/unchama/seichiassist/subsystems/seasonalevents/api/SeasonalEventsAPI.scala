package com.github.unchama.seichiassist.subsystems.seasonalevents.api

import cats.Functor
import cats.effect.Clock
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.Christmas
import io.chrisdavenport.cats.effect.time.JavaTime

import java.time.ZoneId

trait ChristmasEventsAPI[F[_]] extends AnyRef {

  val isInEvent: F[Boolean]

}

object ChristmasEventsAPI {

  import cats.implicits._

  def withF[F[_]: Clock: Functor]: ChristmasEventsAPI[F] = new ChristmasEventsAPI[F] {
    override val isInEvent: F[Boolean] =
      JavaTime
        .fromClock[F]
        .getLocalDate(ZoneId.of("JST", ZoneId.SHORT_IDS))
        .map(Christmas.isInEvent)
  }

  def apply[F[_]: ChristmasEventsAPI]: ChristmasEventsAPI[F] = implicitly
}

trait SeasonalEventsAPI[F[_]] extends AnyRef {

  implicit val christmasEventsAPI: ChristmasEventsAPI[F]

}

object SeasonalEventsAPI {
  def withF[F[_]: Clock: Functor]: SeasonalEventsAPI[F] = new SeasonalEventsAPI[F] {
    override implicit val christmasEventsAPI: ChristmasEventsAPI[F] =
      ChristmasEventsAPI.withF[F]
  }

  def apply[F[_]: SeasonalEventsAPI]: SeasonalEventsAPI[F] = implicitly
}
