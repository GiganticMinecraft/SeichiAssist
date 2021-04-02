package com.github.unchama.seichiassist.data
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

object MenuInventoryDataInScala extends IMenuInventoryData {
  override def getRankingByPlayingTime(page: Int): Inventory = ???

  override def getRankingByVotingCount(page: Int): Inventory = ???

  override def computeRefreshedCombineMenu(player: Player): Inventory = ???

  override def computeHeadPartCustomMenu(player: Player): Inventory = ???

  override def computeMiddlePartCustomMenu(player: Player): Inventory = ???

  override def computeTailPartCustomMenu(player: Player): Inventory = ???

  override def computePartsShopMenu(player: Player): Inventory = ???

  override def getVotingMenuData(player: Player): Inventory = ???

  override def getGiganticBerserkBeforeEvolutionMenu(player: Player): Inventory = ???

  override def getGiganticBerserkAfterEvolutionMenu(player: Player): Inventory = ???
}
