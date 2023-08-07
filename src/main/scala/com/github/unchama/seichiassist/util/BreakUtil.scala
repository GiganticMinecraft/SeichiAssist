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
import com.github.unchama.util.external.{ExternalPlugins, WorldGuardWrapper}
import org.bukkit.ChatColor._
import org.bukkit.World.Environment
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.entity.{Entity, EntityType, Player}

import java.util.Random
import java.util.stream.IntStream
import scala.jdk.CollectionConverters._

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
    if (!WorldGuardWrapper.canBuild(player, checkTarget.getLocation)) {
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
        checkTarget.getType == Material.STONE_SLAB &&
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
    !isProtectedChest(player, checkTarget) && canBreakBlockMadeFromQuartz(
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
   * ブロックが破壊可能な「ネザー水晶でできたブロック」かどうか判定する。
   * @param player ネザー水晶類破壊設定を取得するプレイヤー
   * @param targetBlock 判定を行うブロック
   * @return `targetBlock`が「ネザー水晶でできたブロック」であれば破壊可能かどうか、そうでなければ常にtrue
   */
  private def canBreakBlockMadeFromQuartz(player: Player, targetBlock: Block): Boolean = {
    val materialType = targetBlock.getType
    val isNotQuartzBlockAndQuartzStairs =
      materialType != Material.QUARTZ_BLOCK && materialType != Material.QUARTZ_STAIRS
    // NOTE: targetBlock#getDataが7は下つきハーフブロック、15は上つきハーフブロック
    val isNotQuartzSlab =
      materialType != Material.QUARTZ_SLAB
    val isNotMadeFromQuartz = isNotQuartzBlockAndQuartzStairs && isNotQuartzSlab
    if (isNotMadeFromQuartz) {
      return true
    }

    val canBreakBlockMadeFromQuartz = SeichiAssist
      .instance
      .breakSkillTargetConfigSystem
      .api
      .breakSkillTargetConfig(player, BreakSkillTargetConfigKey.MadeFromNetherQuartz)
      .unsafeRunSync()

    if (!canBreakBlockMadeFromQuartz) {
      ActionBarMessageEffect(s"${RED}スキルでのネザー水晶類ブロックの破壊は無効化されています").run(player).unsafeRunSync()
    }

    canBreakBlockMadeFromQuartz
  }

  private def equalsIgnoreNameCaseWorld(name: String): Boolean = {
    val world = ManagedWorld.fromName(name).getOrElse(return false)

    world.shouldMuteCoreProtect
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
        case Material.PACKED_ICE | Material.MAGMA_BLOCK => 2L
        case _                                          => 1L
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
          val seq: Seq[(Location, Material)] = targetBlocks
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
            .map(block => (block.getLocation.clone(), block.getType))

          // ブロックをすべて[[toMaterial]]に変える
          targetBlocks.foreach(_.setType(toMaterial))

          seq
        })

      breakResults = {
        val plainBreakResult = targetBlocksInformation.map {
          case (location, _) =>
            (location, location.getBlock.getDrops(miningTool).asScala)
        }
        val drops = plainBreakResult.mapFilter {
          case (_, drops) if drops.nonEmpty => Some(drops.head)
          case _                            => None
        }
        val silverFishLocations = plainBreakResult.mapFilter {
          case (location, drops) if drops.isEmpty => Some(location)
          case _                                  => None
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
            case (location, material) =>
              dropLocation.getWorld.playEffect(location, Effect.STEP_SOUND, material)
          }
        }
      }

      // プレイヤーの統計を増やす
      totalCount = totalBreakCount(targetBlocksInformation.map { case (_, m) => m })
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
