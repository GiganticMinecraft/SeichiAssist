package com.github.unchama.seichiassist.subsystems.buildranking.domain

import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount

case class BuildRankingRecord(playerName: String, buildCountAmount: BuildExpAmount)
