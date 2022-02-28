package com.github.unchama.seichiassist.subsystems.present.infrastructure

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.{BukkitObjectInputStream, BukkitObjectOutputStream}
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.util.Using

/**
 * ItemStackとデータベースのBlob (正確にはその文字列表現) を互いに変換する。
 */
object ItemStackBlobProxy {
  type Base64 = String
  def itemStackToBlob(stack: ItemStack): Base64 = {
    Using.resource(new ByteArrayOutputStream()) { baos =>
      Using.resource(new BukkitObjectOutputStream(baos)) { bos => bos.writeObject(stack) }
      Base64Coder.encodeLines(baos.toByteArray)
    }
  }

  def blobToItemStack(data: Base64): ItemStack = {
    Using.resource(new ByteArrayInputStream(Base64Coder.decodeLines(data))) { bais =>
      Using.resource(new BukkitObjectInputStream(bais)) { boi =>
        boi.readObject().asInstanceOf[ItemStack]
      }
    }
  }
}
