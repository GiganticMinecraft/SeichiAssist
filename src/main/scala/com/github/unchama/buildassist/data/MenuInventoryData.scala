package com.github.unchama.buildassist.data

import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.buildassist.repo.InMemoryBulkFillRangeRepo
import com.github.unchama.seichiassist.util.{AsyncInventorySetter, ItemMetaFactory}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.{Bukkit, Material}

object MenuInventoryData {
  import scala.jdk.CollectionConverters._
  private val playerDurability = 3.toShort
  // scala.asJavaはくどいのであらかじめ特殊化
  private val clickToMove = java.util.Collections.singletonList(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
  private val intermidate2 = List(
    s"$RESET${GRAY}ハーフブロックを並べる時の位置を決めます。",
    s"$RESET${GRAY}クリックで切り替え"
  ).asJava
  private val intermidate3 = List(
    s"$RESET${GRAY}ブロックを並べるとき特定のブロックを破壊して並べます。",
    s"$RESET${GRAY}破壊対象ブロック：草,花,水,雪,松明,きのこ",
    s"$RESET${GRAY}クリックで切り替え"
  ).asJava
  private def setAsync0(inv: Inventory, slot: Int, itemStack: ItemStack) = AsyncInventorySetter.setItemAsync(inv, slot, itemStack)
  import scala.util.chaining._
  def getSetBlockSkillData(p: Player): Inventory = {
    //プレイヤーを取得
    val player = p.getPlayer
    //UUID取得
    val uuid = player.getUniqueId
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap(uuid) // If NPE, player is already offline

    val inventory = Bukkit.getServer.createInventory(null, 4 * 9, s"$DARK_PURPLE$BOLD「範囲設置スキル」設定画面")

    var lore = List(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
    // 範囲設置設定
    val useSkill = getOnOff(playerdata.isEnabledBulkBlockPlace)
    val useDirt = getOnOff(playerdata.fillSurface)
    val useMineStack = getOnOff(playerdata.preferMineStackBool)
    val currentRange = InMemoryBulkFillRangeRepo.get(player)

    inventory.tap { inv =>

      //初期画面へ移動
      def setAsync(slot: Int, stack: ItemStack) = setAsync0(inv, slot, stack)
      setAsync(0, new ItemStack(Material.BARRIER, 1).tap { s =>
        s.setDurability(playerDurability)
        val meta = Bukkit.getItemFactory.getItemMeta(s.getType).tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}元のページへ")
          m.setLore(lore.asJava)
        }
        s.setItemMeta(meta)
      })

      //地下空洞自動埋立のON/OFF
      setAsync(4, new ItemStack(Material.DIRT, 1).tap { s =>
        val meta = Bukkit.getItemFactory.getItemMeta(s.getType).tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}設置時に下の空洞を埋める機能")
          val lore = List(
            s"$RESET$AQUA${UNDERLINE}機能の使用設定：$useDirt",
            s"$RESET$AQUA${UNDERLINE}機能の範囲：地下5マスまで"
          )
          m.setLore(lore.asJava)
        }
        s.setItemMeta(meta)
      })


