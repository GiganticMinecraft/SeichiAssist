package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

object FairyMessageTable {

  val morningMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      s"おはよ！${name.value}",
      s"ヤッホー${name.value}",
      s"ふわぁ。。。${name.value}朝は早いね。",
      "うーん、今日も一日頑張ろ！",
      s"今日は整地日和だね！${name.value}"
    )

  val dayMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      s"やあ！${name.value}",
      s"ヤッホー${name.value}！",
      s"あっ、${name.value}じゃん。丁度お腹空いてたんだ！",
      s"この匂い…${name.value}ってがちゃりんごいっぱい持ってる…?",
      "今日のおやつはがちゃりんごいっぱいだ！"
    )

  val nightMessages: NameCalledByFairy => FairyMessages = (name: NameCalledByFairy) =>
    FairyMessages(
      s"やあ！${name.value}",
      s"ヤッホー${name.value}！",
      s"ふわぁ。。。${name.value}は夜も元気だね。",
      s"もう寝ようと思ってたのにー。${name.value}はしょうがないなぁ",
      "こんな時間に呼ぶなんて…りんごははずんでもらうよ？"
    )

}
