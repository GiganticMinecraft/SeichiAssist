package com.github.unchama.generic.tag

// From shapeless

/*
 * Copyright (c) 2011-16 Miles Sabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

object tag {

  /**
   * `T` に幽霊型 `U` を付与したタグ付き型。`T @@ U <: T` であり、実行時表現は `T` そのもの。
   *
   * NOTE(scala3): Scala 2時代はShapeless由来の交差型 `T with Tagged[U]` と
   * `asInstanceOf` によるエンコーディングだったが、Scala 3では交差型の消去規則が
   * 異なり、フィールドのJVM型が `Tagged` 側へ消去されて格納時に
   * ClassCastException を起こす（実際に PluginExecutionContexts の初期化が
   * ExceptionInInitializerError でプラグインのロードを失敗させた）。
   * このため、消去が `T` になる上限境界付き opaque type へ変更した。
   */
  opaque type @@[+T, U] <: T = T

  def apply[U] = new Tagger[U]

  class Tagger[U] {
    def apply[T](t: T): T @@ U = t
  }

}
