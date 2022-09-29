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
   * 一致するものがなければreplacementを追加します。
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

}
