package com.github.unchama.seichiassist.data

import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.MenuType
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.{AsyncInventorySetter, TypeConverter}
import com.github.unchama.seichiassist.{SeichiAssist, VotingFairyStrategy}
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.entity.Player
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.{Bukkit, Material}
import org.bukkit.ChatColor._

import java.util.UUID
import scala.collection.mutable

object MenuInventoryData {
  // とりあえず初期容量で60かくほしておけばよさそう
  private val headPartIndex = new mutable.HashMap[UUID, Int](60, 0.75)
  private val middlePartIndex = new mutable.HashMap[UUID, Int](60, 0.75)
  private val tailPartIndex = new mutable.HashMap[UUID, Int](60, 0.75)
  private val shopIndex = new mutable.HashMap[UUID, Int](60, 0.75)

  private val vfStrategyMapping = Map[VotingFairyStrategy, List[String]](
    VotingFairyStrategy.Much -> List(
      s"$RED$UNDERLINE${BOLD}ガンガンたべるぞ",
      s"$RESET${GRAY}とにかく妖精さんにりんごを開放します。",
      s"$RESET${GRAY}めっちゃ喜ばれます。"
    ),
    VotingFairyStrategy.More -> List(
      s"$YELLOW$UNDERLINE${BOLD}バッチリたべよう",
      s"$RESET${GRAY}食べ過ぎないように注意しつつ",
      s"$RESET${GRAY}妖精さんにりんごを開放します。",
      s"$RESET${GRAY}喜ばれます。"
    ),
    VotingFairyStrategy.Less -> List(
      s"$GREEN$UNDERLINE${BOLD}リンゴだいじに",
      s"$RESET${GRAY}少しだけ妖精さんにりんごを開放します。",
      s"$RESET${GRAY}伝えると大抵落ち込みます。"
    ),
    VotingFairyStrategy.None -> List(
      s"$BLUE$UNDERLINE${BOLD}リンゴつかうな",
      s"$RESET${GRAY}絶対にりんごを開放しません。",
      s"$RESET$GRAY"
    )
  )

  private val toMoveNicknameMenu = new IconItemStackBuilder(Material.BARRIER)
    .title(s"$YELLOW$UNDERLINE${BOLD}二つ名組合せメインメニューへ")
    .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
    .build()

  //投票特典受け取りボタン
  private def getVoteButtonLore(playerdata: PlayerData) = List(
    s"$RESET${GRAY}投票特典を受け取るには",
    s"$RESET${GRAY}投票ページで投票した後",
    s"$RESET${GRAY}このボタンをクリックします",
    s"$RESET${AQUA}特典受取済投票回数：${playerdata.p_givenvote}",
    s"$RESET${AQUA}所有投票pt：${playerdata.effectPoint}"
  )

