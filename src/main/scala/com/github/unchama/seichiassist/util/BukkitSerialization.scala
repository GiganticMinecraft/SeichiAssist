package com.github.unchama.seichiassist.util

import com.github.unchama.seichiassist.util.typeclass.OrderedCollection
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.util.io.{BukkitObjectInputStream, BukkitObjectOutputStream}
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}
import scala.util.Using

object BukkitSerialization {
  type Base64 = String

  @throws[IOException]
  def fromBase64forPocket(data: Base64): Inventory = {
    val items = BukkitSerialization.fromBase64(data)
    val inventory = Bukkit.getServer.createInventory(null, items.size, s"${ChatColor.DARK_PURPLE}${ChatColor.BOLD}4次元ポケット")
    items.zipWithIndex.foreach { case (item, i) =>
      inventory.setItem(i, item)
    }
    inventory
  }

  def fromBase64(serial: Base64): List[ItemStack] = {
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

  /**
   * シリアライズ形式:
   * {{{
   * +------+-----------+-----------+-----+-----------+
   * | size | items(0)  | items(1)  | ... | items(n)  |
   * +------+-----------+-----------+-----+-----------+
   * | Int  | ItemStack | ItemStack | ... | ItemStack |
   * +------+-----------+-----------+-----+-----------+
   * }}}
   * (ここで、`n`=`size-1`、`size=0`のときは`items`はシリアライズされない)
   */
  def toBase64(e: OrderedCollection[ItemStack]): Base64 = {
    try {
      Using.resource(new ByteArrayOutputStream) { outputStream =>
        Using.resource(new BukkitObjectOutputStream(outputStream)) {dataOutput =>
          dataOutput.writeInt(e.size)
          (0 until e.size)
            .foreach(i => dataOutput.writeObject(e(i)))
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
}
