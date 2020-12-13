package com.github.unchama.seichiassist.subsystems.managedfly.domain

/**
 * プレーヤーが動いているかを指し示す状態
 */
sealed trait IdleStatus

case object Idle extends IdleStatus

case object HasMovedRecently extends IdleStatus
