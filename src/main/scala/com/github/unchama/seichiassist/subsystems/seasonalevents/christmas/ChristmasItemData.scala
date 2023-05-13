package com.github.unchama.seichiassist.subsystems.seasonalevents.christmas

import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.Christmas.EVENT_YEAR
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Color.fromRGB
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.meta.{PotionMeta, SkullMeta}
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.potion.{PotionEffect, PotionEffectType}
import org.bukkit.{Bukkit, Material}

import scala.collection.immutable.{List, Set}
import scala.jdk.CollectionConverters._
import scala.util.chaining._

object ChristmasItemData {

  private val christmasItemBaseLore = List("", s"$RESET$GRAY${EVENT_YEAR}クリスマスイベント限定品", "")

  // region ChristmasCake

  private def christmasCakePieceLore(remainingPieces: Int) =
    List(s"$RESET${GRAY}残り摂食可能回数: $remainingPieces/${christmasCakeDefaultPieces}回")

  val christmasCakeDefaultPieces = 7

  def christmasCake(pieces: Int): ItemStack = {
    val itemFlags = Set(ItemFlag.HIDE_ENCHANTS)
    val loreList = {
      val cakeBaseLore: List[String] =
        List("置かずに食べられます", "食べると不運か幸運がランダムで付与されます").map(str => s"$RESET$YELLOW$str")

      christmasItemBaseLore ::: cakeBaseLore ::: christmasCakePieceLore(pieces)
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.CAKE).tap { meta =>
      import meta._
      setDisplayName(s"$AQUA${ITALIC}まいんちゃんお手製クリスマスケーキ")
      addEnchant(Enchantment.MENDING, 1, true)
      setLore(loreList)
      itemFlags.foreach(flg => addItemFlags(flg))
    }

    val cake = new ItemStack(Material.CAKE, 1)
    cake.setItemMeta(itemMeta)

    new NBTItem(cake)
      .tap { nbtItem =>
        import nbtItem._
        setByte(NBTTagConstants.typeIdTag, 1.toByte)
        setByte(NBTTagConstants.cakePieceTag, pieces.toByte)
      }
      .pipe(_.getItem)
  }

  def isChristmasCake(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType == Material.CAKE && {
      new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag) == 1
    }

  // endregion

  // region ChristmasTurkey

  val christmasTurkey: ItemStack = {
    val itemFlags = Set(ItemFlag.HIDE_ENCHANTS)
    val loreList = {
      val lore = List(s"$RESET${YELLOW}食べると移動速度上昇か低下がランダムで付与されます")

      christmasItemBaseLore ::: lore
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKED_CHICKEN).tap { meta =>
      import meta._
      setDisplayName(s"$AQUA${ITALIC}まいんちゃんお手製ローストターキー")
      addEnchant(Enchantment.MENDING, 1, true)
      setLore(loreList)
      itemFlags.foreach(flg => addItemFlags(flg))
    }

    val turkey = new ItemStack(Material.COOKED_CHICKEN, 1)
    turkey.setItemMeta(itemMeta)

    new NBTItem(turkey)
      .tap { nbtItem =>
        import nbtItem._
        setByte(NBTTagConstants.typeIdTag, 2.toByte)
      }
      .pipe(_.getItem)
  }

  def isChristmasTurkey(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType == Material.COOKED_CHICKEN && {
      new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag) == 2
    }

  // endregion

  // region ChristmasPotion

