package com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary

import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerTextureValue}
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.Anniversary.ANNIVERSARY_COUNT
import com.github.unchama.seichiassist.util.EnchantNameToJapanese
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.Material._
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.meta.BookMeta.Generation
import org.bukkit.inventory.meta.{BookMeta, ItemMeta, SkullMeta}
import org.bukkit.inventory.{ItemFlag, ItemStack}

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object AnniversaryItemData {

  // region まいんちゃんの記念頭

  private val mineChan = SkullOwnerTextureValue(
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDhmNTQ0OGI0ZDg4ZTQwYjE0YzgyOGM2ZjFiNTliMzg1NDVkZGE5MzNlNzNkZmYzZjY5NWU2ZmI0Mjc4MSJ9fX0="
  )
  val mineHead: ItemStack = new SkullItemStackBuilder(mineChan)
    .title("まいんちゃん")
    .lore("", s"${YELLOW}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年記念だよ！")
    .build()

  // endregion

  // region 「気になる木」の苗

  val strangeSapling: ItemStack = {
    val loreList = List("", "植えるとすぐ成長する。", "先端のブロックがランダムで変化する。", "極稀にあの「林檎」も...？")
      .map(lore => s"$RESET$GRAY$lore")
      .asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(OAK_SAPLING).tap { meta =>
      import meta._
      setDisplayName(s"$GOLD${BOLD}「気になる木」の苗")
      setLore(loreList)
    }

    val itemStack = new ItemStack(OAK_SAPLING, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap { item =>
        import item._
        setByte(NBTTagConstants.typeIdTag, 1.toByte)
      }
      .pipe(_.getItem)
  }

  val strangeSaplingBlockSet = Set(
    ANVIL,
    BEACON,
    BONE_BLOCK,
    BOOKSHELF,
    BRICK,
    CAKE,
    CAULDRON,
    COAL_BLOCK,
    DIAMOND_BLOCK,
    DRAGON_EGG,
    OAK_FENCE,
    JUNGLE_FENCE,
    ACACIA_FENCE,
    BIRCH_FENCE,
    DARK_OAK_FENCE,
    FLOWER_POT,
    GLOWSTONE,
    GOLD_BLOCK,
    GRASS,
    ICE,
    IRON_BLOCK,
    MELON,
    NETHER_BRICK,
    QUARTZ_BLOCK,
    SAND,
    SPONGE,
    CRAFTING_TABLE
  )

  val strangeSaplingSiinaRate = 0.0008

  def isStrangeSapling(item: ItemStack): Boolean =
    item != null && item.getType == OAK_SAPLING && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }

  // endregion

  // region 修繕の書

  val mendingBook: ItemStack = {
    val loreList = List("", "手に持って右クリックすると", "オフハンドにあるアイテムの耐久値を全回復する")
      .map(lore => s"$RESET$GRAY$lore")
      .asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(WRITTEN_BOOK).tap { meta =>
      import meta._
      setDisplayName(s"$GOLD${BOLD}修繕の書")
      setLore(loreList)
    }

    val itemStack = new ItemStack(WRITTEN_BOOK, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap { item =>
        import item._
        setByte(NBTTagConstants.typeIdTag, 2.toByte)
      }
      .pipe(_.getItem)
  }

  def isMendingBook(item: ItemStack): Boolean = {
    def isOriginal(meta: ItemMeta) =
      meta match {
        case bookMeta: BookMeta =>
          bookMeta.hasGeneration && bookMeta.getGeneration == Generation.ORIGINAL
        case _ => false
      }

    item != null && item.getType == WRITTEN_BOOK && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 2
    } && item.hasItemMeta && isOriginal(item.getItemMeta)
  }

  // endregion

  // region 記念限定シャベル

  val anniversaryShovel: ItemStack = {
    val enchantments =
      Set((Enchantment.DIG_SPEED, 3), (Enchantment.DURABILITY, 4), (Enchantment.MENDING, 1))

    val loreList = {
      val enchDescription = enchantments.map {
        case (ench, lvl) =>
          s"$GRAY${EnchantNameToJapanese.getEnchantName(ench.getKey.getKey, lvl)}"
      }.toList
      val lore = List("", "特殊なエンチャントが付与されています").map(lore => s"$YELLOW$lore")

      enchDescription ::: lore
    }.map(lore => s"$RESET$lore").asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(DIAMOND_SHOVEL).tap { meta =>
      import meta._
      setDisplayName(s"$GOLD${BOLD}SCARLET")
      setLore(loreList)
      addItemFlags(ItemFlag.HIDE_ENCHANTS)
      enchantments.foreach { case (ench, lvl) => addEnchant(ench, lvl, true) }
    }

    val itemStack = new ItemStack(DIAMOND_SHOVEL, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap { item =>
        import item._
        setByte(NBTTagConstants.typeIdTag, 3.toByte)
      }
      .pipe(_.getItem)
  }

  def isAnniversaryShovel(item: ItemStack): Boolean =
    item != null && item.getType == DIAMOND_SHOVEL && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 3
    }

  // endregion

  // SeichiAssistで呼ばれてるだけ
  def anniversaryPlayerHead(head: SkullMeta): SkullMeta = {
    val lore = List(
      "",
      s"$GREEN${ITALIC}大切なあなたへ感謝を。",
      s"$YELLOW$UNDERLINE$ITALIC${ANNIVERSARY_COUNT}th Anniversary"
    ).map(str => s"$RESET$str").asJava
    head.setLore(lore)
    head
  }

  private object NBTTagConstants {
    val typeIdTag = "anniversaryItemTypeId"
  }

}
