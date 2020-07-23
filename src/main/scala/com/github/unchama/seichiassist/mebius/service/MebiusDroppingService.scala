package com.github.unchama.seichiassist.mebius.service

import cats.effect.IO
import com.github.unchama.seichiassist.mebius.domain.MebiusProperty

import scala.util.Random

object MebiusDroppingService {

  // Mebiusドロップ率
  private val averageBlocksToBeBrokenPerMebiusDrop = 50000

  def tryForDrop(ownerName: String): IO[Option[MebiusProperty]] = IO {
    if (Random.nextInt(averageBlocksToBeBrokenPerMebiusDrop) == 0) {
      Some(MebiusProperty.apply(ownerName))
    } else {
      None
    }
  }

}
