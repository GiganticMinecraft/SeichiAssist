package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.bukkit

import cats.data.NonEmptyList
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyAppleConsumeStrategy.{
  NoConsume,
  Consume,
  LessConsume,
  Permissible
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyAppleConsumeStrategy,
  FairyLore
}
import org.bukkit.ChatColor._

object FairyLoreTable {

  val loreTable: Map[FairyAppleConsumeStrategy, FairyLore] = Map(
    Permissible -> FairyLore(
      NonEmptyList.of(
        s"$RED$UNDERLINE${BOLD}ガンガンたべるぞ",
        s"$RESET${GRAY}とにかく妖精さんにりんごを開放します。",
        s"$RESET${GRAY}めっちゃ喜ばれます。"
      )
    ),
    Consume -> FairyLore(
      NonEmptyList.of(
        s"$YELLOW$UNDERLINE${BOLD}バッチリたべよう",
        s"$RESET${GRAY}食べ過ぎないように注意しつつ",
        s"$RESET${GRAY}妖精さんにりんごを開放します。",
        s"$RESET${GRAY}喜ばれます。"
      )
    ),
    LessConsume -> FairyLore(
      NonEmptyList.of(
        s"$GREEN$UNDERLINE${BOLD}リンゴだいじに",
        s"$RESET${GRAY}少しだけ妖精さんにりんごを開放します。",
        s"$RESET${GRAY}伝えると大抵落ち込みます。"
      )
    ),
    NoConsume -> FairyLore(
      NonEmptyList.of(s"$BLUE$UNDERLINE${BOLD}リンゴつかうな", s"$RESET${GRAY}絶対にりんごを開放しません。", "")
    )
  )

}
