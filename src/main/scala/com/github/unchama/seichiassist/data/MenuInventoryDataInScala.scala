package com.github.unchama.seichiassist.data
import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.data.MenuInventoryDataInScala.{GiganticBerserkAfterEvolutionMenu, GiganticBerserkBeforeEvolutionMenu}
import com.github.unchama.seichiassist.data.player.GiganticBerserk
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.TypeConverter
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Interval.Closed
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.{Bukkit, Material, Sound}

object MenuInventoryDataInScala extends IMenuInventoryData {
  private def getEmptyInventory(rows: Int Refined Closed[1, 6], title: String) = {
    Bukkit.getServer.createInventory(null, rows * 9, title)
  }

  // 0-origin
  override def getRankingByPlayingTime(page: Int): Inventory = {
    val inventory = getEmptyInventory(6, s"$DARK_PURPLE${BOLD}ログイン神ランキング")
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
            .title(s"$YELLOW$BOLD${i + 1}位:$WHITE${rd.name}")
            .lore(
              s"$RESET${GREEN}総ログイン時間:${TypeConverter.toTimeString(TypeConverter.toSecond(rd.playtick))}"
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
          .title(s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキング${page + 2}ページ目へ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      )
    }

    val prevPage = (45, if (page == 0) {
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
        .amount(1)
        .title(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()
    } else {
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp)
        .amount(1)
        .title(s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキング${page}ページ目へ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()
    })

    (entries :++ nextPage :+ prevPage).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def getRankingByVotingCount(page: Int): Inventory = {
    val pageLimit = 14
    val perPage = 10
    val inventory = getEmptyInventory(6, s"$DARK_PURPLE${BOLD}投票神ランキング")
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
            .title(s"$YELLOW$BOLD${i + 1}位:$WHITE${rd.name}")
            .lore(
              s"$RESET${GREEN}総投票回数:${rd.p_vote}"
            )
            .build()
        )
      }

    val nextPage = Option.when(page != pageLimit) {
      (
        52,
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowDown)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキング${page + 2}ページ目へ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      )
    }

