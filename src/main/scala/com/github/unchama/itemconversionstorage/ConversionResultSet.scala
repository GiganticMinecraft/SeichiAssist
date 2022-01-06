package com.github.unchama.itemconversionstorage

import cats.effect.IO
import cats.kernel.Monoid
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

sealed trait ConversionResultSet {
  def convertedItems: Seq[ItemStack]
  def unmodifiedItems: Seq[ItemStack]
  final def giveEffect: TargetedEffect[Player] = {
    Util.grantItemStacksEffect[IO](convertedItems ++ unmodifiedItems: _*)
  }
}

object ConversionResultSet {
  // TODO: もしかしてこれってAdditionalAggregate[Int]では？
  case class Plane(convertedItems: Seq[ItemStack], unmodifiedItems: Seq[ItemStack]) {
    def convertedCount: Int = convertedItems.size
  }
  
  object Plane {
    implicit val monoid: Monoid[Plane] = Monoid.instance(Plane(Nil, Nil), {
      case (Plane(convertedItemsA, unmodifiedItemsA), Plane(convertedItemsB, unmodifiedItemsB)) =>
        Plane(convertedItemsA ++ convertedItemsB, unmodifiedItemsA ++ unmodifiedItemsB)
    })
  }
  
  case class AdditionalAggregate[A](convertedItems: Seq[ItemStack], unmodifiedItems: Seq[ItemStack], aggregationResult: A)
  
  object AdditionalAggregate {
    implicit def monoid[A: Monoid]: Monoid[AdditionalAggregate[A]] = Monoid.instance(AdditionalAggregate(Nil, Nil, Monoid[A].empty), {
      case (AdditionalAggregate(convertedItemsA, unmodifiedItemsA, aggregationResultA: A), AdditionalAggregate(convertedItemsB, unmodifiedItemsB, aggregationResultB: A)) =>
        AdditionalAggregate[A](convertedItemsA ++ convertedItemsB, unmodifiedItemsA ++ unmodifiedItemsB, Monoid[A].combine(aggregationResultA, aggregationResultB))
    }) 
  }
}