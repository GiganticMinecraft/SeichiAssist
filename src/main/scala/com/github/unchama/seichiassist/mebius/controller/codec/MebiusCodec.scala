package com.github.unchama.seichiassist.mebius.controller.codec

import com.github.unchama.seichiassist.mebius.domain.MebiusProperty

/**
 * `EntityType` をMebiusの実体(例えばItemStack)と見なし、
 * `EntityType` への `MebiusProperty` の実体化とデコードを行うオブジェクト
 */
trait MebiusCodec[MebiusEntityType] {

  /**
   * (必ずしも有効な`MebiusProperty`を持つとは限らない)実体から `MebiusProperty` をデコードする。
   */
  def decodeMebiusProperty(entity: MebiusEntityType): Option[MebiusProperty]

  /**
   * 与えられた `MebiusProperty` を持つような実体を得る。
   *
   * `Some(property) == decodeMebiusProperty(materialize(property))`
   *
   * を満足する。
   */
  def materialize(property: MebiusProperty): MebiusEntityType

}
