package com.github.unchama.seichiassist.effect

class XYZIterator(val start: XYZTuple, val endInclusive: XYZTuple, val action: (XYZTuple) -> Unit) {
  fun doAction() {
    for (x in start.x..endInclusive.x) {
      for (y in start.y..endInclusive.y) {
        for (z in start.z..endInclusive.z) {
          action(XYZTuple(x, y, z))
        }
      }
    }
  }
}