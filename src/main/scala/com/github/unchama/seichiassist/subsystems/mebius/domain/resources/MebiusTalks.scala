package com.github.unchama.seichiassist.subsystems.mebius.domain.resources

import com.github.unchama.seichiassist.subsystems.mebius.domain.message.MebiusDialogue
import com.github.unchama.seichiassist.subsystems.mebius.domain.property.MebiusLevel

object MebiusTalks {

  private val talkList = List(
    MebiusDialogue("こんにちは！これからよろしくねー！", "いつの間にか被っていた。"),
    MebiusDialogue("僕のこと外さないでね？", "段々成長していくらしい。"),
    MebiusDialogue("モンスターって怖いねえ…", "どこから喋っているのだろう。"),
    MebiusDialogue("どこでもルールって大切だね。", "ちゃんと守らなきゃね。"),
    MebiusDialogue("整地神様って知ってる？偉いんだよ！", "どうやら神様を知ってるみたい。"),
    MebiusDialogue("知らないこと、いっぱい学びたいなぁ。", "どこに記憶しているんだろう。"),
    MebiusDialogue("ゾンビっておいしいのかな？", "それだけはやめておけ。"),
    MebiusDialogue("どこかに僕の兄弟が埋まってるんだー。", "採掘で手に入るのかな。"),
    MebiusDialogue("…はっ！寝てないからね！？", "たまに静かだよね。"),
    MebiusDialogue("スキルって気持ち良いよね！", "マナが吸い取られるけどね。"),
    MebiusDialogue("メインワールドの探検しようよー！", "息抜きは大切だね。"),
    MebiusDialogue("宿題も大切だよ？", "何の話をしてるんだろう…"),
    MebiusDialogue("空を自由に飛びたいなー！", "はい、タケコプター！"),
    MebiusDialogue("ジュースが飲みたいよー！", "どこから飲むつもりだろう。"),
    MebiusDialogue("君の頭って落ち着くねぇ。", "君のお喋りにも慣れたなぁ。"),
    MebiusDialogue("APOLLO様みたいになれるかな？", "どんな関係があるんだろう…"),
    MebiusDialogue("僕って役に立つでしょー！", "静かならもっといいんだけどね。"),
    MebiusDialogue("赤いりんごがあるらしいよ！？", "りんごは普通赤いんだよ。"),
    MebiusDialogue("ヘルメット式電動耳掃除…", "何を怖いことを言っている…"),
    MebiusDialogue("ここまで育つなんてね！", "立派になったもんだね。"),
    MebiusDialogue("動きすぎると酔っちゃうよね。", "三半規管はあるのかな。"),
    MebiusDialogue("僕は整地神様に生み出されたんだよ！", "整地神ってお喋りなのかな…"),
    MebiusDialogue("君とドラゴンを倒す夢を見たよ…", "エンダードラゴンのことかな。"),
    MebiusDialogue("君は僕が育てたと胸を張って言えるね！", "逆でしょう。"),
    MebiusDialogue("ああー饅頭が怖いなあ！", "落語でも見た？あげないよ。"),
    MebiusDialogue("僕にも手足があったらなー…！", "被れなくなるでしょ。"),
    MebiusDialogue("このフィット感…着心地抜群だよね？", "もう少し静かだったらね。"),
    MebiusDialogue("餃子っておいしいんだねえ！", "ニンニク臭がこもってるよ…"),
    MebiusDialogue("君も立派になったねえ", "同じこと思ってたとこ。"),
    MebiusDialogue("育ててくれてありがとう！", "ある意味、最強のヘルメット。")
  )

  assert(talkList.size == MebiusLevel.max.value)

  def at(level: MebiusLevel): MebiusDialogue = talkList(level.value - 1)

}
