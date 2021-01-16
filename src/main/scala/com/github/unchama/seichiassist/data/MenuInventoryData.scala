package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.data.MenuInventoryData.MenuType._
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.{AsyncInventorySetter, ItemMetaFactory, TypeConverter}
import com.github.unchama.seichiassist.{LevelThresholds, SeichiAssist}
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.{ItemMeta, SkullMeta}
import org.bukkit.inventory.{Inventory, ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material}
import org.bukkit.ChatColor._

import java.util
import java.util.function.Consumer
import java.util.{Collections, UUID}
import scala.collection.mutable
import scala.util.chaining._

object MenuInventoryData { // 実際には60人も入ることは無いのでは？
  private val finishedHeadPageBuild = new mutable.HashMap[UUID, Boolean](60, 0.75)
  private val finishedMiddlePageBuild = new mutable.HashMap[UUID, Boolean](60, 0.75)
  private val finishedTailPageBuild = new mutable.HashMap[UUID, Boolean](60, 0.75)
  private val finishedShopPageBuild = new mutable.HashMap[UUID, Boolean](60, 0.75)
  private val headPartIndex = new mutable.HashMap[UUID, Int](60, 0.75)
  private val middlePartIndex = new mutable.HashMap[UUID, Int](60, 0.75)
  private val tailPartIndex = new mutable.HashMap[UUID, Int](60, 0.75)
  private val shopIndex = new mutable.HashMap[UUID, Int](60, 0.75)
  private val taihiIndex = new mutable.HashMap[UUID, Int](60, 0.75)
  private val loreTable = util.Arrays.asList(
    Collections.emptyList,
    util.Arrays.asList(
      s"$RED$UNDERLINE${BOLD}ガンガンたべるぞ",
      s"$RESET${GRAY}とにかく妖精さんにりんごを開放します。",
      s"$RESET${GRAY}めっちゃ喜ばれます。"
    ),
    util.Arrays.asList(
      s"$YELLOW$UNDERLINE${BOLD}バッチリたべよう",
      s"$RESET${GRAY}食べ過ぎないように注意しつつ",
      s"$RESET${GRAY}妖精さんにりんごを開放します。",
      s"$RESET${GRAY}喜ばれます。"
    ),
    util.Arrays.asList(
      s"$GREEN$UNDERLINE${BOLD}リンゴだいじに",
      s"$RESET${GRAY}少しだけ妖精さんにりんごを開放します。",
      s"$RESET${GRAY}伝えると大抵落ち込みます。"
    ),
    util.Arrays.asList(
      s"$BLUE$UNDERLINE${BOLD}リンゴつかうな",
      s"$RESET${GRAY}絶対にりんごを開放しません。",
      s"$RESET$GRAY"
    )
  )
  /**
   * (short) 3はダサいし、マジックコンスタントみたいだよね。
   */
  private val PLAYER_SKULL: Short = 3.toShort
  /**
   * ラムダをいちいち正確に打つのは退屈で疲れる作業だし、かといってメソッドでカプセル化するメリットもない。
   * 加えて、明示的に「まとめる」ことでJVMに対して最適化のヒントとしても使える。
   */
  private val FALSE = () => false
  private val DIG100 = (meta: ItemMeta) => {
    meta.addEnchant(Enchantment.DIG_SPEED, 100, false)
    Unit
  }
  private val toMoveNicknameMenu = build(
    Material.BARRIER,
    s"$YELLOW$UNDERLINE${BOLD}二つ名組合せメインメニューへ",
    s"$RESET$DARK_RED${UNDERLINE}クリックで移動"
  )

  //投票特典受け取りボタン
  private def getVoteButtonLore(playerdata: PlayerData) = util.Arrays.asList(
    s"$RESET${GRAY}投票特典を受け取るには",
    s"$RESET${GRAY}投票ページで投票した後",
    s"$RESET${GRAY}このボタンをクリックします",
    s"$RESET${AQUA}特典受取済投票回数：${playerdata.p_givenvote}",
    s"$RESET${AQUA}所有投票pt：${playerdata.effectPoint}"
  )

