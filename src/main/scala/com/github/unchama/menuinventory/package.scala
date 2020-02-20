package com.github.unchama

import com.github.unchama.generic.tag.tag.@@

import scala.concurrent.ExecutionContext

package object menuinventory {
  object Tags {
    trait LayoutPreparationContextTag
  }

  type LayoutPreparationContext = ExecutionContext @@ Tags.LayoutPreparationContextTag
}
