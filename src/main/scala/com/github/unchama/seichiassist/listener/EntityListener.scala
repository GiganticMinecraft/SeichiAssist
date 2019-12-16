package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}
import com.github.unchama.seichiassist.task.GiganticBerserkTask
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.seichiassist.util.{BreakUtil, Util}
import org.bukkit._
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{Player, Projectile}
import org.bukkit.event.entity._
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

import scala.collection.mutable

class EntityListener extends Listener {
  private val playermap = SeichiAssist.playermap

  @EventHandler def onPlayerActiveSkillEvent(event: ProjectileHitEvent): Unit = { //矢を取得する
    val e = event.getEntity
    if (!e.hasMetadata("ArrowSkill")) return
    val proj = e
    val projsource = proj.getShooter
    if (!projsource.isInstanceOf[Player]) return
    val player = projsource.asInstanceOf[Player]

    if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "ProjectileHitEventの呼び出し")

    //もしサバイバルでなければ処理を終了
    //もしフライ中なら終了
    if ((player.getGameMode ne GameMode.SURVIVAL) || player.isFlying) return

    //壊されるブロックを取得
    val block = player.getWorld.getBlockAt(proj.getLocation.add(proj.getVelocity.normalize))

    //他人の保護がかかっている場合は処理を終了
    if (!ExternalPlugins.getWorldGuard.canBuild(player, block.getLocation)) return

    //ブロックのタイプを取得
    val material = block.getType

    //ブロックタイプがmateriallistに登録されていなければ処理終了
    if (!MaterialSets.materials.contains(material) && e.hasMetadata("ArrowSkill")) return

    //UUIDを取得
    val uuid = player.getUniqueId

    //UUIDを基にプレイヤーデータ取得
    val playerdata = SeichiAssist.playermap.apply(uuid)

    //念のためエラー分岐
    if (playerdata == null) {
      Util.sendPlayerDataNullMessage(player)
      Bukkit.getLogger.warning(player.getName + " -> PlayerData not found.")
      Bukkit.getLogger.warning("EntityListener.onPlayerActiveSkillEvent")
      return
    }

    //整地ワールドでは重力値によるキャンセル判定を行う(スキル判定より先に判定させること)
    if (BreakUtil.getGravity(player, block, false) > 3) {
      player.sendMessage(ChatColor.RED + "整地ワールドでは必ず上から掘ってください。")
      return
    }

    //スキル発動条件がそろってなければ終了
    if (!Util.isSkillEnable(player)) return

    //プレイヤーインベントリを取得
    val inventory = player.getInventory

    //メインハンドとオフハンドを取得
    val mainhanditem = inventory.getItemInMainHand
    val offhanditem = inventory.getItemInOffHand

    //メインハンドにツールがあるか
    val mainhandtoolflag = MaterialSets.breakMaterials.contains(mainhanditem.getType)

    //オフハンドにツールがあるか
    val offhandtoolflag = MaterialSets.breakMaterials.contains(offhanditem.getType)

    //実際に使用するツールを格納する
    val tool = if (mainhandtoolflag) { //メインハンドの時
      mainhanditem
    } else if (offhandtoolflag) { //サブハンドの時
      return
    } else { //どちらにももっていない時処理を終了
      return
    }