  /**
   * 整地量
   *
   * @param page ページ
   * @return メニュー
   */
  def getRankingBySeichiAmount(page: Int): Inventory = {
    val perPage = 45
    val pageLimit = 150 / 45 + 1
    val lowerBound = 100
    val inventory = getEmptyInventory(6, s"$DARK_PURPLE${BOLD}整地神ランキング")
    val itemstack = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL)
    var invIndex = 0
    (perPage * page until perPage + perPage * page)
      .foreach(rank => {
        if (rank >= SeichiAssist.ranklist.size) break //todo: break is not supported
        val rankdata = SeichiAssist.ranklist.apply(rank)
        if (rankdata.totalbreaknum < LevelThresholds.levelExpThresholds(lowerBound - 1)) { //レベル100相当の総整地量判定に変更
          break //todo: break is not supported

        }
        val lore = util.Arrays.asList(
          s"$RESET${GREEN}整地Lv:${rankdata.level}",
          s"$RESET${GREEN}総整地量:${rankdata.totalbreaknum}"
        )
        val skullmeta = buildSkullMeta(
          s"$YELLOW$BOLD${rank + 1}位:$WHITE${rankdata.name}",
          lore,
          rankdata.name
        )
        itemstack.setItemMeta(skullmeta)
        AsyncInventorySetter.setItemAsync(inventory, invIndex, itemstack.clone)
        invIndex += 1
      })
    if (page != pageLimit) { // 整地神ランキング次ページ目を開く
      val skullMeta = buildSkullMeta(
        s"$YELLOW$UNDERLINE${BOLD}整地神ランキング${page + 2}ページ目へ",
        Collections.singletonList(s"$RESET${DARK_RED}${UNDERLINE}クリックで移動"),
        "MHF_ArrowDown"
      )
      itemstack.setItemMeta(skullMeta)
      AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone)
    }
    // 1ページ目を開く
    val (name, lore, ign) = if (page == 0) {
      (
        s"$YELLOW$UNDERLINE${BOLD}ホームへ",
        Collections.singletonList(s"$RESET$DARK_RED${UNDERLINE}クリックで移動"),
        "MHF_ArrowLeft"
      )
    } else {
      // 整地神ランキング前ページ目を開く
      (
        s"$YELLOW$UNDERLINE${BOLD}整地神ランキング${page}ページ目へ",
        Collections.singletonList(s"$RESET$DARK_RED${UNDERLINE}クリックで移動"),
        "MHF_ArrowUp"
      )
    }
    val skullmeta = buildSkullMeta(name, lore, ign)
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone)

    // 総整地量の表記
    val lore = util.Arrays.asList(s"$RESET${AQUA}全プレイヤー総整地量:", s"$RESET$AQUA${SeichiAssist.allplayerbreakblockint}")
    val skullmeta = buildSkullMeta(
      s"$YELLOW$UNDERLINE${BOLD}整地鯖統計データ",
      lore,
      "unchama"
    )
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 53, itemstack.clone)

    inventory
  }

  /**
   * ログイン時間
   *
   * @param page ページ
   * @return メニュー
   */
  def getRankingByPlayingTime(page: Int): Inventory = {
    val pageLimit = 14
    val inventory = getEmptyInventory(6, s"$DARK_PURPLE${BOLD}ログイン神ランキング")
    val itemstack = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL)
    val rankStart = 10 * page
    (rankStart to (rankStart + 9).min(SeichiAssist.ranklist_playtick.size))
      .zipWithIndex
      .foreach { case (rank, invIndex) =>
        val rankdata = SeichiAssist.ranklist_playtick.apply(rank)
        val skullmeta = buildSkullMeta(
          s"$YELLOW$BOLD${rank + 1}位:$WHITE${rankdata.name}",
          Collections.singletonList(s"$RESET${GREEN}総ログイン時間:${TypeConverter.toTimeString(TypeConverter.toSecond(rankdata.playtick))}"),
          rankdata.name
        )
        itemstack.setItemMeta(skullmeta)
        AsyncInventorySetter.setItemAsync(inventory, invIndex, itemstack.clone)
      }

    if (page != pageLimit) {
      val skullmeta = buildSkullMeta(
        s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキング${page + 2}ページ目へ",
        Collections.singletonList(s"$RESET${DARK_RED}${UNDERLINE}クリックで移動"),
        "MHF_ArrowDown"
      )
      itemstack.setItemMeta(skullmeta)
      AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone)
    }
    val skullmeta = if (page == 0)
    // 前のページ / ホームへ
      buildSkullMeta(
        s"$YELLOW$UNDERLINE${BOLD}ホームへ",
        Collections.singletonList(s"$RESET${DARK_RED}${UNDERLINE}クリックで移動"),
        "MHF_ArrowLeft"
      )
    else {
      // 整地神ランキング前ページを開く;
      buildSkullMeta(
        s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキング${page}ページ目へ",
        Collections.singletonList(s"$RESET${DARK_RED}${UNDERLINE}クリックで移動"),
        "MHF_ArrowUp"
      )
    }
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone)

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
    val inventory = getEmptyInventory(6, s"${DARK_PURPLE}${BOLD}投票神ランキング")
    val itemstack = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL)

    (10 * page)
      .to((10 * page + 9).min(SeichiAssist.ranklist_p_vote.size))
      .filter(r => SeichiAssist.ranklist_p_vote(r).p_vote != 0)
      .zipWithIndex
      .foreach { case (voteRank, invIndex) =>
        val rankdata = SeichiAssist.ranklist_p_vote(voteRank)
        val skullmeta = buildSkullMeta(
          s"$YELLOW$BOLD${voteRank + 1}位:$WHITE${rankdata.name}",
          Collections.singletonList(s"$RESET${GREEN}総投票回数:${rankdata.p_vote}"),
          rankdata.name
        )
        itemstack.setItemMeta(skullmeta)
        AsyncInventorySetter.setItemAsync(inventory, invIndex, itemstack.clone)
      }

    if (page != pageLimit) { // 投票神ランキング次ページ目を開く
      val skullmeta = buildSkullMeta(
        s"$YELLOW$UNDERLINE${BOLD}投票神ランキング${page + 2}ページ目へ",
        Collections.singletonList(s"$RESET$DARK_RED${UNDERLINE}クリックで移動"),
        "MHF_ArrowDown"
      )
      itemstack.setItemMeta(skullmeta)
      AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone)
    }
    val skullmeta = if (page == 0)
      buildSkullMeta(
        s"$YELLOW$UNDERLINE${BOLD}ホームへ",
        Collections.singletonList(
          s"$RESET$DARK_RED${UNDERLINE}クリックで移動"
        ),
        "MHF_ArrowLeft"
      )
    else { // 投票神ランキング前ページを開く
      buildSkullMeta(
        s"$YELLOW$UNDERLINE${BOLD}投票神ランキング${page}ページ目へ",
        Collections.singletonList(
          s"$RESET$DARK_RED${UNDERLINE}クリックで移動"
        ),
        "MHF_ArrowUp"
      )
    }
    itemstack.setItemMeta(skullmeta)
    AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone)

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
    val playerdata = SeichiAssist.playermap.apply(uuid)
    //念のためエラー分岐
    if (isError(p, playerdata, "二つ名組み合わせ")) return null
    val inventory = getEmptyInventory(4, MenuType.COMBINE.invName)
    //各ボタンの設定
    finishedHeadPageBuild.put(uuid, false)
    finishedMiddlePageBuild.put(uuid, false)
    finishedTailPageBuild.put(uuid, false)
    finishedShopPageBuild.put(uuid, false)
    headPartIndex.put(uuid, 0)
    middlePartIndex.put(uuid, 0)
    tailPartIndex.put(uuid, 0)
    shopIndex.put(uuid, 0)
    taihiIndex.put(uuid, 0);
    {
      //実績ポイントの最新情報反映ボタン
      // dynamic button
      val lore = util.Arrays.asList(
        s"$RESET${GREEN}クリックで情報を最新化",
        s"$RESET${RED}累計獲得量：${playerdata.achievePoint.cumulativeTotal}",
        s"$RESET${RED}累計消費量：${playerdata.achievePoint.used}",
        s"$RESET${AQUA}使用可能量：${playerdata.achievePoint.left}"
      )
      val itemstack = build(
        Material.EMERALD_ORE,
        s"$YELLOW$UNDERLINE${BOLD}実績ポイント 情報",
        lore
      )
      AsyncInventorySetter.setItemAsync(inventory, 0, itemstack)
    }

    {
      // パーツショップ
      // const button
      val itemstack = build(
        Material.ITEM_FRAME,
        s"$YELLOW$UNDERLINE${BOLD}実績ポイントショップ",
        s"$RESET${GREEN}クリックで開きます"
      )
      AsyncInventorySetter.setItemAsync(inventory, 9, itemstack)
    }
    {
      //エフェクトポイントからの変換ボタン
      val lore = util.Arrays.asList(
        s"$RESET${RED}JMS投票で手に入るポイントを",
        s"$RESET${RED}実績ポイントに変換できます。",
        s"$RESET$YELLOW${BOLD}投票pt 10pt → 実績pt 3pt",
        s"$RESET${AQUA}クリックで変換を一回行います。",
        s"$RESET${GREEN}所有投票pt :${playerdata.effectPoint}",
        s"$RESET${GREEN}所有実績pt :${playerdata.achievePoint.left}"
      )
      val itemstack = build(Material.EMERALD, s"$YELLOW$UNDERLINE${BOLD}ポイント変換ボタン", lore)
      AsyncInventorySetter.setItemAsync(inventory, 1, itemstack)
    }
    {
      val nickname = playerdata.settings.nickname
      val playerTitle = Nicknames.getTitleFor(nickname.id1, nickname.id2, nickname.id3)
      val itemStack = build(
        Material.BOOK,
        s"$YELLOW$UNDERLINE${BOLD}現在の二つ名の確認",
        s"$RESET$RED「$playerTitle」")
      AsyncInventorySetter.setItemAsync(inventory, 4, itemStack)
    }
    {
      val toHeadSelection = build(Material.WATER_BUCKET, s"$YELLOW$UNDERLINE${BOLD}前パーツ選択画面", s"$RESET${RED}クリックで移動します")
      val toMiddleSelection = build(Material.MILK_BUCKET, s"$YELLOW$UNDERLINE${BOLD}中パーツ選択画面", s"$RESET${RED}クリックで移動します")
      val toTailSelection = build(Material.LAVA_BUCKET, s"$YELLOW$UNDERLINE${BOLD}後パーツ選択画面", s"$RESET${RED}クリックで移動します")
      AsyncInventorySetter.setItemAsync(inventory, 11, toHeadSelection)
      AsyncInventorySetter.setItemAsync(inventory, 13, toMiddleSelection)
      AsyncInventorySetter.setItemAsync(inventory, 15, toTailSelection)
    }
    {
      // const Button
      val itemstack = buildPlayerSkull(
        s"$YELLOW$UNDERLINE${BOLD}実績・二つ名メニューへ",
        s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
        "MHF_ArrowLeft"
      )
      AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone)
    }
    inventory
  }

  object MenuType extends Enumeration {

    case object HEAD extends MenuType {
      override def invName: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「前」"
    }

    case object MIDDLE extends MenuType {
      override def invName: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「中」"
    }

    case object TAIL extends MenuType {
      override def invName: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「後」"
    }

    case object SHOP extends MenuType {
      override def invName: String = s"$DARK_PURPLE${BOLD}実績ポイントショップ"
    }

    case object COMBINE extends MenuType {
      override def invName: String = s"$DARK_PURPLE${BOLD}二つ名組合せシステム"
    }

    private def apply(s: String): MenuType = new MenuType {
      override def invName: String = s
    }
  }

  sealed trait MenuType {
    def invName: String
  }

  def setHeadingIndex(uuid: UUID, k: MenuInventoryData.MenuType, index: Int): Unit = {
    k match {
      case HEAD =>
        headPartIndex.put(uuid, index)

      case MIDDLE =>
        middlePartIndex.put(uuid, index)

      case TAIL =>
        tailPartIndex.put(uuid, index)

      case SHOP =>
        shopIndex.put(uuid, index)

    }
  }

  def getHeadingIndex(uuid: UUID, k: MenuInventoryData.MenuType): Option[Int] = {
    k match {
      case HEAD =>
        return headPartIndex.get(uuid)
      case MIDDLE =>
        return middlePartIndex.get(uuid)
      case TAIL =>
        return tailPartIndex.get(uuid)
      case SHOP =>
        return shopIndex.get(uuid)
    }
    throw new AssertionError("This statement shouldn't be reached!")
  }

  /**
   * 二つ名 - 前パーツ
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def computeHeadPartCustomMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap.apply(uuid)
    if (isError(p, playerdata, "二つ名/前パーツ")) return null
    val inventory = getEmptyInventory(4, MenuType.HEAD.invName)
    if (finishedHeadPageBuild.getOrElse(uuid, false)) finishedHeadPageBuild.put(uuid, false)
    else headPartIndex.put(uuid, 1000)
    //解禁済みの実績をチェック→前パーツがあるかをチェック→あればボタン配置
    var inventoryIndex = 0
    for (i <- headPartIndex(uuid) until 9900) {
      if (inventoryIndex < 27) {
        if (playerdata.TitleFlags.contains(i)) {
          val maybeHeadPart = Nicknames.getHeadPartFor(i)
          if (maybeHeadPart.nonEmpty) {
            val itemstack = build(Material.WATER_BUCKET, i.toString, s"$RESET${RED}前パーツ「${maybeHeadPart.get}」")
            AsyncInventorySetter.setItemAsync(inventory, inventoryIndex, itemstack)
            inventoryIndex += 1
          }
        }
        else if (inventoryIndex == 27) { //次ページへのボタンを配置
          val itemstack = buildPlayerSkull(
            s"$YELLOW$UNDERLINE${BOLD}次ページへ",
            s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
            "MHF_ArrowRight"
          )
          // 統一性のために右下へ
          AsyncInventorySetter.setItemAsync(inventory, 35, itemstack)
          finishedHeadPageBuild.put(uuid, true)
          break //todo: break is not supported

        }
      } else {

      }
    }
    //パーツ未選択状態にするボタン
    // Pure Button
    val itemstack = build(
      Material.GRASS,
      s"$YELLOW$UNDERLINE${BOLD}前パーツを未選択状態にする",
      s"$RESET$DARK_RED${UNDERLINE}クリックで実行"
    )
    AsyncInventorySetter.setItemAsync(inventory, 31, itemstack)

    // 二つ名組合せメインページボタン
    AsyncInventorySetter.setItemAsync(inventory, 27, toMoveNicknameMenu)

    inventory
  }

  private def isError(destination: Player, pd: PlayerData, operation: => String): Boolean = {
    if (pd == null) {
      destination.sendMessage(s"${RED}playerdataがありません。管理者に報告してください")
      Bukkit.getServer.getConsoleSender.sendMessage(s"${RED}SeichiAssist[$operation]でエラー発生")
      Bukkit.getLogger.warning(s"${destination}のplayerdataがありません。開発者に報告してください")
      return true
    }
    false
  }

  /**
   * 二つ名 - 中パーツ
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def computeMiddlePartCustomMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap.apply(uuid)
    if (isError(p, playerdata, "二つ名/中パーツ")) return null
    val inventory = getEmptyInventory(4, MenuType.MIDDLE.invName)
    if (finishedMiddlePageBuild.getOrElse(uuid, FALSE)) finishedMiddlePageBuild.put(uuid, false)
    else middlePartIndex.put(uuid, 9900)
    //パーツがあるかをチェック→あればボタン配置
    var inventoryIndex = 0
    for (i <- middlePartIndex(uuid) until 9999) {
      if (inventoryIndex < 27) {
        val maybeMiddlePart = Nicknames.getMiddlePartFor(i)
        //一部の「隠し中パーツ」は取得しているかの確認
        if (9911 <= i && playerdata.TitleFlags.contains(i) && maybeMiddlePart.nonEmpty || maybeMiddlePart.nonEmpty) {
          val itemstack = build(Material.MILK_BUCKET, i.toString, s"$RESET${RED}中パーツ「${maybeMiddlePart.get}」")
          AsyncInventorySetter.setItemAsync(inventory, inventoryIndex, itemstack)
          inventoryIndex += 1
        }
      } else if (inventoryIndex == 27) {
        val lore = Collections.singletonList(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        val itemstack = buildPlayerSkull(s"$YELLOW$UNDERLINE${BOLD}次ページへ", lore, "MHF_ArrowRight")
        AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone)
        finishedMiddlePageBuild.put(uuid, true)
        break //todo: break is not supported

      }
    }
    val itemstack = build(
      Material.GRASS,
      s"$YELLOW$UNDERLINE${BOLD}中パーツを未選択状態にする",
      s"$RESET$DARK_RED${UNDERLINE}クリックで実行"
    )
    AsyncInventorySetter.setItemAsync(inventory, 31, itemstack)

    // 二つ名組合せメインページを開くボタン
    AsyncInventorySetter.setItemAsync(inventory, 27, toMoveNicknameMenu)

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
    val playerdata = SeichiAssist.playermap.apply(uuid)
    if (isError(p, playerdata, "二つ名/後パーツ")) return null
    val inventory = getEmptyInventory(4, MenuType.TAIL.invName)
    if (!finishedTailPageBuild.getOrElse(uuid, FALSE)) tailPartIndex.put(uuid, 1000)
    //解禁済みの実績をチェック→後パーツがあるかをチェック→あればボタン配置
    var inventoryIndex = 0
    for (i <- tailPartIndex(uuid) until 9900) {
      if (inventoryIndex < 27) {
        if (playerdata.TitleFlags.contains(i)) {
          val maybeTailPart = Nicknames.getTailPartFor(i)
          if (maybeTailPart.nonEmpty) {
            val itemstack = build(Material.LAVA_BUCKET, i.toString, s"$RESET${RED}後パーツ「${maybeTailPart.get}」")
            AsyncInventorySetter.setItemAsync(inventory, inventoryIndex, itemstack)
            inventoryIndex += 1
          }
        } else if (inventoryIndex == 27) {
          val itemstack = buildPlayerSkull(
            s"$YELLOW$UNDERLINE${BOLD}次ページへ",
            Collections.singletonList(s"$RESET$DARK_RED${UNDERLINE}クリックで移動"),
            "MHF_ArrowRight")
          AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone)
          finishedTailPageBuild.put(uuid, true)
          break //todo: break is not supported

        }
      } else {

      }
    }
    val itemstack = build(
      Material.GRASS,
      s"$YELLOW$UNDERLINE${BOLD}後パーツを未選択状態にする",
      s"$RESET$DARK_RED${UNDERLINE}クリックで実行"
    )
    AsyncInventorySetter.setItemAsync(inventory, 31, itemstack)

    AsyncInventorySetter.setItemAsync(inventory, 27, toMoveNicknameMenu)

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
    val playerdata = SeichiAssist.playermap.apply(uuid)
    if (isError(p, playerdata, "実績ポイントショップ")) return null
    val inventory = getEmptyInventory(4, MenuType.SHOP.invName)
    val ap = playerdata.achievePoint
    val lore = util.Arrays.asList(
      s"$RESET${GREEN}クリックで情報を最新化",
      s"$RESET${RED}累計獲得量：${ap.cumulativeTotal}",
      s"$RESET${RED}累計消費量：${ap.used}",
      s"$RESET${AQUA}使用可能量：${ap.left}"
    )
    val itemstack = build(Material.EMERALD_ORE, s"$YELLOW$UNDERLINE${BOLD}実績ポイント 情報", lore)
    AsyncInventorySetter.setItemAsync(inventory, 0, itemstack)

    //おしながき
    if (playerdata.samepageflag) shopIndex.put(uuid, taihiIndex(uuid))
    else if (!finishedShopPageBuild.getOrElse(uuid, false)) shopIndex.put(uuid, 9801)
    taihiIndex.put(uuid, shopIndex(uuid))
    playerdata.samepageflag_$eq(false)
    var inventoryIndex = 1
    var forNextI = 0
    for (i <- shopIndex(uuid) to 9833) {

      if (inventoryIndex < 27) {
        if (playerdata.TitleFlags.contains(i)) {
          val itemstack = buildPlayerSkull(s"$YELLOW$UNDERLINE${BOLD}次ページへ", s"$RESET${DARK_RED}${UNDERLINE}クリックで移動", "MHF_ArrowRight")
          AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone)
          finishedShopPageBuild.put(uuid, true)
          forNextI = i
          break //todo: break is not supported
        } else {
          val lore = util.Arrays.asList(s"$RESET${RED}前・後パーツ「${Nicknames.getHeadPartFor(i).getOrElse(() => "")}」", s"$RESET${GREEN}必要ポイント：20", s"$RESET${AQUA}クリックで購入できます")
          val itemstack = build(Material.BEDROCK, i.toString, lore)
          AsyncInventorySetter.setItemAsync(inventory, inventoryIndex, itemstack)
          inventoryIndex += 1
        }
      } else {

      }
    }
    // SAFE: putしているのでキーがないなんてことはない
    shopIndex.put(uuid, Math.max(forNextI, 9911))
    val (head, tail) = (shopIndex(uuid) to 9938)
      .filter(x => !playerdata.TitleFlags.contains(x))
      .take(27)
      .splitAt(26)
    for (i <- shopIndex(uuid) to 9938) {
      if (inventoryIndex >= 27) {

      } else {
        if (!playerdata.TitleFlags.contains(i)) {
          val lore = util.Arrays.asList(s"$RESET${RED}中パーツ「${Nicknames.getMiddlePartFor(i).getOrElse("")}」", s"$RESET${GREEN}必要ポイント：35", s"$RESET${AQUA}クリックで購入できます")
          val itemstack = build(Material.BEDROCK, i.toString, lore)
          AsyncInventorySetter.setItemAsync(inventory, inventoryIndex, itemstack)
          inventoryIndex += 1
        }
        else { //次ページへ遷移するボタン
          val itemstack = buildPlayerSkull(s"$YELLOW$UNDERLINE${BOLD}次ページへ", s"$RESET${DARK_RED}${UNDERLINE}クリックで移動", "MHF_ArrowRight")
          AsyncInventorySetter.setItemAsync(inventory, 35, itemstack)
          finishedShopPageBuild.put(uuid, true)
          break //todo: break is not supported

        }
      }
    }
    AsyncInventorySetter.setItemAsync(inventory, 27, toMoveNicknameMenu)

    inventory
  }

  /**
   * 投票妖精メニュー
   *
   * @param p プレイヤー
   * @return メニュー
   */
  def getVotingMenuData(p: Player): Inventory = { //UUID取得
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap.apply(uuid)
    if (isError(p, playerdata, "投票妖精")) return null
    val inventory = getEmptyInventory(4, s"$DARK_PURPLE${BOLD}投票ptメニュー");
    {
      //投票pt受け取り
      val itemstack = build(
        Material.DIAMOND,
        s"$LIGHT_PURPLE$UNDERLINE${BOLD}クリックで投票特典を受け取れます",
        getVoteButtonLore(playerdata),
        DIG100
      )
      AsyncInventorySetter.setItemAsync(inventory, 0, itemstack)
    }

    {
      // ver0.3.2 投票ページ表示
      val lore = util.Arrays.asList(
        s"$RESET${GREEN}投票すると様々な特典が！",
        s"$RESET${GREEN}1日1回投票出来ます",
        s"$RESET${DARK_GRAY}クリックするとチャット欄に",
        s"$RESET${DARK_GRAY}URLが表示されますので",
        s"$RESET${DARK_GRAY}Tキーを押してから",
        s"$RESET${DARK_GRAY}そのURLをクリックしてください"
      )
      val itemstack = build(Material.BOOK_AND_QUILL, s"$YELLOW$UNDERLINE${BOLD}投票ページにアクセス", lore)
      AsyncInventorySetter.setItemAsync(inventory, 9, itemstack)
    }

    {
      //棒メニューに戻る
      val lore = Collections.singletonList(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
      val itemstack = buildPlayerSkull(s"$YELLOW$UNDERLINE${BOLD}ホームへ", lore, "MHF_ArrowLeft")
      AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone)
    }

    {
      //妖精召喚時間設定トグルボタン
      val list = util.Arrays.asList(
        s"$RESET$GREEN$BOLD${VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy)}",
        "",
        s"$RESET${GRAY}コスト",
        s"$RESET$RED$BOLD${playerdata.toggleVotingFairy * 2}投票pt",
        "",
        s"$RESET$DARK_RED${UNDERLINE}クリックで切替"
      )
      val itemStack = build(Material.WATCH, s"$AQUA$UNDERLINE${BOLD}マナ妖精 時間設定", list)
      AsyncInventorySetter.setItemAsync(inventory, 2, itemStack)
    }

    {
      //妖精契約設定トグル
      val itemStack = new ItemStack(Material.PAPER)
      itemStack.setItemMeta(getVotingFairyContractMeta(playerdata))
      AsyncInventorySetter.setItemAsync(inventory, 11, itemStack)
    }

    {
      //妖精音トグル
      val itemStack = new ItemStack(Material.JUKEBOX)
      itemStack.setItemMeta(getVotingFairySoundsToggleMeta(playerdata.playFairySound))
      AsyncInventorySetter.setItemAsync(inventory, 20, itemStack)
    }

    {
      //妖精召喚
      val lore = util.Arrays.asList(
        s"$RESET$GRAY${playerdata.toggleVotingFairy * 2}投票ptを消費して",
        s"$RESET${GRAY}マナ妖精を呼びます",
        s"$RESET${GRAY}時間 : ${VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy)}",
        s"$RESET${DARK_RED}Lv.10以上で解放"
      )
      val itemStack = build(
        Material.GHAST_TEAR,
        s"$LIGHT_PURPLE$UNDERLINE${BOLD}マナ妖精 召喚",
        lore,
        DIG100
      )
      AsyncInventorySetter.setItemAsync(inventory, 4, itemStack)
    }

    if (playerdata.usingVotingFairy) { //妖精 時間確認
      val lore = util.Arrays.asList(s"$RESET${GRAY}妖精さんはいそがしい。", s"${GRAY}帰っちゃう時間を教えてくれる")
      val itemStack = build(Material.COMPASS, s"$LIGHT_PURPLE$UNDERLINE${BOLD}マナ妖精に時間を聞く", lore, DIG100)
      AsyncInventorySetter.setItemAsync(inventory, 13, itemStack)

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
        .map(x => SeichiAssist.ranklist_p_apple.apply(x))
        .filter(_.p_apple != 0)
        .zipWithIndex
        .collect { case (rankdata, rank) =>
          List(
            s"${GRAY}たくさんくれたﾆﾝｹﾞﾝ第${rank + 1}位！",
            s"${GRAY}なまえ：${rankdata.name} りんご：${rankdata.p_apple}個"
          )
        }
        .flatten
        .toList

      val globalInfo = List(
        s"${AQUA}ぜーんぶで${SeichiAssist.allplayergiveapplelong}個もらえた！",
        "",
        s"$GREEN↓呼び出したﾆﾝｹﾞﾝの情報↓",
        s"${GREEN}今までに${playerdata.p_apple}個もらった",
        s"${GREEN}ﾆﾝｹﾞﾝの中では${yourRank}番目にたくさんくれる！"
      )
      val lore = fairyNotice ::: ranking ::: globalInfo
      val itemStack = build(Material.GOLDEN_APPLE, s"$YELLOW$UNDERLINE$BOLD㊙ がちゃりんご情報 ㊙", lore, DIG100)
      AsyncInventorySetter.setItemAsync(inventory, 6, itemStack)

    }
    inventory
  }

  /**
   * 投票妖精音切り替え
   *
   * @param playSound trueなら鳴らす
   * @return ラベルがついたアイテム
   */
  private def getVotingFairySoundsToggleMeta(playSound: Boolean) = {
    val itemmeta = Bukkit.getItemFactory.getItemMeta(Material.JUKEBOX)
    itemmeta.setDisplayName(s"$GOLD$UNDERLINE${BOLD}マナ妖精の音トグル")
    val lore = List(
      s"$RESET$DARK_GRAY※この機能はデフォルトでONです。",
      s"$RESET$DARK_RED${UNDERLINE}クリックで切替"
    ).prepended(
      if (playSound)
        s"$RESET${GREEN}現在音が鳴る設定になっています。"
      else {
        s"$RESET${RED}現在音が鳴らない設定になっています。"
      }
    )

    if (!playSound) {
      itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false)
    }

    itemmeta.setLore(lore.asJava)
    itemmeta
  }

  /**
   * 投票妖精戦略
   *
   * @param playerdata プレイヤーの設定
   * @return ラベルが付いたアイテム
   */
  private def getVotingFairyContractMeta(playerdata: PlayerData) = {
    val itemmeta = Bukkit.getItemFactory.getItemMeta(Material.PAPER)
    itemmeta.setDisplayName(s"$GOLD$UNDERLINE${BOLD}妖精とのお約束")
    // n % 4 + 1 -> 1..4
    val strategy = playerdata.toggleGiveApple
    val lore = loreTable.get(strategy)
    itemmeta.setLore(lore.asInstanceOf[util.List[String]])
    itemmeta
  }

  /**
   * GiganticBerserk進化設定
   *
   * @param p
   * @return メニュー
   */
  def getGiganticBerserkBeforeEvolutionMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap.apply(uuid)
    if (isError(p, playerdata, "Gigantic進化前確認")) return null
    val inventory = getEmptyInventory(6, s"${DARK_PURPLE}${BOLD}スキルを進化させますか?")
    // 色
    val table = Array[Short](12, 15, 4, 0, 3)
    val itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, table(playerdata.giganticBerserk.stage))
    val itemmeta = itemstack.getItemMeta
    itemmeta.setDisplayName(" ")
    itemstack.setItemMeta(itemmeta)
    placeGiganticBerserkGlass(inventory, itemstack)

    placeGiganticBerserkShape(inventory);

    {
      val lore = util.Arrays.asList(
        s"$RESET${GREEN}進化することにより、スキルの秘めたる力を解放できますが",
        s"$RESET${GREEN}スキルは更に大量の魂を求めるようになり",
        s"$RESET${GREEN}レベル(回復確率)がリセットされます",
        s"$RESET${RED}本当に進化させますか?",
        s"$RESET$DARK_RED${UNDERLINE}クリックで進化させる"
      )

      val itemstack = build(Material.NETHER_STAR, s"${WHITE}スキルを進化させる", lore)
      AsyncInventorySetter.setItemAsync(inventory, 31, itemstack)
    }
    inventory
  }

  def getGiganticBerserkAfterEvolutionMenu(p: Player): Inventory = {
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap.apply(uuid)
    if (isError(p, playerdata, "GiganticBerserk進化後画面")) return null
    val inventory = getEmptyInventory(6, s"${LIGHT_PURPLE}${BOLD}スキルを進化させました")
    val table = Array[Byte](12, 15, 4, 0, 3, 12)
    val b = table(playerdata.giganticBerserk.stage);

    {
      val itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, b)
      val itemmeta = itemstack.getItemMeta

      if (playerdata.giganticBerserk.stage >= 4) {
        itemmeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true)
        itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
      }
      itemmeta.setDisplayName(" ")
      itemstack.setItemMeta(itemmeta)
      placeGiganticBerserkGlass(inventory, itemstack)
    }

    placeGiganticBerserkShape(inventory);
    {
      val itemstack = build(
        Material.NETHER_STAR,
        s"${WHITE}スキルを進化させました！",
        util.Arrays.asList(
          s"$RESET${GREEN}スキルの秘めたる力を解放することで、マナ回復量が増加し",
          s"$RESET${DARK_RED}スキルはより魂を求めるようになりました"
        )
      )
      AsyncInventorySetter.setItemAsync(inventory, 31, itemstack)
    }

    inventory
  }

  private def getEmptyInventory(rows: Int, title: String) = Bukkit.getServer.createInventory(null, rows * 9, title)

  private def buildSkullMeta(name: String, lore: util.List[String], owner: String) = {
    ItemMetaFactory.SKULL
      .getValue
      .tap(_.setDisplayName(name))
      .tap(_.setOwner(owner))
      .tap(_.setLore(lore))
  }

  @deprecated
  private def build[T <: ItemMeta](mat: Material, name: String, singleLore: String): ItemStack =
    build(mat, name, singleLore, { _: T => Unit })

  @deprecated
  private def build[T <: ItemMeta](mat: Material, name: String, singleLineLore: String, modify: Consumer[_ >: T]): ItemStack =
    build(mat, name, Collections.singletonList(singleLineLore),  {
      modify.accept(_: T)
    })

  @deprecated
  private def build[T <: ItemMeta](mat: Material, name: String, singleLineLore: String, modify: T => Unit): ItemStack = {
    build(mat, name, util.Arrays.asList(singleLineLore), modify)
  }

  @deprecated
  private def build[T <: ItemMeta](mat: Material, name: String, lore: util.List[String]): ItemStack =
    build(mat, name, lore, { _: T => Unit })

  @deprecated
  private def build[T <: ItemMeta](mat: Material, name: String, lore: util.List[String], modify: T => Unit) = {
    val temp = new ItemStack(mat)
    // 自己責任。
    @SuppressWarnings(Array("unchecked")) val meta = temp.getItemMeta.asInstanceOf[T]
    if (name != null) meta.setDisplayName(name)
    if (lore != null) meta.setLore(lore)
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    modify(meta)
    temp.setItemMeta(meta)
    temp
  }

  @deprecated
  private def build[T <: ItemMeta](mat: Material, name: String, lore: util.List[String], modify: Consumer[_ >: T]) = {
    build(mat, name, lore, {
      modify.accept(_: T)
    })
  }

  private def buildPlayerSkull(name: String, lore: String, owner: String) =
    buildPlayerSkull(name, Collections.singletonList(lore), owner)

  private def buildPlayerSkull(name: String, lore: util.List[String], owner: String) =
    buildPlayerSkull(name, lore, owner, { _: SkullMeta => _ })

  private def buildPlayerSkull(name: String, lore: util.List[String], owner: String, modify: SkullMeta => Unit) = {
    val ret = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL)
    val sm = ItemMetaFactory.SKULL.getValue
    if (name != null) sm.setDisplayName(name)
    if (lore != null) sm.setLore(lore)
    sm.setOwner(owner)
    modify(sm)
    ret.setItemMeta(sm)
    ret
  }

  private def placeGiganticBerserkShape(inv: Inventory): Unit = {
    val itemstack = build(Material.STICK, " ", null.asInstanceOf[String])
    List(30, 39, 40, 47)
      .foreach(AsyncInventorySetter.setItemAsync(inv, _, itemstack))
  }

  private def placeGiganticBerserkGlass(inv: Inventory, itemstack: ItemStack): Unit = {
    List(6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41)
      .foreach(AsyncInventorySetter.setItemAsync(inv, _, itemstack))
  }
}
