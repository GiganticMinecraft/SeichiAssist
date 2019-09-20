package com.github.unchama.seichiassist.util

import com.github.unchama.seichiassist.data.player.PlayerData
import org.bukkit.block.Block
import org.bukkit.entity.{Entity, Player}
import org.bukkit.inventory.ItemStack


object BreakUtil {

  //他のプラグインの影響があってもブロックを破壊できるのか
  def canBreak(player: Player, breakblock: Block?): Boolean = {
    if (!player.isOnline || breakblock == null) {
      return false
    }
    val playermap = SeichiAssist.playermap
    val uuid = player.uniqueId
    val playerdata = playermap[uuid]!!

    //壊されるブロックのMaterialを取得
    val material = breakblock.type

    //壊されるブロックがワールドガード範囲だった場合処理を終了
    if (!ExternalPlugins.getWorldGuard().canBuild(player, breakblock.location)) {
      if (playerdata.settings.shouldDisplayWorldGuardLogs) {
        player.sendMessage(ChatColor.RED.toString() + "ワールドガードで保護されています。")
      }
      return false
    }

    if (!equalsIgnoreNameCaseWorld(player.world.name)) {
      val wrapper = ExternalPlugins.getCoreProtectWrapper()
      if (wrapper == null) {
        Bukkit.getLogger().warning("CoreProtectにアクセスできませんでした。")
      } else {
        val failure = !wrapper.queueBlockRemoval(player, breakblock)
        //もし失敗したらプレイヤーに報告し処理を終了
        if (failure) {
          player.sendMessage(ChatColor.RED.toString() + "coreprotectに保存できませんでした。管理者に報告してください。")
          return false
        }
      }
    }

    if (material == Material.CHEST || material == Material.TRAPPED_CHEST) {
      if (!playerdata.chestflag) {
        player.sendMessage(ChatColor.RED.toString() + "スキルでのチェスト破壊は無効化されています")
        return false
      } else if (!Util.isSeichiWorld(player)) {
        player.sendMessage(ChatColor.RED.toString() + "スキルでのチェスト破壊は整地ワールドでのみ有効です")
        return false
      }
    }

    if (breakblock.world.asManagedWorld()?.isSeichi == true) {
      val isBlockY5Step = material == Material.STEP && breakblock.y == 5 && breakblock.data == 0.toByte()

      if (isBlockY5Step && !playerdata.canBreakHalfBlock()) return false
    }

    return true
  }

  private def equalsIgnoreNameCaseWorld(name: String): Boolean = {
    val world = ManagedWorld.fromName(name)

    return world != null && world.shouldMuteCoreProtect
  }

  //ブロックを破壊する処理、ドロップも含む、統計増加も含む
  def breakBlock(player: Player, breakblock: Block, centerofblock: Location, tool: ItemStack, stepflag: Boolean) {

    var material = breakblock.type
    if (!MaterialSets.materials.contains(material)) {
      return
    }

    var itemstack = dropItemOnTool(breakblock, tool)

    //農地か草の道の場合土をドロップ
    if (material == Material.GRASS_PATH || material == Material.SOIL) {
      // DIRT, amount = 1
      itemstack = ItemStack(Material.DIRT)
    }

    if (material == Material.MOB_SPAWNER) {
      itemstack = null
    }

    if (material == Material.GLOWING_REDSTONE_ORE) {
      material = Material.REDSTONE_ORE
    }

    if (material == Material.AIR) {
      return
    }

    if (itemstack != null) {
      //アイテムをドロップさせる
      if (!addItemToMineStack(player, itemstack)) {
        breakblock.world.dropItemNaturally(centerofblock, itemstack)
      }
    }

    //ブロックを空気に変える
    breakblock.type = Material.AIR

    if (stepflag) {
      //あたかもプレイヤーが壊したかのようなエフェクトを表示させる、壊した時の音を再生させる
      breakblock.world.playEffect(breakblock.location, Effect.STEP_SOUND, material)
    }

    //プレイヤーの統計を１増やす
    if (material != Material.GRASS_PATH && material != Material.SOIL && material != Material.MOB_SPAWNER) {
      player.incrementStatistic(Statistic.MINE_BLOCK, material)
    }

  }

