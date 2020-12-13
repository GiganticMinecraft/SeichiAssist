package com.github.unchama.seichiassist.subsystems.managedfly.application

case class SystemConfiguration(expConsumptionAmount: Int) {
  require {
    expConsumptionAmount >= 0
  }
}
