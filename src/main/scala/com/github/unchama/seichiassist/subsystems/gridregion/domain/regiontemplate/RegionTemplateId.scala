package com.github.unchama.seichiassist.subsystems.gridregion.domain.regiontemplate

case class RegionTemplateId(value: Int) {
  require(value >= 0)
}
