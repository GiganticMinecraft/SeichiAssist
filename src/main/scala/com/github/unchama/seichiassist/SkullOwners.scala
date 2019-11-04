package com.github.unchama.seichiassist

import com.github.unchama.itemstackbuilder.{SkullOwnerName, SkullOwnerReference}

/**
 * プレーヤーヘッドにownerとして設定されるプレーヤー達に関する定数を保持するオブジェクト
 */
object SkullOwners {
  val whitecat_haru: SkullOwnerReference = "whitecat_haru".asSkullOwnerReference()
  val unchama: SkullOwnerReference = "unchama".asSkullOwnerReference()

  val MHF_ArrowUp: SkullOwnerReference = "MHF_ArrowUp".asSkullOwnerReference()
  val MHF_ArrowDown: SkullOwnerReference = "MHF_ArrowDown".asSkullOwnerReference()
  val MHF_ArrowLeft: SkullOwnerReference = "MHF_ArrowLeft".asSkullOwnerReference()
  val MHF_ArrowRight: SkullOwnerReference = "MHF_ArrowRight".asSkullOwnerReference()

  val MHF_Exclamation: SkullOwnerReference = "MHF_Exclamation".asSkullOwnerReference()

  val MHF_Villager: SkullOwnerReference = "MHF_Villager".asSkullOwnerReference()

  implicit class StringOps(val string: String) {
    def asSkullOwnerReference(): SkullOwnerReference = SkullOwnerName(string)
  }

}