  val christmasPotion: ItemStack = {
    val itemFlags = Set(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)
    val potionEffects = Set(
      new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 30, 0),
      new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 0)
    )
    val loreList = {
      val lore = List(s"$RESET${YELLOW}クリスマスを一人で過ごす鯖民たちの涙（血涙）を集めた瓶")

      christmasItemBaseLore ::: lore
    }.asJava

    val potionMeta =
      Bukkit.getItemFactory.getItemMeta(Material.POTION).asInstanceOf[PotionMeta].tap { meta =>
        import meta._
        setDisplayName(s"$AQUA${ITALIC}みんなの涙")
        setColor(fromRGB(215, 0, 58))
        addEnchant(Enchantment.MENDING, 1, true)
        setLore(loreList)
        itemFlags.foreach(flg => addItemFlags(flg))
        potionEffects.foreach(effect => addCustomEffect(effect, true))
      }

    val potion = new ItemStack(Material.POTION, 1)
    potion.setItemMeta(potionMeta)

    new NBTItem(potion).tap(_.setByte(NBTTagConstants.typeIdTag, 3.toByte)).pipe(_.getItem)
  }

  def isChristmasPotion(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType == Material.POTION && {
      new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag) == 3
    }

  // endregion

  // region ChristmasChestPlate

  val christmasChestPlate: ItemStack = {
    val enchants = Set(Enchantment.MENDING, Enchantment.DURABILITY)
    val loreList = {
      val lore =
        List("特殊エンチャント：迷彩 I", "敵から気づかれにくくなります", "「鮮やかに、キメろ。」").map(str => s"$RESET$WHITE$str")

      christmasItemBaseLore ::: lore
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.DIAMOND_CHESTPLATE).tap { meta =>
      import meta._
      setDisplayName(s"$AQUA${ITALIC}迷彩服（胴）")
      setLore(loreList)
      enchants.foreach(ench => addEnchant(ench, 1, true))
    }

    val chestPlate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1)
    chestPlate.setItemMeta(itemMeta)

    new NBTItem(chestPlate)
      .tap { nbtItem =>
        import nbtItem._
        setByte(NBTTagConstants.typeIdTag, 4.toByte)
        setByte(NBTTagConstants.camouflageEnchLevelTag, 1.toByte)
      }
      .pipe(_.getItem)
  }

  def isChristmasChestPlate(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType == Material.DIAMOND_CHESTPLATE && {
      new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag) == 4
    }

  def calculateStandardDistance(enchLevel: Int, enemyType: EntityType): Double = {
    val rate = enchLevel match {
      case 1 => 0.9
      case 2 => 0.8
      case 3 => 0.5
      case 4 => 0.3
      case 5 => 0.1
      case _ => throw new IllegalArgumentException("不正なエンチャントレベルが指定されました。")
    }
    val isZombie = enemyType == EntityType.ZOMBIE || enemyType == EntityType.ZOMBIE_VILLAGER

    (if (isZombie) 40 else 20) * rate
  }

  // endregion

  // region ChristmasSock

  val christmasSock: ItemStack = {
    val loreList = List(
      "Merry Christmas!",
      s"クリスマスをお祝いして$RED${UNDERLINE}靴下${RESET}をどうぞ！",
      s"$RED${UNDERLINE}アルカディア、エデン、ヴァルハラサーバー メインワールドの",
      s"$RED${UNDERLINE}スポーン地点にいる村人に欲しい物を詰めてもらおう！"
    ).map(str => s"$RESET$str").asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.INK_SAC).tap { meta =>
      import meta._
      setDisplayName(s"${AQUA}靴下(${EVENT_YEAR}年)")
      setLore(loreList)
      addEnchant(Enchantment.DIG_SPEED, 1, true)
      addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    // 赤の染料
    val itemStack = new ItemStack(Material.INK_SAC, 1).tap { itemStack =>
      import itemStack._
      setDurability(1.toShort)
      setItemMeta(itemMeta)
    }

    itemStack
  }

  // endregion

  val christmasMebiusLore: List[String] =
    List("", s"$RESET${WHITE}Merry Christmas! あなたに特別なMebiusを")

  // SeichiAssistで呼ばれてるだけ
  def christmasPlayerHead(head: SkullMeta): SkullMeta = {
    val lore = List(
      "",
      s"$GREEN${ITALIC}大切なあなたへ。",
      s"$YELLOW$UNDERLINE${ITALIC}Merry Christmas $EVENT_YEAR"
    ).map(str => s"$RESET$str").asJava
    head.setLore(lore)
    head
  }

  object NBTTagConstants {
    val typeIdTag = "christmasItemTypeId"
    val cakePieceTag = "christmasCakePiece"
    val camouflageEnchLevelTag = "camouflageEnchLevel"
  }

}
