package com.github.unchama.seichiassist.data
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.TypeConverter
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Interval.Closed
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.{Bukkit, ChatColor, Material}

object MenuInventoryDataInScala extends IMenuInventoryData {
  private def getEmptyInventory(rows: Int Refined Closed[1, 6], title: String) = {
    Bukkit.getServer.createInventory(null, rows * 9, title)
  }

  // 0-origin
  override def getRankingByPlayingTime(page: Int): Inventory = {
    val inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ログイン神ランキング")
    // 1ページに表示される数を変えたいときはココを操作する
    val perPage = 10
    // ${perPage}人分のエントリが${pageLimit + 1}ページに渡って表示される。
    val pageLimit = 14

    // 次のページに到達するかリストの末尾にたどり着くまで
    val takeIndex = page * perPage until ((page + 1) * perPage max SeichiAssist.ranklist_playtick.size)
    val entries = takeIndex
      .map {
        i => (i, SeichiAssist.ranklist_playtick(i))
      }
      .zipWithIndex
      .map { case ((i, rd), ci) =>
        (
          ci,
          new SkullItemStackBuilder(rd.name)
            .amount(1)
            .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (i + 1) + "位:" + "" + ChatColor.WHITE + rd.name)
            .lore(
              ChatColor.RESET + "" + ChatColor.GREEN + "総ログイン時間:" + TypeConverter.toTimeString(TypeConverter.toSecond(rd.playtick))
            )
            .build()
        )
      }
    // これでプレイヤーの計算は終わり
    val nextPage = Option.when(page != pageLimit) {
      (
        52,
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowDown)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + (page + 2) + "ページ目へ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      )
    }

