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

}
