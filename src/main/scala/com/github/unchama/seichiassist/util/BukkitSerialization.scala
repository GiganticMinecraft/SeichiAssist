package com.github.unchama.seichiassist.util

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.util.io.{BukkitObjectInputStream, BukkitObjectOutputStream}
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}
import scala.util.Using

object BukkitSerialization {
  /**
   * エンコード方式:
   * {{{
   * +------+--------------+--------------+-----+--------------+
   * | size | itemstack(0) | itemstack(1) | ... | itemstack(n) |
   * +------+--------------+--------------+-----+--------------+
   * | int  |  ItemStack   |  ItemStack   | ... |  ItemStack   |
   * +------+--------------+--------------+-----+--------------+
   * }}}
   */
  def toBase64(inventory: Inventory): String = BukkitSerialization.serializeToBase64(
    (0 until inventory.getSize).map(inventory.getItem).toList
  )

  @throws[IOException]
  def fromBase64(data: String): Inventory = try {
    val items = BukkitSerialization.deserializeFromBase64(data)
    val inventory = Bukkit.getServer.createInventory(null, items.size, s"${ChatColor.DARK_PURPLE}${ChatColor.BOLD}4次元ポケット")
    items.zipWithIndex.foreach { case (item, i) =>
      inventory.setItem(i, item)
    }
    inventory
  }

  @throws[IOException]
  def fromBase64forPocket(data: String): Inventory = {
    val items = BukkitSerialization.deserializeFromBase64(data)
    val inventory = Bukkit.getServer.createInventory(null, items.size, s"${ChatColor.DARK_PURPLE}${ChatColor.BOLD}4次元ポケット")
    items.zipWithIndex.foreach { case (item, i) =>
      inventory.setItem(i, item)
    }
    inventory
  }

  def serializeToBase64(items: List[ItemStack]): String = {
    try {
      Using.resource(new ByteArrayOutputStream) { outputStream =>
        Using.resource(new BukkitObjectOutputStream(outputStream)) {dataOutput =>
          dataOutput.writeInt(items.size)
          items.indices
            .foreach(i => dataOutput.writeObject(items(i)))
        }
        // 変換後のシリアルデータを取得
        Base64Coder.encodeLines(outputStream.toByteArray)
      }
    } catch {
      case e@(_: ClassCastException | _: IOException) =>
        e.printStackTrace()
        null
    }
  }

  def deserializeFromBase64(serial: String): List[ItemStack] = {
    try {
      Using.resource(new ByteArrayInputStream(Base64Coder.decodeLines(serial))) { inputStream =>
        Using.resource(new BukkitObjectInputStream(inputStream)) { dataInput =>
          val length = dataInput.readInt
          (0 until length)
            .map(_ => dataInput.readObject().asInstanceOf[ItemStack])
            .toList
        }
      }
    } catch {
      case e@(_: ClassCastException | _: IOException | _: ClassNotFoundException) =>
        throw new IOException("Unable to decode class type.", e)
    }
  }
}
