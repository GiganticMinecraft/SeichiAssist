package com.github.unchama.buildassist

trait StepPlaceMode {
  def asHumanReadable: String

  def next: StepPlaceMode
}

object StepPlaceMode {
  sealed object Upper extends StepPlaceMode {
    override def asHumanReadable: String = "上側"

    override def next: StepPlaceMode = Lower
  }

  sealed object Lower extends StepPlaceMode {
    override def asHumanReadable: String = "下側"

    override def next: StepPlaceMode = Both
  }

  sealed object Both extends StepPlaceMode {
    override def asHumanReadable: String = "両方"

    override def next: StepPlaceMode = Upper
  }
}
