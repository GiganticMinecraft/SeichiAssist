package com.github.unchama.seichiassist.effect

class XYZIterator2(val start: XYZTuple, val endInclusive: XYZTuple, val action: (XYZTuple) -> Unit) {
  fun doAction() {
    var x = start.x + 1
    while (x < endInclusive.x) {
      var z = start.z + 1
      while (z < endInclusive.z) {
        var y = start.y + 1
        while (y < endInclusive.y) {
          action(XYZTuple(x, y, z))
          y += 2
        }
        z += 2
      }
      x += 2
    }
  }
}