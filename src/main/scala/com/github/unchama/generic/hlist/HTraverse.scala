package com.github.unchama.generic.hlist

import cats.{Applicative, ~>}
import shapeless.{::, DepFn0, HList, HNil}

/**
 * The object that knows how to traverse an HList of type `L`.
 *
 * {{{
 *                   +-----------++--------------++---------+
 *              L =  |     Int   ::     String   ::    HNil | -----\
 *                   +-----------++--------------++---------+      |
 *                   .           ..              ..         .      | shapeless.ops.hlist.Mapped.Aux[L, SingleArgumentParser, X]
 *                   +-----------++--------------++---------+      |
 *      Mapped[F] =  |   F[Int]  ::   F[String]  ::    HNil | <----+--- X
 *                   +---v-------++---v----------++---------+
 *                   .   v       ..   v          ..         .
 *                   .   v       ..   v          ..         .
 *                   +---v-------++---v----------++---------+
 *                   | A[G[Int]] :: A[G[String]] :: A[HNil] |
 *                   +-----------++--------------++---------+
 *                   .           .. (accumulate) ..         .
 *                   +-----------++--------------++---------+
 *    return type = A[   G[Int]  ::   G[String]  ::   HNil ]|
 *                   ^-----------++--------------++---------+
 *                    \
 *                     \--- Mapped[G]
 * }}}
 *
 */
sealed trait HTraverseKernel {
  type L <: HList
  //noinspection ScalaUnusedSymbol
  // ^ F is actually unused, but replacing them with underscore is NOT supported in Scala 3.
  //   This warning should not be resolved in that way, as it will make harder the migration steps.
  type Mapped[F[_]] <: HList

  /**
   * A higher-kinded traversal method for HLists.
   *
   * `htraverse` applies a function (mapper) to each element in an HList of values wrapped with type constructor `F`,
   * producing a new HList with values wrapped by `G`. This operation requires an `Applicative` instance for `A`.
   *
   * This method is primarily used for transforming one type constructor `F` to another `G` across a heterogeneously
   * typed list, yielding a new HList with elements of the original types, transformed by `G`.
   *
   * @tparam A an effect type with an Applicative instance, which is used to encapsulate the result of the traversal.
   * @tparam F the original type constructor wrapping each type in the input HList.
   * @tparam G the resulting type constructor wrapping each type in the output HList.
   * @param mapper a polymorphic function mapping a type wrapped with `F` to an `A` effect producing a type wrapped with `G`.
   * @param list   an HList of values wrapped with the type constructor `F`, i.e., of type `Mapped[F]`.
   * @return an `A` effect yielding an HList of results of type `Mapped[G]`, each value now wrapped with the type constructor `G`.
   */
  def htraverse[A[_]: Applicative, F[_], G[_]](mapper: F ~> Lambda[a => A[G[a]]], list: Mapped[F]): A[Mapped[G]]
}

trait HNilHTraverseKernel extends HTraverseKernel {
  type L = HNil
  //noinspection ScalaUnusedSymbol
  type Mapped[F[_]] = HNil

  def htraverse[A[_]: Applicative, F[_], G[_]](mapper: F ~> Lambda[a => A[G[a]]], list: HNil): A[HNil] =
    Applicative[A].pure(HNil)
}

object HNilHTraverseKernel extends HNilHTraverseKernel

case class HConsHTraverseKernel[H, T <: HTraverseKernel](tail: T) extends HTraverseKernel {
  type L = H :: tail.L
  type Mapped[F[_]] = F[H] :: tail.Mapped[F]

  def htraverse[A[_]: Applicative, F[_], G[_]](mapper: F ~> Lambda[a => A[G[a]]], list: Mapped[F]): A[Mapped[G]] = {
    Applicative[A].map2(
      mapper.apply[H](list.head),
      tail.htraverse(mapper, list.tail)
    )((headResult, tailResult) => headResult :: tailResult)
  }
}

object HTraverseKernel {
  def apply[L <: HList](implicit mk: MakeHTraverseKernel[L]): mk.Out = mk()

  // Use this method for type inference.
  def apply[L <: HList](l: L)(implicit mk: MakeHTraverseKernel[L]): mk.Out = mk()
}

/**
 * The object that can construct a `HTraverseKernel`.
 */
trait MakeHTraverseKernel[L <: HList] extends DepFn0 { type Out <: HTraverseKernel }

object MakeHTraverseKernel {
  type Aux[L <: HList, Out0 <: HTraverseKernel] = MakeHTraverseKernel[L] { type Out = Out0 }

  implicit def makeHNilHTraverseKernel: Aux[HNil, HNilHTraverseKernel] = new MakeHTraverseKernel[HNil] {
    override type Out = HNilHTraverseKernel
    def apply(): Out = HNilHTraverseKernel
  }

  implicit def makeHConsHTraverseKernel[H, T <: HList, CtOut <: HTraverseKernel](implicit ct: Aux[T, CtOut]): Aux[H :: T, HConsHTraverseKernel[H, CtOut]] =
    new MakeHTraverseKernel[H :: T] {
      override type Out = HConsHTraverseKernel[H, CtOut]
      override def apply(): Out = HConsHTraverseKernel[H, CtOut](ct())
    }
}
