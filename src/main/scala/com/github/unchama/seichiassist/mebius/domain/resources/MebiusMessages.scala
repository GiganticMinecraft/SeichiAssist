package com.github.unchama.seichiassist.mebius.domain.resources

import cats.data.NonEmptyList
import cats.effect.IO
import com.github.unchama.seichiassist.mebius.domain.message.MebiusCombatMessage
import com.github.unchama.util.collection.RandomizedCollection

object MebiusMessages {

  val onBlockBreak = Set(
    "ポコポコポコポコ…整地の音って、落ち着くねえ。",
    "頑張れー！頑張れー！そこをまっすぐ！左にも石があるよー！…うるさい？",
    "一生懸命掘ってると、いつの間にか無心になっちゃうよねえ…！",
    "なんだか眠たくなってきちゃったー、[str1]は平気ー？",
    "今日はどこまで掘るのかなー？",
    "[str1]と一緒に整地するの、楽しいねえ！"
  )

  val onMebiusBreak = Set(
    "ここまでかぁっ…[str1]と一緒に旅したこと、すごく楽しかったなぁ…",
    "この先[str1]のこと、守ってあげられなくなっちゃった…ごめんね…",
    "僕、少しは[str1]の役に立てたかなぁ…もしそうだったら、嬉しいなぁ",
    "[str1]のおかげで最期まで防具としていられたんだぁ…使ってくれて、ありがとう。",
    "最期まで[str1]の頭にいれたことって、すごく幸せなことだよ",
    "もし生まれ変わっても、また[str1]と…"
  )

  val onDamageBreaking = Set(
    "いたた…もうすぐ壊れちゃいそうだ…",
    "もうダメかも…こんなところで、悔しいなぁ",
    "お願い、修繕して欲しいよ…",
    "ごめんね…これ以上は[str1]のこと、守ってあげられそうにないよ…",
    "もっと[str1]と、旅したかったなぁ",
    "まだ平気…壊れるまでは、[str1]のことを守るんだ…"
  )

  val onDamageWarnEnemy: RandomizedCollection[MebiusCombatMessage, IO] = new RandomizedCollection(
    NonEmptyList.of(
      "[str2]からの攻撃だ！気を付けて！",
      "お前なんか余裕なんだからなー！さあ[str1]、やっちゃえ！",
      "びっくりしたなー！人が休んでるときにー！",
      "もーなんで今攻撃してくるのさあああ！",
      "いったーいっ、今僕の小指踏んだなー！？",
      "いてっ！やめろよー！僕を怒らせたら怖いぞー！"
    ).map(MebiusCombatMessage)
  )

  val onMonsterKill: RandomizedCollection[MebiusCombatMessage, IO] = new RandomizedCollection(
    NonEmptyList.of(
      "さすが[str1]！[str2]なんて敵じゃないね！",
      "僕にかかれば[str2]なんてこんなもんだよー！",
      "モンスターってなんで人間を襲うんだろう…？",
      "ねえ[str1]、今の僕のおかげだよね！ね？",
      "たまにはやられてみたいもんだねー、ふふん！",
      "[str2]なんて僕の力を出すまでもなかったね！"
    ).map(MebiusCombatMessage)
  )

  val tips = List(
    "僕の名前は、/mebius naming <名前> コマンドで変更できるよ！<名前>の代わりに新しい名前を入れてね！",
    "僕は整地によって成長するんだー。アイテムレベル30まであるんだよ！",
    "僕たち兄弟のステータスはみんなバラバラなんだよー！",
    "僕たちはこの世界のどこかに埋まってるんだー。整地して僕の兄弟も見つけて欲しいな！",
    "困ったときはwikiを見ようね！",
    "1日1回投票をすると、ガチャ券とピッケルが貰えるよ！",
    "第2整地ワールドは自分で保護を掛けたところしか整地出来ないみたい。誰にも邪魔されずに黙々と掘りたい人に好都合だね。",
    "エリトラ装備中は上を向きながらダッシュすると空を飛べるんだって！",
    "公共施設サーバからデパートに行ってみようよ！修繕の本やダイヤのツールが買えるんだってー！",
    "余った鉱石は公共施設サーバの交換所で交換券に出来るって知ってた？交換券で強いピッケルやスコップが手に入るらしいよ！"
  )

}
