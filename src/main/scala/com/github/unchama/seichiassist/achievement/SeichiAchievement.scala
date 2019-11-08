package com.github.unchama.seichiassist.achievement

import java.time.{DayOfWeek, Month}

import enumeratum.{Enum, EnumEntry}

sealed abstract class SeichiAchievement extends EnumEntry

object SeichiAchievement extends Enum[SeichiAchievement] {
  case class AutoUnlocked[A](id: Int, condition: AchievementCondition[A]) extends SeichiAchievement
  case class ManuallyUnlocked[A](id: Int, condition: AchievementCondition[A]) extends SeichiAchievement
  case class HiddenAutoUnlocked[A](id: Int, condition: HiddenAchievementCondition[A]) extends SeichiAchievement
  case class HiddenManuallyUnlocked[A](id: Int, condition: HiddenAchievementCondition[A]) extends SeichiAchievement
  case class GrantedByConsole[A](id: Int, condition: String, explanation: Option[List[String]]) extends SeichiAchievement

  import AchievementConditions._
  import WithPlaceholder._

  // 整地神ランキング
  object No_1001 extends AutoUnlocked(1001, brokenBlockRankingPosition_<=(1))
  object No_1002 extends AutoUnlocked(1002, brokenBlockRankingPosition_<=(5))
  object No_1003 extends AutoUnlocked(1003, brokenBlockRankingPosition_<=(27))
  object No_1004 extends AutoUnlocked(1004, brokenBlockRankingPosition_<=(50))
  object No_1005 extends AutoUnlocked(1005, brokenBlockRankingPosition_<=(750))
  object No_1006 extends AutoUnlocked(1006, brokenBlockRankingPosition_<=(1000))
  object No_1007 extends AutoUnlocked(1007, brokenBlockRankingPosition_<=(2500))
  object No_1008 extends AutoUnlocked(1008, brokenBlockRankingPosition_<=(5000))
  object No_1009 extends AutoUnlocked(1009, brokenBlockRankingPosition_<=(10000))
  object No_1010 extends AutoUnlocked(1010, brokenBlockRankingPosition_<=(100))
  object No_1011 extends AutoUnlocked(1011, brokenBlockRankingPosition_<=(250))
  object No_1012 extends AutoUnlocked(1012, brokenBlockRankingPosition_<=(500))

  // 整地量
  object No_3001 extends HiddenAutoUnlocked(3001, dependsOn(3002, brokenBlockAmount_>=(2147483646L, "int型の壁")))
  object No_3002 extends AutoUnlocked(3002, brokenBlockAmount_>=(1000000000L, "10億"))
  object No_3003 extends AutoUnlocked(3003, brokenBlockAmount_>=(500000000L, "5億"))
  object No_3004 extends AutoUnlocked(3004, brokenBlockAmount_>=(100000000L, "1億"))
  object No_3005 extends AutoUnlocked(3005, brokenBlockAmount_>=(50000000L, "5000万"))
  object No_3006 extends AutoUnlocked(3006, brokenBlockAmount_>=(10000000L, "1000万"))
  object No_3007 extends AutoUnlocked(3007, brokenBlockAmount_>=(5000000L, "500万"))
  object No_3008 extends AutoUnlocked(3008, brokenBlockAmount_>=(1000000L, "100万"))
  object No_3009 extends AutoUnlocked(3009, brokenBlockAmount_>=(500000L, "50万"))
  object No_3010 extends AutoUnlocked(3010, brokenBlockAmount_>=(100000L, "10万"))
  object No_3011 extends AutoUnlocked(3011, brokenBlockAmount_>=(10000L, "1万"))
  object No_3012 extends HiddenAutoUnlocked(3012, dependsOn(3001, brokenBlockAmount_>=(3000000000L, "30億")))
  object No_3013 extends HiddenAutoUnlocked(3013, dependsOn(3001, brokenBlockAmount_>=(4000000000L, "40億")))
  object No_3014 extends HiddenAutoUnlocked(3014, dependsOn(3001, brokenBlockAmount_>=(5000000000L, "50億")))
  object No_3015 extends HiddenAutoUnlocked(3015, dependsOn(3014, brokenBlockAmount_>=(6000000000L, "60億")))
  object No_3016 extends HiddenAutoUnlocked(3016, dependsOn(3015, brokenBlockAmount_>=(7000000000L, "70億")))
  object No_3017 extends HiddenAutoUnlocked(3017, dependsOn(3016, brokenBlockAmount_>=(8000000000L, "80億")))
  object No_3018 extends HiddenAutoUnlocked(3018, dependsOn(3017, brokenBlockAmount_>=(9000000000L, "90億")))
  object No_3019 extends HiddenAutoUnlocked(3019, dependsOn(3018, brokenBlockAmount_>=(10000000000L, "100億")))

