package com.github.unchama.seichiassist.subsystems.anywhereender

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel

trait SystemConfiguration {

  /**
   * 「どこでもエンダーチェスト」を開くために必要な最小の整地レベル
   */
  val requiredMinimumLevel: SeichiLevel

}
