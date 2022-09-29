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
   * Listの中身で条件に一致するものがあったときに`replacement`を先頭に追加し直します
   */
  def replace[A](list: List[A])(predicate: A => Boolean, replacement: A => A): List[A] = {
    list.find(predicate) match {
      case Some(value) =>
        replacement(value) :: list.filterNot(_ == value)
      case None => list
    }
  }

  /**
   * Listの中身で条件に一致するものがあれば`replacement`を先頭に追加しなおし、
   * 一致するものがなければreplacementを追加します。
   */
  def valueReplaceOrAdd[A](
    list: List[A]
  )(predicate: A => Boolean, replacement: Option[A] => A): List[A] = {
    list.find(predicate) match {
      case Some(value) =>
        replacement(Some(value)) :: list.filterNot(_ == value)
      case None => replacement(None) :: list
    }
  }

}
