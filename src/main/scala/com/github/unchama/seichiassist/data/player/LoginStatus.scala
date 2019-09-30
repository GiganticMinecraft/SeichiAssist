package com.github.unchama.seichiassist.data.player

import java.time.LocalDate

case class LoginStatus(lastLoginDate: Option[LocalDate], totalLoginDay: Int = 0, consecutiveLoginDays: Int = 0)