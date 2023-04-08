# コーディングガイドライン

## 非推奨事項

### Scalaにおけるnullの使用
Scalaにおいては、`null`を使用する代わりに`Option`を使用してください。
Java (特に、Bukkit/Spigot API) から入ってきた値が`null`になる可能性がある場合は、その呼び出しを早いうちに`Option(...)`で囲い、`Option`にすることが推奨されます。この呼び出しで、引数が`null`だった場合は`None`に、そうでなかった場合は`Some(...)`になります。

### Javaコードの追加
Scalaを書くことができる場合はScalaを使ってください。

### メインスレッドですべての処理を行うこと
エンティティやブロックが絡む処理で、メインスレッドで計算なども含めたすべての処理を行うことはサーバーのラグにつながるため、推奨されません。
推奨される処理の記述方法は次のとおりです。

1. 処理に必要なデータをメインスレッドで得る
2. 非メインスレッドに遷移し、処理を行う
3. 再度メインスレッドに遷移し、処理結果の反映を行う

### メニューを実装するためにInventoryClickEventを使うこと
`InventoryClickEvent`はメニュー以外のインベントリ (例: チェスト) を操作したときも拾ってしまうため、メニューを実装するためにホックすることは推奨されません。
代わりに、[Menu](https://github.com/GiganticMinecraft/SeichiAssist/blob/41e63c0493621ff8afa32bce902d34a62ae466d2/src/main/scala/com/github/unchama/menuinventory/Menu.scala)を使ってください。

## 推奨事項
### 「サブシステム」への分割、依存、及び準拠
SeichiAssistには複数の関心事があり、その大半が「サブシステム」と呼ばれるひとまとまりとして`com.github.unchama.seichiassist.subsystems`以下にまとめられています。
[最新の一覧](https://github.com/GiganticMinecraft/SeichiAssist/tree/develop/src/main/scala/com/github/unchama/seichiassist/subsystems)。

各サブシステムはそれぞれ他のサブシステムで使うことを想定したAPIを `trait` として持ち、各サブシステムはその抽象に対してのみ依存することができます。サブシステムの実装はAPIに具象的な実装を与える必要があります。

関心事同士の依存 (例: 整地スキルによってマナを減少させるためにマナを操作する) は 抽象の型を `implicit` パラメーターとして渡すことにより表現されます。

抽象同士の循環参照は推奨されません。

新しくサブシステムを作った場合、`SeichiAssist.scala` 内で初期化することを忘れないでください。初期化を忘れると正しく機能しません。
