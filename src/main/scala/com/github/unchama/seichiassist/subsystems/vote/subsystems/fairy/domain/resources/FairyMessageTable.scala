package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources

import cats.data.NonEmptyVector
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyMessage,
  FairyMessageChoice,
  ScreenNameForFairy
}

object FairyMessageTable {

  /**
   * 朝に妖精を召喚したときに表示されるメッセージ
   */
  val morningMessages: ScreenNameForFairy => FairyMessageChoice = (name: ScreenNameForFairy) =>
    FairyMessageChoice(
      NonEmptyVector.of(
        FairyMessage(s"おはよ！${name.name}"),
        FairyMessage(s"ヤッホー${name.name}"),
        FairyMessage(s"ふわぁ。。。${name.name}朝は早いね。"),
        FairyMessage("うーん、今日も一日頑張ろ！"),
        FairyMessage(s"今日は整地日和だね！${name.name}")
      )
    )

  /**
   * 昼に妖精を召喚したときに表示されるメッセージ
   */
  val dayMessages: ScreenNameForFairy => FairyMessageChoice = (name: ScreenNameForFairy) =>
    FairyMessageChoice(
      NonEmptyVector.of(
        FairyMessage(s"やあ！${name.name}"),
        FairyMessage(s"ヤッホー${name.name}！"),
        FairyMessage(s"あっ、${name.name}じゃん。丁度お腹空いてたんだ！"),
        FairyMessage(s"この匂い…${name.name}ってがちゃりんごいっぱい持ってる…?"),
        FairyMessage("今日のおやつはがちゃりんごいっぱいだ！")
      )
    )

  // 夜に妖精を召喚したときに表示されるメッセージ
  val nightMessages: ScreenNameForFairy => FairyMessageChoice = (name: ScreenNameForFairy) =>
    FairyMessageChoice(
      NonEmptyVector.of(
        FairyMessage(s"やあ！${name.name}"),
        FairyMessage(s"ヤッホー${name.name}！"),
        FairyMessage(s"ふわぁ。。。${name.name}は夜も元気だね。"),
        FairyMessage(s"もう寝ようと思ってたのにー。${name.name}はしょうがないなぁ"),
        FairyMessage("こんな時間に呼ぶなんて…りんごははずんでもらうよ？")
      )
    )

  /**
   * マナが満タンだったときに表示されるメッセージ
   */
  val manaFullMessages: ScreenNameForFairy => FairyMessageChoice = (name: ScreenNameForFairy) =>
    FairyMessageChoice(
      NonEmptyVector.of(
        FairyMessage("整地しないのー？"),
        FairyMessage("たくさん働いて、たくさんりんごを食べようね！"),
        FairyMessage("僕はいつか大きながちゃりんごを食べ尽して見せるっ！"),
        FairyMessage("ちょっと食べ疲れちゃった"),
        FairyMessage(s"${name.name}はどのりんごが好き？僕はがちゃりんご！"),
        FairyMessage("動いてお腹を空かしていっぱい食べるぞー！")
      )
    )

  /**
   * 妖精にりんごが消費されたときに表示されるメッセージ
   */
  val consumed: ScreenNameForFairy => FairyMessageChoice = (name: ScreenNameForFairy) =>
    FairyMessageChoice(
      NonEmptyVector.of(
        FairyMessage("(´～｀)ﾓｸﾞﾓｸﾞ…"),
        FairyMessage("がちゃりんごって美味しいよね！"),
        FairyMessage("あぁ！幸せ！"),
        FairyMessage(s"${name.name}のりんごはおいしいなぁ"),
        FairyMessage("いつもりんごをありがとう！")
      )
    )

  /**
   * 妖精がりんごを消費しなかったときに表示されるメッセージ
   */
  val notConsumed: ScreenNameForFairy => FairyMessageChoice = (name: ScreenNameForFairy) =>
    FairyMessageChoice(
      NonEmptyVector.of(
        FairyMessage("お腹空いたなぁー。"),
        FairyMessage("がちゃりんごがっ！食べたいっ！"),
        FairyMessage("(´；ω；`)ｳｩｩ ﾋﾓｼﾞｲ..."),
        FairyMessage(s"＠うんちゃま ${name.name}が意地悪するんだっ！"),
        FairyMessage("うわーん！お腹空いたよー！")
      )
    )

}
