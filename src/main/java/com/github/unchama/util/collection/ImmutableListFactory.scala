package com.github.unchama.util.collection

import scala.jdk.CollectionConverters._

object ImmutableListFactory {

  // Checked, no scala usage found
  def of[E](): java.util.List[E] = Nil.asJava

  // Checked, no scala usage found
  def of[E](o: E): java.util.List[E] = List(o).asJava

  def of[E](o: Array[E]): java.util.List[E] = o.toList.asJava

  @SafeVarargs
  // Checked, no scala usage found
  def of[E](o: E*): java.util.List[E] = List(o: _*).asJava

}