  import scala.concurrent.duration._

  // 参加時間
  object No_4001 extends HiddenAutoUnlocked(4001, dependsOn(4002, totalPlayTime_>=(2000.hours, "2000時間")))
  object No_4002 extends AutoUnlocked(4002, totalPlayTime_>=(1000.hours, "1000時間"))
  object No_4003 extends AutoUnlocked(4003, totalPlayTime_>=(500.hours, "500時間"))
  object No_4004 extends AutoUnlocked(4004, totalPlayTime_>=(250.hours, "250時間"))
  object No_4005 extends AutoUnlocked(4005, totalPlayTime_>=(100.hours, "100時間"))
  object No_4006 extends AutoUnlocked(4006, totalPlayTime_>=(50.hours, "50時間"))
  object No_4007 extends AutoUnlocked(4007, totalPlayTime_>=(24.hours, "24時間"))
  object No_4008 extends AutoUnlocked(4008, totalPlayTime_>=(15.hours, "15時間"))
  object No_4009 extends AutoUnlocked(4009, totalPlayTime_>=(5.hours, "5時間"))
  object No_4010 extends AutoUnlocked(4010, totalPlayTime_>=(1.hour, "1時間"))
  object No_4011 extends HiddenAutoUnlocked(4011, dependsOn(4002, totalPlayTime_>=(3000.hours, "3000時間")))
  object No_4012 extends HiddenAutoUnlocked(4012, dependsOn(4002, totalPlayTime_>=(4000.hours, "4000時間")))
  object No_4013 extends HiddenAutoUnlocked(4013, dependsOn(4002, totalPlayTime_>=(5000.hours, "5000時間")))
  object No_4014 extends HiddenAutoUnlocked(4014, dependsOn(4013, totalPlayTime_>=(6000.hours, "6000時間")))
  object No_4015 extends HiddenAutoUnlocked(4015, dependsOn(4013, totalPlayTime_>=(7000.hours, "7000時間")))
  object No_4016 extends HiddenAutoUnlocked(4016, dependsOn(4013, totalPlayTime_>=(8000.hours, "8000時間")))
  object No_4017 extends HiddenAutoUnlocked(4017, dependsOn(4013, totalPlayTime_>=(9000.hours, "9000時間")))
  object No_4018 extends HiddenAutoUnlocked(4018, dependsOn(4013, totalPlayTime_>=(10000.hours, "10000時間")))
  object No_4019 extends HiddenAutoUnlocked(4019, dependsOn(4018, totalPlayTime_>=(12000.hours, "12000時間")))
  object No_4020 extends HiddenAutoUnlocked(4020, dependsOn(4019, totalPlayTime_>=(14000.hours, "14000時間")))
  object No_4021 extends HiddenAutoUnlocked(4021, dependsOn(4020, totalPlayTime_>=(16000.hours, "16000時間")))
  object No_4022 extends HiddenAutoUnlocked(4022, dependsOn(4021, totalPlayTime_>=(18000.hours, "18000時間")))
  object No_4023 extends HiddenAutoUnlocked(4023, dependsOn(4022, totalPlayTime_>=(20000.hours, "20000時間")))

