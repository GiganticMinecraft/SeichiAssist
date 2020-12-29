package com.github.unchama.buildassist

sealed trait VerticalAlign {
  def asHumanReadable: String

  def next: VerticalAlign
}

object VerticalAlign {
  case object Off extends VerticalAlign {
    override def asHumanReadable: String = "OFF"

    override def next: VerticalAlign = Upper
  }
  case object Upper extends VerticalAlign {
    override def asHumanReadable: String = "上側"

    override def next: VerticalAlign = Lower
  }
  case object Lower extends VerticalAlign {
    override def asHumanReadable: String = "下側"

    override def next: VerticalAlign = Off
  }
}