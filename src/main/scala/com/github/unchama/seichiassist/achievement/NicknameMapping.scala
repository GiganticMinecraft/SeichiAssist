package com.github.unchama.seichiassist.achievement

import com.github.unchama.generic.CachedFunction
import com.github.unchama.seichiassist.achievement.SeichiAchievement._
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementGroup.PlacedBlockAmount

object NicknameMapping {

  case class NicknameCombination(first: Option[AchievementId],
                                 second: Option[AchievementId] = None,
                                 third: Option[AchievementId] = None)

  val getNicknameCombinationFor: SeichiAchievement => NicknameCombination = CachedFunction {
    case No_1001 => NicknameCombination(Some(1001))
    case No_1002 => NicknameCombination(Some(1002))
    case No_1003 => NicknameCombination(Some(1003))
    case No_1004 => NicknameCombination(Some(1004))
    case No_1005 => NicknameCombination(Some(1005), None, Some(1005))
    case No_1006 => NicknameCombination(Some(1006))
    case No_1007 => NicknameCombination(Some(1007), Some(9904), Some(1007))
    case No_1008 => NicknameCombination(Some(1008), Some(9901), Some(1008))
    case No_1009 => NicknameCombination(Some(1009), Some(9909), Some(1009))
    case No_1010 => NicknameCombination(Some(1010))
    case No_1011 => NicknameCombination(Some(1011), Some(9904), Some(1011))
    case No_1012 => NicknameCombination(Some(1012), Some(9901), Some(1012))

    case No_2001 => NicknameCombination(Some(2001), Some(9905), Some(2001))
    case No_2002 => NicknameCombination(Some(2002), Some(9905), Some(2002))
    case No_2003 => NicknameCombination(Some(2003), Some(9909), Some(2003))
    case No_2004 => NicknameCombination(Some(2004), None, Some(2004))
    case No_2005 => NicknameCombination(Some(2005), None, Some(2005))
    case No_2006 => NicknameCombination(Some(2006), Some(9909), Some(2006))
    case No_2007 => NicknameCombination(Some(2007), None, Some(2007))
    case No_2008 => NicknameCombination(Some(2008), Some(9901), Some(2008))
    case No_2009 => NicknameCombination(Some(2009), None, Some(2009))
    case No_2010 => NicknameCombination(Some(2010), None, Some(2010))
    case No_2011 => NicknameCombination(Some(2011), None, Some(2011))
    case No_2012 => NicknameCombination(Some(2012), Some(9905), Some(2012))
    case No_2013 => NicknameCombination(Some(2013), Some(9905), Some(2013))
    case No_2014 => NicknameCombination(Some(2014), None, Some(2014))


    case No_3001 => NicknameCombination(Some(3001))
    case No_3002 => NicknameCombination(Some(3002), Some(9905), Some(3002))
    case No_3003 => NicknameCombination(Some(3003))
    case No_3004 => NicknameCombination(Some(3004), Some(9902), None)
    case No_3005 => NicknameCombination(Some(3005), None, Some(3005))
    case No_3006 => NicknameCombination(Some(3006))
    case No_3007 => NicknameCombination(Some(3007), Some(9905), None)
    case No_3008 => NicknameCombination(Some(3008))
    case No_3009 => NicknameCombination(Some(3009), None, Some(3009))
    case No_3010 => NicknameCombination(Some(3010), Some(9909), Some(3010))
    case No_3011 => NicknameCombination(Some(3011))
    case No_3012 => NicknameCombination(Some(3012), None, Some(3012))
    case No_3013 => NicknameCombination(Some(3013), Some(9905), Some(3013))
    case No_3014 => NicknameCombination(Some(3014), Some(9909), Some(3014))
    case No_3015 => NicknameCombination(Some(3015))
    case No_3016 => NicknameCombination(Some(3016))
    case No_3017 => NicknameCombination(Some(3017))
    case No_3018 => NicknameCombination(Some(3018))
    case No_3019 => NicknameCombination(Some(3019))

    case No_4001 => NicknameCombination(Some(4001), Some(9905), Some(4001))
    case No_4002 => NicknameCombination(Some(4002), None, Some(4002))
    case No_4003 => NicknameCombination(Some(4003), None, Some(4003))
    case No_4004 => NicknameCombination(Some(4004), Some(9905), Some(4004))
    case No_4005 => NicknameCombination(Some(4005), None, Some(4005))
    case No_4006 => NicknameCombination(Some(4006), Some(9905), Some(4006))
    case No_4007 => NicknameCombination(Some(4007), None, Some(4007))
    case No_4008 => NicknameCombination(Some(4008), None, Some(4008))
    case No_4009 => NicknameCombination(Some(4009), None, Some(4009))
    case No_4010 => NicknameCombination(Some(4010), Some(9905), Some(4010))
    case No_4011 => NicknameCombination(Some(4011), Some(9901), Some(4011))
    case No_4012 => NicknameCombination(Some(4012), None, Some(4012))
    case No_4013 => NicknameCombination(Some(4013), None, Some(4013))
    case No_4014 => NicknameCombination(Some(4014), Some(9905), Some(4014))
    case No_4015 => NicknameCombination(Some(4015))
    case No_4016 => NicknameCombination(Some(4016), None, Some(4016))
    case No_4017 => NicknameCombination(Some(4017))
    case No_4018 => NicknameCombination(Some(4018), None, Some(4018))
    case No_4019 => NicknameCombination(Some(4019), None, Some(4019))
    case No_4020 => NicknameCombination(Some(4020), None, Some(4020))
    case No_4021 => NicknameCombination(Some(4021), None, Some(4021))
    case No_4022 => NicknameCombination(Some(4022), Some(9903), Some(4022))
    case No_4023 => NicknameCombination(Some(4023), None, Some(4023))

    case No_5001 => NicknameCombination(Some(5001), Some(5001))
    case No_5002 => NicknameCombination(Some(5002), None, Some(5002))
    case No_5003 => NicknameCombination(Some(5003))
    case No_5004 => NicknameCombination(Some(5004), None, Some(5004))
    case No_5005 => NicknameCombination(Some(5005), None, Some(5005))
    case No_5006 => NicknameCombination(Some(5006), None, Some(5006))
    case No_5007 => NicknameCombination(Some(5007))
    case No_5008 => NicknameCombination(Some(5008), Some(9905))

    case No_5101 => NicknameCombination(Some(5101), None, Some(5101))
    case No_5102 => NicknameCombination(Some(5102), Some(9907), Some(5102))
    case No_5103 => NicknameCombination(Some(5103), Some(9905), None)
    case No_5104 => NicknameCombination(Some(5104), Some(9905), Some(5104))
    case No_5105 => NicknameCombination(Some(5105), Some(9907), Some(5105))
    case No_5106 => NicknameCombination(Some(5106))
    case No_5107 => NicknameCombination(Some(5107), Some(9909), Some(5107))
    case No_5108 => NicknameCombination(Some(5108))
    case No_5109 => NicknameCombination(Some(5109))
    case No_5110 => NicknameCombination(Some(5110))
    case No_5111 => NicknameCombination(Some(5111))
    case No_5112 => NicknameCombination(Some(5112), None, Some(5112))
    case No_5113 => NicknameCombination(Some(5113), Some(9905), Some(5113))
    case No_5114 => NicknameCombination(Some(5114), None, Some(5114))
    case No_5115 => NicknameCombination(Some(5115))
    case No_5116 => NicknameCombination(Some(5116), Some(9905), Some(5116))
    case No_5117 => NicknameCombination(Some(5117), None, Some(5117))
    case No_5118 => NicknameCombination(Some(5118), None, Some(5118))
    case No_5119 => NicknameCombination(Some(5119), Some(9905), Some(5119))
    case No_5120 => NicknameCombination(Some(5120), Some(5120), Some(5120))

    case No_6001 => NicknameCombination(Some(6001))
    case No_6002 => NicknameCombination(Some(6002), None, Some(6002))
    case No_6003 => NicknameCombination(Some(6003))
    case No_6004 => NicknameCombination(Some(6004), Some(9903), Some(6004))
    case No_6005 => NicknameCombination(Some(6005), Some(9905))
    case No_6006 => NicknameCombination(Some(6006), None, Some(6006))
    case No_6007 => NicknameCombination(Some(6007), Some(9902))
    case No_6008 => NicknameCombination(Some(6008))

    case No_7001 => NicknameCombination(Some(7001), Some(9901), Some(7001))
    case No_7002 => NicknameCombination(Some(7002), Some(9905), Some(7002))
    case No_7003 => NicknameCombination(Some(7003), Some(9905), Some(7003))
    case No_7004 => NicknameCombination(None, Some(7004), None)
    case No_7005 => NicknameCombination(Some(7005), Some(9902), Some(7005))
    case No_7006 => NicknameCombination(Some(7006), Some(9905), Some(7006))
    case No_7007 => NicknameCombination(Some(7007), Some(9905), Some(7007))
    case No_7008 => NicknameCombination(Some(7008), Some(9905), Some(7008))
    case No_7010 => NicknameCombination(Some(7010), None, Some(7010))
    case No_7011 => NicknameCombination(Some(7011), Some(9905), Some(7011))
    case No_7012 => NicknameCombination(Some(7012), None, Some(7012))
    case No_7013 => NicknameCombination(Some(7013))
    case No_7014 => NicknameCombination(Some(7014))
    case No_7015 => NicknameCombination(Some(7015), Some(9904), Some(7015))
    case No_7016 => NicknameCombination(Some(7016), None, Some(7016))
    case No_7017 => NicknameCombination(Some(7017), Some(9905), Some(7017))
    case No_7018 => NicknameCombination(Some(7018), Some(9904), Some(7018))
    case No_7019 => NicknameCombination(Some(7019), None, Some(7019))
    case No_7020 => NicknameCombination(Some(7020), None, Some(7020))
    case No_7021 => NicknameCombination(Some(7021), Some(9905), Some(7021))
    case No_7022 => NicknameCombination(Some(7022), None, Some(7022))
    case No_7023 => NicknameCombination(Some(7023), Some(9905), Some(7023))
    case No_7024 => NicknameCombination(Some(7024), None, Some(7024))
    case No_7025 => NicknameCombination(Some(7025), Some(9905), Some(7025))
    case No_7026 => NicknameCombination(Some(7026), Some(9905), Some(7025))
    case No_7027 => NicknameCombination(Some(7027), None, Some(7027))

    case No_7901 => NicknameCombination(Some(7901), Some(7901), Some(7910))
    case No_7902 => NicknameCombination(Some(7902), None, Some(7902))
    case No_7903 => NicknameCombination(Some(7903), Some(9905), Some(7903))
    case No_7904 => NicknameCombination(Some(7904), Some(9907), Some(7904))
    case No_7905 => NicknameCombination(Some(7905), None, Some(7905))
    case No_7906 => NicknameCombination(Some(7906), None, Some(7906))

    case No_9001 => NicknameCombination(Some(9001))
    case No_9002 => NicknameCombination(Some(9002), None, Some(9002))
    case No_9003 => NicknameCombination(Some(9003))
    case No_9004 => NicknameCombination(Some(9004), Some(9004), Some(9004))
    case No_9005 => NicknameCombination(Some(9005), None, Some(9005))
    case No_9006 => NicknameCombination(Some(9006))
    case No_9007 => NicknameCombination(Some(9007))
    case No_9008 => NicknameCombination(Some(9008), None, Some(9008))
    case No_9009 => NicknameCombination(Some(9009))
    case No_9010 => NicknameCombination(Some(9010), Some(9903), Some(9010))
    case No_9011 => NicknameCombination(Some(9011), None, Some(9011))
    case No_9012 => NicknameCombination(Some(9012), None, Some(9012))
    case No_9013 => NicknameCombination(Some(9013))
    case No_9014 => NicknameCombination(None, Some(9014), None)
    case No_9015 => NicknameCombination(Some(9015), None, Some(9015))
    case No_9016 => NicknameCombination(Some(9016), Some(9016), Some(9016))
    case No_9017 => NicknameCombination(Some(9017), None, Some(9017))
    case No_9018 => NicknameCombination(Some(9018))
    case No_9019 => NicknameCombination(Some(9019), Some(9901), Some(9019))
    case No_9020 => NicknameCombination(Some(9020), None, Some(9020))
    case No_9021 => NicknameCombination(Some(9021), Some(9901), Some(9021))
    case No_9022 => NicknameCombination(Some(9022), None, Some(9022))
    case No_9023 => NicknameCombination(Some(9023), None, Some(9023))
    case No_9024 => NicknameCombination(Some(9024), None, Some(9024))
    case No_9025 => NicknameCombination(Some(9025), Some(9025), Some(9025))
    case No_9026 => NicknameCombination(Some(9026), None, Some(9026))
    case No_9027 => NicknameCombination(Some(9027), None, Some(9027))
    case No_9028 => NicknameCombination(Some(9028), Some(9028), Some(9028))
    case No_9029 => NicknameCombination(Some(9029), Some(9029), Some(9029))
    case No_9030 => NicknameCombination(Some(9030), Some(9905), Some(9030))
    case No_9031 => NicknameCombination(Some(9031), Some(9908), Some(9031))
    case No_9032 => NicknameCombination(Some(9032), None, Some(9032))
    case No_9033 => NicknameCombination(Some(9033), Some(9903), Some(9033))
    case No_9034 => NicknameCombination(Some(9034), None, Some(9034))
    case No_9035 => NicknameCombination(Some(9035), Some(9905), Some(9035))
    case No_9036 => NicknameCombination(Some(9036), None, Some(9036))

    case No_8001 => NicknameCombination(Some(8001), Some(9905), Some(8001))
    case No_8002 => NicknameCombination(Some(8002), Some(9905), Some(8002))
    case No_8003 => NicknameCombination(Some(8003), None, Some(8003))
  }

  def getTitleFor(achievement: SeichiAchievement): Nickname =
    getNicknameCombinationFor(achievement) match {
      case NicknameCombination(first, second, third) =>
        first.flatMap(Nicknames.getHeadPartFor).getOrElse("") +
          second.flatMap(Nicknames.getMiddlePartFor).getOrElse("") +
          third.flatMap(Nicknames.getTailPartFor).getOrElse("")
    }
}
