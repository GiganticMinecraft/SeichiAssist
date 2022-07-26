package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyMessage,
  FairyMessages,
  NameCalledByFairy
}

object FairyMessageTable {

  val morningMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      FairyMessage(s"おはよ！${name.value}"),
      FairyMessage(s"ヤッホー${name.value}"),
      FairyMessage(s"ふわぁ。。。${name.value}朝は早いね。"),
      FairyMessage("うーん、今日も一日頑張ろ！"),
      FairyMessage(s"今日は整地日和だね！${name.value}")
    )

  val dayMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      FairyMessage(s"やあ！${name.value}"),
      FairyMessage(s"ヤッホー${name.value}！"),
      FairyMessage(s"あっ、${name.value}じゃん。丁度お腹空いてたんだ！"),
      FairyMessage(s"この匂い…${name.value}ってがちゃりんごいっぱい持ってる…?"),
      FairyMessage("今日のおやつはがちゃりんごいっぱいだ！")
    )

  val nightMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      FairyMessage(s"やあ！${name.value}"),
      FairyMessage(s"ヤッホー${name.value}！"),
      FairyMessage(s"ふわぁ。。。${name.value}は夜も元気だね。"),
      FairyMessage(s"もう寝ようと思ってたのにー。${name.value}はしょうがないなぁ"),
      FairyMessage("こんな時間に呼ぶなんて…りんごははずんでもらうよ？")
    )

}
