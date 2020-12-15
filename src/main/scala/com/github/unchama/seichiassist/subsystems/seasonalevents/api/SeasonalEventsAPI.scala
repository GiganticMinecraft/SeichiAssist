package com.github.unchama.seichiassist.subsystems.seasonalevents.api

import cats.Functor
import cats.effect.Clock
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.Christmas
import io.chrisdavenport.cats.effect.time.JavaTime
import simulacrum.typeclass

import java.time.ZoneId

@typeclass trait ChristmasEventsAPI[F[_]] {

  val isInEvent: F[Boolean]

}

object ChristmasEventsAPI {

  import cats.implicits._

  def withF[F[_] : Clock : Functor]: ChristmasEventsAPI[F] = new ChristmasEventsAPI[F] {
    override val isInEvent: F[Boolean] =
      JavaTime.fromClock[F]
        .getLocalDate(ZoneId.of("JST", ZoneId.SHORT_IDS))
        .map(Christmas.isInEvent)
  }

}

@typeclass trait SeasonalEventsAPI[F[_]] {

  implicit val christmasEventsAPI: ChristmasEventsAPI[F]

}

object SeasonalEventsAPI {
  def withF[F[_] : Clock : Functor]: SeasonalEventsAPI[F] = new SeasonalEventsAPI[F] {
    override implicit val christmasEventsAPI: ChristmasEventsAPI[F] = ChristmasEventsAPI.withF[F]
  }
}
