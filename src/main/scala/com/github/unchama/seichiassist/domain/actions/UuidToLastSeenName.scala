package com.github.unchama.seichiassist.domain.actions

import java.util.UUID

trait UuidToLastSeenName[F[_]] {

  def entries: F[Map[UUID, String]]

}

object UuidToLastSeenName {

  def apply[F[_]: UuidToLastSeenName]: UuidToLastSeenName[F] = implicitly[UuidToLastSeenName[F]]

}