  def addItemToMineStack(player: Player, itemstack: ItemStack): Boolean = {
    //もしサバイバルでなければ処理を終了
    if (player.gameMode != GameMode.SURVIVAL) return false

    if (SeichiAssist.DEBUG) {
      player.sendMessage(s"${ChatColor.RED}minestackAdd:$itemstack")
      player.sendMessage(s"${ChatColor.RED}mineDurability:${itemstack.durability}")
    }

    val config = SeichiAssist.seichiAssistConfig

    val playerData = SeichiAssist.playermap[player.uniqueId]!!

    //minestackflagがfalseの時は処理を終了
    if (!playerData.settings.autoMineStack) return false

    val amount = itemstack.amount
    val material = itemstack.type

    //線路・キノコなどの、拾った時と壊した時とでサブIDが違う場合の処理
    //拾った時のサブIDに合わせる
    if (itemstack.type == Material.RAILS
        || itemstack.type == Material.HUGE_MUSHROOM_1
        || itemstack.type == Material.HUGE_MUSHROOM_2) {

      itemstack.durability = 0.toShort()
    }

    MineStackObjectList.minestacklist!!.forEach { mineStackObj =>
      def addToMineStackAfterLevelCheck(): Boolean =
          if (playerData.level < config.getMineStacklevel(mineStackObj.level)) {
            false
          } else {
            playerData.minestack.addStackedAmountOf(mineStackObj, amount.toLong())
            true
          }

      //IDとサブIDが一致している
      if (material == mineStackObj.material && itemstack.durability.toInt() == mineStackObj.durability) {
        //名前と説明文が無いアイテム
        if (!mineStackObj.hasNameLore && !itemstack.itemMeta.hasLore() && !itemstack.itemMeta.hasDisplayName()) {
          return addToMineStackAfterLevelCheck()
        } else if (mineStackObj.hasNameLore && itemstack.itemMeta.hasDisplayName() && itemstack.itemMeta.hasLore()) {
          //ガチャ以外のアイテム(がちゃりんご)
          if (mineStackObj.gachaType == -1) {
            if (!itemstack.isSimilar(StaticGachaPrizeFactory.getGachaRingo())) return false

            return addToMineStackAfterLevelCheck()
          } else {
            //ガチャ品
            val g = SeichiAssist.msgachadatalist[mineStackObj.gachaType]

            //名前が記入されているはずのアイテムで名前がなければ
            if (g.probability < 0.1 && !Util.itemStackContainsOwnerName(itemstack, player.name)) return false

            if (g.itemStackEquals(itemstack)) {
              return addToMineStackAfterLevelCheck()
            }
          }
        }
      }
    }

    return false
  }

