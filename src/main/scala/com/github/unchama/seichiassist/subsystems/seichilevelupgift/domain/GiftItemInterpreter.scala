package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import org.bukkit.entity.Player

/**
 * アイテムギフトの付与を実行するインタプリタ。
 */
abstract class GiftItemInterpreter[F[_]] extends (Gift.Item => Kleisli[F, Player, Unit]) {}