    //耐久値がマイナスかつ耐久無限ツールでない時処理を終了
    if (tool.getDurability > tool.getType.getMaxDurability && !tool.getItemMeta.spigot.isUnbreakable) return
    //スキルで破壊されるブロックの時処理を終了
    if (SeichiAssist.managedBlocks.contains(block)) {
      if (SeichiAssist.DEBUG) player.sendMessage("スキルで使用中のブロックです。")
      return
    }
    runArrowSkillofHitBlock(player, proj, block, tool)
    SeichiAssist.managedEntities.$minus$eq(proj)
    proj.remove()
  }

  private def runArrowSkillofHitBlock(player: Player, proj: Projectile, block: Block, tool: ItemStack): Unit = {
    /*遠距離破壊スキルリスナー*/ val uuid = player.getUniqueId
    //playerdataを取得
    val playerdata = playermap.apply(uuid)
    //レベルを取得
    //int skilllevel = playerdata.activeskilldata.skillnum;
    //マナを取得
    val mana = playerdata.activeskilldata.mana
    //元ブロックのマテリアルを取得
    val material = block.getType
    //元ブロックの真ん中の位置を取得
    val centerofblock = block.getLocation.add(0.5, 0.5, 0.5)
    val area = playerdata.activeskilldata.area
    //現在のプレイヤーの向いている方向
    val dir = BreakUtil.getCardinalDirection(player)
    //もし前回とプレイヤーの向いている方向が違ったら範囲を取り直す
    if (!(dir == area.getDir)) {
      area.setDir(dir)
      area.makeArea()
    }
    val start = area.getStartList.get(0)
    val end = area.getEndList.get(0)
    //エフェクト用に壊されるブロック全てのリストデータ
    //一回の破壊の範囲
    val breaklength = area.getBreakLength
    //１回の全て破壊したときのブロック数
    val ifallbreaknum = breaklength.x * breaklength.y * breaklength.z
    val breakBlock = new mutable.HashSet[Block]
    val lavas = new mutable.HashSet[Block]

    import com.github.unchama.seichiassist.data.syntax._
    AxisAlignedCuboid(start, end).gridPoints().foreach { case XYZTuple(x, y, z) =>
      val targetBlock = block.getRelative(x, y, z)
      if (playerdata.level >= SeichiAssist.seichiAssistConfig.getMultipleIDBlockBreaklevel && playerdata.settings.multipleidbreakflag) { //追加テスト(複数種類一括破壊スキル)
        if ((targetBlock.getType ne Material.AIR) && (targetBlock.getType ne Material.BEDROCK))
          if ((targetBlock.getType eq Material.STATIONARY_LAVA) || BreakUtil.BlockEqualsMaterialList(targetBlock))
            if (BreakUtil.canBreak(player, Some.apply(targetBlock)))
              if (targetBlock.getType eq Material.STATIONARY_LAVA) {
                lavas.add(targetBlock)
              } else {
                breakBlock.add(targetBlock)
                SeichiAssist.managedBlocks.$plus$eq(targetBlock)
              }
      } else {
        //条件を満たしていない
        //もし壊されるブロックがもともとのブロックと同じ種類だった場合
        if ((targetBlock.getType eq material) || ((block.getType eq Material.DIRT) && (targetBlock.getType eq Material.GRASS)) || ((block.getType eq Material.GRASS) && (targetBlock.getType eq Material.DIRT)) || ((block.getType eq Material.GLOWING_REDSTONE_ORE) && (targetBlock.getType eq Material.REDSTONE_ORE)) || ((block.getType eq Material.REDSTONE_ORE) && (targetBlock.getType eq Material.GLOWING_REDSTONE_ORE)) || (targetBlock.getType eq Material.STATIONARY_LAVA)) if (BreakUtil.canBreak(player, Some.apply(targetBlock))) if (targetBlock.getType eq Material.STATIONARY_LAVA) lavas.add(targetBlock)
        else {
          breakBlock.add(targetBlock)
          SeichiAssist.managedBlocks.$plus$eq(targetBlock)
        }
      }
    }

    //重力値計算
    val gravity = BreakUtil.getGravity(player, block, false)
    //減る経験値計算
    //実際に破壊するブロック数  * 全てのブロックを破壊したときの消費経験値÷すべての破壊するブロック数 * 重力
    val useMana = breakBlock.size.toDouble * (gravity + 1) * ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum) / ifallbreaknum
    if (SeichiAssist.DEBUG) {
      player.sendMessage(ChatColor.RED + "必要経験値：" + ActiveSkill.getActiveSkillUseExp(playerdata.activeskilldata.skilltype, playerdata.activeskilldata.skillnum))
      player.sendMessage(ChatColor.RED + "全ての破壊数：" + ifallbreaknum)
      player.sendMessage(ChatColor.RED + "実際の破壊数：" + breakBlock.size)
      player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナ：" + useMana)
    }
    //減る耐久値の計算
    //１マス溶岩を破壊するのにはブロック１０個分の耐久が必要
    val durability =
      (tool.getDurability +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), breakBlock.size) +
        BreakUtil.calcDurability(tool.getEnchantmentLevel(Enchantment.DURABILITY), 10 * lavas.size)).toShort
    //重力値の判定
    if (gravity > 15) {
      player.sendMessage(ChatColor.RED + "スキルを使用するには上から掘ってください。")
      SeichiAssist.managedBlocks.$minus$minus$eq(breakBlock)
      return
    }
    //実際に経験値を減らせるか判定
    if (!mana.has(useMana)) { //デバッグ用
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なマナが足りません")
      SeichiAssist.managedBlocks.$minus$minus$eq(breakBlock)
      return
    }
    if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値:" + durability)
    //実際に耐久値を減らせるか判定
    if (tool.getType.getMaxDurability <= durability && !tool.getItemMeta.spigot.isUnbreakable) {
      if (SeichiAssist.DEBUG) player.sendMessage(ChatColor.RED + "アクティブスキル発動に必要なツールの耐久値が足りません")
      SeichiAssist.managedBlocks.$minus$minus$eq(breakBlock)
      return
    }
    //経験値を減らす
    mana.decrease(useMana, player, playerdata.level)
    //耐久値を減らす
    if (!tool.getItemMeta.spigot.isUnbreakable) tool.setDurability(durability)
    //以降破壊する処理
    //溶岩を破壊する処理
    lavas.foreach((value: Block) => {
      def foo(value: Block) = {
        value.setType(Material.AIR)
        // Set#foreachに渡すラムダ式は何らかの戻り値が必要
        0
      }

      foo(value)
    })
    //選択されたブロックを破壊する処理
    //エフェクトが指定されていないときの処理
    if (playerdata.activeskilldata.effectnum == 0) breakBlock.foreach((b: Block) => {
      def foo(b: Block) = {
        BreakUtil.breakBlock(player, b, player.getLocation, tool, false)
        SeichiAssist.managedBlocks.$minus$eq(b)
        0
      }

      foo(b)
    })
    else { //通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
      if (playerdata.activeskilldata.effectnum <= 100) {
        val skilleffect = ActiveSkillEffect.arrayValues
        skilleffect(playerdata.activeskilldata.effectnum - 1).runBreakEffect(player, playerdata.activeskilldata, tool, breakBlock.toSet, start, end, centerofblock)
      }
      else { //スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
        if (playerdata.activeskilldata.effectnum > 100) {
          val premiumeffect = ActiveSkillPremiumEffect.arrayValues
          premiumeffect(playerdata.activeskilldata.effectnum - 1 - 100).runBreakEffect(player, tool, breakBlock.toSet, start, end, centerofblock)
        }
      }
    }
  }

  @EventHandler def onEntityExplodeEvent(event: EntityExplodeEvent) = {
    val e = event.getEntity
    if (e.isInstanceOf[Projectile]) if (e.hasMetadata("ArrowSkill") || e.hasMetadata("Effect")) event.setCancelled(true)
  }

  @EventHandler def onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) = {
    val e = event.getDamager
    if (e.isInstanceOf[Projectile]) if (e.hasMetadata("ArrowSkill") || e.hasMetadata("Effect")) event.setCancelled(true)
  }

  @EventHandler def onPotionSplashEvent(event: PotionSplashEvent) = {
    val thrown = event.getPotion
    if (thrown != null) if (thrown.hasMetadata("ArrowSkill") || thrown.hasMetadata("Effect")) event.setCancelled(true)
  }

  @EventHandler def onDeath(event: EntityDeathEvent): Unit = {
    /*GiganticBerserk用*/
    //死んだMOBがGiganticBerserkの対象MOBでなければ終了
    if (!Util.isEnemy(event.getEntity.getType)) return
    val player = event.getEntity.getKiller
    //MOBを倒したプレイヤーがいなければ終了
    if (player == null) return
    //プレイヤーが整地ワールドに居ない場合終了
    if (!Util.isSeichiWorld(player)) return
    val GBTR = new GiganticBerserkTask
    GBTR.PlayerKillEnemy(player)
  }
}