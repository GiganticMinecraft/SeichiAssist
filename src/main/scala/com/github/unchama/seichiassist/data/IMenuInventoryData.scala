package com.github.unchama.seichiassist.data

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

trait IMenuInventoryData {
  /**
   * ログイン時間
   * @param page
   * @return
   */
  def getRankingByPlayingTime(page: Int): Inventory

  /**
   * 投票回数
   * @param page
   * @return
   */
  def getRankingByVotingCount(page: Int): Inventory

  /**
   * 二つ名組み合わせ
   * @param player
   * @return
   */
  def computeRefreshedCombineMenu(player: Player): Inventory

  /**
   * 二つ名、前パーツ
   * @param player
   * @return
   */
  def computeHeadPartCustomMenu(player: Player): Inventory

  /**
   * 二つ名、中パーツ
   * @param player
   * @return
   */
  def computeMiddlePartCustomMenu(player: Player): Inventory

  /**
   * 二つ名、後パーツ
   * @param player
   * @return
   */
  def computeTailPartCustomMenu(player: Player): Inventory

  /**
   * 実績ポイントショップ
   * @param player
   * @return
   */
  def computePartsShopMenu(player: Player): Inventory

  /**
   * 投票妖精メニュー
   * @param player
   * @return
   */
  def getVotingMenuData(player: Player): Inventory

  /**
   * GiganticBerserk進化設定
   * @param player
   * @return
   */
  def getGiganticBerserkBeforeEvolutionMenu(player: Player): Inventory

  /**
   * GiganticBerserk進化設定
   * @param player
   * @return
   */
  def getGiganticBerserkAfterEvolutionMenu(player: Player): Inventory
}
