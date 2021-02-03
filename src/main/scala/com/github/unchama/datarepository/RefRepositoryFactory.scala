package com.github.unchama.datarepository

trait RefRepositoryFactory[F[_], Key, Value, Repository[ref[_], x] <: KeyedDataRepository[Key, ref[x]]] {

  final type RefCreator[Ref[_]] = (Key, Value) => F[Ref[Value]]

  /**
   * 可変参照を作成する関数 `(Key, Value) => F[Ref[F, Value]]` から [[Repository]] を作成する。
   *
   * 作成された [[Repository]] の可変参照セルが必ず `refCreator` によって作成されたということを保証するため、
   * この関数は可変参照セルの型コンストラクタ [[Ref]] について多相的になっている。
   */
  def instantiate[Ref[_]](refCreator: RefCreator[Ref]): F[Repository[Ref, Value]]

}
