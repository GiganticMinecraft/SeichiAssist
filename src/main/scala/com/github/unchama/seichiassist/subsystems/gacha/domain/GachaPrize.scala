package com.github.unchama.seichiassist.subsystems.gacha.domain

import org.bukkit.inventory.ItemStack

/**
 * @param itemStack ガチャで排出されるアイテム。数もそのまま利用されます。
 */
case class GachaPrize(itemStack: ItemStack, probability: Double, owner: Option[String])
