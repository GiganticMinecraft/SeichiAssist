# コーディングガイドライン

## \[Scala\] nullを使わない
Scalaのファイルにおいては、`null`を使用する代わりに`Option`を使用してください。

Java (特に、Bukkit/Spigot API) から入ってきた値が`null`になる可能性がある場合は、その呼び出しを早いうちに[`Option(...)`](https://www.scala-lang.org/api/2.13.4/scala/Option$.html#apply[A](x:A):Option[A])で囲い、`Option`にすることが推奨されます。この呼び出しで、引数が`null`だった場合は`None`に、そうでなかった場合は`Some(...)`になります。

## \[Scala\] 例外を使わない
例外の代わりに`Either`を使用してください。`Either`を使用すると低コストで合成を行うことができるためです。

## Javaコードを追加しない
Scalaを書くことができる場合はScalaを使ってください。

## メインスレッドでの処理は最小限に
メインスレッドで計算なども含めたすべての処理を行うことはサーバーのラグにつながるため、推奨されません。
推奨される処理の記述方法は次のとおりです。

1. 処理に必要なデータを得る
2. 非メインスレッドに遷移し、処理を行う
3. 処理結果の反映を行う

特に、必要なデータや処理結果の反映にワールドやエンティティが含まれる場合は、データの取得と反映をメインスレッドで行う必要があります。これはBukkitによる制約で、私達ではどうすることも出来ません。

## 型チェック・キャストを使わない
`asInstanceOf`と`isInstanceOf`はできるだけ使わないようにしましょう。Javaのキャスト演算子、`instanceof`演算子と同様の危険性および非効率性があります。最悪の場合全く関係ない型にキャストすることによって`ClassCastException`が発生し、サーバーが落ちる危険性があるので**避けられる場合は絶対に使わないでください。** shapelessなどのコンパイル時型プログラミングを積極的に活用するのも良いでしょう。

もしどうしても使わなければならない場合は、注意して影響範囲を**最小限に**押し込めるとともに、そこへのフローをできる限り制限してください。

----
※以下は、2023年7月25日現在の情報であり、古くなっている可能性があります

## メニュー画面を実装するために`InventoryClickEvent`を使わない
`InventoryClickEvent`はメニュー以外のインベントリ (例: チェスト) を操作したときも拾ってしまうため、メニューを実装するためにホックすることは推奨されません。
代わりに、[`Menu`](https://github.com/GiganticMinecraft/SeichiAssist/blob/41e63c0493621ff8afa32bce902d34a62ae466d2/src/main/scala/com/github/unchama/menuinventory/Menu.scala)を使ってください。

## 「サブシステム」へ分割、依存、及び準拠する
SeichiAssistには複数の関心事があり、その大半が「サブシステム」と呼ばれるひとまとまりとして`com.github.unchama.seichiassist.subsystems`以下にまとめられています。
[最新の一覧](https://github.com/GiganticMinecraft/SeichiAssist/tree/develop/src/main/scala/com/github/unchama/seichiassist/subsystems)。

各サブシステムはそれぞれ他のサブシステムなどで使うことを想定したAPIを `trait` として持ち、各サブシステムはその抽象に対してのみ依存することができます。サブシステムの実装はAPIに具象的な実装を与える必要があります。APIの名前はおおよそ`Api`か`API`で終わります。

関心事同士の依存 (例: 整地スキルによってマナを減少させるためにマナを操作する) は 抽象の型を `implicit` パラメーターとして渡すことにより表現されます。

抽象同士の循環参照は推奨されません。

新しくサブシステムを作った場合、`SeichiAssist.scala` 内で初期化することを忘れないでください。初期化を忘れると正しく機能しません。

サブシステムの構造は慣例的に次のようになっています。新しくサブシステムを作る場合は一貫性のためにこの構造に準拠することが推奨されます。

* (サブシステムのルート)
  * `application` - タスク、データのレポジトリの初期化・終了処理など
  * `bukkit` - Bukkit固有の実装
    * `commands` - Bukkitのコマンド
    * `listeners` - Bukkitのイベントリスナー
  * `domain` - モデル
  * `infrastructure` - インフラ (JDBCによるデータベースへの保存・読み込みなど)
  * `subsystems` - 子サブシステム
  * `System` - サブシステムの「親分」