  // 連続ログイン
  object No_5001 extends AutoUnlocked(5001, consecutiveLoginDays_>=(100))
  object No_5002 extends AutoUnlocked(5002, consecutiveLoginDays_>=(50))
  object No_5003 extends AutoUnlocked(5003, consecutiveLoginDays_>=(30))
  object No_5004 extends AutoUnlocked(5004, consecutiveLoginDays_>=(20))
  object No_5005 extends AutoUnlocked(5005, consecutiveLoginDays_>=(10))
  object No_5006 extends AutoUnlocked(5006, consecutiveLoginDays_>=(5))
  object No_5007 extends AutoUnlocked(5007, consecutiveLoginDays_>=(3))
  object No_5008 extends AutoUnlocked(5008, consecutiveLoginDays_>=(2))

  // 通算ログイン
  object No_5101 extends AutoUnlocked(5101, totalPlayedDays_>=(365))
  object No_5102 extends AutoUnlocked(5102, totalPlayedDays_>=(300))
  object No_5103 extends AutoUnlocked(5103, totalPlayedDays_>=(200))
  object No_5104 extends AutoUnlocked(5104, totalPlayedDays_>=(100))
  object No_5105 extends AutoUnlocked(5105, totalPlayedDays_>=(75))
  object No_5106 extends AutoUnlocked(5106, totalPlayedDays_>=(50))
  object No_5107 extends AutoUnlocked(5107, totalPlayedDays_>=(30))
  object No_5108 extends AutoUnlocked(5108, totalPlayedDays_>=(20))
  object No_5109 extends AutoUnlocked(5109, totalPlayedDays_>=(10))
  object No_5110 extends AutoUnlocked(5110, totalPlayedDays_>=(5))
  object No_5111 extends AutoUnlocked(5111, totalPlayedDays_>=(2))
  object No_5112 extends HiddenAutoUnlocked(5112, dependsOn(5101, totalPlayedDays_>=(400)))
  object No_5113 extends HiddenAutoUnlocked(5113, dependsOn(5112, totalPlayedDays_>=(500)))
  object No_5114 extends HiddenAutoUnlocked(5114, dependsOn(5113, totalPlayedDays_>=(600)))
  object No_5115 extends HiddenAutoUnlocked(5115, dependsOn(5114, totalPlayedDays_>=(700)))
  object No_5116 extends HiddenAutoUnlocked(5116, dependsOn(5115, totalPlayedDays_>=(730)))
  object No_5117 extends HiddenAutoUnlocked(5117, dependsOn(5116, totalPlayedDays_>=(800)))
  object No_5118 extends HiddenAutoUnlocked(5118, dependsOn(5117, totalPlayedDays_>=(900)))
  object No_5119 extends HiddenAutoUnlocked(5119, dependsOn(5118, totalPlayedDays_>=(1000)))
  object No_5120 extends HiddenAutoUnlocked(5120, dependsOn(5119, totalPlayedDays_>=(1095)))

  // 投票数
  object No_6001 extends AutoUnlocked(6001, voteCount_>=(365))
  object No_6002 extends AutoUnlocked(6002, voteCount_>=(200))
  object No_6003 extends AutoUnlocked(6003, voteCount_>=(100))
  object No_6004 extends AutoUnlocked(6004, voteCount_>=(50))
  object No_6005 extends AutoUnlocked(6005, voteCount_>=(25))
  object No_6006 extends AutoUnlocked(6006, voteCount_>=(10))
  object No_6007 extends AutoUnlocked(6007, voteCount_>=(5))
  object No_6008 extends AutoUnlocked(6008, voteCount_>=(1))

