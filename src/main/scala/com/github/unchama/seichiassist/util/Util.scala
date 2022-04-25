package com.github.unchama.seichiassist.util

import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.block.{Block, Skull}
import org.bukkit.entity.EntityType._
import org.bukkit.entity.{EntityType, LivingEntity, Player}
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.inventory.{ItemFlag, ItemStack}

import java.util.Calendar
import java.util.stream.IntStream

object Util {

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  def isGachaTicket(itemStack: ItemStack): Boolean = {
    val containsRightClickMessage: String => Boolean = _.contains(s"${GREEN}右クリックで使えます")

    if (itemStack.getType != Material.SKULL_ITEM) return false

    val skullMeta = itemStack.getItemMeta.asInstanceOf[SkullMeta]

    if (!(skullMeta.hasOwner && skullMeta.getOwner == "unchama")) return false

    skullMeta.hasLore && skullMeta.getLore.asScala.exists(containsRightClickMessage)
  }

  def itemStackContainsOwnerName(itemstack: ItemStack, name: String): Boolean = {
    val meta = itemstack.getItemMeta

    val lore: List[String] =
      if (meta.hasLore)
        meta.getLore.asScala.toList
      else
        Nil

    lore.exists(line =>
      line.contains("所有者：") && line.drop(line.indexOf("所有者：") + 4).toLowerCase == name
        .toLowerCase()
    )
  }

  def getPlayerDirection(player: Player): AbsoluteDirection = {
    var rotation = ((player.getLocation.getYaw + 180) % 360).toDouble

    if (rotation < 0) rotation += 360.0

    // 0,360:south 90:west 180:north 270:east
    if (0.0 <= rotation && rotation < 45.0) AbsoluteDirection.NORTH
    else if (45.0 <= rotation && rotation < 135.0) AbsoluteDirection.EAST
    else if (135.0 <= rotation && rotation < 225.0) AbsoluteDirection.SOUTH
    else if (225.0 <= rotation && rotation < 315.0) AbsoluteDirection.WEST
    else AbsoluteDirection.NORTH
  }

  def isVotingFairyPeriod(start: Calendar, end: Calendar): Boolean = {
    val cur = Calendar.getInstance()
    cur.after(start) && cur.before(end)
  }

  def setDifficulty(worldNameList: List[String], difficulty: Difficulty): Unit = {
    worldNameList.foreach { name =>
      val world = Bukkit.getWorld(name)

      if (world == null)
        Bukkit.getLogger.warning(name + "という名前のワールドは存在しません。")
      else
        world.setDifficulty(difficulty)
    }
  }

  def isEnemy(entityType: EntityType): Boolean = Set(
    BLAZE,
    CAVE_SPIDER,
    CREEPER,
    ELDER_GUARDIAN,
    ENDERMAN,
    ENDERMITE,
    EVOKER,
    GHAST,
    GUARDIAN,
    HUSK,
    MAGMA_CUBE,
    PIG_ZOMBIE,
    SHULKER,
    SILVERFISH,
    SKELETON,
    SLIME,
    SPIDER,
    STRAY,
    VEX,
    VINDICATOR,
    WITCH,
    WITHER_SKELETON,
    ZOMBIE,
    ZOMBIE_VILLAGER
  ).contains(entityType)

  def isMineHeadItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.CARROT_STICK &&
    loreIndexOf(itemstack.getItemMeta.getLore.asScala.toList, "頭を狩り取る形をしている...") >= 0
  }

  def getSkullDataFromBlock(block: Block): Option[ItemStack] = {
    if (block.getType != Material.SKULL) return None

    val skull = block.getState.asInstanceOf[Skull]
    val itemStack = new ItemStack(Material.SKULL_ITEM)

    // SkullTypeがプレイヤー以外の場合，SkullTypeだけ設定して終わり
    if (skull.getSkullType != SkullType.PLAYER) {
      val durability = skull.getSkullType match {
        case SkullType.CREEPER  => SkullType.CREEPER.ordinal.toShort
        case SkullType.DRAGON   => SkullType.DRAGON.ordinal.toShort
        case SkullType.SKELETON => SkullType.SKELETON.ordinal.toShort
        case SkullType.WITHER   => SkullType.WITHER.ordinal.toShort
        case SkullType.ZOMBIE   => SkullType.ZOMBIE.ordinal.toShort
        case _                  => itemStack.getDurability
      }
      return Some(itemStack.tap(_.setDurability(durability)))
    }
    // プレイヤーの頭の場合，ドロップアイテムからItemStackを取得．データ値をPLAYERにして返す
    Some(block.getDrops.asScala.head.tap(_.setDurability(SkullType.PLAYER.ordinal.toShort)))
  }

  /**
   * 指定された`String`が指定された[[ItemStack]]のloreに含まれているかどうか
   *
   * @param itemStack
   *   確認する`ItemStack`
   * @param sentence
   *   探す文字列
   * @return
   *   含まれていれば`true`、含まれていなければ`false`。ただし、`ItemStack`に`ItemMeta`と`Lore`のいずれかがなければfalse
   */
  def isContainedInLore(itemStack: ItemStack, sentence: String): Boolean =
    if (!itemStack.hasItemMeta || !itemStack.getItemMeta.hasLore) false
    else loreIndexOf(itemStack.getItemMeta.getLore.asScala.toList, sentence) >= 0

  /**
   * loreを捜査して、要素の中に`find`が含まれているかを調べる。
   *
   * @param lore
   *   探される対象
   * @param find
   *   探す文字列
   * @return
   *   見つかった場合はその添字、見つからなかった場合は-1
   */
  def loreIndexOf(lore: List[String], find: String): Int = {
    IntStream.range(0, lore.size).filter { i => lore(i).contains(find) }.findFirst().orElse(-1)
  }

  /**
   * 死亡したエンティティの死因が棘の鎧かどうか
   */
  def isEntityKilledByThornsEnchant(entity: LivingEntity): Boolean = {
    if (entity == null) return false
    val event = entity.getLastDamageCause
    if (event == null) return false

    event.getCause == DamageCause.THORNS
  }
}
