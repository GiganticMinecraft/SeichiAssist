package com.github.unchama.itemconversionstorage

import cats.effect.IO
import cats.kernel.Monoid
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

case class ConversionResultSet(convertedItems: Seq[ItemStack], unmodifiedItems: Seq[ItemStack]) {
  def giveEffect: TargetedEffect[Player] = {
    Util.grantItemStacksEffect[IO](convertedItems ++ unmodifiedItems: _*)
  }

  def convertedCount: Int = convertedItems.size
}

object ConversionResultSet {
  implicit val monoid: Monoid[ConversionResultSet] = Monoid.instance(ConversionResultSet(Nil, Nil), {
    case (ConversionResultSet(convertedItemsA, unmodifiedItemsA), ConversionResultSet(convertedItemsB, unmodifiedItemsB)) =>
      ConversionResultSet(convertedItemsA ++ convertedItemsB, unmodifiedItemsA ++ unmodifiedItemsB)
  })
}