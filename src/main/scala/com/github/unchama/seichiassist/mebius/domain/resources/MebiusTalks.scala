package com.github.unchama.seichiassist.mebius.domain.resources

import com.github.unchama.seichiassist.mebius.domain.{MebiusLevel, MebiusTalk}

object MebiusTalks {

  private val talkList = List(
    MebiusTalk("こんにちは！これからよろしくねー！", "いつの間にか被っていた。"),
    MebiusTalk("僕のこと外さないでね？", "段々成長していくらしい。"),
    MebiusTalk("モンスターって怖いねえ…", "どこから喋っているのだろう。"),
    MebiusTalk("どこでもルールって大切だね。", "ちゃんと守らなきゃね。"),
    MebiusTalk("整地神様って知ってる？偉いんだよ！", "どうやら神様を知ってるみたい。"),
    MebiusTalk("知らないこと、いっぱい学びたいなぁ。", "どこに記憶しているんだろう。"),
    MebiusTalk("ゾンビっておいしいのかな？", "それだけはやめておけ。"),
    MebiusTalk("どこかに僕の兄弟が埋まってるんだー。", "採掘で手に入るのかな。"),
    MebiusTalk("…はっ！寝てないからね！？", "たまに静かだよね。"),
    MebiusTalk("スキルって気持ち良いよね！", "マナが吸い取られるけどね。"),
    MebiusTalk("メインワールドの探検しようよー！", "息抜きは大切だね。"),
    MebiusTalk("宿題も大切だよ？", "何の話をしてるんだろう…"),
    MebiusTalk("空を自由に飛びたいなー！", "はい、タケコプター！"),
    MebiusTalk("ジュースが飲みたいよー！", "どこから飲むつもりだろう。"),
    MebiusTalk("君の頭って落ち着くねぇ。", "君のお喋りにも慣れたなぁ。"),
    MebiusTalk("APOLLO様みたいになれるかな？", "どんな関係があるんだろう…"),
    MebiusTalk("僕って役に立つでしょー！", "静かならもっといいんだけどね。"),
    MebiusTalk("赤いりんごがあるらしいよ！？", "りんごは普通赤いんだよ。"),
    MebiusTalk("ヘルメット式電動耳掃除…", "何を怖いことを言っている…"),
    MebiusTalk("ここまで育つなんてね！", "立派になったもんだね。"),
    MebiusTalk("動きすぎると酔っちゃうよね。", "三半規管はあるのかな。"),
    MebiusTalk("僕は整地神様に生み出されたんだよ！", "整地神ってお喋りなのかな…"),
    MebiusTalk("君とドラゴンを倒す夢を見たよ…", "エンダードラゴンのことかな。"),
    MebiusTalk("君は僕が育てたと胸を張って言えるね！", "逆でしょう。"),
    MebiusTalk("ああー饅頭が怖いなあ！", "落語でも見た？あげないよ。"),
    MebiusTalk("僕にも手足があったらなー…！", "被れなくなるでしょ。"),
    MebiusTalk("このフィット感…着心地抜群だよね？", "もう少し静かだったらね。"),
    MebiusTalk("餃子っておいしいんだねえ！", "ニンニク臭がこもってるよ…"),
    MebiusTalk("君も立派になったねえ", "同じこと思ってたとこ。"),
    MebiusTalk("育ててくれてありがとう！", "ある意味、最強のヘルメット。")
  )

  assert(talkList.size == MebiusLevel.max)

  def at(level: MebiusLevel): MebiusTalk = talkList(level.value - 1)

}
