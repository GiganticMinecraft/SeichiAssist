package com.github.unchama.generic

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
   */
  def rePrepend[A](list: List[A])(predicate: A => Boolean, element: A => A): List[A] = {
    list.find(predicate) match {
      case Some(value) =>
        element(value) :: list.filterNot(_ == value)
      case None => list
    }
  }

  /**
   * Listの中身で条件に一致するものがあれば`element`を先頭に追加しなおし、
   * 一致するものがなければ`element(None)`を先頭に追加します
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
  def computeDoubleList[A, B](
    firstList: List[A],
    secondList: List[A]
  )(predicate: A => Boolean, map: Option[(A, A)] => B): B = {
    import cats.implicits._
    val firstTarget = firstList.find(predicate)
    val secondTarget = secondList.find(predicate)
    map(firstTarget.product(secondTarget))
  }

}
