package com.github.unchama.buildassist

import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.seichiassist
import com.github.unchama.seichiassist.{CommonSoundEffects, MineStackObjectList, SeichiAssist}
import org.bukkit.ChatColor._
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.{EquipmentSlot, ItemStack}
import org.bukkit.{Location, Material}

import scala.util.control.Breaks

class PlayerLeftClickListener extends Listener {

  import com.github.unchama.targetedeffect.TargetedEffects._

  @EventHandler
  def onPlayerLeftClickWithStick(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val eventHand = event.getHand

    event.getAction match {
      case Action.LEFT_CLICK_AIR | Action.LEFT_CLICK_BLOCK =>
      case _ => return
    }

    {
      val hasStickOnMainHand = player.getInventory.getItemInMainHand.getType == Material.STICK
      val actionWasOnMainHand = eventHand == EquipmentSlot.HAND

      if (!hasStickOnMainHand || actionWasOnMainHand) return
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
    val action = event.getAction

    val playerWorld = player.getWorld

    val buildAssistPlayerData = BuildAssist.playermap.getOrElse(playerUuid, return)
    val seichiAssistPlayerData = SeichiAssist.playermap.getOrElse(playerUuid, return)

    val inventory = player.getInventory
    val offHandItem = inventory.getItemInOffHand

    action match {
      case Action.LEFT_CLICK_AIR | Action.LEFT_CLICK_BLOCK =>
      case _ => return
    }

    if (!(player.isSneaking &&
        BuildAssist.materiallist.contains(offHandItem.getType) &&
        buildAssistPlayerData.ZoneSetSkillFlag)) return

    //プレイヤーの足の座標を取得
    val playerLocX = player.getLocation.getBlockX
    val playerLocY = player.getLocation.getBlockY
    val playerLocZ = player.getLocation.getBlockZ

    //スキルの範囲設定用
    val areaInt = buildAssistPlayerData.AREAint
    val searchInt = areaInt + 1
    val areaIntB = areaInt * 2 + 1
    val searchIntB = searchInt * 2 + 1

    //同ブロック探索(7*6*7)の開始座標を計算
    var searchX = playerLocX - searchInt
    var searchY = playerLocY - 4
    var searchZ = playerLocZ - searchInt

    //同上(Y座標記録)
    var Y1 = 256
    var Y2 = 256

    //スキル発動条件を満たすか
    var shouldPerformSkill = false

    var block_cnt = 0

    //MineStack No.用
    var no = -1

    val b1 = new Breaks

    //オフハンドアイテムと、範囲内のブロックに一致する物があるかどうか判別
    //同じ物がない場合・同じ物が3か所以上のY軸で存在する場合→SetReady = false
    b1.breakable {
      while (searchY < playerLocY + 2) {
        val block = player.getWorld.getBlockAt(searchX, searchY, searchZ)

        if (offHandItem.getType == block.getType && offHandItem.getData.getData == block.getData) {
          if (Y1 == searchY || Y1 == 256) {
            Y1 = searchY
            shouldPerformSkill = true
          } else if (Y2 == searchY || Y2 == 256) {
            Y2 = searchY
          } else {
            shouldPerformSkill = false
            player.sendMessage(RED.toString + "範囲内に「オフハンドと同じブロック」が多すぎます。(Y軸2つ分以内にして下さい)")
            b1.break
          }
        }
        searchX += 1

        if (searchX > playerLocX + searchInt) {
          searchX = searchX - searchIntB
          searchZ += 1
          if (searchZ > playerLocZ + searchInt) {
            searchZ = searchZ - searchIntB
            searchY += 1
          }
        }
      }
    }

    if (Y1 == 256) {
      player.sendMessage(RED.toString + "範囲内に「オフハンドと同じブロック」を設置してください。(基準になります)")
      shouldPerformSkill = false
    }

    //上の処理で「スキル条件を満たしていない」と判断された場合、処理終了
    if (!shouldPerformSkill) {
      player.sendMessage(RED.toString + "発動条件が満たされませんでした。")
    }

    if (shouldPerformSkill) {
      //実際に範囲内にブロックを設置する処理
      //設置範囲の基準となる座標
      var setBlockX = playerLocX - areaInt
      val setBlockY = Y1
      var setBlockZ = playerLocZ - areaInt
      var setUnder = 1

      var searchedInv = 9

      var ItemInInv: ItemStack = null
      var ItemInInvAmount = 0

      val WGloc = new Location(playerWorld, 0.0, 0.0, 0.0)

      b1.breakable {
        val b2 = new Breaks

        while (setBlockZ < playerLocZ + searchInt) {
          b2.breakable {
            //ブロック設置座標のブロック判別
            if (player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).getType == Material.AIR ||
              player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).getType == Material.SNOW ||
              player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).getType == Material.LONG_GRASS ||
              player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).getType == Material.DEAD_BUSH ||
              player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).getType == Material.YELLOW_FLOWER ||
              player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).getType == Material.RED_ROSE ||
              player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).getType == Material.RED_MUSHROOM ||
              player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).getType == Material.BROWN_MUSHROOM) {
              setUnder = 1

              if (buildAssistPlayerData.zsSkillDirtFlag) {
                while (setUnder < 5) {
                  //設置対象の[setunder]分の下のブロックが空気かどうか
                  if (player.getWorld.getBlockAt(setBlockX, setBlockY - setUnder, setBlockZ).getType == Material.AIR ||
                    player.getWorld.getBlockAt(setBlockX, setBlockY - setUnder, setBlockZ).getType == Material.LAVA ||
                    player.getWorld.getBlockAt(setBlockX, setBlockY - setUnder, setBlockZ).getType == Material.STATIONARY_LAVA ||
                    player.getWorld.getBlockAt(setBlockX, setBlockY - setUnder, setBlockZ).getType == Material.WATER ||
                    player.getWorld.getBlockAt(setBlockX, setBlockY - setUnder, setBlockZ).getType == Material.STATIONARY_WATER) {
                    WGloc.setX(setBlockX.toDouble)
                    WGloc.setY((setBlockY - setUnder).toDouble)
                    WGloc.setZ(setBlockZ.toDouble)
                    //他人の保護がかかっている場合は処理を終了
                    if (!Util.getWorldGuard.canBuild(player, WGloc)) {
                      player.sendMessage(RED.toString + "付近に誰かの保護がかかっているようです")
                    } else {
                      //保護のない場合、土を設置する処理
                      player.getWorld.getBlockAt(setBlockX, setBlockY - setUnder, setBlockZ).setType(Material.DIRT)
                    }
                  }
                  setUnder += 1
                }
              }

              //他人の保護がかかっている場合は処理を終了
              WGloc.setX(setBlockX.toDouble)
              WGloc.setY(setBlockY.toDouble)
              WGloc.setZ(setBlockZ.toDouble)
              if (!Util.getWorldGuard.canBuild(player, WGloc)) {
                player.sendMessage(RED.toString + "付近に誰かの保護がかかっているようです")
                b1.break
              } else {
                //ここでMineStackの処理。flagがtrueならInvに関係なしにここに持ってくる
                if (buildAssistPlayerData.zs_minestack_flag) { //label指定は基本的に禁じ手だが、今回は後付けなので使わせてもらう。(解読性向上のため、1箇所のみの利用)
                  for (cnt <- 0 until MineStackObjectList.minestacklist.size) {
                    if (offHandItem.getType == MineStackObjectList.minestacklist(cnt).material && offHandItem.getData.getData.toInt == MineStackObjectList.minestacklist(cnt).durability) {
                      no = cnt
                      b1.break
                      //no:設置するブロック・max:設置できる最大量
                    }
                  }
                  if (no > 0) {
                    //設置するブロックがMineStackに登録済み
                    //1引く
                    val mineStackObj = MineStackObjectList.minestacklist(no)
                    if (seichiAssistPlayerData.minestack.getStackedAmountOf(mineStackObj) > 0) {
                      seichiAssistPlayerData.minestack.subtractStackedAmountOf(mineStackObj, 1)

                      //設置処理
                      player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).setType(offHandItem.getType)
                      player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).setData(offHandItem.getData.getData)

                      //ブロックカウント
                      block_cnt += 1

                      //あとの設定
                      setBlockX += 1

                      if (setBlockX > playerLocX + areaInt) {
                        setBlockX = setBlockX - areaIntB
                        setBlockZ += 1
                      }
                      b2.break()
                    }
                  }
                }


                //インベントリの左上から一つずつ確認する。
                //※一度「該当アイテムなし」と判断したスロットは次回以降スキップする様に組んであるゾ
                while (searchedInv < 36) {
                  //該当スロットのアイテムデータ取得
                  ItemInInv = player.getInventory.getItem(searchedInv)
                  if (ItemInInv == null) {
                  } else {
                    ItemInInvAmount = ItemInInv.getAmount
                  }
                  //スロットのアイテムが空白だった場合の処理(エラー回避のため)
                  if (ItemInInv == null) {
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
                  } else if (ItemInInv.getType == offHandItem.getType) {
                    //数量以外のデータ(各種メタ)が一致するかどうか検知(仮)
                    val ItemInInvCheck = ItemInInv
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
                        ItemInInv.setType(Material.AIR)
                        ItemInInv.setAmount(1)
                      } else {
                        ItemInInv.setAmount(ItemInInvAmount - 1)
                      }
                      player.getInventory.setItem(searchedInv, ItemInInv)
                      //ブロックを設置する
                      player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).setType(offHandItem.getType)
                      player.getWorld.getBlockAt(setBlockX, setBlockY, setBlockZ).setData(offHandItem.getData.getData)

                      block_cnt += 1 //設置数カウント
                      b1.break()
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
            }
          }

          if (searchedInv == 36) {
            b1.break()
          }

          setBlockX += 1

          if (setBlockX > playerLocX + areaInt) {
            setBlockX = setBlockX - areaIntB
            setBlockZ += 1
          }
        }
      }
    }

    if (Util.inTrackedWorld(player)) {
      Util.addBuild1MinAmount(player, new java.math.BigDecimal(block_cnt * BuildAssist.config.getBlockCountMag)) //設置した数を足す
    }
  }
}
