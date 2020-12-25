package com.github.unchama.util

/**
 * [[Key]] をキーとした参照セルの辞書型データ構造の抽象。
 * 読み書きは [[F]] のコンテキストで行われる作用として記述される。
 *
 * (特にDBや別マシンにあるKVストアなどでは)アクセスが並列に行われる可能性があることから、
 * このI/Fは一切の等式を保証しない。
 */
trait RefDict[F[_], Key, Value] {

  def read(key: Key): F[Value]

  def write(key: Key, value: Value): F[Unit]

}
