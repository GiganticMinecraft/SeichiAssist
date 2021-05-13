package com.github.unchama.seichiassist.util

import cats.effect.{IO, SyncIO}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.seichiskill.ActiveSkillRange._
import com.github.unchama.seichiassist.seichiskill.SeichiSkill.{AssaultArmor, DualBreak, TrialBreak}
import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.{Active, Disabled}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.targetedeffect.player.ActionBarMessageEffect
import com.github.unchama.util.bukkit.ItemStackUtil
import com.github.unchama.util.external.ExternalPlugins
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{Entity, EntityType, Player}
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Dye

import java.util.Random
import java.util.stream.IntStream


object BreakUtil {

  import ManagedWorld._

  def unsafeGetLockedBlocks(): Set[Block] =
    SeichiAssist.instance
      .lockedBlockChunkScope.trackedHandlers.unsafeRunSync()
      .flatten.map(x => x: Block)

  /**
   * 他のプラグインの影響があってもブロックを破壊できるのかを判定する。
   *
   * `lockedBlocks`は[[unsafeGetLockedBlocks()]]の結果が利用されるべきだが、
   * 複数ブロックのキャッシュのためにこれを事前にキャッシュして渡したほうが速い。
   * （引数を省略した場合呼び出しごとに再計算される）
   *
   * @param player 破壊者
   * @param checkTarget 破壊対象のブロック
   * @param lockedBlocks グローバルにロックされているブロックの集合
   */
  def canBreak(player: Player, checkTarget: Block, lockedBlocks: Set[Block] = unsafeGetLockedBlocks()): Boolean = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)

    //壊されるブロックがワールドガード範囲だった場合処理を終了
    if (!ExternalPlugins.getWorldGuard.canBuild(player, checkTarget.getLocation)) {
      if (playerData.settings.shouldDisplayWorldGuardLogs) {
        player.sendMessage(s"${RED}ワールドガードで保護されています。")
      }
      return false
    }

    if (!equalsIgnoreNameCaseWorld(player.getWorld.getName)) {
      val wrapper = ExternalPlugins.getCoreProtectWrapper
      if (wrapper == null) {
        Bukkit.getLogger.warning("CoreProtectにアクセスできませんでした。")
      } else {
        //もし失敗したらプレイヤーに報告し処理を終了
        if (!wrapper.queueBlockRemoval(player, checkTarget)) {
          player.sendMessage(s"${RED}coreprotectに保存できませんでした。管理者に報告してください。")
          return false
        }
      }
    }

    if (ManagedWorld.fromBukkitWorld(checkTarget.getWorld).exists(_.isSeichi)) {
      val halfBlockLayerYCoordinate = {
        val managedWorld = ManagedWorld.fromBukkitWorld(checkTarget.getWorld)
        // 整地専用サーバー（s5）のWORLD_SW_3（Earth整地）は、外部ワールドのため岩盤高度がY0
        if (SeichiAssist.seichiAssistConfig.getServerNum == 5 && managedWorld.contains(ManagedWorld.WORLD_SW_3)) 1
        // エンド整地ワールドには岩盤がないが、Y0にハーフを設置するひとがいるため
        else if (managedWorld.contains(ManagedWorld.WORLD_SW_END)) 0
        // それ以外なら通常通りY5
        else 5
      }

      val isBlockProtectedSlab =
        checkTarget.getType == Material.STEP &&
          checkTarget.getY == halfBlockLayerYCoordinate &&
          checkTarget.getData == 0.toByte

      if (isBlockProtectedSlab && !playerData.canBreakHalfBlock) return false
    }

    !lockedBlocks.contains(checkTarget)
  }

  def canBreakWithSkill(player: Player,
                        checkTarget: Block,
                        lockedBlocks: Set[Block] = unsafeGetLockedBlocks()): Boolean = {
    !isProtectedChest(player, checkTarget) &&
      canBreak(player, checkTarget, lockedBlocks)
  }

  def isProtectedChest(player: Player, checkTarget: Block): Boolean = {
    checkTarget.getType match {
      case Material.CHEST | Material.TRAPPED_CHEST =>
        if (!SeichiAssist.playermap(player.getUniqueId).chestflag) {
          ActionBarMessageEffect(s"${RED}スキルでのチェスト破壊は無効化されています")
            .run(player)
            .unsafeRunSync()
          true
        } else if (!ManagedWorld.fromBukkitWorld(player.getWorld).exists(_.isSeichi)) {
          ActionBarMessageEffect(s"${RED}スキルでのチェスト破壊は整地ワールドでのみ有効です")
            .run(player)
            .unsafeRunSync()
          true
        } else {
          false
        }
      case _ => false
    }
  }

  private def equalsIgnoreNameCaseWorld(name: String): Boolean = {
    val world = ManagedWorld.fromName(name).getOrElse(return false)

    world.shouldMuteCoreProtect
  }

  //ブロックを破壊する処理、ドロップも含む、統計増加も含む
  def breakBlock(player: Player,
                 targetBlock: BlockBreakableBySkill,
                 dropLocation: Location,
                 tool: BreakTool,
                 shouldPlayBreakSound: Boolean)
                (implicit effectEnvironment: EffectEnvironment): Unit =
    effectEnvironment.runEffectAsync(
      "単一ブロックを破壊する",
      massBreakBlock(player, Set(targetBlock), dropLocation, tool, shouldPlayBreakSound)
    )

  private sealed trait BlockBreakResult

  private object BlockBreakResult {

    case class ItemDrop(itemStack: ItemStack) extends BlockBreakResult

    case class SpawnSilverFish(location: Location) extends BlockBreakResult

  }

  /**
   * ブロックをツールで破壊した時のドロップを計算する
   *
   * Bukkit/Spigotが提供するBlock.getDropsは信頼できる値を返さない。
   * 本来はNMSのメソッドを呼ぶのが確実らしいが、一時的な実装として使用している。
   * 参考: https://www.spigotmc.org/threads/getdrops-on-crops-not-functioning-as-expected.167751/#post-1779788
   */
  private def dropItemOnTool(tool: BreakTool)(blockInformation: (Location, Material, Byte)): Option[BlockBreakResult] = {
    val fortuneLevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)

    val (blockLocation, blockMaterial, blockData) = blockInformation

    blockMaterial match {
      case Material.GRASS_PATH | Material.SOIL => return Some(
        BlockBreakResult.ItemDrop(new ItemStack(Material.DIRT))
      )
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
      Some {
        BlockBreakResult.ItemDrop {
          blockMaterial match {
            case Material.GLOWING_REDSTONE_ORE =>
              new ItemStack(Material.REDSTONE_ORE)
            case Material.LOG | Material.LOG_2 | Material.LEAVES | Material.LEAVES_2 =>
              new ItemStack(blockMaterial, 1, b_tree.toShort)
            case Material.MONSTER_EGGS =>
              new ItemStack(Material.STONE)
            case _ =>
              new ItemStack(blockMaterial, 1, blockDataLeast4Bits.toShort)
          }
        }
      }
    } else if (fortuneLevel > 0 && MaterialSets.fortuneMaterials.contains(blockMaterial)) {
      //幸運の処理
      Some {
        BlockBreakResult.ItemDrop {
          blockMaterial match {
            case Material.COAL_ORE =>
              new ItemStack(Material.COAL, bonus)
            case Material.DIAMOND_ORE =>
              new ItemStack(Material.DIAMOND, bonus)
            case Material.LAPIS_ORE =>
              val dye = new Dye()
              dye.setColor(DyeColor.BLUE)

              val withBonus = bonus * (rand * 4 + 4).toInt
              dye.toItemStack(withBonus)
            case Material.EMERALD_ORE =>
              new ItemStack(Material.EMERALD, bonus)
            case Material.REDSTONE_ORE | Material.GLOWING_REDSTONE_ORE =>
              val withBonus = bonus * (rand + 4).toInt
              new ItemStack(Material.REDSTONE, withBonus)
            case Material.QUARTZ_ORE =>
              new ItemStack(Material.QUARTZ, bonus)
            case _ =>
              // unreachable
              new ItemStack(blockMaterial, bonus)
          }
        }
      }
    } else {
      //シルク幸運なしの処理
      blockMaterial match {
        case Material.COAL_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.COAL)))
        case Material.DIAMOND_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.DIAMOND)))
        case Material.LAPIS_ORE =>
          val dye = new Dye()
          dye.setColor(DyeColor.BLUE)
          Some(BlockBreakResult.ItemDrop(dye.toItemStack((rand * 4 + 4).toInt)))
        case Material.EMERALD_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.EMERALD)))
        case Material.REDSTONE_ORE | Material.GLOWING_REDSTONE_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.REDSTONE, (rand + 4).toInt)))
        case Material.QUARTZ_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.QUARTZ)))
        case Material.STONE =>
          Some {
            BlockBreakResult.ItemDrop {
              if (blockData.toInt == 0x00) {
                //焼き石の処理
                new ItemStack(Material.COBBLESTONE)
              } else {
                //他の石の処理
                new ItemStack(blockMaterial, 1, blockDataLeast4Bits.toShort)
              }
            }
          }
        case Material.GRASS =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.DIRT)))
        case Material.GRAVEL =>
          val p = fortuneLevel match {
            case 1 => 0.14
            case 2 => 0.25
            case 3 => 1.00
            case _ => 0.1
          }
          val dropMaterial = if (p > rand) Material.FLINT else Material.GRAVEL

          Some(BlockBreakResult.ItemDrop(new ItemStack(dropMaterial, bonus)))
        case Material.LEAVES | Material.LEAVES_2 =>
          None
        case Material.CLAY =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.CLAY_BALL, 4)))
        case Material.MONSTER_EGGS =>
          Some(BlockBreakResult.SpawnSilverFish(blockLocation))
        case Material.LOG | Material.LOG_2 =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(blockMaterial, 1, b_tree.toShort)))
        case _ =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(blockMaterial, 1, blockDataLeast4Bits.toShort)))
      }
    }
  }

  /**
   * world 内での整地量倍率を計算する。
   * TODO: これはビジネスロジックである。breakcountシステムによって管理されるべき。
   */
  def blockCountWeight(world: World): Double = {
    val managedWorld = ManagedWorld.fromBukkitWorld(world)
    val seichiWorldFactor = if (managedWorld.exists(_.isSeichi)) 1.0 else 0.0
    val sw01Penalty = if (managedWorld.contains(ManagedWorld.WORLD_SW)) 0.8 else 1.0

    seichiWorldFactor * sw01Penalty
  }

  /**
   * マテリアルごとに倍率を掛けた整地量を計算する。
   * TODO: これはビジネスロジックである。breakcountシステムによって管理されるべき。
   */
  def totalBreakCount(materials: Seq[Material]): Long =
    materials
      .filter(MaterialSets.materialsToCountBlockBreak.contains)
      .map {
        //氷塊とマグマブロックの整地量を2倍
        case Material.PACKED_ICE | Material.MAGMA => 2L
        case _ => 1L
      }
      .sum

  /**
   * ブロックの書き換えを行い、ドロップ処理と統計増加の処理を行う`IO`を返す。
   *
   * 返される`IO`は、終了時点で同期スレッドで実行を行っている。
   *
   * @return
   */
  def massBreakBlock(player: Player,
                     targetBlocks: Iterable[BlockBreakableBySkill],
                     dropLocation: Location,
                     miningTool: BreakTool,
                     shouldPlayBreakSound: Boolean,
                     toMaterial: Material = Material.AIR): IO[Unit] = {

    for {
      // 非同期実行ではワールドに触れないので必要な情報をすべて抜く
      targetBlocksInformation <- PluginExecutionContexts.onMainThread.runAction(SyncIO {
        val seq: Seq[(Location, Material, Byte)] = targetBlocks.toSeq
          .filter { block =>
            block.getType match {
              case Material.AIR =>
                Bukkit.getLogger
                  .warning(s"AIRの破壊が${block.getLocation.toString}にて試行されました。")
                false
              case _ => true
            }
          }
          .map(block => (block.getLocation.clone(), block.getType, block.getData))

        // ブロックをすべて[[toMaterial]]に変える
        targetBlocks.foreach(_.setType(toMaterial))

        seq
      })

      breakResults = {
        import cats.implicits._

        val plainBreakResult = targetBlocksInformation.flatMap(dropItemOnTool(miningTool))
        val drops = plainBreakResult.mapFilter {
          case BlockBreakResult.ItemDrop(itemStack) => Some(itemStack)
          case BlockBreakResult.SpawnSilverFish(_) => None
        }
        val silverFishLocations = plainBreakResult.mapFilter {
          case BlockBreakResult.ItemDrop(_) => None
          case BlockBreakResult.SpawnSilverFish(location) => Some(location)
        }

        // 纏めなければ、FAWEの干渉を受け勝手に消される危険性などがある
        // また、後々ドロップする可能性もあるため早めに纏めておいて損はない
        (ItemStackUtil.amalgamate(drops), silverFishLocations)
      }

      itemsToBeDropped <- IO {
        // アイテムのマインスタック自動格納を試みる
        // 格納できなかったらドロップするアイテムとしてリストに入れる
        breakResults._1.flatMap { itemStack =>
          if (!tryAddItemIntoMineStack(player, itemStack))
            Some(itemStack)
          else
            None
        }
      }

      _ <- IO {
        // 壊した時の音を再生する
        if (shouldPlayBreakSound) {
          targetBlocksInformation.foreach { case (location, material, _) =>
            dropLocation.getWorld.playEffect(location, Effect.STEP_SOUND, material)
          }
        }
      }

      //プレイヤーの統計を増やす
      totalCount = totalBreakCount(targetBlocksInformation.map { case (_, m, _) => m })
      blockCountWeight <- IO(blockCountWeight(player.getWorld))
      expIncrease = SeichiExpAmount.ofNonNegative(totalCount * blockCountWeight)

      _ <- SeichiAssist.instance.breakCountSystem.api.incrementSeichiExp.of(player, expIncrease).toIO

      _ <- PluginExecutionContexts.onMainThread.runAction(SyncIO {
        // アイテムドロップは非同期スレッドで行ってはならない
        itemsToBeDropped.foreach(dropLocation.getWorld.dropItemNaturally(dropLocation, _))
        breakResults._2.foreach { location =>
          location.getWorld.spawnEntity(location, EntityType.SILVERFISH)
        }
      })
    } yield ()
  }

  def tryAddItemIntoMineStack(player: Player, itemstack: ItemStack): Boolean = {
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

    /**
     * 必要であれば引数を対応するアイテム向けの[[Material]]へ変換する
     * @param material 変換対象
     * @return 変換されたかもしれないMaterial
     */
    def intoItem(material: Material): Material = {
      material match {
        case Material.ACACIA_DOOR => Material.ACACIA_DOOR_ITEM
        case Material.BIRCH_DOOR => Material.BIRCH_DOOR_ITEM
        case Material.BED_BLOCK => Material.BED
        case Material.BREWING_STAND => Material.BREWING_STAND_ITEM
        case Material.CAULDRON => Material.CAULDRON_ITEM
        case Material.DARK_OAK_DOOR => Material.DARK_OAK_DOOR_ITEM
        case Material.FLOWER_POT => Material.FLOWER_POT_ITEM
        case Material.JUNGLE_DOOR => Material.JUNGLE_DOOR_ITEM
        case Material.SPRUCE_DOOR => Material.SPRUCE_DOOR_ITEM
        case Material.SKULL => Material.SKULL_ITEM
        case Material.NETHER_BRICK => Material.NETHER_BRICK_ITEM
        case Material.WOODEN_DOOR => Material.WOOD_DOOR
        case others => others
      }
    }
    val amount = itemstack.getAmount
    val material = intoItem(itemstack.getType)

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
      def addToMineStackAfterLevelCheck(): Boolean = {
        val level =
          SeichiAssist.instance
            .breakCountSystem.api
            .seichiAmountDataRepository(player)
            .read.unsafeRunSync()
            .levelCorrespondingToExp

        if (level.level < config.getMineStacklevel(mineStackObj.level)) {
          false
        } else {
          playerData.minestack.addStackedAmountOf(mineStackObj, amount.toLong)
          true
        }
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

  def calcManaDrop(player: Player): Double = {
    val isSkillAvailable = SeichiAssist.instance.activeSkillAvailability(player).get.unsafeRunSync()

    //０～１のランダムな値を取得
    val rand = Math.random()

    //10%の確率で経験値付与
    if (isSkillAvailable && rand < 0.1)
      SeichiAssist.playermap(player.getUniqueId).getPassiveExp
    else
      0.0
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
    // 1. 重力値を適用すべきか判定
    // 整地ワールド判定
    if (!Util.isSeichiWorld(player))
      return 0

    // 2. 破壊要因判定
    /** 該当プレイヤーのPlayerData  */
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val skillState = playerData.skillState.get.unsafeRunSync()

    /** 重力値の計算を始めるY座標  */
    val startY: Int =
      if (!isAssault) {
        val usageMode = skillState.usageMode
        if (usageMode != Disabled) {
          skillState.activeSkill match {
            case Some(skill) =>
              skill.range match {
                case MultiArea(effectChunkSize, _) =>
                  val playerDirection = BreakUtil.getCardinalDirection(player)
                  if (playerDirection == "D") {
                    // 下向きによる発動
                    // block＝破壊範囲の最上層ブロックにつき、startは0
                    0
                  } else if (playerDirection == "U") {
                    // 上向きによる発動
                    // block＝破壊範囲の最下層ブロックにつき、startは破壊範囲の高さ
                    effectChunkSize.y
                  } else if (Set(DualBreak, TrialBreak).contains(skill) && usageMode == Active) {
                    // 横向きによる発動のうち、デュアルorトリアルの上破壊
                    // 破壊ブロックの1マス上が破壊されるので、startは2段目から
                    1
                  } else {
                    // その他横向き発動時
                    // 破壊ブロックの高さ＋破壊範囲の高さ－2（2段目が手動破壊対象となるため）
                    effectChunkSize.y - 2
                  }
                case RemoteArea(effectChunkSize) =>
                  // 破壊ブロックの高さ＋破壊範囲の高さ－2（2段目が手動破壊対象となるため）
                  effectChunkSize.y - 2
              }
            // 破壊スキルが選択されていなければ初期座標は破壊ブロックと同値
            case None => 0
          }
        } else 0
      } else {
        skillState.assaultSkill match {
          case Some(skill) =>
            if (skill == AssaultArmor)
              // アサルトアーマーの場合
              // スキル高さ - 足位置で1 - blockが1段目なので1
              skill.range.effectChunkSize.y - 2
            else
              // その他のアサルトスキルの場合
              // 高さはスキル/2の切り上げ…blockが1段目なので-1してプラマイゼロ
              (skill.range.effectChunkSize.y - 1) / 2
          case None => 0
        }
      }

    // 3. 重力値計算
    /** OPENHEIGHTマス以上のtransparentmateriallistブロックの連続により、地上判定とする。  */
    val OPENHEIGHT = 3
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
        gravity += (if (target.getType == Material.WATER) 2 else 1)
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
