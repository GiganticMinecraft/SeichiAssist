package com.github.unchama.itemconversion

import cats.effect.IO
import cats.kernel.Monoid
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

final case class ConversionResultSet[A](convertedItems: Seq[ItemStack], unmodifiedItems: Seq[ItemStack], aggregationResult: A) {
  def giveEffect: TargetedEffect[Player] = {
    Util.grantItemStacksEffect[IO](convertedItems ++ unmodifiedItems: _*)
  }
}

object ConversionResultSet {
  implicit def monoid[A: Monoid]: Monoid[ConversionResultSet[A]] = Monoid.instance(ConversionResultSet(Nil, Nil, Monoid[A].empty), {
    // NOTE: DON'T annotate aggregationResult* with `A`; Doing it will emit "unchecked" warning by scalac
    case (ConversionResultSet(convertedItemsA, unmodifiedItemsA, aggregationResultA), ConversionResultSet(convertedItemsB, unmodifiedItemsB, aggregationResultB)) =>
      ConversionResultSet[A](convertedItemsA ++ convertedItemsB, unmodifiedItemsA ++ unmodifiedItemsB, Monoid[A].combine(aggregationResultA, aggregationResultB))
  })

  def apply[A: Monoid](convertedItems: Seq[ItemStack], unmodifiedItems: Seq[ItemStack]): ConversionResultSet[A] =
    ConversionResultSet(convertedItems, unmodifiedItems, Monoid[A].empty)

  def fromConvertedItemCount(convertedItems: Seq[ItemStack], unmodifiedItems: Seq[ItemStack]): ConversionResultSet[Int] =
    ConversionResultSet(convertedItems, unmodifiedItems, convertedItems.size)
}