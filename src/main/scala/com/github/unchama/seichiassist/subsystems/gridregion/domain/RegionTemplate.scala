package com.github.unchama.seichiassist.subsystems.gridregion.domain

case class RegionTemplateId(value: Int) {
  require(value >= 0)
}

case class RegionTemplate(templateId: RegionTemplateId, shape: SubjectiveRegionShape)
