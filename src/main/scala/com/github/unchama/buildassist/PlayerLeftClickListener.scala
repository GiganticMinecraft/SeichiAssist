package com.github.unchama.buildassist

import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.seichiassist
import com.github.unchama.seichiassist.{CommonSoundEffects, MineStackObjectList, SeichiAssist}
import org.bukkit.ChatColor._
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.{Location, Material}

import scala.util.control.Breaks

class PlayerLeftClickListener extends Listener {

  import com.github.unchama.targetedeffect._

  @EventHandler
  def onPlayerLeftClickWithStick(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer

    event.getAction match {
      case Action.LEFT_CLICK_AIR | Action.LEFT_CLICK_BLOCK =>
      case _ => return
    }

    {
      val hasStickOnMainHand = player.getInventory.getItemInMainHand.getType == Material.STICK
      val actionWasOnMainHand = event.getHand == EquipmentSlot.HAND

      if (!hasStickOnMainHand || !actionWasOnMainHand) return
    }

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

    seichiassist.unsafe.runAsyncTargetedEffect(player)(
      sequentialEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        BuildMainMenu.open
      ),
      "BuildMainMenuを開く"
    )

    event.setCancelled(true)
  }

  // 範囲設置スキルの発動を担うハンドラメソッド
  @EventHandler
  def onPlayerAttemptToMassBuild(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val playerUuid = player.getUniqueId

    val playerWorld = player.getWorld

    val buildAssistPlayerData = BuildAssist.playermap.getOrElse(playerUuid, return)
    val seichiAssistPlayerData = SeichiAssist.playermap.getOrElse(playerUuid, return)

    val inventory = player.getInventory
    val offHandItem = inventory.getItemInOffHand

    event.getAction match {
      case Action.LEFT_CLICK_BLOCK =>
      case _ => return
    }

    if (!(player.isSneaking &&
        BuildAssist.materiallist.contains(offHandItem.getType) &&
        buildAssistPlayerData.ZoneSetSkillFlag)) return

    val clickedBlock = event.getClickedBlock

    if (!(offHandItem.getType == clickedBlock.getType && offHandItem.getData.getData == clickedBlock.getData)) {
      player.sendMessage(s"$RED「オフハンドと同じブロック」をクリックしてください。(基準になります)")
      return
    }

    //スキルの範囲設定用
    val areaInt = buildAssistPlayerData.AREAint
    val searchInt = areaInt + 1
    val areaIntB = areaInt * 2 + 1

    //設置範囲の基準となる座標
    val centerX = clickedBlock.getX
    val surfaceY = clickedBlock.getY
    val centerZ = clickedBlock.getZ

    var setBlockX = centerX - areaInt
    var setBlockZ = centerZ - areaInt

    var searchedInv = 9

    var ItemInInvAmount = 0

    var block_cnt = 0

    //MineStack No.用
    var no = -1

    val replaceableMaterials = Set(
      Material.AIR,
      Material.SNOW,
      Material.LONG_GRASS,
      Material.DEAD_BUSH,
      Material.YELLOW_FLOWER,
      Material.RED_ROSE,
      Material.RED_MUSHROOM,
      Material.BROWN_MUSHROOM
    )

    val fillTargetMaterials = Set(
      Material.AIR,
      Material.LAVA,
      Material.STATIONARY_LAVA,
      Material.WATER,
      Material.STATIONARY_WATER
    )

    val b1 = new Breaks
    b1.breakable {
      while (setBlockZ < centerZ + searchInt) {
        val b2 = new Breaks

        b2.breakable {
          //ブロック設置座標のブロック判別
          val surfaceLocation = new Location(playerWorld, setBlockX, surfaceY, setBlockZ)
          val currentBlockAtSurface = surfaceLocation.getBlock

          def commitPlacement(): Unit = {
            currentBlockAtSurface.setType(offHandItem.getType)
            currentBlockAtSurface.setData(offHandItem.getData.getData)

            //ブロックカウント
            block_cnt += 1
          }

          if (replaceableMaterials.contains(currentBlockAtSurface.getType)) {
            if (buildAssistPlayerData.zsSkillDirtFlag) {
              (1 to 5).foreach { setBlockYOffsetBelow =>
                val fillLocation = new Location(playerWorld, setBlockX, surfaceY - setBlockYOffsetBelow, setBlockZ)
                val blockToBeFilled = fillLocation.getBlock

                if (fillTargetMaterials.contains(blockToBeFilled.getType)) {
                  if (!Util.getWorldGuard.canBuild(player, fillLocation)) {
                    //他人の保護がかかっている場合は通知を行う
                    player.sendMessage(s"${RED}付近に誰かの保護がかかっているようです")
                  } else {
                    //保護のない場合、土を設置する処理
                    blockToBeFilled.setType(Material.DIRT)
                  }
                }
              }
            }

            //他人の保護がかかっている場合は処理を終了
            if (!Util.getWorldGuard.canBuild(player, surfaceLocation)) {
              player.sendMessage(s"${RED}付近に誰かの保護がかかっているようです")
              b1.break
            }

            //ここでMineStackの処理。flagがtrueならInvに関係なしにここに持ってくる
            if (buildAssistPlayerData.zs_minestack_flag) {
              val b3 = new Breaks
              b3.breakable {
                for (cnt <- 0 until MineStackObjectList.minestacklist.size) {
                  if (offHandItem.getType == MineStackObjectList.minestacklist(cnt).material && offHandItem.getData.getData.toInt == MineStackObjectList.minestacklist(cnt).durability) {
                    no = cnt
                    b3.break
                    //no:設置するブロック・max:設置できる最大量
                  }
                }
              }
              if (no > 0) {
                //設置するブロックがMineStackに登録済み
                //1引く
                val mineStackObj = MineStackObjectList.minestacklist(no)
                if (seichiAssistPlayerData.minestack.getStackedAmountOf(mineStackObj) > 0) {
                  seichiAssistPlayerData.minestack.subtractStackedAmountOf(mineStackObj, 1)

                  commitPlacement()
                  b2.break()
                }
              }
            }

            //インベントリの左上から一つずつ確認する。
            //※一度「該当アイテムなし」と判断したスロットは次回以降スキップする様に組んであるゾ
            while (searchedInv < 36) {
              //該当スロットのアイテムデータ取得
              val itemInInv = player.getInventory.getItem(searchedInv)
              if (itemInInv == null) {
              } else {
                ItemInInvAmount = itemInInv.getAmount
              }
              //スロットのアイテムが空白だった場合の処理(エラー回避のため)
              if (itemInInv == null) {
                //確認したスロットが空気だった場合に次スロットへ移動
                if (searchedInv == 35) {
                  searchedInv = 0
                } else if (searchedInv == 8) {
                  searchedInv = 36
                  player.sendMessage(RED.toString + "アイテムが不足しています！")
                } else {
                  searchedInv += 1
                }
                //スロットアイテムがオフハンドと一致した場合
              } else if (itemInInv.getType == offHandItem.getType) {
                //数量以外のデータ(各種メタ)が一致するかどうか検知(仮)
                val ItemInInvCheck = itemInInv
                ItemInInvCheck.setAmount(1)
                offHandItem.setAmount(1)

                if (ItemInInvCheck != offHandItem) {
                  if (searchedInv == 35) {
                    searchedInv = 0
                  } else if (searchedInv == 8) {
                    searchedInv = 36
                    player.sendMessage(RED.toString + "アイテムが不足しています!")
                  } else {
                    searchedInv += 1
                  }
                } else {
                  //取得したインベントリデータから数量を1ひき、インベントリに反映する
                  if (ItemInInvAmount == 1) {
                    itemInInv.setType(Material.AIR)
                    itemInInv.setAmount(1)
                  } else {
                    itemInInv.setAmount(ItemInInvAmount - 1)
                  }
                  player.getInventory.setItem(searchedInv, itemInInv)

                  commitPlacement()
                  b2.break()
                }
              } else {
                //確認したスロットが違うアイテムだった場合に、次のスロットへと対象を移す
                if (searchedInv == 35) {
                  searchedInv = 0
                } else if (searchedInv == 8) {
                  searchedInv = 36
                  player.sendMessage(RED.toString + "アイテムが不足しています!")
                } else {
                  searchedInv += 1
                }
              }
            }
          }

          if (searchedInv == 36) {
            b1.break()
          }
        }

        setBlockX += 1

        if (setBlockX > centerX + areaInt) {
          setBlockX = setBlockX - areaIntB
          setBlockZ += 1
        }
      }
    }

    if (Util.inTrackedWorld(player)) {
      Util.addBuild1MinAmount(player, new java.math.BigDecimal(block_cnt * BuildAssist.config.getBlockCountMag)) //設置した数を足す
    }
  }
}
