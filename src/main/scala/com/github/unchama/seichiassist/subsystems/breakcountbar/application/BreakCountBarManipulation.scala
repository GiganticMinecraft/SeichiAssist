package com.github.unchama.seichiassist.subsystems.breakcountbar.application

import cats.Applicative
import com.github.unchama.minecraft.objects.MinecraftBossBar
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{
  SeichiExpAmount,
  SeichiStarLevel
}
import org.bukkit.ChatColor.{BOLD, GOLD}

import java.text.DecimalFormat

object BreakCountBarManipulation {

  import cats.implicits._

  def write[F[_]: Applicative](
    seichiAmountData: SeichiAmountData,
    bossBar: MinecraftBossBar[F]
  ): F[Unit] = {
    val currentExp = seichiAmountData.expAmount
    val levelProgress = seichiAmountData.levelProgress
    val level = seichiAmountData.levelCorrespondingToExp
    val starLevel = seichiAmountData.starLevelCorrespondingToExp

    // 3桁毎カンマ区切り
    def formatAmount(expAmount: SeichiExpAmount): String = {
      val decimalFormat = new DecimalFormat("#,##0")
      decimalFormat.format(expAmount.amount.bigDecimal)
    }

    val text = if (starLevel != SeichiStarLevel.zero) {
      val levelText = s"Lv ${level.level}☆${starLevel.level}"
      val breakAmountText = s"(総整地量: ${formatAmount(currentExp)})"
      s"$GOLD$BOLD$levelText $breakAmountText"
    } else {
      val levelText = s"Lv ${level.level}"
      val progressText =
        s"(総整地量: ${formatAmount(currentExp)} | 次のレベルまで: ${formatAmount(levelProgress.expAmountToNextLevel)})"
      s"$GOLD$BOLD$levelText $progressText"
    }

    List(bossBar.progress.write(levelProgress.progress), bossBar.title.write(text))
      .sequence
      .as(())
  }

}
