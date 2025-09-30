package com.github.unchama.seichiassist.menus.ranking

import cats.effect.IO
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.ranking.api.RankingProvider
import com.github.unchama.seichiassist.subsystems.ranking.domain.values.{LoginTime, VoteCount}
import com.github.unchama.seichiassist.subsystems.ranking.domain.{
  Ranking,
  RankingRecord,
  RankingRecordWithPosition
}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

object RankingMenu {

  class Environment[R](
    implicit val rankingApi: RankingProvider[IO, R],
    val ioCanOpenRankingMenuItself: IO CanOpen RankingMenu[R],
    val ioCanOpenRankingRootMenu: IO CanOpen RankingRootMenu.type,
    val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player],
    val nonServerThreadContextShift: NonServerThreadContextShift[IO]
  )

}

trait RankingMenuTemplate[R] {

  /**
   * ランキングの名前。
   *
   * 例えば、「整地神ランキング」など。
   */
  val rankingName: String

  /**
   * ランキングのレコードから、表示すべきloreを計算する。
   *
   * 例えば、整地量ランキングでは、このメソッドが返すloreが整地量や整地レベルなどを含むのが妥当であろう。
   */
  def recordDataLore(data: R): List[String]

  /**
   * ランキング内のデータをすべて合算したデータから、表示すべきloreを計算する。
   *
   * 例えば、整地量ランキングでは、「全プレイヤー総整地量: ...」などと表示すべきであろう。
   */
  def combinedDataLore(data: R): List[String]
}

object RankingMenuTemplates {

  val seichi: RankingMenuTemplate[SeichiAmountData] =
    new RankingMenuTemplate[SeichiAmountData] {
      override val rankingName: String = "整地神ランキング"
      override def recordDataLore(data: SeichiAmountData): List[String] = {
        val levelLine = {
          val starLevel = data.starLevelCorrespondingToExp.level
          val level = data.levelCorrespondingToExp.level

          if (starLevel > 0)
            s"整地Lv:$level☆$starLevel"
          else
            s"整地Lv:$level"
        }

        List(s"$RESET$GREEN$levelLine", s"$RESET${GREEN}総整地量:${data.expAmount.formatted}")
      }
      override def combinedDataLore(data: SeichiAmountData): List[String] = List(
        s"$RESET${AQUA}全プレイヤー総整地量: ${data.expAmount.formatted}"
      )
    }

  val build: RankingMenuTemplate[BuildAmountData] = new RankingMenuTemplate[BuildAmountData] {
    override val rankingName: String = "建築神ランキング"
    override def recordDataLore(data: BuildAmountData): List[String] = List(
      s"$RESET${GREEN}建築Lv:${data.levelCorrespondingToExp.level}",
      s"$RESET${GREEN}総建築量:${data.expAmount.amount.bigDecimal}"
    )
    override def combinedDataLore(data: BuildAmountData): List[String] = List(
      s"$RESET${AQUA}全プレイヤー総建築量: ${data.expAmount.amount.bigDecimal}"
    )
  }

  val login: RankingMenuTemplate[LoginTime] = new RankingMenuTemplate[LoginTime] {
    override val rankingName: String = "ログイン神ランキング"
    override def recordDataLore(data: LoginTime): List[String] = List(
      s"$RESET${GREEN}総ログイン時間:${data.formatted}"
    )
    override def combinedDataLore(data: LoginTime): List[String] = List(
      s"$RESET${AQUA}全プレイヤー総ログイン時間: ${data.formatted}"
    )
  }

  val vote: RankingMenuTemplate[VoteCount] = new RankingMenuTemplate[VoteCount] {
    override val rankingName: String = "投票神ランキング"
    override def recordDataLore(data: VoteCount): List[String] = List(
      s"$RESET${GREEN}総投票回数:${data.value}回"
    )
    override def combinedDataLore(data: VoteCount): List[String] = List(
      s"$RESET${AQUA}全プレイヤー総投票回数: ${data.value}回"
    )
  }
}

