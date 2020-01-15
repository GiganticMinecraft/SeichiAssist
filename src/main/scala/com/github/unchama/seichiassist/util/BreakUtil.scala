package com.github.unchama.seichiassist.util

import java.util.Random
import java.util.stream.IntStream

import cats.effect.IO
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{Entity, EntityType, Player}
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Dye


object BreakUtil {

  import ManagedWorld._

  //他のプラグインの影響があってもブロックを破壊できるのか
  def canBreak(player: Player, breakblockOption: Option[Block]): Boolean = {
    val breakblock = breakblockOption.getOrElse(return false)
    if (!player.isOnline) return false

    val playermap = SeichiAssist.playermap
    val uuid = player.getUniqueId
    val playerdata = playermap(uuid)

    //壊されるブロックのMaterialを取得
    val material = breakblock.getType

    //壊されるブロックがワールドガード範囲だった場合処理を終了
    if (!ExternalPlugins.getWorldGuard.canBuild(player, breakblock.getLocation)) {
      if (playerdata.settings.shouldDisplayWorldGuardLogs) {
        player.sendMessage(RED.toString + "ワールドガードで保護されています。")
      }
      return false
    }

    if (!equalsIgnoreNameCaseWorld(player.getWorld.getName)) {
      val wrapper = ExternalPlugins.getCoreProtectWrapper
      if (wrapper == null) {
        Bukkit.getLogger.warning("CoreProtectにアクセスできませんでした。")
      } else {
        val failure = !wrapper.queueBlockRemoval(player, breakblock)
        //もし失敗したらプレイヤーに報告し処理を終了
        if (failure) {
          player.sendMessage(RED.toString + "coreprotectに保存できませんでした。管理者に報告してください。")
          return false
        }
      }
    }

    if (material == Material.CHEST || material == Material.TRAPPED_CHEST) {
      if (!playerdata.chestflag) {
        player.sendMessage(RED.toString + "スキルでのチェスト破壊は無効化されています")
        return false
      } else if (!Util.isSeichiWorld(player)) {
        player.sendMessage(RED.toString + "スキルでのチェスト破壊は整地ワールドでのみ有効です")
        return false
      }
    }

    if (breakblock.getWorld.asManagedWorld().exists(_.isSeichi)) {
      val isBlockY5Step = material == Material.STEP && breakblock.getY == 5 && breakblock.getData == 0.toByte

      if (isBlockY5Step && !playerdata.canBreakHalfBlock) return false
    }

    true
  }

  private def equalsIgnoreNameCaseWorld(name: String): Boolean = {
    val world = ManagedWorld.fromName(name).getOrElse(return false)

    world.shouldMuteCoreProtect
  }

  //ブロックを破壊する処理、ドロップも含む、統計増加も含む
  def breakBlock(player: Player,
                 targetBlock: Block,
                 dropLocation: Location,
                 tool: ItemStack,
                 shouldPlayBreakSound: Boolean): Unit =
    unsafe.runIOAsync(
      "単一ブロックを破壊する",
      massBreakBlock(player, Set(targetBlock), dropLocation, tool, shouldPlayBreakSound)
    )

