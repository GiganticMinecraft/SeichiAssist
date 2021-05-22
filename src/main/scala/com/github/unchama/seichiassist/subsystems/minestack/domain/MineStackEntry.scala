package com.github.unchama.seichiassist.subsystems.minestack.domain

case class MineStackEntry[IS](category: MineStackCategory, name: String, item: MineStackItemArchetype[IS])