      //設定状況の表示
      setAsync(13, new ItemStack(Material.STONE, 1).tap { s =>
        val meta = Bukkit.getItemFactory.getItemMeta(s.getType).tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}現在の設定は以下の通りです")
          lore = List(
            s"$RESET$AQUA${UNDERLINE}スキルの使用設定：$useSkill",
            s"$RESET$AQUA${UNDERLINE}スキルの範囲設定：$currentRange×$currentRange",
            s"$RESET$AQUA${UNDERLINE}MineStack優先設定:$useMineStack")
          m.setLore(lore.asJava)
        }
        s.setItemMeta(meta)
      })

      //範囲を最大化
      setAsync(19, new ItemStack(Material.SKULL_ITEM, 11).tap { s =>
        s.setDurability(playerDurability)
        val meta = ItemMetaFactory.SKULL.getValue.tap { m =>
          m.setDisplayName(s"$RED$UNDERLINE${BOLD}範囲設定を最大値に変更")
          lore = List(
            s"$RESET${AQUA}現在の範囲設定：$currentRange×$currentRange",
            s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：11×11"
          )
          m.setLore(lore.asJava)
          m.setOwner("MHF_ArrowUp")
        }

        s.setItemMeta(meta)
      })

      //範囲を一段階増加
      setAsync(20, new ItemStack(Material.SKULL_ITEM, 7).tap { s =>
        s.setDurability(playerDurability)
        val meta = ItemMetaFactory.SKULL.getValue.tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階大きくする")
          lore = List(
            s"$RESET${AQUA}現在の範囲設定：$currentRange×$currentRange",
            s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：${currentRange + 2}×${currentRange + 2}",
            s"$RESET$RED※範囲設定の最大値は11×11※"
          )
          m.setLore(lore.asJava)
          m.setOwner("MHF_ArrowUp")
        }

        s.setItemMeta(meta)
      })

      //範囲を初期値へ
      setAsync(22, new ItemStack(Material.SKULL_ITEM, 5).tap { s =>
        s.setDurability(playerDurability)
        val meta = ItemMetaFactory.SKULL.getValue.tap { m =>
          m.setDisplayName(s"$RED$UNDERLINE${BOLD}範囲設定を初期値に変更")
          lore = List(
            s"$RESET${AQUA}現在の範囲設定：$currentRange×$currentRange",
            s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：5×5"
          )
          m.setLore(lore.asJava)
          m.setOwner("MHF_TNT")
        }

        s.setItemMeta(meta)
      })

      //範囲を一段階減少
      setAsync(24, new ItemStack(Material.SKULL_ITEM, 3).tap { s =>
        s.setDurability(playerDurability)
        val meta = ItemMetaFactory.SKULL.getValue.tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階小さくする")
          lore = List(
            s"$RESET${AQUA}現在の範囲設定：$currentRange×$currentRange",
            s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：${currentRange - 2}×${currentRange - 2}",
            s"$RESET$RED※範囲設定の最小値は3×3※"
          )
          m.setLore(lore.asJava)
          m.setOwner("MHF_ArrowDown")
        }

        s.setItemMeta(meta)
      })

      //範囲を最小化
      setAsync(25, new ItemStack(Material.SKULL_ITEM, 1).tap { s =>
        val meta = ItemMetaFactory.SKULL.getValue.tap { m =>
          m.setDisplayName(s"$RED$UNDERLINE${BOLD}範囲設定を最小値に変更")
          lore = List(
            s"$RESET${AQUA}現在の範囲設定：$currentRange×$currentRange",
            s"$RESET$AQUA${UNDERLINE}変更後の範囲設定：3×3"
          )
          m.setLore(lore.asJava)
          m.setOwner("MHF_ArrowDown")
        }
        s.setDurability(playerDurability)
        s.setItemMeta(meta)
      })

      //MineStack優先設定
      setAsync(35, new ItemStack(Material.CHEST, 1).tap { s =>
        val meta = Bukkit.getItemFactory.getItemMeta(s.getType).tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定：$useMineStack")
          lore = List(
            s"$RESET${GRAY}スキルでブロックを並べるとき",
            s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
            s"$RESET${GRAY}建築LV${BuildAssist.config.getRangeFillSkillPreferMineStackLevel}以上で利用可能",
            s"$RESET${GRAY}クリックで切り替え"
          )
          m.setLore(lore.asJava)
        }
        s.setItemMeta(meta)
      })
    }
  }

  //ブロックを並べる設定メニュー
  def getBlockLineUpData(p: Player): Inventory = {
    //UUID取得
    val uuid = p.getUniqueId
    //プレイヤーデータ
    val playerdata = BuildAssist.playermap(uuid)

    val inventory = Bukkit.getServer.createInventory(null, 4 * 9, s"$DARK_PURPLE$BOLD「ブロックを並べるスキル（仮）」設定").tap { inv =>
      def setAsync(slot: Int, stack: ItemStack) = setAsync0(inv, slot, stack)

      // ホームを開く
      setAsync(27, new ItemStack(Material.SKULL_ITEM, 1).tap { s =>
        s.setDurability(playerDurability)
        val meta = ItemMetaFactory.SKULL.getValue.tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
          m.setLore(clickToMove)
          m.setOwner("MHF_ArrowLeft")
        }

        s.setItemMeta(meta)
      })

      //ブロックを並べるスキル設定
      setAsync(0, new ItemStack(Material.WOOD, 1).tap { s =>
        val meta = s.getItemMeta.tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}ブロックを並べるスキル（仮） ：${BuildAssist.lineFillFlag(playerdata.lineFillFlag)}")
          val lore = List(
            s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
            s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
            s"$RESET${GRAY}建築LV${BuildAssist.config.getLinearFillSkillLevel}以上で利用可能",
            s"$RESET${GRAY}クリックで切り替え"
          )
          m.setLore(lore.asJava)
        }
        s.setItemMeta(meta)
      })

      //ブロックを並べるスキルハーフブロック設定
      setAsync(1, new ItemStack(Material.STEP, 1).tap { s =>
        val meta = s.getItemMeta.tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}ハーフブロック設定 ：${BuildAssist.lineUpStepStr(playerdata.lineUpStepFlag)}")
          m.setLore(intermidate2)
        }

        s.setItemMeta(meta)
      })

      //ブロックを並べるスキル一部ブロックを破壊して並べる設定
      setAsync(2, new ItemStack(Material.TNT, 1).tap { s =>
        val meta = s.getItemMeta.tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}破壊設定 ：${getOnOff(playerdata.breakLightBlockFlagBool)}")
          m.setLore(intermidate3)
        }

        s.setItemMeta(meta)
      })

      //MineStackの方を優先して消費する設定
      setAsync(8, new ItemStack(Material.CHEST, 1).tap { s =>
        val meta = Bukkit.getItemFactory.getItemMeta(Material.CHEST).tap { m =>
          m.setDisplayName(s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定 ：${getOnOff(playerdata.preferMineStackBool)}")
          val lore = List(
            s"$RESET${GRAY}スキルでブロックを並べるとき",
            s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
            s"$RESET${GRAY}建築LV${BuildAssist.config.getLinearFillSkillPreferMineStackLevel}以上で利用可能",
            s"$RESET${GRAY}クリックで切り替え"
          )
          m.setLore(lore.asJava)
        }

        s.setItemMeta(meta)
      })
    }

    inventory
  }

  def getOnOff(isOn: Boolean): String = if (isOn) "YES" else "NO"
}
