package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import com.github.unchama.itemmigration.bukkit.util.MigrationHelper
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._

/**
 * 2021年に行われた5周年記念イベントの中の、21億企画の目標達成報酬として配布されたアイテムに修繕と耐久力5エンチャントを付与する
 */
object V1_4_0_AddEnchantsTo2_1billionRewardItems {

  // 年の判別用
  private val commonLore = s"${WHITE}21/07/22に開催された5周年記念企画"

  // アイテム名だけだと、21億企画報酬ではなく以前のイベントで交換可能だった当該アイテムも含んでしまいかねないので、説明文も確認する
  private val titanReplicaName = Seq(
    RED -> "T",
    GOLD -> "I",
    YELLOW -> "T",
    GREEN -> "A",
    BLUE -> "N ", // スペースは故意
    WHITE -> "Replica II"
  ).map { case (color, str) => s"$color$BOLD$ITALIC$str" }.mkString
  private val titanReplicaLore = s"${WHITE}30億/1日を突破した記念に配布されたものです。"

  private val gaeaReplicaName = Seq(
    RED -> "G",
    GOLD -> "A",
    YELLOW -> "E",
    GREEN -> "A ", // スペースは故意
    WHITE -> "Whiteday Replica"
  ).map { case (color, str) => s"$color$BOLD$ITALIC$str" }.mkString
  private val gaeaReplicaLore = s"${WHITE}35億/1日を突破した記念に配布されたものです。"

  import eu.timepit.refined.auto._

  def migration: ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 4, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )

  def migrationFunction(itemStack: ItemStack): ItemStack = {
    if (!is21billionRewardItems(itemStack)) return itemStack

    import scala.util.chaining._

    itemStack.clone().tap { item =>
      import item._

      addEnchantment(Enchantment.MENDING, 1)

      // Durabilityはバニラ範囲では5に到達しないためunsafeに付与すべき
      addUnsafeEnchantment(Enchantment.DURABILITY, 5)
    }
  }

  def is21billionRewardItems(itemStack: ItemStack): Boolean = {
    if (
      itemStack == null || !itemStack.hasItemMeta || !itemStack
        .getItemMeta
        .hasDisplayName || !itemStack.getItemMeta.hasLore
    ) return false
    isRewardedGaeaReplica(itemStack) || isRewardedTitanReplica(itemStack)
  }

  def isRewardedTitanReplica(item: ItemStack): Boolean = {
    val lores = item.getItemMeta.getLore.asScala
    item.getItemMeta.getDisplayName == titanReplicaName && lores.contains(
      titanReplicaLore
    ) && lores.contains(commonLore)
  }

  def isRewardedGaeaReplica(item: ItemStack): Boolean = {
    val lores = item.getItemMeta.getLore.asScala
    item.getItemMeta.getDisplayName == gaeaReplicaName && lores.contains(
      gaeaReplicaLore
    ) && lores.contains(commonLore)
  }
}
