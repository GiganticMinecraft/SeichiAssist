package com.github.unchama.seichiassist

/**
 * プレーヤーヘッドにownerとして設定されるプレーヤー達に関する定数を保持するオブジェクト
 */
object SkullOwners {
  val whitecat_haru = "whitecat_haru".asSkullOwnerReference()
  val unchama = "unchama".asSkullOwnerReference()

  val MHF_ArrowUp = "MHF_ArrowUp".asSkullOwnerReference()
  val MHF_ArrowDown = "MHF_ArrowDown".asSkullOwnerReference()
  val MHF_ArrowLeft = "MHF_ArrowLeft".asSkullOwnerReference()
  val MHF_ArrowRight = "MHF_ArrowRight".asSkullOwnerReference()

  val MHF_Exclamation = "MHF_Exclamation".asSkullOwnerReference()

  val MHF_Villager = "MHF_Villager".asSkullOwnerReference()
}

def String.asSkullOwnerReference(): SkullOwnerReference = Right(this)
