package com.github.unchama.seichiassist.subsystems.managedfly.application

import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration

trait FlyDurationPersistenceRepository[SyncContext[_], Key] {

  def writePair(key: Key, duration: Option[RemainingFlyDuration]): SyncContext[Unit]

  def read(key: Key): SyncContext[Option[RemainingFlyDuration]]

}
