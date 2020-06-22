package com.github.unchama

import com.github.unchama.generic.tag.tag.@@

import scala.concurrent.ExecutionContext

package object concurrent {

  trait RepeatingTaskContextTag
  type RepeatingTaskContext = ExecutionContext @@ RepeatingTaskContextTag

}
