package com.github.unchama.seichiassist.subsystems.anywhereender

package object domain {
  type CanAccessEverywhereEnderChest = Either[AccessDenialReason, Unit]
}
