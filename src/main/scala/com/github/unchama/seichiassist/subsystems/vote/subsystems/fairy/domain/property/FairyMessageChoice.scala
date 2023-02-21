package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import cats.data.NonEmptyVector

case class FairyMessageChoice(messages: NonEmptyVector[FairyMessage])
