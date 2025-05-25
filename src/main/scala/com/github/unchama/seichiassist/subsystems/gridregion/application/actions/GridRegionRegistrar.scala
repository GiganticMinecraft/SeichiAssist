package com.github.unchama.seichiassist.subsystems.gridregion.application.actions

import cats.Monad
import com.github.unchama.seichiassist.subsystems.gridregion.domain._

class GridRegionRegistrar[F[_]: Monad, Location, Player, World](
  implicit regionDefiner: RegionDefiner[F, Location],
  policy: RegionCreationPolicy[F, Player, World, Location],
  getGridLimitPerWorld: GetGridUnitSizeLimitPerWorld[F, World],
  getRegionCountLimit: GetRegionCountLimit[F, World],
  createRegion: CreateRegion[F, Player, Location]
) {

  import cats.implicits._

  /**
   * @return `currentLocation` を基準とした `shape` の領域の保護を作成した結果を返す作用
   */
  def validateAndCreateRegion(
    player: Player,
    world: World,
    currentLocation: Location,
    shape: SubjectiveRegionShape
  ): F[RegionCreationResult] = for {
    corners <- regionDefiner.getSelectionCorners(currentLocation, shape)
    result <- policy.validate(world, shape, corners, player)
    _ <- createRegion(player, corners).whenA(result == RegionCreationResult.Success)
  } yield result

}
