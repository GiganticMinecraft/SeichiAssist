package com.github.unchama.itemstackbuilder

import java.util.UUID

sealed trait SkullOwnerReference

case class SkullOwnerUuidWithName(uuid: UUID, name: String) extends SkullOwnerReference

case class SkullOwnerUuid(uuid: UUID) extends SkullOwnerReference

/**
 * UUID指定だけでは、カスタムテクスチャをヘッドに設定することができない場合に備えて、`TextureValue`を指定するもの
 */
case class SkullOwnerTextureValue(textureValue: String) extends SkullOwnerReference
