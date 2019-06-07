package com.github.unchama.seichiassist.util

import arrow.core.Either
import com.github.unchama.seichiassist.text.Text
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * Created by karayuu on 2019/06/07
 */

/**
 * [Inventory] を作成します。
 * [InventoryType.CHEST] の [Inventory] を作成する場合は [Either.Left] に作成する [Inventory] の大きさを、
 * それ以外の [Inventory] を作成する場合は [Either.Right] に [InventoryType] を入れてください。
 */
fun createInventory(inventoryHolder: InventoryHolder, property: Either<Int, InventoryType>, title: Text): Inventory {
    return when (property) {
        is Either.Left -> {
            Bukkit.createInventory(inventoryHolder, property.a, title.stringValue())
        }
        is Either.Right -> {
            Bukkit.createInventory(inventoryHolder, property.b, title.stringValue())
        }
    }
}