// TODO: Rは生のデータ型を想定しているが、何らかのenumを想定することはできるか？
//       ここにSeichiAmountData等を書きたくない気持ちがある。
case class RankingMenu[R](template: RankingMenuTemplate[R], pageIndex: Int = 0) extends Menu {
  import eu.timepit.refined.auto._
  import cats.implicits._

  final private val perPage = 45
  final private val cutoff = 150

  override type Environment = RankingMenu.Environment[R]
  override val frame: MenuFrame =
    MenuFrame(6.chestRows, s"$DARK_PURPLE$BOLD${template.rankingName}")

  private def uiOperationSection(
    totalNumberOfPages: Int
  )(implicit environment: Environment): IO[Seq[(Int, Button)]] = {
    import environment._

    def buttonToTransferTo(
      pageIndex: Int,
      skullOwnerReference: SkullOwnerReference
    ): IO[Button] =
      new SkullItemStackBuilder(skullOwnerReference).buildAsync().map { itemStack =>
        CommonButtons.transferButton(
          itemStack,
          s"${template.rankingName}${pageIndex + 1}ページ目へ",
          RankingMenu(template, pageIndex)
        )
      }

    val goBackToStickMenuSection =
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft).buildAsync().map { itemStack =>
        Seq(
          ChestSlotRef(5, 0) -> CommonButtons
            .transferButton(itemStack, "ランキングメニューへ戻る", RankingRootMenu)
        )
      }

    val previousPageButtonSection =
      if (pageIndex > 0)
        buttonToTransferTo(pageIndex - 1, SkullOwners.MHF_ArrowUp).map { button =>
          Seq(ChestSlotRef(5, 7) -> button)
        }
      else
        IO.pure(Seq.empty)

    val nextPageButtonSection =
      if (pageIndex + 1 < totalNumberOfPages)
        buttonToTransferTo(pageIndex + 1, SkullOwners.MHF_ArrowDown).map { button =>
          Seq(ChestSlotRef(5, 8) -> button)
        }
      else
        IO.pure(Seq.empty)

    (for {
      goBackToStickMenuSection <- goBackToStickMenuSection
      previousPageButtonSection <- previousPageButtonSection
      nextPageButtonSection <- nextPageButtonSection
    } yield goBackToStickMenuSection ++ previousPageButtonSection ++ nextPageButtonSection): IO[
      Seq[(Int, Button)]
    ]
  }

  private def rankingSection(
    ranking: Ranking[R]
  )(implicit environment: Environment): IO[Seq[(Int, Button)]] = {
    import environment.playerHeadSkinAPI
    import environment.nonServerThreadContextShift

    def entry(position: Int, record: RankingRecord[R]): IO[Button] = {
      new SkullItemStackBuilder(record.uuid)
        .title(s"$YELLOW$BOLD${position}位:$WHITE${record.playerName}")
        .lore(template.recordDataLore(record.value))
        .buildAsync()
        .map(Button(_))
    }

    ranking
      .recordsWithPositions
      .take(cutoff)
      .slice(pageIndex * perPage, pageIndex * perPage + perPage)
      .zipWithIndex
      .parTraverse {
        case (RankingRecordWithPosition(record, position), index) =>
          entry(position, record).map(button => index -> button)
      }
  }

  private def totalAmountSection(
    ranking: Ranking[R]
  )(implicit environment: Environment): IO[Seq[(Int, Button)]] = {
    import environment.playerHeadSkinAPI

    new SkullItemStackBuilder(SkullOwners.unchama)
      .title(s"$YELLOW$UNDERLINE${BOLD}整地鯖統計データ")
      .lore(template.combinedDataLore(ranking.total))
      .buildAsync()
      .map { itemStack => Seq(ChestSlotRef(5, 4) -> Button(itemStack)) }
  }

  /**
   * @return
   *   `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = for {
    ranking <- environment.rankingApi.ranking.read
    recordsToInclude <- IO.pure(ranking.recordCount min cutoff)
    totalNumberOfPages <- IO.pure(Math.ceil(recordsToInclude / 45.0).toInt)
    rankingSection <- rankingSection(ranking)
    uiOperationSection <- uiOperationSection(totalNumberOfPages)
    totalAmountSection <- totalAmountSection(ranking)
  } yield MenuSlotLayout(rankingSection ++ uiOperationSection ++ totalAmountSection: _*)
}
