package com.github.unchama.menuinventory

import com.github.unchama.generic.tag.tag.@@

import scala.concurrent.ExecutionContext

object Tags {

  trait LayoutPreparationContextTag

}

object Types {

  import Tags._

  type LayoutPreparationContext = ExecutionContext @@ LayoutPreparationContextTag
}
