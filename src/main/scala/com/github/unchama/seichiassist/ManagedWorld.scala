package com.github.unchama.seichiassist

import enumeratum._
import org.bukkit.World

sealed class ManagedWorld(val alphabetName: String, val japaneseName: String) extends EnumEntry

case object ManagedWorld extends Enum[ManagedWorld] {

  val values: IndexedSeq[ManagedWorld] = findValues
  case object WORLD_SPAWN extends ManagedWorld("world_spawn", "スポーンワールド")

  // "world"は旧メインワールドのidであり既に存在しない
  case object WORLD_2 extends ManagedWorld("world_2", "メインワールド")

  case object WORLD_SW extends ManagedWorld("world_SW", "第一整地ワールド")

  case object WORLD_SW_2 extends ManagedWorld("world_SW_2", "第二整地ワールド")

  case object WORLD_SW_3 extends ManagedWorld("world_SW_3", "第三整地ワールド")

  case object WORLD_SW_4 extends ManagedWorld("world_SW_4", "第四整地ワールド")

  case object WORLD_SW_NETHER extends ManagedWorld("world_SW_nether", "整地ネザー")

  case object WORLD_SW_END extends ManagedWorld("world_SW_the_end", "整地エンド")

  case object WORLD_BUILD extends ManagedWorld("world_build", "建築専用ワールド")

  case object WORLD_DOT extends ManagedWorld("world_dot", "ドット絵ワールド")

  implicit class ManagedWorldOps(val managedWorld: ManagedWorld) extends AnyVal {
    def isSeichi: Boolean = managedWorld match {
      case WORLD_SW
           | WORLD_SW_2
           | WORLD_SW_3
           | WORLD_SW_4
           | WORLD_SW_NETHER
           | WORLD_SW_END
           | WORLD_BUILD => true
      case _ => false
    }

    def shouldMuteCoreProtect: Boolean = isSeichiWorldWithWGRegions

    /**
     * 保護を掛けて整地するワールドであるかどうか
     */
    def isSeichiWorldWithWGRegions: Boolean = managedWorld match {
      case WORLD_SW_2 | WORLD_SW_4 => true
      case _ => false
    }

    /**
     * ブロックがカウントされるワールドかどうか
     */
    def shouldTrackBuildBlock: Boolean = managedWorld match {
      case WORLD_2
        | WORLD_SW
        | WORLD_SW_2
        | WORLD_SW_3
        | WORLD_SW_4
        | WORLD_SW_NETHER
        | WORLD_SW_END
        | WORLD_DOT
        | WORLD_BUILD => true
      case _ => false
    }

    /**
     * 直列設置スキルを発動できるワールドかどうか
     */
    def isBlockLineUpSkillEnabled: Boolean = if (SeichiAssist.DEBUG) {
      true
    } else shouldTrackBuildBlock
  }

  implicit class WorldOps(val world: World) {
    def asManagedWorld(): Option[ManagedWorld] = fromBukkitWorld(world)

    def isSeichi: Boolean = asManagedWorld().exists(_.isSeichi)

    def shouldTrackBuildBlock: Boolean = asManagedWorld().exists(_.shouldTrackBuildBlock)

    def isBlockLineUpSkillEnabled: Boolean = asManagedWorld().exists(_.isBlockLineUpSkillEnabled)
  }

  val seichiWorlds: IndexedSeq[ManagedWorld] = values.filter(_.isSeichi)

  def fromBukkitWorld(world: World): Option[ManagedWorld] = fromName(world.getName)

  def fromName(worldName: String): Option[ManagedWorld] = values.find(_.alphabetName == worldName)
}
