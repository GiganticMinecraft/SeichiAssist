package com.github.unchama.util.collection

// TODO should not inherit HashMap directly
class MutableInvertibleMap<K, V> : HashMap<K, V> {
  private val inverse: HashMap<V, K>

  constructor() {
    this.inverse = HashMap()
  }

  private constructor(map: HashMap<K, V>, inverse: HashMap<V, K>) : super(map) {
    this.inverse = HashMap(inverse)
  }

  constructor(map: HashMap<K, V>) : this(map, HashMap(map.map { (k, v) -> v to k }.toMap()))

  constructor(bMap: MutableInvertibleMap<K, V>) : this(bMap, bMap.inverse)

  override fun put(key: K, value: V): V? {
    if (this.containsKey(key)) {
      this.inverse.remove(this[key])
    }

    if (this.inverse.containsKey(value))
      throw IllegalArgumentException("There already exists a mapping to the given value.")

    this.inverse[value] = key
    return super.put(key, value)
  }

  override fun remove(key: K): V? {
    if (!this.containsKey(key)) return null

    val removed = super.remove(key)
    this.inverse.remove(removed)
    return removed
  }

  fun removeValue(value: V): K? {
    val removed = this.inverse.remove(value)

    if (removed != null) {
      this.remove(removed)
    }

    return removed
  }

  fun getInverse(): MutableInvertibleMap<V, K> {
    return MutableInvertibleMap(this.inverse, this)
  }
}
