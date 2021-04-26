package com.github.unchama.seichiassist.subsystems.buildranking.domain

import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData

case class BuildRankingRecord(playerName: String, buildAmountData: BuildAmountData)
