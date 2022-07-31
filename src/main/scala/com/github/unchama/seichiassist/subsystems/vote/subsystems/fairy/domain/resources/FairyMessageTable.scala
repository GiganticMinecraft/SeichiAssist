package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources

import cats.data.NonEmptyVector
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyMessage,
  FairyMessages,
  NameCalledByFairy
}

object FairyMessageTable {

  /**
   *   朝に妖精を召喚したときに表示されるメッセージ
   */
  val morningMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      NonEmptyVector.of(
        FairyMessage(s"おはよ！${name.value}"),
        FairyMessage(s"ヤッホー${name.value}"),
        FairyMessage(s"ふわぁ。。。${name.value}朝は早いね。"),
        FairyMessage("うーん、今日も一日頑張ろ！"),
        FairyMessage(s"今日は整地日和だね！${name.value}")
      )
    )

  /**
   * 昼に妖精を召喚したときに表示されるメッセージ
   */
  val dayMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      NonEmptyVector.of(
        FairyMessage(s"やあ！${name.value}"),
        FairyMessage(s"ヤッホー${name.value}！"),
        FairyMessage(s"あっ、${name.value}じゃん。丁度お腹空いてたんだ！"),
        FairyMessage(s"この匂い…${name.value}ってがちゃりんごいっぱい持ってる…?"),
        FairyMessage("今日のおやつはがちゃりんごいっぱいだ！")
      )
    )

  // 夜に妖精を召喚したときに表示されるメッセージ
  val nightMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      NonEmptyVector.of(
        FairyMessage(s"やあ！${name.value}"),
        FairyMessage(s"ヤッホー${name.value}！"),
        FairyMessage(s"ふわぁ。。。${name.value}は夜も元気だね。"),
        FairyMessage(s"もう寝ようと思ってたのにー。${name.value}はしょうがないなぁ"),
        FairyMessage("こんな時間に呼ぶなんて…りんごははずんでもらうよ？")
      )
    )

  /**
   * マナが満タンだったときに表示されるメッセージ
   */
  val manaFullMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      NonEmptyVector.of(
        FairyMessage("整地しないのー？"),
        FairyMessage("たくさん働いて、たくさんりんごを食べようね！"),
        FairyMessage("僕はいつか大きながちゃりんごを食べ尽して見せるっ！"),
        FairyMessage("ちょっと食べ疲れちゃった"),
        FairyMessage(s"${name.value}はどのりんごが好き？僕はがちゃりんご！"),
        FairyMessage("動いてお腹を空かしていっぱい食べるぞー！")
      )
    )

  /**
   * 妖精にりんごが消費されたときに表示されるメッセージ
   */
  val consumed: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      NonEmptyVector.of(
        FairyMessage("(´～｀)ﾓｸﾞﾓｸﾞ…"),
        FairyMessage("がちゃりんごって美味しいよね！"),
        FairyMessage("あぁ！幸せ！"),
        FairyMessage(s"${name.value}のりんごはおいしいなぁ"),
        FairyMessage("いつもりんごをありがとう！")
      )
    )

  /**
   * 妖精がりんごを消費しなかったときに表示されるメッセージ
   */
  val notConsumed: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      NonEmptyVector.of(
        FairyMessage("お腹空いたなぁー。"),
        FairyMessage("がちゃりんごがっ！食べたいっ！"),
        FairyMessage("(´；ω；`)ｳｩｩ ﾋﾓｼﾞｲ..."),
        FairyMessage(s"＠うんちゃま ${name.value}が意地悪するんだっ！"),
        FairyMessage("うわーん！お腹空いたよー！")
      )
    )

}
