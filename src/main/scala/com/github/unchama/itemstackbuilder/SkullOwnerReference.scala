package com.github.unchama.itemstackbuilder

import java.util.UUID

sealed trait SkullOwnerReference

case class SkullOwnerUuid(uuid: UUID) extends SkullOwnerReference

case class SkullOwnerName(name: String) extends SkullOwnerReference