  /**
   * ブロックの書き換えを行い、ドロップ処理と統計増加の処理を行う`IO`を返す。
   *
   * 返される`IO`は、終了時点で同期スレッドで実行を行っている。
   * @return
   */
  def massBreakBlock(player: Player,
                     targetBlocks: Iterable[Block],
                     dropLocation: Location,
                     miningTool: ItemStack,
                     shouldPlayBreakSound: Boolean,
                     toMaterial: Material = Material.AIR): IO[Unit] =
    for {
      _ <- PluginExecutionContexts.syncShift.shift

      materialFilteredBlocks <- IO { targetBlocks.filter(b => MaterialSets.materials.contains(b.getType)).toList }

      // 非同期実行ではワールドに触れないので必要な情報をすべて抜く
      targetBlocksInformation <- IO {
        materialFilteredBlocks.map(block => (block.getLocation.clone(), block.getType, block.getData))
      }

      // ブロックをすべて[[toMaterial]]に変える
      _ <- IO {
        materialFilteredBlocks.foreach(_.setType(toMaterial))
      }

      _ <- PluginExecutionContexts.asyncShift.shift

      _ <- IO {
        // 壊した時の音を再生する
        if (shouldPlayBreakSound) {
          targetBlocksInformation.foreach { case (location, material, _) =>
            dropLocation.getWorld.playEffect(location, Effect.STEP_SOUND, material)
          }
        }
      }
      _ <- IO {
        //プレイヤーの統計を増やす
        targetBlocksInformation.map { case (_, m, _) => m }
          .map {
            case Material.GLOWING_REDSTONE_ORE => Material.REDSTONE_ORE
            case others@_ => others
          }
          .filter {
            case Material.GRASS_PATH | Material.SOIL | Material.MOB_SPAWNER | Material.ENDER_PORTAL_FRAME | Material.ENDER_PORTAL => false
            case _ => true
          }
          .foreach(player.incrementStatistic(Statistic.MINE_BLOCK, _))
      }
      itemsToBeDropped <- IO {
        // アイテムのマインスタック自動格納を試みる
        targetBlocksInformation
          .flatMap(dropItemOnTool(miningTool))
          .flatMap { itemStack =>
            if (!addItemToMineStack(player, itemStack)) {
              Some(itemStack)
            } else {
              None
            }
          }
      }

      _ <- PluginExecutionContexts.syncShift.shift

      _ <- IO {
        // アイテムドロップは非同期スレッドで行ってはならない
        itemsToBeDropped.foreach(dropLocation.getWorld.dropItemNaturally(dropLocation, _))
      }
    } yield ()

  def addItemToMineStack(player: Player, itemstack: ItemStack): Boolean = {
    //もしサバイバルでなければ処理を終了
    if (player.getGameMode != GameMode.SURVIVAL) return false

    if (SeichiAssist.DEBUG) {
      player.sendMessage(s"${RED}minestackAdd:$itemstack")
      player.sendMessage(s"${RED}mineDurability:${itemstack.getDurability}")
    }

    val config = SeichiAssist.seichiAssistConfig

    val playerData = SeichiAssist.playermap(player.getUniqueId)

    //minestackflagがfalseの時は処理を終了
    if (!playerData.settings.autoMineStack) return false

    val amount = itemstack.getAmount
    val material = itemstack.getType

    //線路・キノコなどの、拾った時と壊した時とでサブIDが違う場合の処理
    //拾った時のサブIDに合わせる
    if (itemstack.getType == Material.RAILS
      || itemstack.getType == Material.HUGE_MUSHROOM_1
      || itemstack.getType == Material.HUGE_MUSHROOM_2
      || itemstack.getType == Material.PURPUR_STAIRS
      || itemstack.getType == Material.BONE_BLOCK) {

      itemstack.setDurability(0.toShort)
    }

    MineStackObjectList.minestacklist.foreach { mineStackObj =>
      def addToMineStackAfterLevelCheck(): Boolean =
        if (playerData.level < config.getMineStacklevel(mineStackObj.level)) {
          false
        } else {
          playerData.minestack.addStackedAmountOf(mineStackObj, amount.toLong)
          true
        }

      //IDとサブIDが一致している
      if (material == mineStackObj.material && itemstack.getDurability.toInt == mineStackObj.durability) {
        //名前と説明文が無いアイテム
        if (!mineStackObj.hasNameLore && !itemstack.getItemMeta.hasLore && !itemstack.getItemMeta.hasDisplayName) {
          return addToMineStackAfterLevelCheck()
        } else if (mineStackObj.hasNameLore && itemstack.getItemMeta.hasDisplayName && itemstack.getItemMeta.hasLore) {
          //ガチャ以外のアイテム(がちゃりんご)
          if (mineStackObj.gachaType == -1) {
            if (!itemstack.isSimilar(StaticGachaPrizeFactory.getGachaRingo)) return false

            return addToMineStackAfterLevelCheck()
          } else {
            //ガチャ品
            val g = SeichiAssist.msgachadatalist(mineStackObj.gachaType)

            //名前が記入されているはずのアイテムで名前がなければ
            if (g.probability < 0.1 && !Util.itemStackContainsOwnerName(itemstack, player.getName)) return false

            if (g.itemStackEquals(itemstack)) {
              return addToMineStackAfterLevelCheck()
            }
          }
        }
      }
    }

    false
  }

