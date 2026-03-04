package com.github.unchama.itemstackbuilder

import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.HeadSkinUrl

import java.util.UUID

sealed trait SkullOwnerReference

case class SkullOwnerUuid(uuid: UUID, resolvedSkinUrl: Option[HeadSkinUrl] = None)
    extends SkullOwnerReference

/**
 * UUID指定だけでは、カスタムテクスチャをヘッドに設定することができない場合に備えて、`TextureValue`を指定するもの
 */
case class SkullOwnerTextureValue(textureValue: String) extends SkullOwnerReference

/**
 * `textureUrl`は以下の記事の方法で取得されたスキンのURL
 * @see https://qiita.com/yuta0801/items/edb4804dfb867ea82c5a
 */
case class SkullOwnerUuidWithNameWithTextureUrl(uuid: UUID, name: String, textureUrl: String)
    extends SkullOwnerReference
