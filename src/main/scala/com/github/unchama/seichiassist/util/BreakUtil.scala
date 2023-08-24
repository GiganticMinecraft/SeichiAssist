package com.github.unchama.seichiassist.util

import cats.Monad
import cats.effect.{IO, SyncIO}
import com.github.unchama.generic.ApplicativeExtra.whenAOrElse
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.seichiskill.ActiveSkillRange._
import com.github.unchama.seichiassist.seichiskill.SeichiSkill.{
  AssaultArmor,
  DualBreak,
  TrialBreak
}
import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.{Active, Disabled}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.CardinalDirection
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.BreakSkillTargetConfigKey
import com.github.unchama.targetedeffect.player.ActionBarMessageEffect
import com.github.unchama.util.bukkit.ItemStackUtil
import com.github.unchama.util.external.ExternalPlugins
import org.bukkit.ChatColor._
import org.bukkit.World.Environment
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
    SeichiAssist
      .instance
      .lockedBlockChunkScope
      .trackedHandlers
      .unsafeRunSync()
      .flatten
      .map(x => x: Block)

  /**
   * 他のプラグインの影響があってもブロックを破壊できるのかを判定する。
   *
   * `lockedBlocks`は[[unsafeGetLockedBlocks()]]の結果が利用されるべきだが、
   * 複数ブロックのキャッシュのためにこれを事前にキャッシュして渡したほうが速い。 （引数を省略した場合呼び出しごとに再計算される）
   *
   * @param player
   * 破壊者
   * @param checkTarget
   * 破壊対象のブロック
   * @param lockedBlocks
   * グローバルにロックされているブロックの集合
   */
  def canBreak(
    player: Player,
    checkTarget: Block,
    lockedBlocks: Set[Block] = unsafeGetLockedBlocks()
  ): Boolean = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)

    // 壊されるブロックがワールドガード範囲だった場合処理を終了
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
        // もし失敗したらプレイヤーに報告し処理を終了
        if (!wrapper.queueBlockRemoval(player, checkTarget)) {
          player.sendMessage(s"${RED}coreprotectに保存できませんでした。管理者に報告してください。")
          return false
        }
      }
    }

    if (checkTarget.getWorld.isSeichi) {
      val halfBlockLayerYCoordinate = {
        val managedWorld = ManagedWorld.fromBukkitWorld(checkTarget.getWorld)
        // 整地専用サーバー（s5）のWORLD_SW_3（Earth整地）は、外部ワールドのため岩盤高度がY0
        if (
          SeichiAssist.seichiAssistConfig.getServerNum == 5 && managedWorld.contains(
            ManagedWorld.WORLD_SW_3
          )
        ) 1
        // エンド整地ワールドには岩盤がないが、Y0にハーフを設置するひとがいるため
        else if (managedWorld.contains(ManagedWorld.WORLD_SW_END)) 0
        // それ以外なら通常通りY5
        else 5
      }

      val isBlockProtectedSlab =
        checkTarget.getType == Material.STEP &&
          checkTarget.getY == halfBlockLayerYCoordinate &&
          checkTarget.getData == 0.toByte

      if (isBlockProtectedSlab) return false
    }

    !lockedBlocks.contains(checkTarget)
  }

  def canBreakWithSkill(
    player: Player,
    checkTarget: Block,
    lockedBlocks: Set[Block] = unsafeGetLockedBlocks()
  ): Boolean = {
    !isProtectedChest(player, checkTarget) && !isProtectedNetherQuartzBlock(
      player,
      checkTarget
    ) &&
    canBreak(player, checkTarget, lockedBlocks)
  }

  def isProtectedChest(player: Player, checkTarget: Block): Boolean = {
    checkTarget.getType match {
      case Material.CHEST | Material.TRAPPED_CHEST =>
        if (
          !SeichiAssist
            .instance
            .breakSkillTargetConfigSystem
            .api
            .breakSkillTargetConfig(player, BreakSkillTargetConfigKey.Chest)
            .unsafeRunSync()
        ) {
          ActionBarMessageEffect(s"${RED}スキルでのチェスト破壊は無効化されています").run(player).unsafeRunSync()
          true
        } else if (!player.getWorld.isSeichi) {
          ActionBarMessageEffect(s"${RED}スキルでのチェスト破壊は整地ワールドでのみ有効です").run(player).unsafeRunSync()
          true
        } else {
          false
        }
      case _ => false
    }
  }

  /**
   * ブロックがネザー水晶類破壊フラグの保護対象かどうか判定する。
   * ここでいう「ネザー水晶類」に鉱石は含まれない。
   * @param player ネザー水晶類破壊設定を取得するプレイヤー
   * @param targetBlock `targetBlock`が保護対象のネザー水晶類ブロックかどうか
   * @return ブロックがネザー水晶類破壊フラグの保護対象かどうか
   */
  private def isProtectedNetherQuartzBlock(player: Player, targetBlock: Block): Boolean = {
    val materialType = targetBlock.getType
    val isQuartzBlock =
      materialType == Material.QUARTZ_BLOCK || materialType == Material.QUARTZ_STAIRS
    val isQuartzSlab = materialType == Material.STEP && targetBlock.getData == 7.toByte
    val isQuartz = isQuartzBlock || isQuartzSlab
    val isProtectedNetherQuartzBlock = isQuartz && !SeichiAssist
      .instance
      .breakSkillTargetConfigSystem
      .api
      .breakSkillTargetConfig(player, BreakSkillTargetConfigKey.MadeFromNetherQuartz)
      .unsafeRunSync()

    if (isProtectedNetherQuartzBlock) {
      ActionBarMessageEffect(s"${RED}スキルでのネザー水晶類ブロックの破壊は無効化されています").run(player).unsafeRunSync()
    }

    isProtectedNetherQuartzBlock
  }

  private def equalsIgnoreNameCaseWorld(name: String): Boolean = {
    val world = ManagedWorld.fromName(name).getOrElse(return false)

    world.shouldMuteCoreProtect
  }

  // ブロックを破壊する処理、ドロップも含む、統計増加も含む
  def breakBlock(
    player: Player,
    targetBlock: BlockBreakableBySkill,
    dropLocation: Location,
    tool: BreakTool,
    shouldPlayBreakSound: Boolean
  )(implicit effectEnvironment: EffectEnvironment): Unit =
    effectEnvironment.unsafeRunEffectAsync(
      "単一ブロックを破壊する",
      massBreakBlock(player, Set(targetBlock), dropLocation, tool, shouldPlayBreakSound)
    )

  sealed trait BlockBreakResult

  object BlockBreakResult {

    case class ItemDrop(itemStack: ItemStack) extends BlockBreakResult

    case class SpawnSilverFish(location: Location) extends BlockBreakResult

  }

  /**
   * ブロックをツールで破壊した時のドロップを計算する
   *
   * Bukkit/Spigotが提供するBlock.getDropsは信頼できる値を返さない。 本来はNMSのメソッドを呼ぶのが確実らしいが、一時的な実装として使用している。 参考:
   * https://www.spigotmc.org/threads/getdrops-on-crops-not-functioning-as-expected.167751/#post-1779788
   */
  def dropItemOnTool(
    tool: BreakTool
  )(blockInformation: (Location, Material, Byte)): Option[BlockBreakResult] = {
    val fortuneLevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)

    val (blockLocation, blockMaterial, blockData) = blockInformation

    blockMaterial match {
      case Material.GRASS_PATH | Material.SOIL =>
        return Some(BlockBreakResult.ItemDrop(new ItemStack(Material.DIRT)))
      case Material.MOB_SPAWNER | Material.ENDER_PORTAL_FRAME | Material.ENDER_PORTAL =>
        return None
      case _ =>
    }

    val rand = Math.random()
    val bonus = Math.max(1, rand * (fortuneLevel + 2)).toInt

    val blockDataLeast4Bits = (blockData & 0x0f).toByte
    val b_tree = (blockData & 0x03).toByte

    val silkTouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH)

    if (silkTouch > 0) {
      // シルクタッチの処理
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
      // 幸運の処理
      Some {
        BlockBreakResult.ItemDrop {
          blockMaterial match {
            case Material.COAL_ORE =>
              new ItemStack(Material.COAL, bonus)
            case Material.DIAMOND_ORE =>
              new ItemStack(Material.DIAMOND, bonus)
            case Material.EMERALD_ORE =>
              new ItemStack(Material.EMERALD, bonus)
            case Material.QUARTZ_ORE =>
              new ItemStack(Material.QUARTZ, bonus)
            // レッドストーン鉱石, グロウストーン, スイカブロック, シーランタン, ラピスラズリ鉱石は、
            // ドロップアイテムの個数を求める計算が通常の鉱石の扱いと異なるため、特別な処理が必要である。
            case Material.REDSTONE_ORE | Material.GLOWING_REDSTONE_ORE =>
              val withBonus = (rand * (fortuneLevel + 2) + 4).toInt
              new ItemStack(Material.REDSTONE, withBonus)
            case Material.LAPIS_ORE =>
              val dye = new Dye()
              dye.setColor(DyeColor.BLUE)
              // 幸運エンチャントなしで掘った時のアイテムが得られる個数(4~9)に、幸運ボーナスを掛ける
              val withBonus = (rand * 6 + 4).toInt * bonus
              dye.toItemStack(withBonus)
            // グロウストーンは幸運エンチャントがついていると高確率でより多くのダストをドロップする
            // しかし、最大でも4個までしかドロップしない
            case Material.GLOWSTONE =>
              val withBonus = (rand * (fortuneLevel + 3) + 2).toInt
              val amount = if (withBonus > 4) 4 else withBonus
              new ItemStack(Material.GLOWSTONE_DUST, amount)
            // 同様に、メロンブロックは幸運エンチャントがついている場合、９個までしかドロップしない
            case Material.MELON_BLOCK =>
              val withBonus = (rand * (fortuneLevel + 5) + 3).toInt
              val amount = if (withBonus > 9) 9 else withBonus
              new ItemStack(Material.MELON, amount)
            case Material.SEA_LANTERN =>
              val withBonus = (rand * (fortuneLevel + 2) + 2).toInt
              val amount = if (withBonus > 5) 5 else withBonus
              new ItemStack(Material.PRISMARINE_CRYSTALS, amount)
            case _ =>
              // unreachable
              new ItemStack(blockMaterial, bonus)
          }
        }
      }
    } else {
      // シルク幸運なしの処理
      blockMaterial match {
        case Material.COAL_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.COAL)))
        case Material.DIAMOND_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.DIAMOND)))
        case Material.LAPIS_ORE =>
          val dye = new Dye()
          dye.setColor(DyeColor.BLUE)
          Some(BlockBreakResult.ItemDrop(dye.toItemStack((rand * 6 + 4).toInt)))
        case Material.EMERALD_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.EMERALD)))
        case Material.REDSTONE_ORE | Material.GLOWING_REDSTONE_ORE =>
          Some(
            BlockBreakResult.ItemDrop(new ItemStack(Material.REDSTONE, ((rand * 2) + 4).toInt))
          )
        case Material.QUARTZ_ORE =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.QUARTZ)))
        // グロウストーンは、2から4個のグロウストーンダストをドロップする
        case Material.GLOWSTONE =>
          Some(
            BlockBreakResult
              .ItemDrop(new ItemStack(Material.GLOWSTONE_DUST, (rand * 3 + 2).toInt))
          )
        // スイカブロックは、3から7個のスイカをドロップする
        case Material.MELON_BLOCK =>
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.MELON, (rand * 5 + 3).toInt)))
        // シーランタンは、2から3個のプリズマリンクリスタルをドロップする
        case Material.SEA_LANTERN =>
          Some(
            BlockBreakResult
              .ItemDrop(new ItemStack(Material.PRISMARINE_CRYSTALS, (rand * 2 + 2).toInt))
          )
        case Material.STONE =>
          Some {
            BlockBreakResult.ItemDrop {
              if (blockData.toInt == 0x00) {
                // 焼き石の処理
                new ItemStack(Material.COBBLESTONE)
              } else {
                // 他の石の処理
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
        case Material.WOOD_STEP | Material.STEP | Material.STONE_SLAB2
            if (blockDataLeast4Bits & 8) != 0 =>
          // 上付きハーフブロックをそのままドロップするとmissing textureとして描画されるため、下付きの扱いとする
          Some(
            BlockBreakResult
              .ItemDrop(new ItemStack(blockMaterial, 1, (blockDataLeast4Bits & 7).toShort))
          )
        case Material.BOOKSHELF =>
          // 本棚を破壊すると、本が3つドロップする
          Some(BlockBreakResult.ItemDrop(new ItemStack(Material.BOOK, 3)))
        case _ =>
          Some(
            BlockBreakResult
              .ItemDrop(new ItemStack(blockMaterial, 1, blockDataLeast4Bits.toShort))
          )
      }
    }
  }

  /**
   * TODO: これはビジネスロジックである。breakcountシステムによって管理されるべき。
   *
   * @param world
   * 対象ワールド
   * @return
   * ワールドに対応する整地量の倍率を計算する作用
   */
  def blockCountWeight[F[_]: Monad](world: World): F[Double] =
    Monad[F].pure {
      val managedWorld = ManagedWorld.fromBukkitWorld(world)
      val seichiWorldFactor = if (managedWorld.exists(_.isSeichi)) 1.0 else 0.0
      val sw01Penalty =
        if (managedWorld.contains(ManagedWorld.WORLD_SW)) 0.8 else 1.0

      seichiWorldFactor * sw01Penalty
    }

  /**
   * マテリアルごとに倍率を掛けた整地量を計算する。 TODO: これはビジネスロジックである。breakcountシステムによって管理されるべき。
   */
  def totalBreakCount(materials: Seq[Material]): Long =
    materials
      .filter(MaterialSets.materialsToCountBlockBreak.contains)
      .map {
        // 氷塊とマグマブロックの整地量を2倍
        case Material.PACKED_ICE | Material.MAGMA => 2L
        case _                                    => 1L
      }
      .sum

  /**
   * ブロックの書き換えを行い、ドロップ処理と統計増加の処理を行う`IO`を返す。
   *
   * 返される`IO`は、終了時点で同期スレッドで実行を行っている。
   *
   * @return
   */
  def massBreakBlock(
    player: Player,
    targetBlocks: Iterable[BlockBreakableBySkill],
    dropLocation: Location,
    miningTool: BreakTool,
    shouldPlayBreakSound: Boolean,
    toMaterial: Material = Material.AIR
  ): IO[Unit] = {
    import cats.implicits._

    for {
      // 非同期実行ではワールドに触れないので必要な情報をすべて抜く
      targetBlocksInformation <- PluginExecutionContexts
        .onMainThread
        .runAction(SyncIO {
          val seq: Seq[(Location, Material, Byte)] = targetBlocks
            .toSeq
            .filter { block =>
              block.getType match {
                case Material.AIR =>
                  if (SeichiAssist.DEBUG)
                    Bukkit.getLogger.warning(s"AIRの破壊が${block.getLocation.toString}にて試行されました。")
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
        val plainBreakResult = targetBlocksInformation.flatMap(dropItemOnTool(miningTool))
        val drops = plainBreakResult.mapFilter {
          case BlockBreakResult.ItemDrop(itemStack) => Some(itemStack)
          case BlockBreakResult.SpawnSilverFish(_)  => None
        }
        val silverFishLocations = plainBreakResult.mapFilter {
          case BlockBreakResult.ItemDrop(_)               => None
          case BlockBreakResult.SpawnSilverFish(location) => Some(location)
        }

        // 纏めなければ、FAWEの干渉を受け勝手に消される危険性などがある
        // また、後々ドロップする可能性もあるため早めに纏めておいて損はない
        (ItemStackUtil.amalgamate(drops), silverFishLocations)
      }

      currentAutoMineStackState <- SeichiAssist
        .instance
        .mineStackSystem
        .api
        .autoMineStack(player)

      itemsToBeDropped <-
        // アイテムのマインスタック自動格納を試みる
        // 格納できなかったらドロップするアイテムとしてリストに入れる
        breakResults._1.toList.traverse { itemStack =>
          whenAOrElse(currentAutoMineStackState)(
            SeichiAssist
              .instance
              .mineStackSystem
              .api
              .mineStackRepository
              .tryIntoMineStack(player, itemStack, itemStack.getAmount),
            false
          ).map(Option.unless(_)(itemStack))
        }

      _ <- IO {
        // 壊した時の音を再生する
        if (shouldPlayBreakSound) {
          targetBlocksInformation.foreach {
            case (location, material, _) =>
              dropLocation.getWorld.playEffect(location, Effect.STEP_SOUND, material)
          }
        }
      }

      // プレイヤーの統計を増やす
      totalCount = totalBreakCount(targetBlocksInformation.map { case (_, m, _) => m })
      blockCountWeight <- blockCountWeight[IO](player.getWorld)
      expIncrease = SeichiExpAmount.ofNonNegative(totalCount * blockCountWeight)

      _ <- SeichiAssist
        .instance
        .breakCountSystem
        .api
        .incrementSeichiExp
        .of(player, expIncrease)
        .toIO

      _ <- PluginExecutionContexts
        .onMainThread
        .runAction(SyncIO {
          // アイテムドロップは非同期スレッドで行ってはならない
          itemsToBeDropped
            .flatten
            .foreach(dropLocation.getWorld.dropItemNaturally(dropLocation, _))
          breakResults._2.foreach { location =>
            location.getWorld.spawnEntity(location, EntityType.SILVERFISH)
          }
        })
    } yield ()
  }

  def calcManaDrop(player: Player): Double = {
    val isSkillAvailable =
      SeichiAssist.instance.activeSkillAvailability(player).get.unsafeRunSync()

    // ０～１のランダムな値を取得
    val rand = Math.random()

    // 10%の確率で経験値付与
    if (isSkillAvailable && rand < 0.1)
      SeichiAssist.playermap(player.getUniqueId).getPassiveExp
    else
      0.0
  }

  // num回だけ耐久を減らす処理
  def calcDurability(enchantmentLevel: Int, num: Int): Short = {
    val rand = new Random()
    val probability = 1.0 / (enchantmentLevel + 1.0)

    IntStream.range(0, num).filter { _ => probability > rand.nextDouble() }.count().toShort
  }

  /**
   * 重力値計算対象のブロックかどうかを判定します。
   * 対象ブロック：以下のいずれかを満たす
   *  - Material.isSolid == true になるブロック（ただし岩盤を除く）
   *  - 液体ブロック（水,溶岩）
   *    ref: [バージョン1.12.x時の最新記事アーカイブ](https://minecraft.fandom.com/wiki/Solid_block?oldid=1132868)
   */
  private def isAffectedByGravity(material: Material): Boolean = {
    material match {
      case Material.BEDROCK                                          => false
      case m if MaterialSets.fluidMaterials.contains(m) || m.isSolid => true
      case _                                                         => false
    }
  }

  /**
   * @param player
   * 破壊プレイヤー
   * @param block
   * 手動破壊対象またはアサルト/遠距離の指定座標
   * @param isAssault
   * true: アサルトアーマーによる破壊 false: アクティブスキルまたは手動による破壊
   * @return
   * 重力値（破壊範囲の上に積まれているブロック数）
   */
  def getGravity(player: Player, block: Block, isAssault: Boolean): Int = {
    // 1. 重力値を適用すべきか判定
    // 整地ワールド判定
    if (!player.getWorld.isSeichi)
      return 0

    // 2. 破壊要因判定
    /**
     * 該当プレイヤーのPlayerData
     */
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val skillState = playerData.skillState.get.unsafeRunSync()

    /**
     * 重力値の計算を始めるY座標
     */
    val blockRelativeHeight: Int =
      if (!isAssault) {
        val usageMode = skillState.usageMode
        if (usageMode != Disabled) {
          skillState.activeSkill match {
            case Some(skill) =>
              skill.range match {
                case MultiArea(effectChunkSize, _) =>
                  val playerDirection = BreakUtil.getCardinalDirection(player)
                  if (playerDirection == CardinalDirection.Down) {
                    // 下向きによる発動
                    // block＝破壊範囲の最上層ブロックにつき、startは0
                    0
                  } else if (playerDirection == CardinalDirection.Up) {
                    // 上向きによる発動
                    // block＝破壊範囲の最下層ブロックにつき、startは破壊範囲の高さ
                    effectChunkSize.y
                  } else if (
                    Set(DualBreak, TrialBreak).contains(skill) && usageMode == Active
                  ) {
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
    /**
     * isAffectedByGravityを満たさないMaterialを持つブロックが
     * この回数以上連続したとき、重力値のカウントをストップする。
     */
    val surfaceThreshold = 3

    var surfaceCandidateCount = 0

    /**
     * 重力値
     */
    var gravity = 0

    /**
     * 最大ループ数
     */
    val maxY = if (player.getWorld.getEnvironment == Environment.NETHER) 121 else 255
    val maxOffsetY = maxY - blockRelativeHeight

    // NOTE: `1 until 0`など、`x > y`が満たされる`x until y`はイテレーションが行われない
    for (offsetY <- 1 to maxOffsetY) {

      /**
       * 確認対象ブロック
       */
      val target = block.getRelative(0, blockRelativeHeight + offsetY, 0)
      // 対象ブロックが重力値に影響を与えるブロックではない場合
      if (!isAffectedByGravity(target.getType)) {
        // カウンタを加算
        surfaceCandidateCount += 1
        if (surfaceCandidateCount >= surfaceThreshold) {
          return gravity
        }
      } else {
        // カウンタをクリア
        surfaceCandidateCount = 0
        // 重力値を加算(水は2倍にする)
        gravity += (if (target.getType == Material.WATER) 2 else 1)
      }
    }

    gravity
  }

  /**
   * エンティティが向いている方向を計算して取得する
   *
   * @param entity
   * 対象とするエンティティ
   * @return
   * エンティティが向いている方向が座標軸方向に近似できた場合はnon-nullな[[CardinalDirection]]、そうでない場合は`null`
   */
  def getCardinalDirection(entity: Entity): CardinalDirection = {
    var rotation = ((entity.getLocation.getYaw + 180) % 360).toDouble
    val loc = entity.getLocation
    val pitch = loc.getPitch
    if (rotation < 0) {
      rotation += 360.0
    }

    if (pitch <= -30) {
      CardinalDirection.Up
    } else if (pitch >= 25) {
      CardinalDirection.Down
    } else if (0 <= rotation && rotation < 45.0) {
      CardinalDirection.North
    } else if (45.0 <= rotation && rotation < 135.0) {
      CardinalDirection.East
    } else if (135.0 <= rotation && rotation < 225.0) {
      CardinalDirection.South
    } else if (225.0 <= rotation && rotation < 315.0) {
      CardinalDirection.West
    } else if (315.0 <= rotation && rotation < 360.0) {
      CardinalDirection.North
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

    // もし失敗したらプレイヤーに報告し処理を終了
    if (failure) {
      player.sendMessage(RED.toString + "error:coreprotectに保存できませんでした。管理者に報告してください。")
      return false
    }
    true
  }

  /**
   * プレーヤーがスキルを使うときに複数種類ブロック同時破壊を行うかどうかを返す関数。
   *
   *   - プレーヤーの整地レベルが `SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreakLevel` 以上である、かつ、
   *   - 以下二条件のうちどちらかが満たされている
   *     - プレーヤーが「整地ワールド」に居る、または
   *     - `PlayerData.settings.performMultipleIDBlockBreakWhenOutsideSeichiWorld`（以下「フラグ」）が
   *       `true` になっている
   *
   * 「整地スキルを使えるワールド」と「整地ワールド」の概念が一致していない事から、 単純にフラグを返すだけではないので注意。 例えば、メインワールドでは、整地レベルが十分かつフラグが
   * `true` のときのみ複数種類ブロック破壊をする。
   */
  def performsMultipleIDBlockBreakWhenUsingSkills(player: Player): SyncIO[Boolean] = for {
    seichiAmountData <-
      SeichiAssist.instance.breakCountSystem.api.seichiAmountDataRepository(player).read
    currentWorld <- SyncIO(player.getWorld)
    flag <- SyncIO(
      SeichiAssist
        .playermap(player.getUniqueId)
        .settings
        .performMultipleIDBlockBreakWhenOutsideSeichiWorld
    )
  } yield {
    import ManagedWorld._

    val isLevelAboveThreshold =
      seichiAmountData.levelCorrespondingToExp.level >= SeichiAssist
        .seichiAssistConfig
        .getMultipleIDBlockBreakLevel

    isLevelAboveThreshold && (currentWorld.isSeichi || flag)
  }
}
