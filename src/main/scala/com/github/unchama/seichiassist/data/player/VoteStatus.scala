package com.github.unchama.seichiassist.data.player

import java.time.LocalDateTime

case class VoteStatus(lastVote: LocalDateTime, chainLength: Int = 0, totalVote: Int = 0)
