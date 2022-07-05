package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import org.bukkit.entity.Player

/**
 * アイテムギフトの付与を実行するインタプリタ。
 */
abstract class GiftItemInterpreter[F[_], G[_]]
    extends ((Gift.Item, GachaPointApi[F, G, Player]) => Kleisli[F, Player, Unit]) {}
