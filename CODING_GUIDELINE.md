# コーディングガイドライン

## 非推奨事項

### Scalaにおけるnullの使用
Scalaにおいては、`null`を使用する代わりに`Option`を使用してください。
Java (特に、Bukkit/Spigot API) から入ってきた値が`null`になる可能性がある場合は、その呼び出しを`Option(...)`として囲い、`Option`にすることが推奨されます。この呼び出しで、引数が`null`であった場合は`None`に、そうでなかった場合は`Some(...)`になります。

### Javaコードの追加
Scalaを書くことができる場合はScalaを使ってください。

### メインスレッドですべての処理を行うこと
エンティティやブロックが絡む処理で、メインスレッドで計算なども含めたすべての処理を行うことはサーバーのラグにつながるため、推奨されません。
推奨される処理の記述方法は次のとおりです。

1. 処理に必要なデータをメインスレッドで得る
2. 非メインスレッドに遷移し、処理を行う
3. 再度メインスレッドに遷移し、処理結果の反映を行う

### メニューを実装するためにInventoryClickEventを使うこと
`InventoryClickEvent`はメニュー以外のインベントリ (例: チェスト) を操作したときも発行されるのでメニューを実装するためにホックすることは推奨されません。
代わりに、[Menu](https://github.com/GiganticMinecraft/SeichiAssist/blob/41e63c0493621ff8afa32bce902d34a62ae466d2/src/main/scala/com/github/unchama/menuinventory/Menu.scala)を使ってください。

## 推奨事項
### 「サブシステム」への分割、依存、及び準拠
SeichiAssistには複数の関心事があり、その大半が「サブシステム」と呼ばれるひとまとまりとして`com.github.unchama.seichiassist.subsystems`以下にまとめられています。
[最新の一覧](https://github.com/GiganticMinecraft/SeichiAssist/tree/develop/src/main/scala/com/github/unchama/seichiassist/subsystems)。
以下に、2023年2月21日現在の[一覧](https://github.com/GiganticMinecraft/SeichiAssist/tree/5f29ce1a095d0e8dd4c301665e088af4c6d5ec3a/src/main/scala/com/github/unchama/seichiassist/subsystems)を示します。

(TODO)

