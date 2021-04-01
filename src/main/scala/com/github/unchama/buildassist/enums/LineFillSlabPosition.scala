package com.github.unchama.buildassist.enums

trait LineFillSlabPosition {
  def next: LineFillSlabPosition with Product
}

object LineFillSlabPosition {
  case object Upper extends LineFillSlabPosition {
    def next: LineFillSlabPosition with Product = Lower
  }

  case object Lower extends LineFillSlabPosition {
    def next: LineFillSlabPosition with Product = Both
  }

  case object Both extends LineFillSlabPosition {
    def next: LineFillSlabPosition with Product = Upper
  }
}
