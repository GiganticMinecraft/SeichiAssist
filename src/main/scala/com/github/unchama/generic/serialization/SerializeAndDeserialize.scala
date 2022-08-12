package com.github.unchama.generic.serialization

/**
 * シリアライズ / デシリアライズが行える型の型クラス
 */
trait SerializeAndDeserialize[ParseError, T] {

  /**
   * [[T]] の値を文字列にシリアライズする。
   */
  def serialize(value: T): String

  /**
   * 文字列から [[T]] の値を構築することを試みる。
   *
   * [[T]] の値が構築できなかった場合は、理由を説明する
   * [[ParseError]] が [[Left]] で返る。
   */
  def deserialize(serialized: String): Either[ParseError, T]

}
