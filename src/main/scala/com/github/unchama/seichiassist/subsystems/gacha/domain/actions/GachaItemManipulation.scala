package com.github.unchama.seichiassist.subsystems.gacha.domain.actions

import com.github.unchama.generic.ReadWrite

/**
 * ガチャ景品アイテムスタックに対する操作の抽象。
 */
trait GachaItemManipulation[ItemStack, F[_]] {

  val amount: ReadWrite[F, Int]

  val cloneStack: F[ItemStack]

}
