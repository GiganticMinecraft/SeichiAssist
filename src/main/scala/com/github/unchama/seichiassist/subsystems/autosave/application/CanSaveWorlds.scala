package com.github.unchama.seichiassist.subsystems.autosave.application

import simulacrum.typeclass

@typeclass trait CanSaveWorlds[F[_]] extends AnyRef {

  val saveAllWorlds: F[Unit]

}
