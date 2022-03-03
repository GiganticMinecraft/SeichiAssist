package com.github.unchama.seichiassist.subsystems.autosave.application

trait CanSaveWorlds[F[_]] extends AnyRef {

  val saveAllWorlds: F[Unit]

}

object CanSaveWorlds {
  def apply[F[_]: CanSaveWorlds]: CanSaveWorlds[F] = implicitly
}
