package com.github.unchama.minecraft.paper

import java.util.stream.Collectors
import org.bukkit.inventory.{Inventory, InventoryHolder}
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryType

import java.util.List as juList

// TODO: import safe null language feature here.

/**
 * Spigot API -> PaperMCの移行で生じたdeprecation warningを吸収するためのshim
 */
object InlinedTextComponentBridger {
  extension (self: String) {
    def parseAsLegacyTextComponent: Component =
      LegacyComponentSerializer.legacySection().deserialize(self)
  }

  extension (self: Component) {
    def toLegacyText: String = LegacyComponentSerializer.legacySection().serialize(self)
  }

  extension (self: org.bukkit.Server) {
    def createInventoryAsLegacy(
      owner: InventoryHolder | Null,
      typ: InventoryType,
      title: String
    ): Inventory =
      self.createInventory(owner, typ, title.parseAsLegacyTextComponent)

    def createInventoryAsLegacy(
      owner: InventoryHolder | Null,
      rows: Int,
      title: String
    ): Inventory =
      self.createInventory(owner, rows, title.parseAsLegacyTextComponent)
  }

  extension (self: org.bukkit.inventory.InventoryView) {
    def getTitleAsLegacy: String = self.title().toLegacyText
  }

  extension (self: org.bukkit.inventory.meta.ItemMeta) {
    def setDisplayNameAsLegacy(s: String | Null): Unit = {
      if (s ne null) {
        self.displayName(s.parseAsLegacyTextComponent)
      } else {
        self.displayName(null)
      }
    }

    def setLoreAsLegacy(lines: juList[String] | Null): Unit = {
      if (lines.ne(null)) {
        self.lore(
          lines.stream().map(line => line.parseAsLegacyTextComponent).collect(Collectors.toList)
        )
      } else {
        self.lore(null)
      }
    }

    def getLoreAsLegacy: juList[String] | Null = {
      val loreComponents = self.lore()

      if (loreComponents ne null) {
        loreComponents.stream().map(c => c.toLegacyText).collect(Collectors.toList)
      } else {
        null
      }
    }
  }

}
