package com.github.unchama.buildassist

import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.seichiassist.{CommonSoundEffects, MineStackObjectList, SeichiAssist}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.{EquipmentSlot, ItemStack}
import org.bukkit.{Location, Material}

import scala.util.control.Breaks

class PlayerRightClickListener extends Listener {
  private var playermap = BuildAssist.playermap

  import com.github.unchama.targetedeffect.TargetedEffects._

  @EventHandler
  def onPlayerMenuUIEvent(event: PlayerInteractEvent) {
    //プレイヤーを取得
    val player = event.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    //ワールドデータを取得
    val playerworld = player.getWorld
    //プレイヤーが起こしたアクションを取得
    val action = event.getAction
    //アクションを起こした手を取得
    val equipmentslot = event.getHand
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap.getOrElse(uuid, return)
    val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)

    if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
      //左クリックの処理
      if (player.getInventory.getItemInMainHand.getType == Material.STICK) {
        //メインハンドに棒を持っているときの処理

        //オフハンドのアクション実行時処理を終了
        if (equipmentslot == EquipmentSlot.OFF_HAND) {
          return
        }

        event.setCancelled(true)


        sequentialEffect[Player](
            CommonSoundEffects.menuTransitionFenceSound,
            BuildMainMenu.open
        )(player).unsafeRunAsync {
          case Left(error) =>
            println("Caught exception while opening BuildMainMenu")
            error.printStackTrace()
        }
      } else if (player.isSneaking) {

        //プレイヤーインベントリを取得
        val inventory = player.getInventory
        //メインハンドとオフハンドを取得
        val mainhanditem = inventory.getItemInMainHand
        val offhanditem = inventory.getItemInOffHand

        //メインハンドにブロックがあるか
        val mainhandtoolflag = BuildAssist.materiallist.contains(mainhanditem.getType)
        //オフハンドにブロックがあるか
        val offhandtoolflag = BuildAssist.materiallist.contains(offhanditem.getType)


        //場合分け
        if (offhandtoolflag) {
          //スキルフラグON以外のときは終了
          if (!playerdata.ZoneSetSkillFlag) {
            return
          }
          //オフハンドの時

          //Location playerloc = player.getLocation();
          //Block block = player.getWorld().getBlockAt(playerloc.getBlockX(), playerloc.getBlockY() -1 , playerloc.getBlockZ());

          //プレイヤーの足の座標を取得
          val playerlocx = player.getLocation.getBlockX
          val playerlocy = player.getLocation.getBlockY
          val playerlocz = player.getLocation.getBlockZ

          //スキルの範囲設定用
          val AREAint = playerdata.AREAint
          val SEARCHint = AREAint + 1
          val AREAintB = AREAint * 2 + 1
          val SEARCHintB = SEARCHint * 2 + 1


          //同ブロック探索(7*6*7)の開始座標を計算
          var searchX = playerlocx - SEARCHint
          var searchY = playerlocy - 4
          var searchZ = playerlocz - SEARCHint


          //同上(Y座標記録)
          var Y1 = 256
          var Y2 = 256

          //スキル発動条件を満たすか
          var SetReady = false

          //
          var block_cnt = 0

          //MineStack No.用
          var no = -1

          //MineStack設置できる最大量取得しておく
          val max = 0

          val b1 = new Breaks

          //オフハンドアイテムと、範囲内のブロックに一致する物があるかどうか判別
          //同じ物がない場合・同じ物が3か所以上のY軸で存在する場合→SetReady = false
          b1.breakable {
            while (searchY < playerlocy + 2) {
              val block = player.getWorld.getBlockAt(searchX, searchY, searchZ).getState

              if (offhanditem.getType == player.getWorld.getBlockAt(searchX, searchY, searchZ).getType && offhanditem.getData.getData == player.getWorld.getBlockAt(searchX, searchY, searchZ).getData) {

                if (Y1 == searchY || Y1 == 256) {
                  Y1 = searchY
                  SetReady = true
                } else if (Y2 == searchY || Y2 == 256) {
                  Y2 = searchY
                } else {
                  SetReady = false
                  player.sendMessage(RED.toString() + "範囲内に「オフハンドと同じブロック」が多すぎます。(Y軸2つ分以内にして下さい)")
                  b1.break
                }
              }
              searchX += 1

              if (searchX > playerlocx + SEARCHint) {
                searchX = searchX - SEARCHintB
                searchZ += 1
                if (searchZ > playerlocz + SEARCHint) {
                  searchZ = searchZ - SEARCHintB
                  searchY += 1
                }

              }
            }
          }

          if (Y1 == 256) {
            player.sendMessage(RED.toString() + "範囲内に「オフハンドと同じブロック」を設置してください。(基準になります)")
            SetReady = false
          }

          //上の処理で「スキル条件を満たしていない」と判断された場合、処理終了
          if (SetReady == false) {
            player.sendMessage(RED.toString() + "発動条件が満たされませんでした。")
          }

          if (SetReady == true) {
            //実際に範囲内にブロックを設置する処理
            //設置範囲の基準となる座標
            var setblockX = playerlocx - AREAint
            val setblockY = Y1
            var setblockZ = playerlocz - AREAint
            var setunder = 1

            var searchedInv = 9

            var ItemInInv: ItemStack = null
            var ItemInInvAmount = 0

            val WGloc = new Location(playerworld, 0.0, 0.0, 0.0)

            b1.breakable {
              val b2 = new Breaks

              while (setblockZ < playerlocz + SEARCHint) {
                b2.breakable {
                  //ブロック設置座標のブロック判別
                  if (player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).getType == Material.AIR ||
                      player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).getType == Material.SNOW ||
                      player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).getType == Material.LONG_GRASS ||
                      player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).getType == Material.DEAD_BUSH ||
                      player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).getType == Material.YELLOW_FLOWER ||
                      player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).getType == Material.RED_ROSE ||
                      player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).getType == Material.RED_MUSHROOM ||
                      player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).getType == Material.BROWN_MUSHROOM) {
                    setunder = 1
                    if (playerdata.zsSkillDirtFlag) {
                      while (setunder < 5) {
                        //設置対象の[setunder]分の下のブロックが空気かどうか
                        if (player.getWorld.getBlockAt(setblockX, setblockY - setunder, setblockZ).getType == Material.AIR ||
                            player.getWorld.getBlockAt(setblockX, setblockY - setunder, setblockZ).getType == Material.LAVA ||
                            player.getWorld.getBlockAt(setblockX, setblockY - setunder, setblockZ).getType == Material.STATIONARY_LAVA ||
                            player.getWorld.getBlockAt(setblockX, setblockY - setunder, setblockZ).getType == Material.WATER ||
                            player.getWorld.getBlockAt(setblockX, setblockY - setunder, setblockZ).getType == Material.STATIONARY_WATER) {
                          WGloc.setX(setblockX.toDouble)
                          WGloc.setY((setblockY - setunder).toDouble)
                          WGloc.setZ(setblockZ.toDouble)
                          //他人の保護がかかっている場合は処理を終了
                          if (!Util.getWorldGuard.canBuild(player, WGloc)) {
                            player.sendMessage(RED.toString() + "付近に誰かの保護がかかっているようです")
                          } else {
                            //保護のない場合、土を設置する処理
                            player.getWorld.getBlockAt(setblockX, setblockY - setunder, setblockZ).setType(Material.DIRT)
                          }
                        }
                        setunder += 1

                      }
                    }

                    //他人の保護がかかっている場合は処理を終了
                    WGloc.setX(setblockX.toDouble)
                    WGloc.setY(setblockY.toDouble)
                    WGloc.setZ(setblockZ.toDouble)
                    if (!Util.getWorldGuard.canBuild(player, WGloc)) {
                      player.sendMessage(RED.toString() + "付近に誰かの保護がかかっているようです")
                      b1.break
                    } else {
                      //ここでMineStackの処理。flagがtrueならInvに関係なしにここに持ってくる
                      if (playerdata.zs_minestack_flag) {//label指定は基本的に禁じ手だが、今回は後付けなので使わせてもらう。(解読性向上のため、1箇所のみの利用)
                        for (cnt <- 0 until MineStackObjectList.minestacklist.size) {
                          if (offhanditem.getType == MineStackObjectList.minestacklist(cnt).material && offhanditem.getData.getData.toInt == MineStackObjectList.minestacklist(cnt).durability) {
                            no = cnt
                            b1.break
                            //no:設置するブロック・max:設置できる最大量
                          }
                        }
                        if (no > 0) {
                          //設置するブロックがMineStackに登録済み
                          //1引く
                          val mineStackObj = MineStackObjectList.minestacklist(no)
                          if (playerdata_s.minestack.getStackedAmountOf(mineStackObj) > 0) {
                            //player.sendMessage("MineStackよりブロック消費");
                            //player.sendMessage("MineStackブロック残量(前):" + playerdata_s.getMinestack().getNum(no));
                            playerdata_s.minestack.subtractStackedAmountOf(mineStackObj, 1)
                            //player.sendMessage("MineStackブロック残量(後):" + playerdata_s.getMinestack().getNum(no));

                            //設置処理
                            player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).setType(offhanditem.getType)
                            player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).setData(offhanditem.getData.getData)

                            //ブロックカウント
                            block_cnt += 1

                            //あとの設定
                            setblockX += 1

                            if (setblockX > playerlocx + AREAint) {
                              setblockX = setblockX - AREAintB
                              setblockZ += 1
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
                            player.sendMessage(RED.toString() + "アイテムが不足しています！")
                          } else {
                            searchedInv += 1
                          }
                          //スロットアイテムがオフハンドと一致した場合
                        } else if (ItemInInv.getType == offhanditem.getType) {
                          //数量以外のデータ(各種メタ)が一致するかどうか検知(仮)
                          val ItemInInvCheck = ItemInInv
                          ItemInInvCheck.setAmount(1)
                          offhanditem.setAmount(1)

                          if (ItemInInvCheck != offhanditem) {
                            if (searchedInv == 35) {
                              searchedInv = 0
                            } else if (searchedInv == 8) {
                              searchedInv = 36
                              player.sendMessage(RED.toString() + "アイテムが不足しています!")
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
                            player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).setType(offhanditem.getType)
                            player.getWorld.getBlockAt(setblockX, setblockY, setblockZ).setData(offhanditem.getData.getData)

                            block_cnt += 1  //設置数カウント
                            b1.break()
                          }
                        } else {
                          //確認したスロットが違うアイテムだった場合に、次のスロットへと対象を移す
                          if (searchedInv == 35) {
                            searchedInv = 0
                          } else if (searchedInv == 8) {
                            searchedInv = 36
                            player.sendMessage(RED.toString() + "アイテムが不足しています!")
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

                setblockX += 1

                if (setblockX > playerlocx + AREAint) {
                  setblockX = setblockX - AREAintB
                  setblockZ += 1

                }
              }
            }
          }
          //終了ログがうるさいので無くす
          //player.sendMessage(RED + "敷き詰めスキル：処理終了" ) ;

          if (Util.inTrackedWorld(player)) {
            Util.addBuild1MinAmount(player, new java.math.BigDecimal(block_cnt * BuildAssist.config.getBlockCountMag))  //設置した数を足す
          }

          return


        } else if (mainhandtoolflag) {
          //メインハンドの時
          return
        } else {
          //どちらにももっていない時処理を終了
          return
        }

      }
    }
  }
}