  private def dropItemOnTool(tool: ItemStack)(blockInformation: (Location, Material, Byte)): Option[ItemStack] = {
    val fortuneLevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)

    val (blockLocation, blockMaterial, blockData) = blockInformation

    blockMaterial match {
      case Material.GRASS_PATH | Material.SOIL => return Some(new ItemStack(Material.DIRT))
      case Material.MOB_SPAWNER | Material.ENDER_PORTAL_FRAME | Material.ENDER_PORTAL => return None
      case _ =>
    }

    val rand = Math.random()
    val bonus = Math.max(1, rand * (fortuneLevel + 2) - 1).toInt

    val blockDataLeast4Bits = (blockData & 0x0F).toByte
    val b_tree = (blockData & 0x03).toByte

    val silkTouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH)

    if (silkTouch > 0) {
      //シルクタッチの処理
      blockMaterial match {
        case Material.GLOWING_REDSTONE_ORE =>
          Some(new ItemStack(Material.REDSTONE_ORE))
        case Material.LOG | Material.LOG_2 | Material.LEAVES | Material.LEAVES_2 =>
          Some(new ItemStack(blockMaterial, 1, b_tree.toShort))
        case Material.MONSTER_EGGS =>
          Some(new ItemStack(Material.STONE))
        case _ =>
          Some(new ItemStack(blockMaterial, 1, blockDataLeast4Bits.toShort))
      }
    } else if (fortuneLevel > 0 && MaterialSets.luckMaterials.contains(blockMaterial)) {
      //幸運の処理
      blockMaterial match {
        case Material.COAL_ORE =>
          Some(new ItemStack(Material.COAL, bonus))
        case Material.DIAMOND_ORE =>
          Some(new ItemStack(Material.DIAMOND, bonus))
        case Material.LAPIS_ORE =>
          val dye = new Dye()
          dye.setColor(DyeColor.BLUE)

          val withBonus = bonus * (rand * 4 + 4).toInt
          Some(dye.toItemStack(withBonus))
        case Material.EMERALD_ORE =>
          Some(new ItemStack(Material.EMERALD, bonus))
        case Material.REDSTONE_ORE | Material.GLOWING_REDSTONE_ORE =>
          val withBonus = bonus * (rand + 4).toInt
          Some(new ItemStack(Material.REDSTONE, withBonus))
        case Material.QUARTZ_ORE =>
          Some(new ItemStack(Material.QUARTZ, bonus))
        case _ =>
          // TODO this section is unreachable
          Some(new ItemStack(blockMaterial, bonus))
      }
    } else {
      //シルク幸運なしの処理
      blockMaterial match {
        case Material.COAL_ORE =>
          Some(new ItemStack(Material.COAL))
        case Material.DIAMOND_ORE =>
          Some(new ItemStack(Material.DIAMOND))
        case Material.LAPIS_ORE =>
          val dye = new Dye()
          dye.setColor(DyeColor.BLUE)
          Some(dye.toItemStack((rand * 4 + 4).toInt))
        case Material.EMERALD_ORE =>
          Some(new ItemStack(Material.EMERALD))
        case Material.REDSTONE_ORE | Material.GLOWING_REDSTONE_ORE =>
          Some(new ItemStack(Material.REDSTONE, (rand + 4).toInt))
        case Material.QUARTZ_ORE => Some(new ItemStack(Material.QUARTZ))
        case Material.STONE =>
          //Material.STONEの処理
          if (blockData.toInt == 0x00) {
            //焼き石の処理
            Some(new ItemStack(Material.COBBLESTONE))
          } else {
            //他の石の処理
            Some(new ItemStack(blockMaterial, 1, blockDataLeast4Bits.toShort))
          }
        case Material.GRASS => Some(new ItemStack(Material.DIRT))
        case Material.GRAVEL =>
          val p = fortuneLevel match {
            case 1 => 0.14
            case 2 => 0.25
            case 3 => 1.00
            case _ => 0.1
          }
          val dropMaterial = if (p > rand) Material.FLINT else Material.GRAVEL

          Some(new ItemStack(dropMaterial, bonus))
        case Material.LEAVES | Material.LEAVES_2 => None
        case Material.CLAY => Some(new ItemStack(Material.CLAY_BALL, 4))
        case Material.MONSTER_EGGS =>
          blockLocation.getWorld.spawnEntity(blockLocation, EntityType.SILVERFISH)
          None
        case Material.LOG | Material.LOG_2 => Some(new ItemStack(blockMaterial, 1, b_tree.toShort))
        case _ =>
          //breakblcokのままのアイテムスタックを保存
          Some(new ItemStack(blockMaterial, 1, blockDataLeast4Bits.toShort))
      }
    }
  }

  def calcManaDrop(playerdata: PlayerData): Double = {
    //０～１のランダムな値を取得
    val rand = Math.random()
    //10%の確率で経験値付与
    if (rand < 0.1) {
      //Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
      if (playerdata.level < 8 || !playerdata.activeskilldata.skillcanbreakflag) {
        0.0
      } else if (playerdata.level < 18) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(1)
      } else if (playerdata.level < 28) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(2)
      } else if (playerdata.level < 38) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(3)
      } else if (playerdata.level < 48) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(4)
      } else if (playerdata.level < 58) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(5)
      } else if (playerdata.level < 68) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(6)
      } else if (playerdata.level < 78) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(7)
      } else if (playerdata.level < 88) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(8)
      } else if (playerdata.level < 98) {
        SeichiAssist.seichiAssistConfig.getDropExplevel(9)
      } else {
        SeichiAssist.seichiAssistConfig.getDropExplevel(10)
      }
    } else {
      0.0
    }
  }

  //num回だけ耐久を減らす処理
  def calcDurability(enchantmentLevel: Int, num: Int): Short = {
    val rand = new Random()
    val probability = 1.0 / (enchantmentLevel + 1.0)

    IntStream.range(0, num)
      .filter { _ => probability > rand.nextDouble() }
      .count().toShort
  }

  /**
   * @param player    破壊プレイヤー
   * @param block     手動破壊対象またはアサルト/遠距離の指定座標
   * @param isAssault true:	アサルトアーマーによる破壊
   *                  false:	アクティブスキルまたは手動による破壊
   * @return 重力値（破壊範囲の上に積まれているブロック数）
   */
  def getGravity(player: Player, block: Block, isAssault: Boolean): Int = {
    /** OPENHEIGHTマス以上のtransparentmateriallistブロックの連続により、地上判定とする。  */
    val OPENHEIGHT = 3

    // 1. 重力値を適用すべきか判定
    // 整地ワールド判定
    if (!Util.isSeichiWorld(player)) {
      return 0
    }

    // 2. 破壊要因判定
    /** 該当プレイヤーのPlayerData  */
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    /** ActiveSkillのリスト  */
    val skilllist = ActiveSkill.values()

    /** 重力値の計算を始めるY座標  */
    val startY: Int = if (!isAssault) {
      // Activeスキルの場合

      /** 破壊要因スキルタイプ  */
      val breakSkillType = playerdata.activeskilldata.skilltype
      /** 破壊要因スキルレベル  */
      val breakSkillLevel = playerdata.activeskilldata.skillnum
      /** 破壊スキル使用判定  */
      val isBreakSkill = breakSkillType > 0 && playerdata.activeskilldata.mineflagnum > 0

      // 重力値を計算開始するBlockのために、startY(blockのY方向offset値)を計算
      // 破壊スキルが選択されていなければ初期座標は破壊ブロックと同値
      if (!isBreakSkill) 0
      else if (breakSkillType == ActiveSkill.ARROW.gettypenum()) {
        /** 選択中のスキルの破壊範囲  */
        val skillBreakArea = skilllist(breakSkillType - 1).getBreakLength(breakSkillLevel)

        // 破壊ブロックの高さ＋破壊範囲の高さ－2（2段目が手動破壊対象となるため）
        skillBreakArea.y - 2
      } else {
        /** 該当プレイヤーが向いている方向  */
        val dir = BreakUtil.getCardinalDirection(player)
        // 下向きによる発動
        if (dir == "D") {
          // block＝破壊範囲の最上層ブロックにつき、startは0
          0
        } else if (dir == "U") {
          /** 選択中のスキルの破壊範囲  */
          val skillBreakArea = skilllist(breakSkillType - 1).getBreakLength(breakSkillLevel)
          // block＝破壊範囲の最下層ブロックにつき、startは破壊範囲の高さ
          skillBreakArea.y
        } else if ((breakSkillLevel == 1 || breakSkillLevel == 2) && playerdata.activeskilldata.mineflagnum == 1) {
          // 破壊ブロックの1マス上が破壊されるので、startは2段目から
          1
        } else {
          /** 選択中のスキルの破壊範囲  */
          val skillBreakArea = skilllist(breakSkillType - 1).getBreakLength(breakSkillLevel)
          // 破壊ブロックの高さ＋破壊範囲の高さ－2（2段目が手動破壊対象となるため）
          skillBreakArea.y - 2
        } // その他横向き発動時
        // 横向きによる発動のうち、デュアルorトリアルのmineflagnumが1(上破壊)
        // 上向きによる発動
      } // 単範囲/複数範囲破壊スキルの場合
      // 遠距離スキルの場合向きに依らずblock中心の横範囲となる
    } else {
      /** 破壊要因スキルタイプ  */
      val breakSkillType = playerdata.activeskilldata.assaulttype
      /** 破壊要因スキルレベル  */
      val breakSkillLevel = playerdata.activeskilldata.assaultnum
      /** 選択中のスキルの破壊範囲  */
      val skillBreakArea = skilllist(breakSkillType - 1).getBreakLength(breakSkillLevel)
      // アサルトアーマーの場合
      if (breakSkillType == ActiveSkill.ARMOR.gettypenum()) {
        // スキル高さ - 足位置で1 - blockが1段目なので1
        skillBreakArea.y - 2
      } else {
        // 高さはスキル/2の切り上げ…blockが1段目なので-1してプラマイゼロ
        (skillBreakArea.y - 1) / 2
      } // その他のアサルトスキルの場合
    } // Assaultスキルの場合

    // 3. 重力値計算
    /** OPENHEIGHTに達したかの計測カウンタ  */
    var openCount = 0
    /** 重力値  */
    var gravity = 0
    /** 最大ループ数  */
    val YMAX = 255
    for (checkPointer <- 1 until YMAX) {
      /** 確認対象ブロック  */
      val target = block.getRelative(0, startY + checkPointer, 0)
      // 対象ブロックが地上判定ブロックの場合
      if (MaterialSets.transparentMaterials.contains(target.getType)) {
        // カウンタを加算
        openCount += 1
        if (openCount >= OPENHEIGHT) {
          return gravity
        }
      } else {
        // カウンタをクリア
        openCount = 0
        // 重力値を加算(水をは2倍にする)
        if (target.getType == Material.WATER) {
          gravity += 2
        } else {
          gravity += 1
        }
      }
    }

    gravity
  }

  def getCardinalDirection(entity: Entity): String = {
    var rotation = ((entity.getLocation.getYaw + 180) % 360).toDouble
    val loc = entity.getLocation
    val pitch = loc.getPitch
    if (rotation < 0) {
      rotation += 360.0
    }

    if (pitch <= -30) {
      "U"
    } else if (pitch >= 25) {
      "D"
    } else if (0 <= rotation && rotation < 45.0) {
      "N"
    } else if (45.0 <= rotation && rotation < 135.0) {
      "E"
    } else if (135.0 <= rotation && rotation < 225.0) {
      "S"
    } else if (225.0 <= rotation && rotation < 315.0) {
      "W"
    } else if (315.0 <= rotation && rotation < 360.0) {
      "N"
    } else {
      null
    }
  }

  def logRemove(player: Player, removedBlock: Block): Boolean = {
    val wrapper = ExternalPlugins.getCoreProtectWrapper
    if (wrapper == null) {
      player.sendMessage(RED.toString + "error:coreprotectに保存できませんでした。管理者に報告してください。")
      return false
    }

    val failure = !wrapper.queueBlockRemoval(player, removedBlock)

    //もし失敗したらプレイヤーに報告し処理を終了
    if (failure) {
      player.sendMessage(RED.toString + "error:coreprotectに保存できませんでした。管理者に報告してください。")
      return false
    }
    true
  }

}
