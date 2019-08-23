package com.github.unchama.seichiassist.data.playerdata

import java.time.LocalDate

data class LoginStatus(val lastLoginDate: LocalDate?, val totalLoginDay: Int = 0, val chainLoginDay: Int = 0)