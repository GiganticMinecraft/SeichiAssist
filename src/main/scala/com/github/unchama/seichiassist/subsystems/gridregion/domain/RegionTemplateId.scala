package com.github.unchama.seichiassist.subsystems.gridregion.domain

case class RegionTemplateId(value: Int) {
  require(value >= 0)
}
