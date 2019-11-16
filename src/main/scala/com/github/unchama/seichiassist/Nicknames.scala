package com.github.unchama.seichiassist

object Nicknames {
  val map: Map[Int, NicknameParts] = Map(
  // 前後パーツ(購入用)
  9801 -> HeadTail(s"お兄さん", s"お兄さん"),
  9802 -> HeadTail(s"戦隊", s"戦隊"),
  9803 -> HeadTail(s"軍", s"軍"),
  9804 -> HeadTail(s"うどん", s"うどん"),
  9805 -> HeadTail(s"厨二病", s"厨二病"),
  9806 -> HeadTail(s"現世", s"現世"),
  9807 -> HeadTail(s"天界", s"天界"),
  9808 -> HeadTail(s"冥界", s"冥界"),
  9809 -> HeadTail(s"地獄", s"地獄"),
  9810 -> HeadTail(s"最終兵器", s"最終兵器"),
  9811 -> HeadTail(s"ごりら", s"ごりら"),
  9812 -> HeadTail(s"うほうほ", s"うほうほ"),
  9813 -> HeadTail(s"暑い", s"暑い"),
  9814 -> HeadTail(s"寒い", s"寒い"),
  9815 -> HeadTail(s"笑う", s"笑う"),
  9816 -> HeadTail(s"泣く", s"泣く"),
  9817 -> HeadTail(s"怒る", s"怒る"),
  9818 -> HeadTail(s"最初", s"最初"),
  9819 -> HeadTail(s"最後", s"最後"),
  9820 -> HeadTail(s"ァ！", s"ァ！"),
  9821 -> HeadTail(s"ィ！", s"ィ！"),
  9822 -> HeadTail(s"ゥ！", s"ゥ！"),
  9823 -> HeadTail(s"ェ！", s"ェ！"),
  9824 -> HeadTail(s"ォ！", s"ォ！"),
  9825 -> HeadTail(s"茸", s"茸"),
  9826 -> HeadTail(s"筍", s"筍"),
  9827 -> HeadTail(s"キラー", s"キラー"),
  9828 -> HeadTail(s"ビーム", s"ビーム"),
  9829 -> HeadTail(s"バリア", s"バリア"),
  9830 -> HeadTail(s"定食", s"定食"),
  9831 -> HeadTail(s"襲来", s"襲来"),
  9832 -> HeadTail(s"撃退", s"撃退"),
  // 整地ランキング実績
  1001 -> HeadTail(s"整地神", s"整地神"),
  1002 -> HeadTail(s"四天王", s"四天王"),
  1003 -> HeadTail(s"重鎮", s"重鎮"),
  1004 -> HeadTail(s"大御所", s"大御所"),
  1005 -> HeadTail(s"ラヴ！", s"整地"),
  1006 -> HeadTail(s"欲まみれ", s"欲まみれ"),
  1007 -> HeadTail(s"整地", s"恋する"),
  1008 -> HeadTail(s"大地", s"友達"),
  1009 -> HeadTail(s"門戸", s"叩く"),
  1010 -> HeadTail(s"百傑", s"百傑"),
  1011 -> HeadTail(s"超人", s"挑む"),
  1012 -> HeadTail(s"継続", s"力なり"),
  // スターレベル実績
  // 整地量実績
  3001 -> HeadTail(s"超克者", s"超克者"),
  3002 -> HeadTail(s"永遠", s"旅人"),
  3003 -> HeadTail(s"破壊神", s"破壊神"),
  3004 -> HeadTail(s"理不尽", s"理不尽"),
  3005 -> HeadTail(s"枯れぬ", s"野望"),
  3006 -> HeadTail(s"登頂者", s"登頂者"),
  3007 -> HeadTail(s"不屈", s"不屈"),
  3008 -> HeadTail(s"ミリオネア", s"ミリオネア"),
  3009 -> HeadTail(s"夢追い", s"人"),
  3010 -> HeadTail(s"高み", s"目指す"),
  3011 -> HeadTail(s"参戦者", s"参戦者"),
  3012 -> HeadTail(s"いざ", s"新天地"),
  3013 -> HeadTail(s"努力", s"化身"),
  3014 -> HeadTail(s"虚無", s"連れる"),
  3015 -> HeadTail(s"敗北者", s"敗北者"),
  3016 -> HeadTail(s"開拓者", s"開拓者"),
  3017 -> HeadTail(s"侵略者", s"侵略者"),
  3018 -> HeadTail(s"蹂躙者", s"蹂躙者"),
  3019 -> HeadTail(s"真破壊神", s"真破壊神"),
  // ログイン時間実績
  4001 -> HeadTail(s"社蓄", s"極み"),
  4002 -> HeadTail(s"休日", s"爆撃機"),
  4003 -> HeadTail(s"企業", s"戦士"),
  4004 -> HeadTail(s"目覚め", s"夜"),
  4005 -> HeadTail(s"元気", s"百倍"),
  4006 -> HeadTail(s"三日目", s"朝"),
  4007 -> HeadTail(s"完徹", s"マン"),
  4008 -> HeadTail(s"さらば", s"終電"),
  4009 -> HeadTail(s"減らぬ", s"課題"),
  4010 -> HeadTail(s"期待", s"新人"),
  4011 -> HeadTail(s"道", s"続く"),
  4012 -> HeadTail(s"広がる", s"限界"),
  4013 -> HeadTail(s"過ぎ去る", s"過去"),
  4014 -> HeadTail(s"時", s"開拓者"),
  4015 -> HeadTail(s"止まれない", s"止まれない"),
  4016 -> HeadTail(s"挫けず", s"走る"),
  4017 -> HeadTail(s"一年分", s"一年分"),
  4018 -> HeadTail(s"次元", s"圧縮"),
  4019 -> HeadTail(s"失った", s"ブレーキ"),
  4020 -> HeadTail(s"最早", s"人生"),
  4021 -> HeadTail(s"ルーチン", s"ワーク"),
  4022 -> HeadTail(s"手段", s"目的"),
  4023 -> HeadTail(s"見えぬ", s"終着点"),
  // 中パーツ(初期開放)
  9901 -> MiddleOnly(s"は"),
  9902 -> MiddleOnly(s"な"),
  9903 -> MiddleOnly(s"と"),
  9904 -> MiddleOnly(s"に"),
  9905 -> MiddleOnly(s"の"),
  9906 -> MiddleOnly(s"も"),
  9907 -> MiddleOnly(s"が"),
  9908 -> MiddleOnly(s"で"),
  9909 -> MiddleOnly(s"を"),
  9910 -> MiddleOnly(s"から"),
  // 中パーツ(購入用)
  9911 -> MiddleOnly(s"的"),
  9912 -> MiddleOnly(s"・"),
  9913 -> MiddleOnly(s"＆"),
  9914 -> MiddleOnly(s"対"),
  9915 -> MiddleOnly(s"＝"),
  9916 -> MiddleOnly(s"☆"),
  9917 -> MiddleOnly(s"～"),
  9918 -> MiddleOnly(s"ぬ"),
  9919 -> MiddleOnly(s"より"),
  9920 -> MiddleOnly(s"風"),
  9921 -> MiddleOnly(s"式"),
  9922 -> MiddleOnly(s"－"),
  9923 -> MiddleOnly(s"＋"),
  9924 -> MiddleOnly(s"×"),
  9925 -> MiddleOnly(s"÷"),
  9926 -> MiddleOnly(s"＠"),
  9927 -> MiddleOnly(s"†"),
  9928 -> MiddleOnly(s"♡"),
  9929 -> MiddleOnly(s"."),
  9930 -> MiddleOnly(s"！"),
  9931 -> MiddleOnly(s"？"),
  9932 -> MiddleOnly(s"＃"),
  // 連続ログイン実績
  5001 -> HeadMiddle(s"絶え間", s"なき"),
  5002 -> HeadTail(s"永久", s"ループ"),
  5003 -> HeadTail(s"義務感", s"義務感"),
  5004 -> HeadTail(s"大好き", s"なのです"),
  5005 -> HeadTail(s"もはや", s"日課"),
  5006 -> HeadTail(s"マイ", s"ブーム"),
  5007 -> HeadTail(s"止まらない", s"止まらない"),
  5008 -> HeadTail(s"お楽しみ", s"お楽しみ"),
  // 通算ログイン実績
  5101 -> HeadTail(s"いざ", s"二週目"),
  5102 -> HeadTail(s"終わり", s"見えた"),
  5103 -> HeadTail(s"無欠勤", s"無欠勤"),
  5104 -> HeadTail(s"百", s"軌跡"),
  5105 -> HeadTail(s"ここ", s"おうち"),
  5106 -> HeadTail(s"忠誠心", s"忠誠心"),
  5107 -> HeadTail(s"月", s"捧げる"),
  5108 -> HeadTail(s"とにかく", s"入る"),
  5109 -> HeadTail(s"常連", s"常連"),
  5110 -> HeadTail(s"顔見知り", s"顔見知り"),
  5111 -> HeadTail(s"再び", s"再び"),
  5112 -> HeadTail(s"何でも", s"知ってる"),
  5113 -> HeadTail(s"無尽蔵", s"気合"),
  5114 -> HeadTail(s"親子", s"揃って"),
  5115 -> HeadTail(s"役員会", s"役員会"),
  5116 -> HeadTail(s"三度目", s"正直"),
  5117 -> HeadTail(s"いつまで", s"やろうか"),
  5118 -> HeadTail(s"昇給", s"まだですか"),
  5119 -> HeadTail(s"歴史", s"生き証人"),
  5120 -> FullSet(s"石の上", s"にも", s"三年"),
  // 投票数実績
  6001 -> HeadTail(s"狂信者", s"狂信者"),
  6002 -> HeadTail(s"全身", s"全霊"),
  6003 -> HeadTail(s"洗脳済", s"洗脳済"),
  6004 -> HeadTail(s"虜", s"なった"),
  6005 -> HeadOnly(s"魅惑"),
  6006 -> HeadTail(s"投票", s"魔"),
  6007 -> HeadOnly(s"熱烈"),
  6008 -> HeadTail(s"お気に入り", s"お気に入り"),
  // 公式イベント実績
  7001 -> HeadTail(s"目指す", s"栄光"),
  7002 -> HeadTail(s"貫禄", s"王者"),
  7003 -> HeadTail(s"顕現", s"立役者"),
  7004 -> FullSet(s"無", s"無から有へと", s"有"),
  7005 -> HeadTail(s"確か", s"一歩"),
  7006 -> HeadTail(s"春", s"妖精"),
  7007 -> HeadTail(s"匠", s"挑戦状"),
  7008 -> HeadTail(s"神器", s"造形主"),
  7009 -> HeadTail(s"生誕祭", s"採掘王"),
  7010 -> HeadTail(s"凍てつく", s"情熱"),
  7011 -> HeadTail(s"灼熱", s"海岸線"),
  7012 -> HeadTail(s"不沈", s"邸宅"),
  7013 -> HeadTail(s"雅", s"雅"),
  7014 -> HeadTail(s"トレビアン", s"トレビアン"),
  7015 -> HeadTail(s"未来", s"繋ぐ"),
  7016 -> HeadTail(s"妄想", s"炸裂"),
  7017 -> HeadTail(s"神秘", s"芸術家"),
  7018 -> HeadTail(s"自由", s"描く"),
  7019 -> HeadTail(s"情報屋", s"ぴっぴ"),
  7020 -> HeadTail(s"ペペロン", s"チーノ"),
  7021 -> HeadTail(s"奇跡", s"使徒"),
  7022 -> HeadTail(s"もしゃ", s"もしゃ"),
  7023 -> HeadTail(s"かみ", s"つかい"),
  7024 -> HeadTail(s"舞い散る", s"緑葉"),
  7025 -> HeadTail(s"ぬぬぬ", s"森"),
  7026 -> HeadTail(s"鋼鉄", s"意志"),
  7027 -> HeadTail(s"ベスト", s"エイジ"),
  7901 -> FullSet(s"幕張", s"→", s"整地鯖"),
  7902 -> HeadTail(s"顔バレ", s"しました"),
  7903 -> HeadTail(s"三次元", s"住人"),
  7904 -> HeadTail(s"やべー奴", s"いた"),
  7905 -> HeadTail(s"まいん", s"ちゃん"),
  7906 -> HeadTail(s"まいくら", s"ひろば"),
  // 極秘実績
  8001 -> HeadTail(s"時空", s"旅人"),
  8002 -> HeadTail(s"幸運", s"申し子"),
  8003 -> HeadTail(s"タスク", s"撃墜王"),
  // 記念日実績
  9001 -> HeadTail(s"お年玉", s"お年玉"),
  9002 -> HeadTail(s"サンタ", s"さん"),
  9003 -> HeadTail(s"おそば", s"おそば"),
  9004 -> FullSet(s"ヤギ", s"族", s"頭領"),
  9005 -> HeadTail(s"亀", s"ではない"),
  9006 -> HeadTail(s"鬼", s"鬼"),
  9007 -> HeadTail(s"創立者", s"創立者"),
  9008 -> HeadTail(s"友チョコ", s"０枚"),
  9009 -> HeadTail(s"魚類", s"魚類"),
  9010 -> HeadTail(s"どかん", s"一発"),
  9011 -> HeadTail(s"必要資金", s"０円"),
  9012 -> HeadTail(s"平等", s"主義"),
  9013 -> HeadTail(s"刈らないで", s"刈らないで"),
  9014 -> MiddleOnly(s" Iu999 "),
  9015 -> HeadTail(s"全科", s"Ｃ評価"),
  9016 -> FullSet(s"塵", s"しか", s"残さん"),
  9017 -> HeadTail(s"余す所", s"なき"),
  9018 -> HeadTail(s"紙耐久", s"紙耐久"),
  9019 -> HeadTail(s"空気", s"美味也"),
  9020 -> HeadTail(s"溢れ出る", s"母性"),
  9021 -> HeadTail(s"始点", s"分岐点"),
  9022 -> HeadTail(s"褪せない", s"記録"),
  9023 -> HeadTail(s"滲み出る", s"父性"),
  9024 -> HeadTail(s"ぐるっと", s"回って"),
  9025 -> FullSet(s"ｖ", s"(・v・)", s"ｖ"),
  9026 -> HeadTail(s"繋がる", s"願い"),
  9027 -> HeadTail(s"過密", s"地帯"),
  9028 -> FullSet(s"食卓", s"からの", s"食卓"),
  9029 -> FullSet(s"ミン", s"ミン", s"ミン"),
  9030 -> HeadTail(s"ジャングル", s"王"),
  9031 -> HeadTail(s"全力", s"逃げて"),
  9032 -> HeadTail(s"全部", s"焦げた"),
  9033 -> HeadTail(s"団子", s"団子"),
  9034 -> HeadTail(s"全額", s"投資"),
  9035 -> HeadTail(s"無尽蔵", s"気力"),
  9036 -> HeadTail(s"猛毒", s"直撃"),
  )
}

sealed trait NicknameParts {
  def head(): Option[String]
  
  def middle(): Option[String]
  
  def tail(): Option[String]
}

case class HeadTail(private val _head: String, private val _tail: String) extends NicknameParts {
  val head = Some(_head)
  val middle = None
  val tail = Some(_tail)
}

case class MiddleOnly(private val _middle: String) extends NicknameParts {
  val head = None
  val middle = Some(_middle)
  val tail = None
}

case class HeadOnly(private val _head: String) extends NicknameParts {
  val head = Some(_head)
  val middle = None
  val tail = None
}

case class FullSet(private val _head: String, private val _middle: String, private val _tail: String) extends NicknameParts {
  val head = Some(_head)
  val middle = Some(_middle)
  val tail = Some(_tail)
}

case class HeadMiddle(private val _head: String, private val _middle: String) extends NicknameParts {
  val head = Some(_head)
  val middle = Some(_middle)
  val tail = None
}

case class Undefined(private val id: Int) extends NicknameParts {
  val head = Some(s"[?H$id]")
  val middle = Some(s"[?M$id]")
  val tail = Some(s"[?T$id]")
}