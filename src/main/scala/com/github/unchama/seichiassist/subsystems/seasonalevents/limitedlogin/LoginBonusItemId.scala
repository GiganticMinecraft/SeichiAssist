package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

sealed trait LoginBonusItemId

case class LoginBonus(itemId: LoginBonusItemId, amount: Int)

//region 配布するものの列挙

case object LoginBonusGachaTicket extends LoginBonusItemId

//endregion