    val prevPage = (45, if (page == 0) {
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
        .amount(1)
        .title(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()
    } else {
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp)
        .amount(1)
        .title(s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキング${page}ページ目へ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
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
    val title = s"$DARK_PURPLE${BOLD}二つ名組合せシステム"
    val inventory = getEmptyInventory(4, title)
    val entries = Map(
      0 -> {
        // dyn
        new IconItemStackBuilder(Material.EMERALD_ORE)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイント 情報")
          .lore(
            s"$RESET${GREEN}クリックで情報を最新化",
            s"$RESET${RED}累計獲得量：${playerdata.achievePoint.cumulativeTotal}",
            s"$RESET${RED}累計消費量：${playerdata.achievePoint.used}",
            s"$RESET${AQUA}使用可能量：${playerdata.achievePoint.left}"
          )
          .build()
      },
      9 -> {
        // const
        new IconItemStackBuilder(Material.ITEM_FRAME)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイントショップ")
          .lore(s"$RESET${GREEN}クリックで開きます")
          .build()
      },
      1 -> {
        // dyn
        new IconItemStackBuilder(Material.EMERALD)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}ポイント変換ボタン")
          .lore(
            s"$RESET${RED}JMS投票で手に入るポイントを",
            s"$RESET${RED}実績ポイントに変換できます。",
            s"$RESET$YELLOW${BOLD}投票pt 10pt → 実績pt 3pt",
            s"$RESET${AQUA}クリックで変換を一回行います。",
            s"$RESET${GREEN}所有投票pt :${playerdata.effectPoint}",
            s"$RESET${GREEN}所有実績pt :${playerdata.achievePoint.left}"
          )
          .build()
      },
      4 -> {
        // dyn
        val nickname = playerdata.settings.nickname
        val playerTitle = Nicknames.getTitleFor(nickname.id1, nickname.id2, nickname.id3)
        new IconItemStackBuilder(Material.BOOK)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}現在の二つ名の確認")
          .lore(s"$RESET$RED「$playerTitle」")
          .build()
      },
      11 -> {
        // const
        new IconItemStackBuilder(Material.WATER_BUCKET)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}前パーツ選択画面")
          .lore(s"$RESET${RED}クリックで移動します")
          .build()
      },
      13 -> {
        // const
        new IconItemStackBuilder(Material.MILK_BUCKET)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}中パーツ選択画面")
          .lore(s"$RESET${RED}クリックで移動します")
          .build()
      },
      15 -> {
        // const
        new IconItemStackBuilder(Material.LAVA_BUCKET)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}後パーツ選択画面")
          .lore(s"$RESET${RED}クリックで移動します")
          .build()
      },
      27 -> {
        // const
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}実績・二つ名メニューへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      }
    )
    entries.foreach { case (i, is) => inventory.setItem(i, is) }
    inventory
  }

  override def computeHeadPartCustomMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap.getOrElse(player.getUniqueId, return null)
    val inventory = getEmptyInventory(4, s"$DARK_PURPLE${BOLD}二つ名組合せ「前」")
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
          .lore(s"$RESET${RED}前パーツ「$partialTitle」")
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
          .title(s"$YELLOW$UNDERLINE${BOLD}次ページへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      },
      31 -> {
        // const
        new IconItemStackBuilder(Material.GRASS)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}前パーツを未選択状態にする")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで実行")
          .build()
      },
      27 -> {
        // const
        new IconItemStackBuilder(Material.BANNER)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}二つ名組合せメインメニューへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      }
    ).foreach { case (i, is) => inventory.setItem(i, is)}

    inventory
  }

  // TODO L491の論理学パズル: 本当にこれで合ってる？
  override def computeMiddlePartCustomMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val inventory = getEmptyInventory(4, s"$DARK_PURPLE${BOLD}二つ名組合せ「中」")

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
          .lore(s"$RESET${RED}中パーツ「$part」")
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
          .title(s"$YELLOW$UNDERLINE${BOLD}次ページへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      )
    }

    val specialButton = Map(
      31 -> {
        new IconItemStackBuilder(Material.GRASS)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}中パーツを未選択状態にする")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで実行")
          .build()
      },

      27 -> {
        // const
        new IconItemStackBuilder(Material.BANNER)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}二つ名組合せメインメニューへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      }
    )

    (entries :++ nextPageOpt :++ specialButton).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def computeTailPartCustomMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap.getOrElse(player.getUniqueId, return null)
    val inventory = getEmptyInventory(4, s"$DARK_PURPLE${BOLD}二つ名組合せ「後」")
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
          .lore(s"$RESET${RED}後パーツ「$partialTitle」")
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
          .title(s"$YELLOW$UNDERLINE${BOLD}次ページへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      },
      31 -> {
        // const
        new IconItemStackBuilder(Material.GRASS)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}後パーツを未選択状態にする")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで実行")
          .build()
      },
      27 -> {
        // const
        new IconItemStackBuilder(Material.BANNER)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}二つ名組合せメインメニューへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      }
    ).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def computePartsShopMenu(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val flags = playerdata.TitleFlags
    val inventory = getEmptyInventory(4, s"$DARK_PURPLE${BOLD}実績ポイントショップ")

    val sellingHeadTailParts = (9801 to 9833)
      .filter(id => !flags.contains(id))
      .map(id => {
        (
          id,
          new IconItemStackBuilder(Material.BEDROCK)
            .amount(1)
            .title(id.toString)
            .lore(
              s"$RESET${RED}前・後パーツ「${Nicknames.getHeadPartFor(id).getOrElse(() => "")}」",
              s"$RESET${GREEN}必要ポイント：20",
              s"$RESET${AQUA}クリックで購入できます"
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
            s"$RESET${RED}中パーツ「${Nicknames.getMiddlePartFor(id).getOrElse(() => "")}」",
            s"$RESET${GREEN}必要ポイント：35",
            s"$RESET${AQUA}クリックで購入できます"
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
          .title(s"$YELLOW$UNDERLINE${BOLD}次ページへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      )
    }

    (entries :++ nextPageOpt).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def getVotingMenuData(player: Player): Inventory = {
    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val inventory = getEmptyInventory(4, s"$DARK_PURPLE${BOLD}投票ptメニュー")

    val entries = Map(
      0 -> {
        // dyn
        new IconItemStackBuilder(Material.DIAMOND)
          .amount(1)
          .title(s"$LIGHT_PURPLE$UNDERLINE${BOLD}クリックで投票特典を受け取れます")
          .lore(
            s"$RESET${GRAY}投票特典を受け取るには",
            s"$RESET${GRAY}投票ページで投票した後",
            s"$RESET${GRAY}このボタンをクリックします",
            s"$RESET${AQUA}特典受取済投票回数：${playerdata.p_givenvote}",
            s"$RESET${AQUA}所有投票pt：${playerdata.effectPoint}"
          )
          // TODO originally DIG100
          .enchanted()
          .build()
      },
      9 -> {
        new IconItemStackBuilder(Material.BOOK_AND_QUILL)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}投票ページにアクセス")
          .lore(
            s"$RESET${GREEN}投票すると様々な特典が！",
            s"$RESET${GREEN}1日1回投票出来ます",
            s"$RESET${DARK_GRAY}クリックするとチャット欄に",
            s"$RESET${DARK_GRAY}URLが表示されますので",
            s"$RESET${DARK_GRAY}Tキーを押してから",
            s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          )
          .build()
      },
      27 -> {
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
          .build()
      },
      2 -> {
        new IconItemStackBuilder(Material.WATCH)
          .amount(1)
          .title(s"$AQUA$UNDERLINE${BOLD}マナ妖精 時間設定")
          .lore(
            s"$RESET$GREEN$BOLD${VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy)}",
            "",
            s"$RESET${GRAY}コスト",
            s"$RESET$RED$BOLD${playerdata.toggleVotingFairy * 2}投票pt",
            "",
            s"$RESET$DARK_RED${UNDERLINE}クリックで切替"
          )
          .build()
      },
      11 -> {
        val strategy = playerdata.toggleGiveApple match {
          case 1 =>
            List(
              s"$RED$UNDERLINE${BOLD}ガンガンたべるぞ",
              s"$RESET${GRAY}とにかく妖精さんにりんごを開放します。",
              s"$RESET${GRAY}めっちゃ喜ばれます。"
            )
          case 2 =>
            List(
              s"$YELLOW$UNDERLINE${BOLD}バッチリたべよう",
              s"$RESET${GRAY}食べ過ぎないように注意しつつ",
              s"$RESET${GRAY}妖精さんにりんごを開放します。",
              s"$RESET${GRAY}喜ばれます。"
            )
          case 3 =>
            List(
              s"$GREEN$UNDERLINE${BOLD}リンゴだいじに",
              s"$RESET${GRAY}少しだけ妖精さんにりんごを開放します。",
              s"$RESET${GRAY}伝えると大抵落ち込みます。"
            )
          case 4 =>
            List(
              s"$BLUE$UNDERLINE${BOLD}リンゴつかうな",
              s"$RESET${GRAY}絶対にりんごを開放しません。",
              s"$RESET$GRAY"
            )
          case _ => throw new AssertionError("This statement shouldn't be reached!")
        }
        new IconItemStackBuilder(Material.PAPER)
          .amount(1)
          .title(s"$GOLD$UNDERLINE${BOLD}妖精とのお約束")
          .lore(strategy)
          .build()
      },
      20 -> {
        val playSound: Boolean = playerdata.toggleVFSound
        val lore = if (playSound) {
          List(
            s"$RESET${GREEN}現在音が鳴る設定になっています。",
            s"$RESET$DARK_GRAY※この機能はデフォルトでONです。",
            s"$RESET$DARK_RED${UNDERLINE}クリックで切替"
          )
        } else {
          List(
            s"$RESET${RED}現在音が鳴らない設定になっています。",
            s"$RESET$DARK_GRAY※この機能はデフォルトでONです。",
            s"$RESET$DARK_RED${UNDERLINE}クリックで切替"
          )
        }
        val builder = new IconItemStackBuilder(Material.JUKEBOX)
          .amount(1)
          .title(s"$GOLD$UNDERLINE${BOLD}マナ妖精の音トグル")
          .lore(lore)

        // TODO originally DIG100-compatible op
        if (!playSound) builder.enchanted()

        builder.build()
      },
      4 -> {
        new IconItemStackBuilder(Material.GHAST_TEAR)
          .amount(1)
          .title(s"$LIGHT_PURPLE$UNDERLINE${BOLD}マナ妖精 召喚")
          .lore(
            s"$RESET$GRAY${playerdata.toggleVotingFairy * 2}投票ptを消費して",
            s"$RESET${GRAY}マナ妖精を呼びます",
            s"$RESET${GRAY}時間 : ${VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy)}",
            s"$RESET${DARK_RED}Lv.10以上で解放"
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
          .title(s"$LIGHT_PURPLE$UNDERLINE${BOLD}マナ妖精に時間を聞く")
          .lore(
            s"$RESET${GRAY}妖精さんはいそがしい。",
            s"${GRAY}帰っちゃう時間を教えてくれる"
          )
          .enchanted()
          .build()
      )
    }

    val vfRankingOpt = Option.when(usingVF) {
      val header = List(
        s"$RESET$RED$BOLD※ﾆﾝｹﾞﾝに見られないように気を付けること！",
        s"$RESET$RED$BOLD  毎日大妖精からデータを更新すること！",
        "",
        s"$RESET$GOLD${BOLD}昨日までにがちゃりんごを",
        s"$RESET$GOLD${BOLD}たくさんくれたﾆﾝｹﾞﾝたち",
        s"$RESET${DARK_GRAY}召喚されたらラッキーだよ！"
      )

      val ranking = (0 to (3 max SeichiAssist.ranklist_p_apple.size))
        .map(SeichiAssist.ranklist_p_apple)
        .takeWhile(_.p_apple > 0)
        .zipWithIndex
        .flatMap { case (rankdata, rank) =>
          List(
            s"${GRAY}たくさんくれたﾆﾝｹﾞﾝ第${rank + 1}位！",
            s"${GRAY}なまえ：${rankdata.name} りんご：${rankdata.p_apple}個"
          )
        }

      val yourRank = playerdata.calcPlayerApple()
      val statistics = List(
        s"${AQUA}ぜーんぶで${SeichiAssist.allplayergiveapplelong}個もらえた！",
        "",
        s"$GREEN↓呼び出したﾆﾝｹﾞﾝの情報↓",
        s"${GREEN}今までに${playerdata.p_apple}個もらった",
        s"${GREEN}ﾆﾝｹﾞﾝの中では${yourRank}番目にたくさんくれる！"
      )

      val lore = header :++ ranking :++ statistics

      (
        6,
        new IconItemStackBuilder(Material.GOLDEN_APPLE)
          .amount(1)
          .title(s"$YELLOW$UNDERLINE$BOLD㊙ がちゃりんご情報 ㊙")
          .lore(lore)
          .build()
      )
    }

    (entries ++ vfCheckTimeOpt ++ vfRankingOpt).foreach { case (i, is) => inventory.setItem(i, is) }

    inventory
  }

  override def getGiganticBerserkBeforeEvolutionMenu(player: Player): Inventory = {
    menuToInventory(GiganticBerserkBeforeEvolutionMenu)
  }

  // 注釈: これは型合わせとしてのみ用いる。
  private def menuToInventory(menu: Menu) = ???

  override def getGiganticBerserkAfterEvolutionMenu(player: Player): Inventory = {
    menuToInventory(GiganticBerserkAfterEvolutionMenu)
  }

  object GiganticBerserkBeforeEvolutionMenu extends Menu {
    /**
     * メニューを開く操作に必要な環境情報の型。
     * 例えば、メニューが利用するAPIなどをここを通して渡すことができる。
     */
    override type Environment = Unit
    /**
     * メニューのサイズとタイトルに関する情報
     */
    override val frame: MenuFrame = MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}スキルを進化させますか?")

    /**
     * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
     */
    override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = IO {
      val pd = SeichiAssist.playermap(player.getUniqueId)
      val color: Short = pd.giganticBerserk.stage match {
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

      val stick = new IconItemStackBuilder(Material.STICK)
        .title(" ")
        .lore()
        .build()

      val glasses = Seq(6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41).map(x => (x, new Button(is, List()))).toMap
      val sticks = Set(30, 39, 40, 47).map(x => (x, new Button(stick, List()))).toMap
      val executeButton = (
        31,
        new Button(
          new IconItemStackBuilder(Material.NETHER_STAR)
            .amount(1)
            .title(s"${WHITE}スキルを進化させる")
            .lore(
              s"$RESET${GREEN}進化することにより、スキルの秘めたる力を解放できますが",
              s"$RESET${GREEN}スキルは更に大量の魂を求めるようになり",
              s"$RESET${GREEN}レベル(回復確率)がリセットされます",
              s"$RESET${RED}本当に進化させますか?",
              s"$RESET$DARK_RED${UNDERLINE}クリックで進化させる"
            )
            .build(),
          List(
            new FilteredButtonEffect(
              ClickEventFilter.LEFT_CLICK,
              SequentialEffect(
                // GBのレベルを上げる
                UnfocusedEffect {
                  pd.giganticBerserk = GiganticBerserk(0, 0, pd.giganticBerserk.stage + 1)
                },
                FocusedSoundEffect(Sound.BLOCK_END_GATEWAY_SPAWN, 1f, 0.5f),
                FocusedSoundEffect(Sound.ENTITY_ENDERDRAGON_AMBIENT, 1f, 0.8f),
                GiganticBerserkAfterEvolutionMenu.open
              )
            )
          )
        )
      )

      new MenuSlotLayout(
        glasses ++ sticks + executeButton
      )
    }
  }

  object GiganticBerserkAfterEvolutionMenu extends Menu {
    /**
     * メニューを開く操作に必要な環境情報の型。
     * 例えば、メニューが利用するAPIなどをここを通して渡すことができる。
     */
    override type Environment = Unit
    /**
     * メニューのサイズとタイトルに関する情報
     */
    override val frame: MenuFrame = MenuFrame(6.chestRows, s"$LIGHT_PURPLE${BOLD}スキルを進化させました")

    /**
     * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
     */
    override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = IO {
      val pd = SeichiAssist.playermap(player.getUniqueId)
      val stage = pd.giganticBerserk.stage
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

      val glass = builder.build()
      val glasses = Seq(6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41).map((_, new Button(glass, List()))).toMap
      val stick = new IconItemStackBuilder(Material.STICK)
        .amount(1)
        .title(" ")
        .lore()
        .build()

      val sticks = Seq(30, 39, 40, 47).map((_, new Button(stick, List())))
      val executedButton = (
        31,
        new Button(
          new IconItemStackBuilder(Material.NETHER_STAR)
            .title(s"${WHITE}スキルを進化させました！")
            .lore(
              s"$RESET${GREEN}スキルの秘めたる力を解放することで、マナ回復量が増加し",
              s"$RESET${DARK_RED}スキルはより魂を求めるようになりました"
            )
            .build(),
          List(
            new FilteredButtonEffect(
              ClickEventFilter.LEFT_CLICK,
              SequentialEffect(

              )
            )
          )
        )
      )

      new MenuSlotLayout(glasses ++ sticks + executedButton)
    }
  }
}
