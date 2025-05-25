package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.Monad

trait RegionCreationPolicy[F[_], Player, World, Location] {

  def isGridProtectionEnabledWorld(world: World): F[Boolean]

  def isNotOverlapping(
    world: World,
    regionSelectionCorners: RegionSelectionCorners[Location]
  ): F[Boolean]

  def isWithinRegionUnitSizeLimit(
    shape: SubjectiveRegionShape,
    world: World,
    getGridLimitPerWorld: GetGridUnitSizeLimitPerWorld[F, World]
  ): F[Boolean]

  def isNotOverRegionCountLimit(
    world: World,
    player: Player,
    getRegionCountLimit: GetRegionCountLimit[F, World]
  ): F[Boolean]

  import cats.implicits._
  protected implicit val F: Monad[F]

  final def validate(
    world: World,
    shape: SubjectiveRegionShape,
    regionSelectionCorners: RegionSelectionCorners[Location],
    player: Player
  )(
    implicit getGridLimitPerWorld: GetGridUnitSizeLimitPerWorld[F, World],
    getRegionCountLimit: GetRegionCountLimit[F, World]
  ): F[RegionCreationResult] = for {
    isEnabledWorld <- isGridProtectionEnabledWorld(world)
    isNotOverlapping <- isNotOverlapping(world, regionSelectionCorners)
    isWithinRegionUnitSizeLimit <- isWithinRegionUnitSizeLimit(
      shape,
      world,
      getGridLimitPerWorld
    )
    isNotOverRegionCountLimit <- isNotOverRegionCountLimit(world, player, getRegionCountLimit)
  } yield {
    if (
      isEnabledWorld && isNotOverlapping && isWithinRegionUnitSizeLimit && isNotOverRegionCountLimit
    ) {
      RegionCreationResult.Success
    } else if (!isEnabledWorld) {
      RegionCreationResult.WorldProhibitsRegionCreation
    } else {
      RegionCreationResult.Error
    }

  }

}
