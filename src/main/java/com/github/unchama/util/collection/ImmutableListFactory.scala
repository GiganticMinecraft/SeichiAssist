package com.github.unchama.util.collection

import scala.collection.JavaConversions._

object ImmutableListFactory {

  // Checked, no scala usage found
  def of[E](): java.util.List[E] = Nil

  // Checked, no scala usage found
  def of[E](o: E): java.util.List[E] = List(o)

  def of[E](o: Array[E]): java.util.List[E] = o.toList

  @SafeVarargs
  // Checked, no scala usage found
  def of[E](o: E*): java.util.List[E] = List(o: _*)

}