  // 公式イベント
  object No_7001 extends GrantedByConsole(7001, "公式イベント「整地大会」に参加する", None)
  object No_7002 extends GrantedByConsole(7002, "公式イベント「整地大会」にて総合優勝", None)
  object No_7003 extends GrantedByConsole(7003, "公式イベント「建築コンペ」で最優秀賞獲得", None)
  object No_7004 extends GrantedByConsole(7004, "公式イベント「建築コンペ」で優秀賞獲得", None)
  object No_7005 extends GrantedByConsole(7005, "公式イベント「建築コンペ」で佳作賞獲得", None)
  object No_7006 extends GrantedByConsole(7006, "公式イベント「第一回建築コンペ」で配布", Some(List("開催テーマは「桜」でした。")))
  object No_7007 extends GrantedByConsole(7007, "公式イベント「第二回建築コンペ」で配布", Some(List("開催テーマは「アスレチック」でした。")))
  object No_7008 extends GrantedByConsole(7008, "公式イベント「GTテクスチャコンペ」で採用", None)
  // 7009は元の仕様では獲得していないと表示されないことになっている
  object No_7009 extends GrantedByConsole(7009, "公式イベント「第二回整地大会」で優勝", Some(List("整地鯖一周年記念イベントでした！")))
  object No_7010 extends GrantedByConsole(7010, "公式イベント「第三回建築コンペ」で配布", Some(List("開催テーマＡは「氷像(夏)」でした。")))
  object No_7011 extends GrantedByConsole(7011, "公式イベント「第三回建築コンペ」で配布", Some(List("開催テーマＢは「海岸建築(夏)」でした。")))
  object No_7012 extends GrantedByConsole(7012, "公式イベント「第三回建築コンペ」で配布", Some(List("開催テーマＣは「海上建築(夏)」でした。")))
  object No_7013 extends GrantedByConsole(7013, "公式イベント「第四回建築コンペ」で配布", Some(List("開催テーマＡは「和風建築」でした。")))
  object No_7014 extends GrantedByConsole(7014, "公式イベント「第四回建築コンペ」で配布", Some(List("開催テーマＢは「洋風建築」でした。")))
  object No_7015 extends GrantedByConsole(7015, "公式イベント「第四回建築コンペ」で配布", Some(List("開催テーマＣは「モダン建築」でした。")))
  object No_7016 extends GrantedByConsole(7016, "公式イベント「第四回建築コンペ」で配布", Some(List("開催テーマＤは「ファンタジー」でした。")))
  object No_7017 extends GrantedByConsole(7017, "公式イベント「イラストコンテスト」でグランプリ獲得", None)
  object No_7018 extends GrantedByConsole(7018, "公式イベント「イラストコンテスト」に参加する", None)
  object No_7019 extends GrantedByConsole(7019, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(Yukki_XD)獲得")))
  object No_7020 extends GrantedByConsole(7020, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(rukure2017)獲得")))
  object No_7021 extends GrantedByConsole(7021, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(Lucky3028)獲得")))
  object No_7022 extends GrantedByConsole(7022, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(tokuzi_)獲得")))
  object No_7023 extends GrantedByConsole(7023, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(kamikami46)獲得")))
  object No_7024 extends GrantedByConsole(7024, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(CourageousLeaf)獲得")))
  object No_7025 extends GrantedByConsole(7025, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(nubasu)獲得")))
  object No_7026 extends GrantedByConsole(7026, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(tetsusan)獲得")))
  object No_7027 extends GrantedByConsole(7027, "公式イベント「イラストコンテスト」で配布", Some(List("条件：審査員賞(tar0ss)獲得")))
  object No_7901 extends GrantedByConsole(7901, "超会議2018で配布", None)
  object No_7902 extends GrantedByConsole(7902, "超会議2018で配布", None)
  object No_7903 extends GrantedByConsole(7903, "超会議2018で配布", None)
  object No_7904 extends GrantedByConsole(7904, "超会議2018で配布", None)
  object No_7905 extends GrantedByConsole(7905, "超会議2018で配布", None)
  object No_7906 extends GrantedByConsole(7906, "超会議2018で配布", None)

  // 特殊
  object No_9001 extends ManuallyUnlocked(9001, playedOn(Month.JANUARY, 1, "とある始まりの日"))
  object No_9002 extends ManuallyUnlocked(9002, playedOn(Month.DECEMBER, 25, "とある聖夜の日"))
  object No_9003 extends ManuallyUnlocked(9003, playedOn(Month.DECEMBER, 31, "とある終わりの日"))
  object No_9004 extends ManuallyUnlocked(9004, playedIn(Month.JANUARY))
  object No_9005 extends ManuallyUnlocked(9005, playedIn(Month.FEBRUARY))
  object No_9006 extends ManuallyUnlocked(9006, playedOn(Month.FEBRUARY, 3, "とあるお豆の絨毯爆撃の日"))
  object No_9007 extends ManuallyUnlocked(9007, playedOn(Month.FEBRUARY, 11, "建国記念日"))
  object No_9008 extends ManuallyUnlocked(9008, playedOn(Month.FEBRUARY, 14, "とあるカカオまみれの日"))
  object No_9009 extends ManuallyUnlocked(9009, playedIn(Month.MARCH))
  object No_9010 extends ManuallyUnlocked(9010, playedOn(Month.MARCH, 3, "とある女の子の日"))
  object No_9011 extends ManuallyUnlocked(9011, playedOn(Month.MARCH, 14, "燃え尽きたカカオだらけの日"))
  object No_9012 extends ManuallyUnlocked(9012, playedOn(Month.MARCH, 20, "春分の日"))
  object No_9013 extends ManuallyUnlocked(9013, playedIn(Month.APRIL))
  object No_9014 extends ManuallyUnlocked(9014, playedOn(Month.APRIL, 1, "とある嘘の日"))
  object No_9015 extends ManuallyUnlocked(9015, playedOn(Month.APRIL, 15, "とある良い子の日"))
  object No_9016 extends ManuallyUnlocked(9016, playedOn(Month.APRIL, 22, "とある掃除デー"))
  object No_9017 extends ManuallyUnlocked(9017, playedIn(Month.MAY))
  object No_9018 extends ManuallyUnlocked(9018, playedOn(Month.MAY, 5, "とある子供の日"))
  object No_9019 extends ManuallyUnlocked(9019, playedOn(Month.MAY, 5, "端午の節句"))
  object No_9020 extends ManuallyUnlocked(9020, playedOn(Month.MAY, 2, DayOfWeek.SUNDAY, "母の日"))
  object No_9021 extends ManuallyUnlocked(9021, playedIn(Month.JUNE))
  object No_9022 extends ManuallyUnlocked(9022, playedOn(Month.JUNE, 12, "とある日記の日"))
  object No_9023 extends ManuallyUnlocked(9023, playedOn(Month.JUNE, 17, "父の日"))
  object No_9024 extends ManuallyUnlocked(9024, playedOn(Month.JUNE, 29, "とある生誕の日"))
  object No_9025 extends ManuallyUnlocked(9025, playedIn(Month.JULY))
  object No_9026 extends ManuallyUnlocked(9026, playedOn(Month.JULY, 7, "七夕"))
  object No_9027 extends ManuallyUnlocked(9027, playedOn(Month.JULY, 17, "とある東京の日"))
  object No_9028 extends ManuallyUnlocked(9028, playedOn(Month.JULY, 29, "とある肉の日"))
  object No_9029 extends ManuallyUnlocked(9029, playedIn(Month.AUGUST))
  object No_9030 extends ManuallyUnlocked(9030, playedOn(Month.AUGUST, 7, "とあるバナナの日"))
  object No_9031 extends ManuallyUnlocked(9031, playedOn(Month.AUGUST, 16, "とあるJDの日"))
  object No_9032 extends ManuallyUnlocked(9032, playedOn(Month.AUGUST, 29, "とある焼肉の日"))
  object No_9033 extends ManuallyUnlocked(9033, playedIn(Month.SEPTEMBER))
  object No_9034 extends ManuallyUnlocked(9034, playedOn(Month.SEPTEMBER, 2, "とあるくじの日"))
  object No_9035 extends ManuallyUnlocked(9035, playedOn(Month.SEPTEMBER, 12, "とあるマラソンの日"))
  object No_9036 extends ManuallyUnlocked(9036, playedOn(Month.SEPTEMBER, 29, "とあるふぐの日"))

  // 極秘任務
  object No_8001 extends HiddenAutoUnlocked(8001, conditionFor8001)
  object No_8002 extends HiddenAutoUnlocked(8002, conditionFor8002)
  object No_8003 extends HiddenManuallyUnlocked(8003, conditionFor8003)

  val values: IndexedSeq[SeichiAchievement] = findValues
}
