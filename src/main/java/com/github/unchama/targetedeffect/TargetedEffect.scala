package com.github.unchama.targetedeffect

import arrow.typeclasses.Monoid
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import kotlin.coroutines.Continuation

/**
 * Minecraft内の何らかの対象[T]に向けた作用を持ち,
 * [runFor]メソッドにより作用を[T]に及ぼすことができるオブジェクトへのtrait.
 *
 * [runFor]の副作用は, Arrow Fxの設計理念に従いコルーチンの中で発動される.
 */
trait TargetedEffect[-T] {
  @SuspendingMethod def runFor(minecraftObject: T)
}

object TargetedEffect {
  def monoid[T](): Monoid[TargetedEffect[T]] = new Monoid[TargetedEffect[T]] {
    override def empty(): TargetedEffect[T] = EmptyEffect

    override def combine(a: TargetedEffect[T], b: TargetedEffect[T]): TargetedEffect[T] =
        TargetedEffect { _ =>
          a.runFor _
          b.runFor _
        }
  }

  def apply[T](effect: Continuation[Unit] => T => Unit): TargetedEffect[T] = new TargetedEffect[T] {
    override @SuspendingMethod def runFor(minecraftObject: T): Unit = effect(continuation)(minecraftObject)
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