    val prevPage = (45, if (page == 0) {
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
        .amount(1)
        .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ")
        .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
        .build()
    } else {
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp)
        .amount(1)
        .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + page + "ページ目へ")
        .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
        .build()
    })

    (entries :++ nextPage :+ prevPage).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def getRankingByVotingCount(page: Int): Inventory = {
    val pageLimit = 14
    val perPage = 10
    val inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票神ランキング")
    val takeIndex = page * perPage until ((page + 1) * perPage max SeichiAssist.ranklist_playtick.size)
    val entries = takeIndex
      .map {
        i => (i, SeichiAssist.ranklist_playtick(i))
      }
      .takeWhile { case (_, rd) => rd.p_vote > 0 }
      .zipWithIndex
      .map { case ((i, rd), ci) =>
        (
          ci,
          new SkullItemStackBuilder(rd.name)
            .amount(1)
            .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (i + 1) + "位:" + "" + ChatColor.WHITE + rd.name)
            .lore(
              ChatColor.RESET + "" + ChatColor.GREEN + "総投票回数:" + rd.p_vote
            )
            .build()
        )
      }

    val nextPage = Option.when(page != pageLimit) {
      (
        52,
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowDown)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + (page + 2) + "ページ目へ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      )
    }

    val prevPage = (45, if (page == 0) {
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
        .amount(1)
        .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ")
        .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
        .build()
    } else {
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp)
        .amount(1)
        .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + page + "ページ目へ")
        .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
        .build()
    })

    (entries :++ nextPage :+ prevPage).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def computeRefreshedCombineMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap.getOrElse(player.getUniqueId, {
      // TODO: ここの考慮は要るのか？
      return null
    })
    val title = "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "二つ名組合せシステム"
    val inventory = getEmptyInventory(4, title)
    val entries = Map(
      0 -> {
        // dyn
        new IconItemStackBuilder(Material.EMERALD_ORE)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報")
          .lore(
            ChatColor.RESET + "" + ChatColor.GREEN + "クリックで情報を最新化",
            ChatColor.RESET + "" + ChatColor.RED + "累計獲得量：" + playerdata.achievePoint.cumulativeTotal,
            ChatColor.RESET + "" + ChatColor.RED + "累計消費量：" + playerdata.achievePoint.used,
            ChatColor.RESET + "" + ChatColor.AQUA + "使用可能量：" + playerdata.achievePoint.left
          )
          .build()
      },
      9 -> {
        // const
        new IconItemStackBuilder(Material.ITEM_FRAME)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイントショップ")
          .lore(ChatColor.RESET + "" + ChatColor.GREEN + "クリックで開きます")
          .build()
      },
      1 -> {
        // dyn
        new IconItemStackBuilder(Material.EMERALD)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ポイント変換ボタン")
          .lore(
            ChatColor.RESET + "" + ChatColor.RED + "JMS投票で手に入るポイントを",
            ChatColor.RESET + "" + ChatColor.RED + "実績ポイントに変換できます。",
            ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "投票pt 10pt → 実績pt 3pt",
            ChatColor.RESET + "" + ChatColor.AQUA + "クリックで変換を一回行います。",
            ChatColor.RESET + "" + ChatColor.GREEN + "所有投票pt :" + playerdata.effectPoint,
            ChatColor.RESET + "" + ChatColor.GREEN + "所有実績pt :" + playerdata.achievePoint.left
          )
          .build()
      },
      4 -> {
        // dyn
        val nickname = playerdata.settings.nickname
        val playerTitle = Nicknames.getTitleFor(nickname.id1, nickname.id2, nickname.id3)
        new IconItemStackBuilder(Material.BOOK)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在の二つ名の確認")
          .lore(ChatColor.RESET + "" + ChatColor.RED + "「" + playerTitle + "」")
          .build()
      },
      11 -> {
        // const
        new IconItemStackBuilder(Material.WATER_BUCKET)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツ選択画面")
          .lore(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します")
          .build()
      },
      13 -> {
        // const
        new IconItemStackBuilder(Material.MILK_BUCKET)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツ選択画面")
          .lore(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します")
          .build()
      },
      15 -> {
        // const
        new IconItemStackBuilder(Material.LAVA_BUCKET)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツ選択画面")
          .lore(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します")
          .build()
      },
      27 -> {
        // const
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      }
    )
    entries.foreach { case (i, is) => inventory.setItem(i, is) }
    inventory
  }

  override def computeHeadPartCustomMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap.getOrElse(player.getUniqueId, return null)
    val inventory = getEmptyInventory(4, "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "二つ名組合せ「前」")
    val startIndex: Int = ???
    (startIndex to 9900)
      // 全列挙
      .map(x => (x, Nicknames.getHeadPartFor(x)))
      // IDに対応する実績がある
      .filter(_._2.nonEmpty)
      // 実績を解除している
      .filter { case (i, _) => playerdata.TitleFlags.contains(i) }
      // 当然対応している実績が存在するのでunwrapしても問題ない
      .map { case (i, opt) => (i, opt.get) }
      // そのうち先頭の27個を
      .take(9 * 3)
      // ItemStackにして
      .map { case (i, partialTitle) =>
        new IconItemStackBuilder(Material.WATER_BUCKET)
          .amount(1)
          .title(i.toString)
          .lore(ChatColor.RESET + "" + ChatColor.RED + "前パーツ「" + partialTitle + "」")
          .build()
      }
      .zipWithIndex
      // インベントリへ
      .foreach { case (is, i) => inventory.setItem(i, is) }

    Map(
      35 -> {
        // const
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      },
      31 -> {
        // const
        new IconItemStackBuilder(Material.GRASS)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツを未選択状態にする")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行")
          .build()
      },
      27 -> {
        // const
        new IconItemStackBuilder(Material.BANNER)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      }
    ).foreach { case (i, is) => inventory.setItem(i, is)}

    inventory
  }

  // TODO L491の論理学パズル: 本当にこれで合ってる？
  override def computeMiddlePartCustomMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val inventory = getEmptyInventory(4, "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "二つ名組合せ「中」")

    val startIndex: Int = ???
    val entries = (startIndex to 9999)
      .map(id => (id, Nicknames.getMiddlePartFor(id)))
      .filter(_._2.nonEmpty)
      .map { case (id, opt) => (id, opt.get) }
      // 隠し中パーツは取得しているか確認
      .filter { case (id, _) => id <= 9910 || playerdata.TitleFlags.contains(id) }
      .map { case (id, part) =>
        new IconItemStackBuilder(Material.MILK_BUCKET)
          .title(id.toString)
          .lore(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + part + "」")
          .build()
      }
      .take(27)
      .zipWithIndex
      .map(_.swap)

    val nextPageOpt = Option.when(entries.size == 27) {
      (
        35,
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      )
    }

    val specialButton = Map(
      31 -> {
        new IconItemStackBuilder(Material.GRASS)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツを未選択状態にする")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行")
          .build()
      },

      27 -> {
        // const
        new IconItemStackBuilder(Material.BANNER)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      }
    )

    (entries :++ nextPageOpt :++ specialButton).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def computeTailPartCustomMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap.getOrElse(player.getUniqueId, return null)
    val inventory = getEmptyInventory(4, "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "二つ名組合せ「後」")
    val startIndex: Int = ???
    (startIndex to 9900)
      // 全列挙
      .map(x => (x, Nicknames.getTailPartFor(x)))
      // IDに対応する実績がある
      .filter(_._2.nonEmpty)
      // 実績を解除している
      .filter { case (i, _) => playerdata.TitleFlags.contains(i) }
      // 当然対応している実績が存在するのでunwrapしても問題ない
      .map { case (i, opt) => (i, opt.get) }
      // そのうち先頭の27個を
      .take(9 * 3)
      // ItemStackにして
      .map { case (i, partialTitle) =>
        new IconItemStackBuilder(Material.LAVA_BUCKET)
          .amount(1)
          .title(i.toString)
          .lore(ChatColor.RESET + "" + ChatColor.RED + "後パーツ「" + partialTitle + "」")
          .build()
      }
      .zipWithIndex
      // インベントリへ
      .foreach { case (is, i) => inventory.setItem(i, is) }

    Map(
      35 -> {
        // const
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      },
      31 -> {
        // const
        new IconItemStackBuilder(Material.GRASS)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツを未選択状態にする")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行")
          .build()
      },
      27 -> {
        // const
        new IconItemStackBuilder(Material.BANNER)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      }
    ).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def computePartsShopMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val flags = playerdata.TitleFlags
    val inventory = getEmptyInventory(4, "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "実績ポイントショップ")

    val sellingHeadTailParts = (9801 to 9833)
      .filter(id => !flags.contains(id))
      .map(id => {
        (
          id,
          new IconItemStackBuilder(Material.BEDROCK)
            .amount(1)
            .title(id.toString)
            .lore(
              ChatColor.RESET + "" + ChatColor.RED + "前・後パーツ「" + Nicknames.getHeadPartFor(id).getOrElse(() => "") + "」",
              ChatColor.RESET + "" + ChatColor.GREEN + "必要ポイント：20",
              ChatColor.RESET + "" + ChatColor.AQUA + "クリックで購入できます"
            )
            .build()
        )
      })

    val sellingMiddleParts = (9911 to 9938)
      .filter(id => !flags.contains(id))
      .map(id => {
        val is = new IconItemStackBuilder(Material.BEDROCK)
          .amount(1)
          .title(id.toString)
          .lore(
            ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + Nicknames.getMiddlePartFor(id).getOrElse(() => "") + "」",
            ChatColor.RESET + "" + ChatColor.GREEN + "必要ポイント：35",
            ChatColor.RESET + "" + ChatColor.AQUA + "クリックで購入できます"
          ).build()

        (id, is)
      })

    val playerSeek: Int = ???
    val entries = (sellingHeadTailParts :++ sellingMiddleParts)
      .filter { case (id, _) => playerSeek <= id }
      .take(27)
      .zipWithIndex
      .map { case ((_, is), id) => (id, is) }

    // 現在のページから溢れたときにだけ出現する
    val nextPageOpt = Option.when(entries.size == 27) {
      (
        35,
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowRight)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      )
    }

    (entries :++ nextPageOpt).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def getVotingMenuData(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票ptメニュー")

    val entries = Map(
      0 -> {
        // dyn
        new IconItemStackBuilder(Material.DIAMOND)
          .amount(1)
          .title(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "クリックで投票特典を受け取れます")
          .lore(
            ChatColor.RESET + "" + ChatColor.GRAY + "投票特典を受け取るには",
            ChatColor.RESET + "" + ChatColor.GRAY + "投票ページで投票した後",
            ChatColor.RESET + "" + ChatColor.GRAY + "このボタンをクリックします",
            ChatColor.RESET + "" + ChatColor.AQUA + "特典受取済投票回数：" + playerdata.p_givenvote,
            ChatColor.RESET + "" + ChatColor.AQUA + "所有投票pt：" + playerdata.effectPoint
          )
          // TODO originally DIG100
          .enchanted()
          .build()
      },
      9 -> {
        new IconItemStackBuilder(Material.BOOK_AND_QUILL)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票ページにアクセス")
          .lore(
            ChatColor.RESET + "" + ChatColor.GREEN + "投票すると様々な特典が！",
            ChatColor.RESET + "" + ChatColor.GREEN + "1日1回投票出来ます",
            ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に",
            ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので",
            ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから",
            ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
          )
          .build()
      },
      27 -> {
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ")
          .lore(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動")
          .build()
      },
      2 -> {
        new IconItemStackBuilder(Material.WATCH)
          .amount(1)
          .title(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 時間設定")
          .lore(
            ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + "" + VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy),
            "",
            ChatColor.RESET + "" + ChatColor.GRAY + "コスト",
            ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "" + playerdata.toggleVotingFairy * 2 + "投票pt",
            "",
            ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
          )
          .build()
      },
      11 -> {
        val strategy = playerdata.toggleGiveApple match {
          case 1 =>
            List(
              ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ガンガンたべるぞ",
              ChatColor.RESET + "" + ChatColor.GRAY + "とにかく妖精さんにりんごを開放します。",
              ChatColor.RESET + "" + ChatColor.GRAY + "めっちゃ喜ばれます。"
            )
          case 2 =>
            List(
              ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "バッチリたべよう",
              ChatColor.RESET + "" + ChatColor.GRAY + "食べ過ぎないように注意しつつ",
              ChatColor.RESET + "" + ChatColor.GRAY + "妖精さんにりんごを開放します。",
              ChatColor.RESET + "" + ChatColor.GRAY + "喜ばれます。"
            )
          case 3 =>
            List(
              ChatColor.GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リンゴだいじに",
              ChatColor.RESET + "" + ChatColor.GRAY + "少しだけ妖精さんにりんごを開放します。",
              ChatColor.RESET + "" + ChatColor.GRAY + "伝えると大抵落ち込みます。"
            )
          case 4 =>
            List(
              ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リンゴつかうな",
              ChatColor.RESET + "" + ChatColor.GRAY + "絶対にりんごを開放しません。",
              ChatColor.RESET + "" + ChatColor.GRAY + ""
            )
          case _ => throw new AssertionError("This statement shouldn't be reached!")
        }
        new IconItemStackBuilder(Material.PAPER)
          .amount(1)
          .title(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "妖精とのお約束")
          .lore(strategy)
          .build()
      },
      20 -> {
        val playSound: Boolean = playerdata.toggleVFSound
        val lore = if (playSound) {
          List(
            ChatColor.RESET + "" + ChatColor.GREEN + "現在音が鳴る設定になっています。",
            ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※この機能はデフォルトでONです。",
            ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
          )
        } else {
          List(
            ChatColor.RESET + "" + ChatColor.RED + "現在音が鳴らない設定になっています。",
            ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※この機能はデフォルトでONです。",
            ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
          )
        }
        val builder = new IconItemStackBuilder(Material.JUKEBOX)
          .amount(1)
          .title(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精の音トグル")
          .lore(lore)

        // TODO originally DIG100-compatible op
        if (!playSound) builder.enchanted()

        builder.build()
      },
      4 -> {
        new IconItemStackBuilder(Material.GHAST_TEAR)
          .amount(1)
          .title(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 召喚")
          .lore(
            ChatColor.RESET + "" + ChatColor.GRAY + "" + playerdata.toggleVotingFairy * 2 + "投票ptを消費して",
            ChatColor.RESET + "" + ChatColor.GRAY + "マナ妖精を呼びます",
            ChatColor.RESET + "" + ChatColor.GRAY + "時間 : " + VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy),
            ChatColor.RESET + "" + ChatColor.DARK_RED + "Lv.10以上で解放"
          )
          // TODO originally DIG100
          .enchanted()
          .build()
      }
    )

    val usingVF = playerdata.usingVotingFairy
    val vfCheckTimeOpt = Option.when(usingVF) {
      (
        13,
        new IconItemStackBuilder(Material.COMPASS)
          .amount(1)
          .title(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精に時間を聞く")
          .lore(
            ChatColor.RESET + "" + ChatColor.GRAY + "妖精さんはいそがしい。",
            ChatColor.GRAY + "帰っちゃう時間を教えてくれる"
          )
          .enchanted()
          .build()
      )
    }

    val vfRankingOpt = Option.when(usingVF) {
      val header = List(
        ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "※ﾆﾝｹﾞﾝに見られないように気を付けること！",
        ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "  毎日大妖精からデータを更新すること！",
        "",
        ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "昨日までにがちゃりんごを",
        ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "たくさんくれたﾆﾝｹﾞﾝたち",
        ChatColor.RESET + "" + ChatColor.DARK_GRAY + "召喚されたらラッキーだよ！"
      )

      val ranking = (0 to (3 max SeichiAssist.ranklist_p_apple.size))
        .map(SeichiAssist.ranklist_p_apple)
        .takeWhile(_.p_apple > 0)
        .zipWithIndex
        .flatMap { case (rankdata, rank) =>
          List(
            ChatColor.GRAY + "たくさんくれたﾆﾝｹﾞﾝ第" + (rank + 1) + "位！",
            ChatColor.GRAY + "なまえ：" + rankdata.name + " りんご：" + rankdata.p_apple + "個"
          )
        }

      val yourRank = playerdata.calcPlayerApple()
      val statistics = List(
        ChatColor.AQUA + "ぜーんぶで" + SeichiAssist.allplayergiveapplelong + "個もらえた！",
        "",
        ChatColor.GREEN + "↓呼び出したﾆﾝｹﾞﾝの情報↓",
        ChatColor.GREEN + "今までに" + playerdata.p_apple + "個もらった",
        ChatColor.GREEN + "ﾆﾝｹﾞﾝの中では" + yourRank + "番目にたくさんくれる！"
      )

      val lore = header :++ ranking :++ statistics

      (
        6,
        new IconItemStackBuilder(Material.GOLDEN_APPLE)
          .amount(1)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "㊙ がちゃりんご情報 ㊙")
          .lore(lore)
          .build()
      )
    }

    (entries ++ vfCheckTimeOpt ++ vfRankingOpt).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def getGiganticBerserkBeforeEvolutionMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "スキルを進化させますか?")
    val color: Short = playerdata.giganticBerserk.stage match {
      case 0 => 12
      case 1 => 15
      case 2 => 4
      case 3 => 0
      case 4 => 3
      case _ => throw new AssertionError("This statement shouldn't be reached!")
    }

    val is = new IconItemStackBuilder(Material.STAINED_GLASS_PANE, color)
      .title(" ")
      .build()

    // L954
    Set(6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41).foreach(inventory.setItem(_, is))

    val stick = new IconItemStackBuilder(Material.STICK)
      .amount(1)
      .title(" ")
      .lore()
      .build()

    Set(30, 39, 40, 47).foreach(inventory.setItem(_, stick))

    val evolve = new IconItemStackBuilder(Material.NETHER_STAR)
      .amount(1)
      .title(ChatColor.WHITE + "スキルを進化させる")
      .lore(
        ChatColor.RESET + "" + ChatColor.GREEN + "進化することにより、スキルの秘めたる力を解放できますが",
        ChatColor.RESET + "" + ChatColor.GREEN + "スキルは更に大量の魂を求めるようになり",
        ChatColor.RESET + "" + ChatColor.GREEN + "レベル(回復確率)がリセットされます",
        ChatColor.RESET + "" + ChatColor.RED + "本当に進化させますか?",
        ChatColor.RESET + "" + ChatColor.DARK_RED + ChatColor.UNDERLINE + "クリックで進化させる"
      )
      .build()

    inventory.setItem(31, evolve)

    inventory
  }

  override def getGiganticBerserkAfterEvolutionMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val inventory = getEmptyInventory(6, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "スキルを進化させました")
    val stage = playerdata.giganticBerserk.stage
    val color: Short = stage match {
      case 0 => 12
      case 1 => 15
      case 2 => 4
      case 3 => 0
      case 4 => 3
      case 5 => 12
      case _ => throw new AssertionError("This statement shouldn't be reached!")
    }
    val builder = new IconItemStackBuilder(Material.STAINED_GLASS_PANE, color)
      .title(" ")

    if (stage >= 4) {
      // TODO: Original: DAMAGE_ALL<1, Hidden>
      builder.enchanted()
    }

    val stick = new IconItemStackBuilder(Material.STICK)
      .amount(1)
      .title(" ")
      .lore()
      .build()

    Set(30, 39, 40, 47).foreach(inventory.setItem(_, stick))

    val evolved = new IconItemStackBuilder(Material.NETHER_STAR)
      .title(ChatColor.WHITE + "スキルを進化させました！")
      .lore(
        ChatColor.RESET + "" + ChatColor.GREEN + "スキルの秘めたる力を解放することで、マナ回復量が増加し",
        ChatColor.RESET + "" + ChatColor.DARK_RED + "スキルはより魂を求めるようになりました"
      )
      .build()

    inventory.setItem(31, evolved)

    inventory
  }
}
