package com.github.unchama.buildassist

import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.seichiassist.CommonSoundEffects
import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.Schedulers
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.sequentialEffect
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal

class PlayerRightClickListener : Listener {
  internal var playermap = BuildAssist.playermap

  @EventHandler
  fun onPlayerMenuUIEvent(event: PlayerInteractEvent) {
    //プレイヤーを取得
    val player = event.player
    //UUID取得
    val uuid = player.uniqueId
    //ワールドデータを取得
    val playerworld = player.world
    //プレイヤーが起こしたアクションを取得
    val action = event.action
    //アクションを起こした手を取得
    val equipmentslot = event.hand
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap[uuid] ?: return
    val playerdata_s = SeichiAssist.playermap[uuid] ?: return

    //プレイヤーデータが無い場合は処理終了
    if (playerdata == null) {
      return
    }

    if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
      //左クリックの処理
      if (player.inventory.itemInMainHand.type == Material.STICK) {
        //メインハンドに棒を持っているときの処理

        //オフハンドのアクション実行時処理を終了
        if (equipmentslot == EquipmentSlot.OFF_HAND) {
          return
        }

        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              BuildMainMenu.open
          ).runFor(player)
        }
      } else if (player.isSneaking) {

        //プレイヤーインベントリを取得
        val inventory = player.inventory
        //メインハンドとオフハンドを取得
        val mainhanditem = inventory.itemInMainHand
        val offhanditem = inventory.itemInOffHand

        //メインハンドにブロックがあるか
        val mainhandtoolflag = BuildAssist.materiallist.contains(mainhanditem.type)
        //オフハンドにブロックがあるか
        val offhandtoolflag = BuildAssist.materiallist.contains(offhanditem.type)


        //場合分け
        if (offhandtoolflag) {
          //スキルフラグON以外のときは終了
          if (!playerdata.ZoneSetSkillFlag == true) {
            return
          }
          //オフハンドの時

          //Location playerloc = player.getLocation();
          //Block block = player.getWorld().getBlockAt(playerloc.getBlockX(), playerloc.getBlockY() -1 , playerloc.getBlockZ());

          //プレイヤーの足の座標を取得
          val playerlocx = player.location.blockX
          val playerlocy = player.location.blockY
          val playerlocz = player.location.blockZ

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

          //オフハンドアイテムと、範囲内のブロックに一致する物があるかどうか判別
          //同じ物がない場合・同じ物が3か所以上のY軸で存在する場合→SetReady = false
          while (searchY < playerlocy + 2) {
            val block = player.world.getBlockAt(searchX, searchY, searchZ).state

            if (offhanditem.type == player.world.getBlockAt(searchX, searchY, searchZ).type && offhanditem.data.data == player.world.getBlockAt(searchX, searchY, searchZ).data) {

              if (Y1 == searchY || Y1 == 256) {
                Y1 = searchY
                SetReady = true
              } else if (Y2 == searchY || Y2 == 256) {
                Y2 = searchY
              } else {
                SetReady = false
                player.sendMessage(ChatColor.RED.toString() + "範囲内に「オフハンドと同じブロック」が多すぎます。(Y軸2つ分以内にして下さい)")
                break
              }
            }
            searchX++

            if (searchX > playerlocx + SEARCHint) {
              searchX = searchX - SEARCHintB
              searchZ++
              if (searchZ > playerlocz + SEARCHint) {
                searchZ = searchZ - SEARCHintB
                searchY++
              }

            }
          }

          if (Y1 == 256) {
            player.sendMessage(ChatColor.RED.toString() + "範囲内に「オフハンドと同じブロック」を設置してください。(基準になります)")
            SetReady = false
          }

          //上の処理で「スキル条件を満たしていない」と判断された場合、処理終了
          if (SetReady == false) {
            player.sendMessage(ChatColor.RED.toString() + "発動条件が満たされませんでした。")
          }

          if (SetReady == true) {
            //実際に範囲内にブロックを設置する処理
            //設置範囲の基準となる座標
            var setblockX = playerlocx - AREAint
            val setblockY = Y1
            var setblockZ = playerlocz - AREAint
            var setunder = 1

            var searchedInv = 9

            var ItemInInv: ItemStack? = null
            var ItemInInvAmount = 0

            val WGloc = Location(playerworld, 0.0, 0.0, 0.0)


            while (setblockZ < playerlocz + SEARCHint) {
              //ブロック設置座標のブロック判別
              if (player.world.getBlockAt(setblockX, setblockY, setblockZ).type == Material.AIR ||
                  player.world.getBlockAt(setblockX, setblockY, setblockZ).type == Material.SNOW ||
                  player.world.getBlockAt(setblockX, setblockY, setblockZ).type == Material.LONG_GRASS ||
                  player.world.getBlockAt(setblockX, setblockY, setblockZ).type == Material.DEAD_BUSH ||
                  player.world.getBlockAt(setblockX, setblockY, setblockZ).type == Material.YELLOW_FLOWER ||
                  player.world.getBlockAt(setblockX, setblockY, setblockZ).type == Material.RED_ROSE ||
                  player.world.getBlockAt(setblockX, setblockY, setblockZ).type == Material.RED_MUSHROOM ||
                  player.world.getBlockAt(setblockX, setblockY, setblockZ).type == Material.BROWN_MUSHROOM) {
                setunder = 1
                if (playerdata.zsSkillDirtFlag) {
                  while (setunder < 5) {
                    //設置対象の[setunder]分の下のブロックが空気かどうか
                    if (player.world.getBlockAt(setblockX, setblockY - setunder, setblockZ).type == Material.AIR ||
                        player.world.getBlockAt(setblockX, setblockY - setunder, setblockZ).type == Material.LAVA ||
                        player.world.getBlockAt(setblockX, setblockY - setunder, setblockZ).type == Material.STATIONARY_LAVA ||
                        player.world.getBlockAt(setblockX, setblockY - setunder, setblockZ).type == Material.WATER ||
                        player.world.getBlockAt(setblockX, setblockY - setunder, setblockZ).type == Material.STATIONARY_WATER) {
                      WGloc.x = setblockX.toDouble()
                      WGloc.y = (setblockY - setunder).toDouble()
                      WGloc.z = setblockZ.toDouble()
                      //他人の保護がかかっている場合は処理を終了
                      if (!Util.getWorldGuard()!!.canBuild(player, WGloc)) {
                        player.sendMessage(ChatColor.RED.toString() + "付近に誰かの保護がかかっているようです")
                      } else {
                        //保護のない場合、土を設置する処理
                        player.world.getBlockAt(setblockX, setblockY - setunder, setblockZ).type = Material.DIRT
                      }
                    }
                    setunder++

                  }
                }

                //他人の保護がかかっている場合は処理を終了
                WGloc.x = setblockX.toDouble()
                WGloc.y = setblockY.toDouble()
                WGloc.z = setblockZ.toDouble()
                if (!Util.getWorldGuard()!!.canBuild(player, WGloc)) {
                  player.sendMessage(ChatColor.RED.toString() + "付近に誰かの保護がかかっているようです")
                  break
                } else {
                  //ここでMineStackの処理。flagがtrueならInvに関係なしにここに持ってくる
                  if (playerdata.zs_minestack_flag) {//label指定は基本的に禁じ手だが、今回は後付けなので使わせてもらう。(解読性向上のため、1箇所のみの利用)
                    for (cnt in 0 until MineStackObjectList.minestacklist!!.size) {
                      if (offhanditem.type == MineStackObjectList.minestacklist!![cnt].material && offhanditem.data.data.toInt() == MineStackObjectList.minestacklist!![cnt].durability) {
                        no = cnt
                        break
                        //no:設置するブロック・max:設置できる最大量
                      }
                    }
                    if (no > 0) {
                      //設置するブロックがMineStackに登録済み
                      //1引く
                      val mineStackObj = MineStackObjectList.minestacklist!![no]
                      if (playerdata_s.minestack.getStackedAmountOf(mineStackObj) > 0) {
                        //player.sendMessage("MineStackよりブロック消費");
                        //player.sendMessage("MineStackブロック残量(前):" + playerdata_s.getMinestack().getNum(no));
                        playerdata_s.minestack.subtractStackedAmountOf(mineStackObj, 1)
                        //player.sendMessage("MineStackブロック残量(後):" + playerdata_s.getMinestack().getNum(no));

                        //設置処理
                        player.world.getBlockAt(setblockX, setblockY, setblockZ).type = offhanditem.type
                        player.world.getBlockAt(setblockX, setblockY, setblockZ).data = offhanditem.data.data

                        //ブロックカウント
                        block_cnt++

                        //あとの設定
                        setblockX++

                        if (setblockX > playerlocx + AREAint) {
                          setblockX = setblockX - AREAintB
                          setblockZ++
                        }
                        continue
                      }
                    }
                  }


                  //インベントリの左上から一つずつ確認する。
                  //※一度「該当アイテムなし」と判断したスロットは次回以降スキップする様に組んであるゾ
                  while (searchedInv < 36) {
                    //該当スロットのアイテムデータ取得
                    ItemInInv = player.inventory.getItem(searchedInv)
                    if (ItemInInv == null) {
                    } else {
                      ItemInInvAmount = ItemInInv.amount
                    }
                    //スロットのアイテムが空白だった場合の処理(エラー回避のため)
                    if (ItemInInv == null) {
                      //確認したスロットが空気だった場合に次スロットへ移動
                      if (searchedInv == 35) {
                        searchedInv = 0
                      } else if (searchedInv == 8) {
                        searchedInv = 36
                        player.sendMessage(ChatColor.RED.toString() + "アイテムが不足しています！")
                      } else {
                        searchedInv++
                      }
                      //スロットアイテムがオフハンドと一致した場合
                    } else if (ItemInInv.type == offhanditem.type) {
                      //数量以外のデータ(各種メタ)が一致するかどうか検知(仮)
                      val ItemInInvCheck = ItemInInv
                      ItemInInvCheck.amount = 1
                      offhanditem.amount = 1

                      if (ItemInInvCheck != offhanditem) {
                        if (searchedInv == 35) {
                          searchedInv = 0
                        } else if (searchedInv == 8) {
                          searchedInv = 36
                          player.sendMessage(ChatColor.RED.toString() + "アイテムが不足しています!")
                        } else {
                          searchedInv++
                        }
                      } else {
                        //取得したインベントリデータから数量を1ひき、インベントリに反映する
                        if (ItemInInvAmount == 1) {
                          ItemInInv.type = Material.AIR
                          ItemInInv.amount = 1
                        } else {
                          ItemInInv.amount = ItemInInvAmount - 1
                        }
                        player.inventory.setItem(searchedInv, ItemInInv)
                        //ブロックを設置する
                        player.world.getBlockAt(setblockX, setblockY, setblockZ).type = offhanditem.type
                        player.world.getBlockAt(setblockX, setblockY, setblockZ).data = offhanditem.data.data

                        block_cnt++  //設置数カウント
                        break

                      }
                    } else {
                      //確認したスロットが違うアイテムだった場合に、次のスロットへと対象を移す
                      if (searchedInv == 35) {
                        searchedInv = 0
                      } else if (searchedInv == 8) {
                        searchedInv = 36
                        player.sendMessage(ChatColor.RED.toString() + "アイテムが不足しています!")
                      } else {
                        searchedInv++
                      }
                    }
                  }
                }
              }
              if (searchedInv == 36) {
                break
              }

              setblockX++

              if (setblockX > playerlocx + AREAint) {
                setblockX = setblockX - AREAintB
                setblockZ++

              }
            }
          }
          //終了ログがうるさいので無くす
          //player.sendMessage(ChatColor.RED + "敷き詰めスキル：処理終了" ) ;

          if (Util.isBlockCount(player) == true) {
            Util.addBuild1MinAmount(player, BigDecimal(block_cnt * BuildAssist.config.blockCountMag))  //設置した数を足す
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
