package com.github.unchama.seichiassist.subsystems.mebius.domain.resources

import cats.data.NonEmptyList
import com.github.unchama.seichiassist.subsystems.mebius.domain.message.{
  MebiusCombatMessage,
  MebiusPlayerMessage
}
import com.github.unchama.util.collection.RandomizedCollection

object MebiusMessages {

  val onBlockBreak: RandomizedCollection[MebiusPlayerMessage] = new RandomizedCollection(
    NonEmptyList
      .of(
        "ポコポコポコポコ…整地の音って、落ち着くねえ。",
        "頑張れー！頑張れー！そこをまっすぐ！左にも石があるよー！…うるさい？",
        "一生懸命掘ってると、いつの間にか無心になっちゃうよねえ…！",
        "なんだか眠たくなってきちゃったー、[str1]は平気ー？",
        "今日はどこまで掘るのかなー？",
        "[str1]と一緒に整地するの、楽しいねえ！",
        "ブロックが1つも浮いていないと、ちょうど空が見えて綺麗だよね！",
        "ねえ、いま僕の兄弟の声がしなかった？気のせいかなあ",
        "ポコポコ…ザクザク…音が気持ちいいよね！",
        "整地♪整地♪ せ・い・ちー♪",
        "あとちょっと掘ろうよ！",
        "僕もスキルでバーっと掘ってみたいなー"
      )
      .map(MebiusPlayerMessage)
  )

  val onMebiusBreak: RandomizedCollection[MebiusPlayerMessage] = new RandomizedCollection(
    NonEmptyList
      .of(
        "ここまでかぁっ…[str1]と一緒に旅したこと、すごく楽しかったなぁ…",
        "この先[str1]のこと、守ってあげられなくなっちゃった…ごめんね…",
        "僕、少しは[str1]の役に立てたかなぁ…もしそうだったら、嬉しいなぁ",
        "[str1]のおかげで最期まで防具としていられたんだぁ…使ってくれて、ありがとう。",
        "最期まで[str1]の頭にいれたことって、すごく幸せなことだよ",
        "もし生まれ変わっても、また[str1]と…",
        "ごめんね…[str1]とずっと一緒でいたかったけど、僕がついていけなかったよ…"
      )
      .map(MebiusPlayerMessage)
  )

  val onDamageBreaking: RandomizedCollection[MebiusPlayerMessage] = new RandomizedCollection(
    NonEmptyList
      .of(
        "いたた…もうすぐ壊れちゃいそうだ…",
        "もうダメかも…こんなところで、悔しいなぁ",
        "お願い、修繕して欲しいよ…",
        "ごめんね…これ以上は[str1]のこと、守ってあげられそうにないよ…",
        "もっと[str1]と、旅したかったなぁ",
        "まだ平気…壊れるまでは、[str1]のことを守るんだ…",
        "僕のこと、大事にしてね？"
      )
      .map(MebiusPlayerMessage)
  )

  val onDamageWarnEnemy: RandomizedCollection[MebiusCombatMessage] = new RandomizedCollection(
    NonEmptyList
      .of(
        "[str2]からの攻撃だ！気を付けて！",
        "お前なんか余裕なんだからなー！さあ[str1]、やっちゃえ！",
        "びっくりしたなー！人が休んでるときにー！",
        "もーなんで今攻撃してくるのさあああ！",
        "いったーいっ、今僕の小指踏んだなー！？",
        "いてっ！やめろよー！僕を怒らせたら怖いぞー！",
        "うわぁっ！飲み物がこぼれちゃったじゃないかー！"
      )
      .map(MebiusCombatMessage)
  )

  val onMonsterKill: RandomizedCollection[MebiusCombatMessage] = new RandomizedCollection(
    NonEmptyList
      .of(
        "さすが[str1]！[str2]なんて敵じゃないね！",
        "僕にかかれば[str2]なんてこんなもんだよー！",
        "モンスターってなんで人間を襲うんだろう…？",
        "ねえ[str1]、今の僕のおかげだよね！ね？",
        "たまにはやられてみたいもんだねー、ふふん！",
        "[str2]なんて僕の力を出すまでもなかったね！",
        "やるね！僕も負けてらんないぞー！"
      )
      .map(MebiusCombatMessage)
  )

  val tips: List[String] = List(
    "僕の名前は、/mebius naming <名前> コマンドで変更できるよ！<名前>の代わりに新しい名前を入れてね！",
    "僕は整地によって成長するんだー。アイテムレベル30まであるんだよ！",
    "僕たち兄弟のステータスはみんなバラバラなんだよー！",
    "僕たちはこの世界のどこかに埋まってるんだー。整地して僕の兄弟も見つけて欲しいな！",
    "困ったときは公式HPを見ようね！",
    "1日1回投票をすると、ガチャ券とピッケルが貰えるよ！",
    "第2、第4整地ワールドは自分で保護を掛けたところしか整地出来ないみたい。誰にも邪魔されずに黙々と掘りたい人に好都合だね。",
    "エリトラ装備中は上を向きながらダッシュすると空を飛べるんだって！",
    "公共施設サーバーからデパートに行ってみようよ！修繕の本やダイヤのツールが買えるんだってー！",
    "余った鉱石は公共施設サーバーの交換所で交換券に出来るって知ってた？交換券で強いピッケルやスコップが手に入るらしいよ！",
    "僕はレベルアップするたびにどんどん強くなっていくんだー！",
    "たまには休憩してね！僕との約束だよ！",
    "ガチャを引けばすごいツールが出るかもしれないんだって！",
    "綺麗に掘るのが整地の醍醐味だよねー！",
    // TODO: 整地鯖7周年を記念するMebiusの特別なセリフ。イベント終了時には削除する
    // https://github.com/GiganticMinecraft/SeichiAssist/pull/2076
    "おめでとう！整地鯖7周年のご挨拶、だよ！",
    "整地鯖は7歳になったんだって！誕生日ケーキもらえないかなあ。",
    "ねぇねぇ！6/29は特別で大切な日なんだよ！",
    "こんぐらっちゅれーしょんず、せいちさーばー7thあにばーさりー！",
    "6/29は7周年のお祝い！お祝いと言ったらごちそうだね！何食べよっか？",
    "7周年！これからの整地鯖楽しみだね！"
  )

}
