package com.github.unchama.seichiassist

import arrow.core.Either
import arrow.core.Right
import java.util.*

/**
 * プレーヤーヘッドにownerとして設定されるプレーヤー達に関する定数を保持するオブジェクト
 */
object SkullOwners {
  const val whitecat_haru: String = "whitecat_haru"
  const val unchama: String = "unchama"

  const val MHF_ArrowUp = "MHF_ArrowUp"
  const val MHF_ArrowDown = "MHF_ArrowDown"
  const val MHF_ArrowLeft = "MHF_ArrowLeft"
  const val MHF_ArrowRight = "MHF_ArrowRight"

  const val MHF_Villager = "MHF_Villager"
}
typealias SkullOwnerReference = Either<UUID, String>

fun String.asSkullOwnerReference(): SkullOwnerReference = Right(this)
