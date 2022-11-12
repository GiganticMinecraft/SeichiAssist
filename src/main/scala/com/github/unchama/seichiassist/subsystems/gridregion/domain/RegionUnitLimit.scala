package com.github.unchama.seichiassist.subsystems.gridregion.domain

case class RegionUnitLimit(value: Int) {
  require(value >= 0)
}
