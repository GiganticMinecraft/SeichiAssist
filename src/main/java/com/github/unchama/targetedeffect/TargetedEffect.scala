package com.github.unchama.targetedeffect

/**
 * Minecraft内の何らかの対象[T]に向けた作用を持ち,
 * [runFor]メソッドにより作用を[T]に及ぼすことができるオブジェクトへのinterface.
 *
 * [runFor]の副作用は, Arrow Fxの設計理念に従いコルーチンの中で発動される.
 */
interface TargetedEffect<in T>{
  suspend def runFor(minecraftObject: T)

  companion object {
    def <T> monoid(): Monoid<TargetedEffect<T>> = object : Monoid<TargetedEffect<T>> {
      override def empty(): TargetedEffect<T> = EmptyEffect
      override def TargetedEffect<T>.combine(b: TargetedEffect<T>): TargetedEffect<T> =
          TargetedEffect {
            this.runFor(it)
            b.runFor(it)
          }
    }

    operator def <T> invoke(effect: suspend (T) -> Unit): TargetedEffect<T> = object : TargetedEffect<T> {
      override suspend def runFor(minecraftObject: T) = effect(minecraftObject)
    }
  }
}

/**
 * [TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
 */
def <T> deferredEffect(f: suspend () -> TargetedEffect<T>): TargetedEffect<T> = TargetedEffect { f().runFor(it) }

/**
 * 実行対象の[T]から[TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
 */
def <T> computedEffect(f: suspend (T) -> TargetedEffect<T>): TargetedEffect<T> = TargetedEffect { f(it).runFor(it) }

def <T> sequentialEffect(vararg effects: TargetedEffect<T>): TargetedEffect<T> = effects.toList().asSequentialEffect()
