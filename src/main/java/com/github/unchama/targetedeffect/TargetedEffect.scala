package com.github.unchama.targetedeffect

import arrow.typeclasses.Monoid
import kotlin.coroutines.Continuation

/**
 * Minecraft内の何らかの対象[T]に向けた作用を持ち,
 * [runFor]メソッドにより作用を[T]に及ぼすことができるオブジェクトへのtrait.
 *
 * [runFor]の副作用は, Arrow Fxの設計理念に従いコルーチンの中で発動される.
 */
trait TargetedEffect[-T] {
  def runFor(minecraftObject: T, cont: Continuation[Unit])
}

object TargetedEffect {
  def monoid[T](): Monoid[TargetedEffect[T]] = new Monoid[TargetedEffect[T]] {
    override def empty(): TargetedEffect[T] = EmptyEffect

    override def combine(a: TargetedEffect[T], b: TargetedEffect[T]): TargetedEffect[T] =
        TargetedEffect { cont =>
          a.runFor(_, cont)
          b.runFor(_, cont)
        }
  }

  def apply[T](effect: Continuation[Unit] => T => Unit): TargetedEffect[T] = new TargetedEffect[T] {
    override def runFor(minecraftObject: T, continuation: Continuation[Unit]): Unit = effect(continuation)(minecraftObject)
  }
}

object TargetedEffects {
  /**
   * [TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
   */
  def deferredEffect[T](f: Continuation[Unit] => TargetedEffect[T]): TargetedEffect[T] =
    TargetedEffect { cont => f(cont).runFor(_, cont) }

  /**
  * 実行対象の[T]から[TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
  */
  def computedEffect[T](f: Continuation[Unit] => T => TargetedEffect[T]): TargetedEffect[T] =
    TargetedEffect { cont => o => f(cont)(o).runFor(o, cont) }

  def sequentialEffect[T](effects: TargetedEffect[T]*): TargetedEffect[T] = effects.toList.asSequentialEffect()
}