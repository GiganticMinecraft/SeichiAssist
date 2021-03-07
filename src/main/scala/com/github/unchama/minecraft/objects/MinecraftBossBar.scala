package com.github.unchama.minecraft.objects

import com.github.unchama.generic.ReadWrite

/**
 * ボスバーに対する操作の抽象
 */
trait MinecraftBossBar[F[_]] {
  type Player
  type Style
  type Color
  type Flag

  trait FlagOperations {
    def add(flag: Flag): F[Unit]

    def remove(flag: Flag): F[Unit]

    def has(flag: Flag): F[Unit]
  }

  trait PlayerOperations {
    val removeAll: F[Unit]

    def add(player: Player): F[Unit]

    def remove(player: Player): F[Unit]

    def getAll: F[List[Player]]
  }

  val title: ReadWrite[F, String]
  val color: ReadWrite[F, Color]
  val style: ReadWrite[F, Style]
  val progress: ReadWrite[F, Double]
  val visibility: ReadWrite[F, Boolean]

  val flags: FlagOperations
  val players: PlayerOperations
}
