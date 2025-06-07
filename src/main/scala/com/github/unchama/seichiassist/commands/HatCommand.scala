package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import org.bukkit.Material
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.ChatColor

object HatCommand {
  private val notWearable: Set[Material] = Set(
    // 武器
    Material.WOODEN_SWORD,
    Material.STONE_SWORD,
    Material.IRON_SWORD,
    Material.GOLDEN_SWORD,
    Material.DIAMOND_SWORD,
    Material.NETHERITE_SWORD,
    Material.BOW,
    Material.CROSSBOW,
    Material.TRIDENT,
    Material.WOODEN_AXE,
    Material.STONE_AXE,
    Material.IRON_AXE,
    Material.GOLDEN_AXE,
    Material.DIAMOND_AXE,
    Material.NETHERITE_AXE,
    Material.WOODEN_SHOVEL,
    Material.STONE_SHOVEL,
    Material.IRON_SHOVEL,
    Material.GOLDEN_SHOVEL,
    Material.DIAMOND_SHOVEL,
    Material.NETHERITE_SHOVEL,
    Material.WOODEN_PICKAXE,
    Material.STONE_PICKAXE,
    Material.IRON_PICKAXE,
    Material.GOLDEN_PICKAXE,
    Material.DIAMOND_PICKAXE,
    Material.NETHERITE_PICKAXE,
    Material.WOODEN_HOE,
    Material.STONE_HOE,
    Material.IRON_HOE,
    Material.GOLDEN_HOE,
    Material.DIAMOND_HOE,
    Material.NETHERITE_HOE,
    // 防具
    Material.LEATHER_CHESTPLATE,
    Material.LEATHER_LEGGINGS,
    Material.LEATHER_BOOTS,
    Material.IRON_CHESTPLATE,
    Material.IRON_LEGGINGS,
    Material.IRON_BOOTS,
    Material.GOLDEN_CHESTPLATE,
    Material.GOLDEN_LEGGINGS,
    Material.GOLDEN_BOOTS,
    Material.DIAMOND_CHESTPLATE,
    Material.DIAMOND_LEGGINGS,
    Material.DIAMOND_BOOTS,
    Material.NETHERITE_CHESTPLATE,
    Material.NETHERITE_LEGGINGS,
    Material.NETHERITE_BOOTS,
    Material.CHAINMAIL_CHESTPLATE,
    Material.CHAINMAIL_LEGGINGS,
    Material.CHAINMAIL_BOOTS,
    // 盾
    Material.SHIELD
  )

  val executor: TabExecutor = BuilderTemplates
    .playerCommandBuilder
    .buildWith { context =>
      val player = context.sender
      val mainHandItem = player.getInventory.getItemInMainHand
      val currentHeadItem = player.getInventory.getHelmet

      IO {
        if (mainHandItem.getType == Material.AIR) {
          emptyEffect
        } else if (notWearable.contains(mainHandItem.getType())) {
          MessageEffect(s"${ChatColor.RED}そのアイテムは頭にかぶることができません。")
        } else {
          SequentialEffect(
            TargetedEffect.delay[IO, Player] { p =>
              // swapすることでアイテムの過不足を防ぐ
              p.getInventory.setHelmet(mainHandItem)
              p.getInventory.setItemInMainHand(currentHeadItem)
            },
            MessageEffect("メインハンドに持っていたアイテムを頭にかぶりました。")
          )
        }
      }
    }
    .asNonBlockingTabExecutor()
}
