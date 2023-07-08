package com.github.unchama.generic

import scala.annotation.tailrec

object ListExtra {

  /**
   * Listの内容を[[B]]または[[C]]に分類し、
   * tupleとして返します。
   */
  def partitionWith[A, B, C](
    list: List[A]
  )(partitioningFunction: A => Either[B, C]): (List[B], List[C]) = {
    list match {
      case ::(head, next) =>
        val partitionedNext = partitionWith(next)(partitioningFunction)
        partitioningFunction(head) match {
          case Left(value)  => (value :: partitionedNext._1, partitionedNext._2)
          case Right(value) => (partitionedNext._1, value :: partitionedNext._2)
        }
      case Nil => (Nil, Nil)
    }
  }

  /**
   * Listの中身で条件に一致するものがあったときに`element`を先頭に追加し直します
   * listの中に与えられたpredicateに合致する要素があった場合、その要素をelementで写して変換します。そのような要素がなかった場合、そのままlistを返却します。
   *
   * @param list      処理対象
   * @param predicate 抽出する要素の述語
   * @param element   抽出された要素を写す関数
   * @return 一致した要素が先頭に移動されたかもしれない[[List]]
   */
  def rePrepend[A](list: List[A])(predicate: A => Boolean, element: A => A): List[A] = {
    list.find(predicate) match {
      case Some(value) =>
        element(value) :: list.filterNot(_ == value)
      case None => list
    }
  }

  /**
   * 与えられたListにおいて、述語に合致する要素を全て取り除いた上で先頭に追加します。
   *
   * @param list      処理対象
   * @param predicate 述語
   * @param element   先頭に追加するために、述語で検索した結果を移す関数。`Option.get`は例外の原因となりえることに注意。
   * @return Listの中身で条件に一致するものがあれば一番最初に見つかった要素と同値のもの全てを取り除いてからその要素を先頭に追加したListを返し、
   *         一致するものがなければ`element(None)`を先頭に追加します
   */
  def rePrependOrAdd[A](
    list: List[A]
  )(predicate: A => Boolean, element: Option[A] => A): List[A] = {
    list.find(predicate) match {
      case Some(value) =>
        element(Some(value)) :: list.filterNot(_ == value)
      case None => element(None) :: list
    }
  }

  /**
   * `conditions`に当てはまる値が2つの[[List]]に存在するか探し、その結果の直積を`compute`で写した結果を返します。
   *
   * @param predicate 探す値の条件を関数で記述します。
   * @param map    探した結果を受け取り、写す関数を渡します。
   *                   この引数の関数は、conditionsにマッチする要素がfirstListとsecondListそれぞれに見つかれば、
   *                   それぞれの結果を[[Tuple2]]に格納した[[Some]]が、そうでなければ[[None]]が渡されます。
   * @return `firstList`と`secondList`から`conditions`に合致する要素を探した結果を`compute`で写した値
   */
  def findBothThenMap[A, B](
    firstList: List[A],
    secondList: List[A]
  )(predicate: A => Boolean, map: Option[(A, A)] => B): B = {
    import cats.implicits._
    val firstTarget = firstList.find(predicate)
    val secondTarget = secondList.find(predicate)
    map(firstTarget.product(secondTarget))
  }

  /**
   * 最初の要素が `predicate` を満たすようになるまで `list` を左回転する。
   *
   * `list` が空である場合、または `list` を一周回転させても最初の要素が
   * `predicate` を満たすことが無かった (つまり、すべての要素が
   * `predicate` を満たさなかった) 場合には `None` が返される。
   */
  @tailrec
  def rotateLeftUntil[A](list: List[A])(predicate: A => Boolean): Option[List[A]] = {
    list.find(predicate) match {
      case Some(_) =>
        if (predicate(list.head)) Some(list)
        else rotateLeftUntil(list.filterNot(_ == list.head) :+ list.head)(predicate)
      case _ => None
    }
  }

}