  /**
   * ログイン時間
   *
   * @param page ページ
   * @return メニュー
   */
  def getRankingByPlayingTime(page: Int): Inventory = {
    val pageLimit = 14
    val inventory = getEmptyInventory(6, s"$DARK_PURPLE${BOLD}ログイン神ランキング")
    val rankStart = 10 * page
    val ranking = (rankStart to (rankStart + 9).min(SeichiAssist.ranklist_playtick.size))
      .zipWithIndex
      .map { case (rank, invIndex) =>
        val rankdata = SeichiAssist.ranklist_playtick(rank)
        val itemstack = new SkullItemStackBuilder(rankdata.name)
          .title(s"$YELLOW$BOLD${rank + 1}位:$WHITE${rankdata.name}")
          .lore(s"$RESET${GREEN}総ログイン時間:${TypeConverter.toTimeString(TypeConverter.toSecond(rankdata.playtick))}")
          .build()

        (invIndex, itemstack)
      }

    val nextPage = Option.when(page != pageLimit) {
      val itemstack = new SkullItemStackBuilder("MHF_ArrowDown")
        .title(s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキング${page + 2}ページ目へ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()

      (52, itemstack)
    }

    val prevPage = {
      val (title, lore, ign) = if (page == 0) (
        s"$YELLOW$UNDERLINE${BOLD}ホームへ",
        s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
        "MHF_ArrowLeft"
      ) else (
        // 整地神ランキング前ページを開く
        s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキング${page}ページ目へ",
        s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
        "MHF_ArrowUp"
      )

      val itemstack = new SkullItemStackBuilder(ign)
        .title(title)
        .lore(lore)
        .build()

      (45, itemstack)
    }

    (ranking :++ nextPage :+ prevPage).foreach { case (i, is) =>
      AsyncInventorySetter.setItemAsync(inventory, i, is)
    }
    inventory
  }

  /**
   * 投票回数
   *
   * @param page ページ
   * @return メニュー
   */
  def getRankingByVotingCount(page: Int): Inventory = {
    val pageLimit = 14
    val inventory = getEmptyInventory(6, s"$DARK_PURPLE${BOLD}投票神ランキング")

    val ranking = (10 * page)
      .to((10 * page + 9).min(SeichiAssist.ranklist_p_vote.size))
      .filter(r => SeichiAssist.ranklist_p_vote(r).p_vote != 0)
      .zipWithIndex
      .map { case (voteRank, invIndex) =>
        val rankdata = SeichiAssist.ranklist_p_vote(voteRank)
        val itemstack = new SkullItemStackBuilder(rankdata.name)
          .title(s"$YELLOW$BOLD${voteRank + 1}位:$WHITE${rankdata.name}")
          .lore(s"$RESET${GREEN}総投票回数:${rankdata.p_vote}")
          .build()

        (invIndex, itemstack)
      }

    val nextPage = Option.when(page != pageLimit) {
      (52, new SkullItemStackBuilder("MHF_ArrowDown")
        .title(s"$YELLOW$UNDERLINE${BOLD}投票神ランキング${page + 2}ページ目へ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build())
    }

    val prevPage = {
      val (title, lore, ign) = if (page == 0) (
        s"$YELLOW$UNDERLINE${BOLD}ホームへ",
        s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
        "MHF_ArrowLeft"
      ) else (
        s"$YELLOW$UNDERLINE${BOLD}投票神ランキング${page}ページ目へ",
        s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
        "MHF_ArrowUp"
      )
      val itemstack = new SkullItemStackBuilder(ign)
        .title(title)
        .lore(lore)
        .build()

      (45, itemstack)
    }

    (ranking :++ nextPage :+ prevPage).foreach { case (i, is) =>
      AsyncInventorySetter.setItemAsync(inventory, i, is)
    }

    inventory
  }

  /**
   * 二つ名組み合わせ
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def computeRefreshedCombineMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)
    //念のためエラー分岐
    if (isError(p, playerdata, "二つ名組み合わせ")) return null
    val inventory = getEmptyInventory(4, MenuType.COMBINE.inventoryTitle)
    //各ボタンの設定
    headPartIndex.put(uuid, 0)
    middlePartIndex.put(uuid, 0)
    tailPartIndex.put(uuid, 0)
    shopIndex.put(uuid, 0)
    Map(
      //実績ポイントの最新情報反映ボタン
      0 -> new IconItemStackBuilder(Material.EMERALD_ORE)
        .lore(
          s"$RESET${GREEN}クリックで情報を最新化",
          s"$RESET${RED}累計獲得量：${playerdata.achievePoint.cumulativeTotal}",
          s"$RESET${RED}累計消費量：${playerdata.achievePoint.used}",
          s"$RESET${AQUA}使用可能量：${playerdata.achievePoint.left}"
        )
        .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイント 情報"),

      // パーツショップ
      9 -> new IconItemStackBuilder(Material.ITEM_FRAME)
        .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイントショップ")
        .lore(s"$RESET${GREEN}クリックで開きます"),

      //エフェクトポイントからの変換ボタン
      1 -> new IconItemStackBuilder(Material.EMERALD)
        .title(s"$YELLOW$UNDERLINE${BOLD}ポイント変換ボタン")
        .lore(
          s"$RESET${RED}JMS投票で手に入るポイントを",
          s"$RESET${RED}実績ポイントに変換できます。",
          s"$RESET$YELLOW${BOLD}投票pt 10pt → 実績pt 3pt",
          s"$RESET${AQUA}クリックで変換を一回行います。",
          s"$RESET${GREEN}所有投票pt :${playerdata.effectPoint}",
          s"$RESET${GREEN}所有実績pt :${playerdata.achievePoint.left}"
        ),

      4 -> {
        val nickname = playerdata.settings.nickname
        val playerTitle = Nicknames.getTitleFor(nickname.id1, nickname.id2, nickname.id3)
        new IconItemStackBuilder(Material.BOOK)
          .title(s"$YELLOW$UNDERLINE${BOLD}現在の二つ名の確認")
          .lore(s"$RESET$RED「$playerTitle」")
      },

      11 -> new IconItemStackBuilder(Material.WATER_BUCKET)
        .title(s"$YELLOW$UNDERLINE${BOLD}前パーツ選択画面")
        .lore(s"$RESET${RED}クリックで移動します"),

      13 -> new IconItemStackBuilder(Material.MILK_BUCKET)
        .title(s"$YELLOW$UNDERLINE${BOLD}中パーツ選択画面")
        .lore(s"$RESET${RED}クリックで移動します"),

      15 -> new IconItemStackBuilder(Material.LAVA_BUCKET)
        .title(s"$YELLOW$UNDERLINE${BOLD}後パーツ選択画面")
        .lore(s"$RESET${RED}クリックで移動します"),

      27 -> new SkullItemStackBuilder("MHF_ArrowLeft")
        .title(s"$YELLOW$UNDERLINE${BOLD}実績・二つ名メニューへ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
    ).foreach { case (k, v) => AsyncInventorySetter.setItemAsync(inventory, k, v.build())}

    inventory
  }

  def setHeadingIndex(uuid: UUID, k: MenuType, index: Int): Unit = {
    k match {
      case MenuType.HEAD => headPartIndex.put(uuid, index)
      case MenuType.MIDDLE => middlePartIndex.put(uuid, index)
      case MenuType.TAIL => tailPartIndex.put(uuid, index)
      case MenuType.SHOP => shopIndex.put(uuid, index)
    }
  }

  def getHeadingIndex(uuid: UUID, k: MenuType): Option[Int] = {
    k match {
      case MenuType.HEAD => headPartIndex.get(uuid)
      case MenuType.MIDDLE => middlePartIndex.get(uuid)
      case MenuType.TAIL => tailPartIndex.get(uuid)
      case MenuType.SHOP => shopIndex.get(uuid)
      case _ =>
        throw new AssertionError("This statement shouldn't be reached!")
    }
  }

  private val commonNextPageButton = new SkullItemStackBuilder("MHF_ArrowRight")
    .title(s"$YELLOW$UNDERLINE${BOLD}次ページへ")
    .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
    .build()

  /**
   * 二つ名 - 前パーツ
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def computeHeadPartCustomMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)
    if (isError(p, playerdata, "二つ名/前パーツ")) return null
    val inventory = getEmptyInventory(4, MenuType.HEAD.inventoryTitle)
    // TODO
    val headIndex = 1000
    val headParts = (headIndex until 9900)
      // 解除済みの実績をチェック
      .filter(i => playerdata.TitleFlags.contains(i))
      // 前パーツがあるかチェック
      .filter(Nicknames.getHeadPartFor(_).nonEmpty)
      .take(27)
      .zipWithIndex
      // あればボタン配置
      .map { case (i, invIndex) =>
        val headPart = Nicknames.getHeadPartFor(i).get()
        val itemstack = new IconItemStackBuilder(Material.WATER_BUCKET)
          .title(i.toString)
          .lore(s"$RESET${RED}前パーツ「$headPart」")
          .build()
        (invIndex, itemstack)
      }

    val nextPage = (35, commonNextPageButton)

    val makeUnselected = (31, new IconItemStackBuilder(Material.GRASS)
      .title(s"$YELLOW$UNDERLINE${BOLD}前パーツを未選択状態にする")
      .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで実行")
      .build()
    )

    val backButton = (27, toMoveNicknameMenu)

    (headParts :+ nextPage :+ makeUnselected :+ backButton).foreach { case (invIndex, is) =>
      AsyncInventorySetter.setItemAsync(inventory, invIndex, is)
    }
    inventory
  }

  private def isError(destination: Player, pd: PlayerData, operation: => String): Boolean = {
    pd.ifNotNull(return false)
    destination.sendMessage(s"${RED}playerdataがありません。管理者に報告してください")
    Bukkit.getServer.getConsoleSender.sendMessage(s"${RED}SeichiAssist[$operation]でエラー発生")
    Bukkit.getLogger.warning(s"${destination}のplayerdataがありません。開発者に報告してください")
    true
  }

  /**
   * 二つ名 - 中パーツ
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def computeMiddlePartCustomMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)
    if (isError(p, playerdata, "二つ名/中パーツ")) return null
    val inventory = getEmptyInventory(4, MenuType.MIDDLE.inventoryTitle)
    // TODO
    val headIndex = 9900
    val partButtons = (headIndex until 9999)
      // 実績を獲得しているかチェック
      .filter(playerdata.TitleFlags.contains)
      // 二つ名が割り当てられているか
      .filter(Nicknames.getMiddlePartFor(_).nonEmpty)
      .take(27)
      .zipWithIndex
      // ボタン配置
      .map { case (i, inventoryIndex) =>
        val middlePart = Nicknames.getMiddlePartFor(i).get
        val itemstack = new IconItemStackBuilder(Material.MILK_BUCKET)
          .title(i.toString)
          .lore(s"$RESET${RED}中パーツ「$middlePart」")
          .build()
        (inventoryIndex, itemstack)
      }

    val nextPageButton = (35, new SkullItemStackBuilder("MHF_ArrowRight")
      .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
      .title(s"$YELLOW$UNDERLINE${BOLD}次ページへ")
      .build()
    )

    val unselectButton = (
      31,
      new IconItemStackBuilder(Material.GRASS)
        .title(s"$YELLOW$UNDERLINE${BOLD}中パーツを未選択状態にする")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで実行")
        .build()
    )

    // 二つ名組合せメインページを開くボタン
    val backButton = (27, toMoveNicknameMenu)

    (partButtons :+ nextPageButton :+ unselectButton :+ backButton).foreach { case (invIndex, is) =>
      AsyncInventorySetter.setItemAsync(inventory, invIndex, is)
    }

    inventory
  }

  /**
   * 二つ名 - 後パーツ
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def computeTailPartCustomMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)
    if (isError(p, playerdata, "二つ名/後パーツ")) return null
    val inventory = getEmptyInventory(4, MenuType.TAIL.inventoryTitle)
    // TODO
    val headIndex = 1000
    val partButtons = (headIndex until 9900)
      // 解除済みの実績か？
      .filter(playerdata.TitleFlags.contains)
      // 後パーツが定義されているか？
      .filter(Nicknames.getTailPartFor(_).nonEmpty)
      .take(27)
      .zipWithIndex
      // あればボタン配置
      .map { case (i, invIndex) =>
        val tailPart = Nicknames.getTailPartFor(i).get
        val itemstack = new IconItemStackBuilder(Material.LAVA_BUCKET)
          .title(i.toString)
          .lore(s"$RESET${RED}後パーツ「$tailPart」")
          .build()
        (invIndex, itemstack)
      }

    val nextPage = (35, new SkullItemStackBuilder("MHF_ArrowRight")
      .title(s"$YELLOW$UNDERLINE${BOLD}次ページへ")
      .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
      .build()
    )

    val unselect = (31, new IconItemStackBuilder(Material.GRASS)
      .title(s"$YELLOW$UNDERLINE${BOLD}後パーツを未選択状態にする")
      .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで実行")
      .build()
    )

    val backButton = (27, toMoveNicknameMenu)

    (partButtons :+ nextPage :+ unselect :+ backButton).foreach { case (invIndex, item) =>
      AsyncInventorySetter.setItemAsync(inventory, invIndex, item)
    }

    inventory
  }

  /**
   * 実績ポイントショップ
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def computePartsShopMenu(p: Player): Inventory = { //プレイヤーを取得
    val uuid = p.getUniqueId
    //プレイヤーデータ
    val playerdata = SeichiAssist.playermap(uuid)
    if (isError(p, playerdata, "実績ポイントショップ")) return null
    val inventory = getEmptyInventory(4, MenuType.SHOP.inventoryTitle)
    val ap = playerdata.achievePoint
    val currentPoints = {
      val itemstack = new IconItemStackBuilder(Material.EMERALD_ORE)
        .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイント 情報")
        .lore(
          s"$RESET${GREEN}クリックで情報を最新化",
          s"$RESET${RED}累計獲得量：${ap.cumulativeTotal}",
          s"$RESET${RED}累計消費量：${ap.used}",
          s"$RESET${AQUA}使用可能量：${ap.left}"
        )

      (0, itemstack)
    }

    // TODO
    val headIndex = 9801
    val headTailParts = (headIndex to 9833)
      // 解除してないショップ限定二つ名を列挙する
      .takeWhile(!playerdata.TitleFlags.contains(_))
      .zipWithIndex
      .map { case (partId, invIndex) =>
        (
          invIndex,
          partId,
          new IconItemStackBuilder(Material.BEDROCK)
            .title(partId.toString)
            .lore(
              s"$RESET${RED}前・後パーツ「${Nicknames.getHeadPartFor(partId).getOrElse("")}」",
              s"$RESET${GREEN}必要ポイント：20",
              s"$RESET${AQUA}クリックで購入できます"
            )
            .build()
        )
      }

    val middleSellingPartHead = headTailParts.maxBy(_._2)._2

    val middleSellingParts = (middleSellingPartHead to 9938)
      .takeWhile(!playerdata.TitleFlags.contains(_))
      .zipWithIndex
      .map { case(partId, invIndex) =>
        (
          invIndex,
          partId,
          new IconItemStackBuilder(Material.BEDROCK)
            .title(partId.toString)
            .lore(
              s"$RESET${RED}中パーツ「${Nicknames.getMiddlePartFor(partId).getOrElse("")}」",
              s"$RESET${GREEN}必要ポイント：35",
              s"$RESET${AQUA}クリックで購入できます"
            )
            .build()
        )
      }

    val backButton = (27, null, toMoveNicknameMenu)
    (currentPoints +: headTailParts :++ middleSellingParts :+ backButton).foreach { case (invIndex: Int, _, item: ItemStack) =>
      AsyncInventorySetter.setItemAsync(inventory, invIndex, item)
    }

    inventory
  }

  /**
   * 投票妖精メニュー
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def getVotingMenuData(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)
    if (isError(p, playerdata, "投票妖精")) return null
    val inventory = getEmptyInventory(4, s"$DARK_PURPLE${BOLD}投票ptメニュー")
    val main = Map(
      0 -> {
        //投票pt受け取り
        new IconItemStackBuilder(Material.DIAMOND)
          .title(s"$LIGHT_PURPLE$UNDERLINE${BOLD}クリックで投票特典を受け取れます")
          .lore(getVoteButtonLore(playerdata))
          .enchanted()
      },
      9 -> {
        // ver0.3.2 投票ページ表示
        new IconItemStackBuilder(Material.BOOK_AND_QUILL)
          .title(s"$YELLOW$UNDERLINE${BOLD}投票ページにアクセス")
          .lore(
            s"$RESET${GREEN}投票すると様々な特典が！",
            s"$RESET${GREEN}1日1回投票出来ます",
            s"$RESET${DARK_GRAY}クリックするとチャット欄に",
            s"$RESET${DARK_GRAY}URLが表示されますので",
            s"$RESET${DARK_GRAY}Tキーを押してから",
            s"$RESET${DARK_GRAY}そのURLをクリックしてください"
          )
      },
      27 -> {
        //棒メニューに戻る
        new SkullItemStackBuilder("MHF_ArrowLeft")
          .title(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
          .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
      },
      2 -> {
        //妖精召喚時間設定トグルボタン
        new IconItemStackBuilder(Material.WATCH)
          .title(s"$AQUA$UNDERLINE${BOLD}マナ妖精 時間設定")
          .lore(
            s"$RESET$GREEN$BOLD${VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy)}",
            "",
            s"$RESET${GRAY}コスト",
            s"$RESET$RED$BOLD${playerdata.toggleVotingFairy * 2}投票pt",
            "",
            s"$RESET$DARK_RED${UNDERLINE}クリックで切替"
          )
      },
      11 -> {
        //妖精戦略トグル
        new IconItemStackBuilder(Material.PAPER)
          .title(s"$GOLD$UNDERLINE${BOLD}妖精とのお約束")
          .lore(vfStrategyMapping(playerdata.toggleGiveApple))
      },
      20 -> {
        //妖精音トグル
        val builder = new IconItemStackBuilder(Material.JUKEBOX)
          .title(s"$GOLD$UNDERLINE${BOLD}マナ妖精の音トグル")
          .lore(
            List(
              s"$RESET$DARK_GRAY※この機能はデフォルトでONです。",
              s"$RESET$DARK_RED${UNDERLINE}クリックで切替"
            ) :+ (if (playerdata.playFairySound) s"$RESET${GREEN}現在音が鳴る設定になっています。"
            else s"$RESET${RED}現在音が鳴らない設定になっています。")
          )

        if (playerdata.playFairySound)
          builder.enchanted()

        builder
      },
      4 -> {
        //妖精召喚
        new IconItemStackBuilder(Material.GHAST_TEAR)
          .title(s"$LIGHT_PURPLE$UNDERLINE${BOLD}マナ妖精 召喚")
          .lore(
            s"$RESET$GRAY${playerdata.toggleVotingFairy * 2}投票ptを消費して",
            s"$RESET${GRAY}マナ妖精を呼びます",
            s"$RESET${GRAY}時間 : ${VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy)}",
            s"$RESET${DARK_RED}Lv.10以上で解放"
          )
          .enchanted()
      }
    )

    val VF = Option.when(playerdata.usingVotingFairy) {
      Map(
        13 -> {
          //妖精 時間確認
          new IconItemStackBuilder(Material.COMPASS)
            .title(s"$LIGHT_PURPLE$UNDERLINE${BOLD}マナ妖精に時間を聞く")
            .lore(s"$RESET${GRAY}妖精さんはいそがしい。", s"${GRAY}帰っちゃう時間を教えてくれる")
            .enchanted()
        },

        6 -> {
          val yourRank = playerdata.calcPlayerApple()
          val fairyNotice = List(
            s"$RESET$RED$BOLD※ﾆﾝｹﾞﾝに見られないように気を付けること！",
            s"$RESET$RED$BOLD  毎日大妖精からデータを更新すること！",
            "",
            s"$RESET$GOLD${BOLD}昨日までにがちゃりんごを",
            s"$RESET$GOLD${BOLD}たくさんくれたﾆﾝｹﾞﾝたち",
            s"$RESET${DARK_GRAY}召喚されたらラッキーだよ！"
          )
          val ranking = (0 to 3.min(SeichiAssist.ranklist_p_apple.size))
            .map(SeichiAssist.ranklist_p_apple(_))
            .filter(_.p_apple != 0)
            .zipWithIndex
            .collect { case (rankdata, rank) =>
              List(
                s"${GRAY}たくさんくれたﾆﾝｹﾞﾝ第${rank + 1}位！",
                s"${GRAY}なまえ：${rankdata.name} りんご：${rankdata.p_apple}個"
              )
            }
            .flatten
          val globalInfo = List(
            s"${AQUA}ぜーんぶで${SeichiAssist.allplayergiveapplelong}個もらえた！",
            "",
            s"$GREEN↓呼び出したﾆﾝｹﾞﾝの情報↓",
            s"${GREEN}今までに${playerdata.p_apple}個もらった",
            s"${GREEN}ﾆﾝｹﾞﾝの中では${yourRank}番目にたくさんくれる！"
          )
          new IconItemStackBuilder(Material.GOLDEN_APPLE)
            .title(s"$YELLOW$UNDERLINE$BOLD㊙ がちゃりんご情報 ㊙")
            .lore(fairyNotice :++ ranking :++ globalInfo)
            .enchanted()
        }
      )
    }

    main.toList.appendedAll(VF.getOrElse(Map())).foreach { case (i, is) =>
      AsyncInventorySetter.setItemAsync(inventory, i, is.build())
    }
    inventory
  }

  /**
   * 色
   */
  private val GBTable = List[Short](12, 15, 4, 0, 3)
  /**
   * GiganticBerserk進化設定
   *
   * @param p
   * @return メニュー
   */
  def getGiganticBerserkBeforeEvolutionMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)
    if (isError(p, playerdata, "Gigantic進化前確認")) return null
    val inventory = getEmptyInventory(6, s"$DARK_PURPLE${BOLD}スキルを進化させますか?")
    val itemstack = new IconItemStackBuilder(Material.STAINED_GLASS_PANE, GBTable(playerdata.giganticBerserk.stage))
      .amount(1)
      .title(" ")
      .build()

    initGBShape(inventory, itemstack)

    {
      val itemstack = new IconItemStackBuilder(Material.NETHER_STAR)
        .title(s"${WHITE}スキルを進化させる")
        .lore(
          s"$RESET${GREEN}進化することにより、スキルの秘めたる力を解放できますが",
          s"$RESET${GREEN}スキルは更に大量の魂を求めるようになり",
          s"$RESET${GREEN}レベル(回復確率)がリセットされます",
          s"$RESET${RED}本当に進化させますか?",
          s"$RESET$DARK_RED${UNDERLINE}クリックで進化させる"
        )

      AsyncInventorySetter.setItemAsync(inventory, 31, itemstack.build())
    }
    inventory
  }

  def getGiganticBerserkAfterEvolutionMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)
    if (isError(p, playerdata, "GiganticBerserk進化後画面")) return null
    val inventory = getEmptyInventory(6, s"$LIGHT_PURPLE${BOLD}スキルを進化させました")
    val b = GBTable(playerdata.giganticBerserk.stage)

    {
      val itemstack = new IconItemStackBuilder(Material.STAINED_GLASS_PANE, b)
        .title(" ")
        .enchanted()
        .build()
      initGBShape(inventory, itemstack)
    }

    {
      val itemstack = new IconItemStackBuilder(Material.NETHER_STAR)
        .title(s"${WHITE}スキルを進化させました！")
        .lore(
          s"$RESET${GREEN}スキルの秘めたる力を解放することで、マナ回復量が増加し",
          s"$RESET${DARK_RED}スキルはより魂を求めるようになりました"
        )
        .build()
      AsyncInventorySetter.setItemAsync(inventory, 31, itemstack)
    }

    inventory
  }

  private def getEmptyInventory(rows: Int, title: String) = Bukkit.getServer.createInventory(null, rows * 9, title)

  private def initGBShape(inv: Inventory, item: ItemStack): Unit = {
    List(6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41)
      .foreach(AsyncInventorySetter.setItemAsync(inv, _, item))

    val itemstack = new IconItemStackBuilder(Material.STICK)
      .title(" ")
      .lore(" ")
      .build()

    List(30, 39, 40, 47)
      .foreach(AsyncInventorySetter.setItemAsync(inv, _, itemstack))
  }
}
