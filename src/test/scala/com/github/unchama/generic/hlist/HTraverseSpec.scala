package com.github.unchama.generic.hlist

import cats.Applicative
import cats.arrow.FunctionK
import org.scalatest.wordspec.AnyWordSpec
import shapeless.HNil
import com.github.unchama.generic.hlist.MakeHTraverseKernel._
import com.github.unchama.generic.hlist.HTraverseKernel
import shapeless.::
import shapeless.syntax.std.tuple.productTupleOps

class HTraverseSpec extends AnyWordSpec {
  case class MyBox[A](inner: A)

  object MyBox {
    implicit val applicative: Applicative[MyBox] = new Applicative[MyBox] {
      override def pure[A](x: A): MyBox[A] = MyBox(x)

      override def ap[A, B](ff: MyBox[A => B])(fa: MyBox[A]): MyBox[B] = MyBox(ff.inner(fa.inner))
    }
  }

  "Traverse on HNil" should {
    "return HNil" in {
      val v = HNil
      val r = HTraverseKernel[HNil].htraverse(new FunctionK[Option, Lambda[a => MyBox[List[a]]]] {
        override def apply[A](fa: Option[A]): MyBox[List[A]] = MyBox(fa.toList)
      }, v)
      assert(r.inner == v)
    }
  }

  /*
  // TODO: this test case should be tested
  "Traverse on HCons" should {
    "return HCons" in {
      val h = (Some(1): Option[Int], Some("2"): Option[String]).productElements
      val r: MyBox[List[Int] :: List[String] :: HNil] =
        HTraverseKernel.apply(h).htraverse[MyBox, Option, List](new FunctionK[Option, Lambda[a => MyBox[List[a]]]] {
          override def apply[A](fa: Option[A]): MyBox[List[A]] = MyBox(fa.toList)
        }, h)

      assert(r.inner == (List(1), List("2")).productElements)
    }
  }
  */
}
