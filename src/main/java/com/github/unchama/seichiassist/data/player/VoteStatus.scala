package com.github.unchama.seichiassist.data.player

import java.time.LocalDateTime

case class VoteStatus(val lastVote: LocalDateTime, val chainLength: Int = 0, val totalVote: Int = 0) {
}