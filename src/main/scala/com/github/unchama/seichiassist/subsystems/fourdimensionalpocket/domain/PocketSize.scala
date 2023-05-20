package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain

case class PocketSize(chestRows: Int) {
  require(1 <= chestRows && chestRows <= 6, "chestRows should be in [1, 6]")

  lazy val totalStackCount: Int = chestRows * 9
}

object PocketSize {

  def fromTotalStackCount(totalStackCount: Int): PocketSize = {
    require(totalStackCount % 9 == 0)

    PocketSize(totalStackCount / 9)
  }

}