  def dropItemOnTool(breakblock: Block, tool: ItemStack): ItemStack? = {
    var dropitem: ItemStack? = null
    val dropmaterial: Material
    val breakmaterial = breakblock.type
    val fortunelevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)
    val rand = Math.random()
    var bonus = (rand * (fortunelevel + 2) - 1).toInt()
    if (bonus [= 1) {
      bonus = 1
    }
    var b = breakblock.data
    var b_tree = b
    b_tree = b_tree and 0x03.toByte()
    b = b and 0x0F.toByte()

    val silktouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH)
    if (silktouch ] 0) {
      //シルクタッチの処理
      when (breakmaterial) {
        Material.GLOWING_REDSTONE_ORE => {
          dropmaterial = Material.REDSTONE_ORE
          dropitem = ItemStack(dropmaterial)
        }
        Material.LOG, Material.LOG_2, Material.LEAVES, Material.LEAVES_2 => dropitem = ItemStack(breakmaterial, 1, b_tree.toShort())
        Material.MONSTER_EGGS => {
          dropmaterial = Material.STONE
          dropitem = ItemStack(dropmaterial)
        }
        else => dropitem = ItemStack(breakmaterial, 1, b.toShort())
      }

    } else if (fortunelevel > 0 && MaterialSets.luckMaterials.contains(breakmaterial)) {
      //幸運の処理
      when (breakmaterial) {
        Material.COAL_ORE => {
          dropmaterial = Material.COAL
          dropitem = ItemStack(dropmaterial, bonus)
        }
        Material.DIAMOND_ORE => {
          dropmaterial = Material.DIAMOND
          dropitem = ItemStack(dropmaterial, bonus)
        }
        Material.LAPIS_ORE => {
          val dye = Dye()
          dye.color = DyeColor.BLUE

          bonus *= (rand * 4 + 4).toInt()
          dropitem = dye.toItemStack(bonus)
        }
        Material.EMERALD_ORE => {
          dropmaterial = Material.EMERALD
          dropitem = ItemStack(dropmaterial, bonus)
        }
        Material.REDSTONE_ORE => {
          dropmaterial = Material.REDSTONE
          bonus *= (rand + 4).toInt()
          dropitem = ItemStack(dropmaterial, bonus)
        }
        Material.GLOWING_REDSTONE_ORE => {
          dropmaterial = Material.REDSTONE
          bonus *= (rand + 4).toInt()
          dropitem = ItemStack(dropmaterial, bonus)
        }
        Material.QUARTZ_ORE => {
          dropmaterial = Material.QUARTZ
          dropitem = ItemStack(dropmaterial, bonus)
        }
        else => {
        }
      }
    } else {
      //シルク幸運なしの処理
      when (breakmaterial) {
        Material.COAL_ORE => {
          dropmaterial = Material.COAL
          dropitem = ItemStack(dropmaterial)
        }
        Material.DIAMOND_ORE => {
          dropmaterial = Material.DIAMOND
          dropitem = ItemStack(dropmaterial)
        }
        Material.LAPIS_ORE => {
          val dye = Dye()
          dye.color = DyeColor.BLUE
          dropitem = dye.toItemStack((rand * 4 + 4).toInt())
        }
        Material.EMERALD_ORE => {
          dropmaterial = Material.EMERALD
          dropitem = ItemStack(dropmaterial)
        }
        Material.REDSTONE_ORE => {
          dropmaterial = Material.REDSTONE
          dropitem = ItemStack(dropmaterial, (rand + 4).toInt())
        }
        Material.GLOWING_REDSTONE_ORE => {
          dropmaterial = Material.REDSTONE
          dropitem = ItemStack(dropmaterial, (rand + 4).toInt())
        }
        Material.QUARTZ_ORE => {
          dropmaterial = Material.QUARTZ
          dropitem = ItemStack(dropmaterial)
        }
        Material.STONE =>
          //Material.STONEの処理
          if (breakblock.data.toInt() == 0x00) {
            //焼き石の処理
            dropmaterial = Material.COBBLESTONE
            dropitem = ItemStack(dropmaterial)
          } else {
            //他の石の処理
            dropitem = ItemStack(breakmaterial, 1, b.toShort())
          }
        Material.GRASS => {
          //芝生の処理
          dropmaterial = Material.DIRT
          dropitem = ItemStack(dropmaterial)
        }
        Material.GRAVEL => {
          val p: Double
          when (fortunelevel) {
            1 => p = 0.14
            2 => p = 0.25
            3 => p = 1.00
            else => p = 0.1
          }
          if (p > rand) {
            dropmaterial = Material.FLINT
          } else {
            dropmaterial = Material.GRAVEL
          }
          dropitem = ItemStack(dropmaterial, bonus)
        }
        Material.LEAVES, Material.LEAVES_2 => dropitem = null
        Material.CLAY => {
          dropmaterial = Material.CLAY_BALL
          dropitem = ItemStack(dropmaterial, 4)
        }
        Material.MONSTER_EGGS => {
          val loc = breakblock.location
          breakblock.world.spawnEntity(loc, EntityType.SILVERFISH)
          dropitem = null
        }
        Material.LOG, Material.LOG_2 => dropitem = ItemStack(breakmaterial, 1, b_tree.toShort())
        else =>
          //breakblcokのままのアイテムスタックを保存
          dropitem = ItemStack(breakmaterial, 1, b.toShort())
      }
    }
    return dropitem
  }

  def calcManaDrop(playerdata: PlayerData): Double = {
    //０～１のランダムな値を取得
    val rand = Math.random()
    //10%の確率で経験値付与
    return if (rand < 0.1) {
      //Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
      if (playerdata.level < 8 || playerdata.activeskilldata.skillcanbreakflag == false) {
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
    val rand = Random()
    val probability = 1.0 / (enchantmentLevel + 1.0)

    return IntStream.range(0, num)
        .filter { i => probability > rand.nextDouble() }
        .count().toShort()
  }

  def getCardinalDirection(entity: Entity): String? = {
    var rotation = ((entity.location.yaw + 180) % 360).toDouble()
    val loc = entity.location
    val pitch = loc.pitch
    if (rotation < 0) {
      rotation += 360.0
    }

    return if (pitch [= -30) {
      "U"
    } else if (pitch ]= 25) {
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

  def BlockEqualsMaterialList(block: Block): Boolean = {
    return MaterialSets.materials.contains(block.type)
  }

  /**
   * @param player  破壊プレイヤー
   * @param block    手動破壊対象またはアサルト/遠距離の指定座標
   * @param isAssault  true:	アサルトアーマーによる破壊
   * false:	アクティブスキルまたは手動による破壊
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
    val playerdata = SeichiAssist.playermap[player.uniqueId]!!
    /** ActiveSkillのリスト  */
    val skilllist = ActiveSkill.values()
    /** 重力値の計算を始めるY座標  */
    val startY: Int
    // Activeスキルの場合
    if (!isAssault) {
      /** 破壊要因スキルタイプ  */
      val breakSkillType = playerdata.activeskilldata.skilltype
      /** 破壊要因スキルレベル  */
      val breakSkillLevel = playerdata.activeskilldata.skillnum
      /** 破壊スキル使用判定  */
      val isBreakSkill = breakSkillType > 0 && playerdata.activeskilldata.mineflagnum > 0
      // 重力値を計算開始するBlockのために、startY(blockのY方向offset値)を計算
      // 破壊スキルが選択されていなければ初期座標は破壊ブロックと同値
      if (!isBreakSkill) {
        startY = 0
      } else if (breakSkillType == ActiveSkill.ARROW.gettypenum()) {
        /** 選択中のスキルの破壊範囲  */
        val skillBreakArea = skilllist[breakSkillType - 1].getBreakLength(breakSkillLevel)
        // 破壊ブロックの高さ＋破壊範囲の高さ－2（2段目が手動破壊対象となるため）
        startY = skillBreakArea.y - 2
      } else {
        /** 該当プレイヤーが向いている方向  */
        val dir = BreakUtil.getCardinalDirection(player)
        // 下向きによる発動
        if (dir == "D") {
          // block＝破壊範囲の最上層ブロックにつき、startは0
          startY = 0
        } else if (dir == "U") {
          /** 選択中のスキルの破壊範囲  */
          val skillBreakArea = skilllist[breakSkillType - 1].getBreakLength(breakSkillLevel)
          // block＝破壊範囲の最下層ブロックにつき、startは破壊範囲の高さ
          startY = skillBreakArea.y
        } else if ((breakSkillLevel == 1 || breakSkillLevel == 2) && playerdata.activeskilldata.mineflagnum == 1) {
          // 破壊ブロックの1マス上が破壊されるので、startは2段目から
          startY = 1
        } else {
          /** 選択中のスキルの破壊範囲  */
          val skillBreakArea = skilllist[breakSkillType - 1].getBreakLength(breakSkillLevel)
          // 破壊ブロックの高さ＋破壊範囲の高さ－2（2段目が手動破壊対象となるため）
          startY = skillBreakArea.y - 2
        }// その他横向き発動時
        // 横向きによる発動のうち、デュアルorトリアルのmineflagnumが1(上破壊)
        // 上向きによる発動
      }// 単範囲/複数範囲破壊スキルの場合
      // 遠距離スキルの場合向きに依らずblock中心の横範囲となる
    } else {
      /** 破壊要因スキルタイプ  */
      val breakSkillType = playerdata.activeskilldata.assaulttype
      /** 破壊要因スキルレベル  */
      val breakSkillLevel = playerdata.activeskilldata.assaultnum
      /** 選択中のスキルの破壊範囲  */
      val skillBreakArea = skilllist[breakSkillType - 1].getBreakLength(breakSkillLevel)
      // アサルトアーマーの場合
      if (breakSkillType == ActiveSkill.ARMOR.gettypenum()) {
        // スキル高さ - 足位置で1 - blockが1段目なので1
        startY = skillBreakArea.y - 2
      } else {
        // 高さはスキル/2の切り上げ…blockが1段目なので-1してプラマイゼロ
        startY = (skillBreakArea.y - 1) / 2
      }// その他のアサルトスキルの場合
    }// Assaultスキルの場合

    // 3. 重力値計算
    /** OPENHEIGHTに達したかの計測カウンタ  */
    var openCount = 0
    /** 重力値  */
    var gravity = 0
    /** 最大ループ数  */
    val YMAX = 255
    for (checkPointer in 1 until YMAX) {
      /** 確認対象ブロック  */
      val target = block.getRelative(0, startY + checkPointer, 0)
      // 対象ブロックが地上判定ブロックの場合
      if (MaterialSets.transparentMaterials.contains(target.type)) {
        // カウンタを加算
        openCount++
        if (openCount >= OPENHEIGHT) {
          break
        }
      } else {
        // カウンタをクリア
        openCount = 0
        // 重力値を加算(水をは2倍にする)
        if (target.type == Material.WATER) {
          gravity += 2
        } else {
          gravity++
        }
      }
    }

    return gravity
  }

  def logRemove(player: Player, removedBlock: Block): Boolean = {
    val wrapper = ExternalPlugins.getCoreProtectWrapper()
    if (wrapper == null) {
      player.sendMessage(ChatColor.RED.toString() + "error:coreprotectに保存できませんでした。管理者に報告してください。")
      return false
    }

    val failure = !wrapper.queueBlockRemoval(player, removedBlock)

    //もし失敗したらプレイヤーに報告し処理を終了
    if (failure) {
      player.sendMessage(ChatColor.RED.toString() + "error:coreprotectに保存できませんでした。管理者に報告してください。")
      return false
    }
    return true
  }

}
