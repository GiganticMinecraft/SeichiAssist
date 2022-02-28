package com.github.unchama.util

import cats.Functor
import cats.effect.Sync

import scala.util.Random

trait RandomEffect[F[_]] {

  import cats.implicits._

  /**
   * [0, n) の範囲にある整数を生成するプログラム。
   */
  def getNaturalLessThan(n: Int): F[Int]

  /**
   * [0.0, 1.0] の範囲にある浮動小数点を生成するプログラム。
   */
  def sampleFUnitInterval: F[Float]

  /**
   * [0.0, 1.0] の範囲にある倍精度浮動小数点を生成するプログラム。
   */
  def sampleDUnitInterval: F[Double]

  /**
   * 確率 1/n のベルヌーイ試行を行うプログラム。
   */
  final def tryForOneIn(n: Int)(implicit F: Functor[F]): F[Boolean] = {
    require(n > 0)
    getNaturalLessThan(n - 1).map(_ == 0)
  }

}

object RandomEffect {

  def createFromRandom[F[_]: Sync](random: Random): RandomEffect[F] = new RandomEffect[F] {
    override def getNaturalLessThan(n: Int): F[Int] = Sync[F].delay {
      random.nextInt(n)
    }

    override def sampleFUnitInterval: F[Float] = Sync[F].delay {
      random.nextFloat()
    }

    override def sampleDUnitInterval: F[Double] = Sync[F].delay {
      random.nextDouble()
    }
  }

  def apply[F[_]: RandomEffect]: RandomEffect[F] = implicitly

}
