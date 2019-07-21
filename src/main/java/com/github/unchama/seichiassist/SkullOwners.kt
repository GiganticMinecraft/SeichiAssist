package com.github.unchama.seichiassist

import arrow.core.Either
import arrow.core.Right
import java.util.*

/**
 * プレーヤーヘッドにownerとして設定されるプレーヤー達に関する定数を保持するオブジェクト
 */
object SkullOwners {
  val whitecat_haru: UUID = UUID.fromString("394f76df-883d-4855-9e6a-d1a800c1ab1c")
  val unchama: UUID = UUID.fromString("b66cc3f6-a045-42ad-b4b8-320f20caf140")

  const val MHF_ArrowUp = "MHF_ArrowUp"
  const val MHF_ArrowDown = "MHF_ArrowDown"
  const val MHF_ArrowLeft = "MHF_ArrowLeft"
  const val MHF_ArrowRight = "MHF_ArrowRight"

  const val MHF_Villager = "MHF_Villager"
}
typealias SkullOwnerReference = Either<UUID, String>

fun String.asSkullOwnerReference(): SkullOwnerReference = Right(this)
