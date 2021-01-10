**参照:** [masterへのマージ履歴](https://github.com/GiganticMinecraft/SeichiAssist/pulls?utf8=✓&q=is%3Apr+sort%3Aupdated-desc+is%3Aclosed+base%3Amaster)
## 1.6.0 - 2020-12-13 (#811)
## 1.5.3 - 2020-11-14 (#765)
## 1.5.2 - 2020-10-23 (#731)
## 1.5.1 - 2020-10-19 (#719)
## 1.5.0 - 2020-10-18 (#713)
## 1.4.8 - 2020-08-23 (#663)
## 1.4.7 - 2020-08-23 (#652)
## 1.4.6 - 2020-08-23 (#650)
## 1.4.5 - 2020-08-22 (#647)
## 1.4.4 - 2020-08-22 (#644)
## 1.4.3 - 2020-08-22 (#643)
## 1.4.2 - 2020-08-19 (#635)
## 1.4.1 - 2020-08-19 (#633)
### 追加 / Added
- マイグレーションシステムの実装 (#582)
- チェスト破壊のメッセージをアクションバーに移す (idea/8156)

### 修正 / Fixed
- xargsのオプションをMac互換にする
- 棒メニューBが表示されなくなる不具合を修正 (#592)
- プレイヤーヘッドを刈り取ったときにCoreProtectに記録されない問題を修正 (#600)
- 棒を持ちながら感圧版を踏めるようにする (#591)
- Mebiusのシステムを整理 (#606)
- プレイヤーのデータがロードされていなかったらキックする (#604)
- Spigotの依存解決に失敗する問題を修正 (#573)

### 改善 / Improved
- CIでインクリメンタルコンパイルをする (#565)
- `updateLevel`の単純化 (#574)
- Util.javaの整理 (#575)
- 使われていない配列の削除 (#577)
- `TypeConverter`の削除 (#579)
- `updatePlayerLevel`の単純化 (#581)
- `Config.java`の整理 (#585)
- `static`な`FileConfiguration`を持つ`Config`クラスを裁く (#586)
- `BuildBlock.java`の整理 (#587)
- 設置がキャンセルされていたら建築量にカウントしない (#595)
- 30分整地ランキングにスターレベルを含む (#597)
- ガチャ券の判定方法の改善 (#598)
- アイテムマイグレーションの適用バージョンをログに出す (#612)
- use `circleci` commands (#615)
- 所有者UUIDをMebiusに書き込む
- クールダウンの下限を消す
- ワールドデータ変換時のメモリ使用量を抑える (#628)
- 経験値瓶を一つにまとめる (#599)

## \[1.3.3\] - 2020-07-04 \([#563](https://github.com/GiganticMinecraft/SeichiAssist/pull/563)\)
### 修正 / Fixed
- 建築量を小数第一位まで表示するようにする (#560)
- `サンダーストーム` → `サンダー・ストーム` (#555)

## 1.3.2 - 2020-06-14 (#543)
### 修正 / Fixed
- ドキュメント内のtypoを修正 (#538)
- 表記ゆれの修正
- カラムの整理

## 1.3.1 - 2020-06-12 (#537)
### 修正 / Fixed
- アサルトスキル解法のメッセージのフォーマットを修正
### 改善 / Improved
- アクティブスキル解除ボタンの表示
- 不必要な正規表現を消去

## \[1.3.0\] - 2020-06-12 \([#534](https://github.com/GiganticMinecraft/SeichiAssist/pull/534)\)
### 追加 / Added
- マグマブロック+バケツ→溶岩バケツ (#515)

### 修正 / Fixed
- 整地ワールドの判定を修正
- `/x-transfer`で保護の上限数を超えて譲渡できてしまう問題を修正
- スポーンワールド → メインワールド
- 表記ゆれを修正
- まとめ引きした際のメッセージを修正
- メッセージに含まれるカラーコードが展開されていない問題を修正
- 建築量が指数表記になっている問題を修正 (#517)
- スキルを再実装する (#522)
- READMEの軽微な編集 (#524)
- 共有インベントリが格納できないことがある問題を修正 (#514)
- 小数リテラルを使用する (#521)
- プレミアムエフェクトポイントがdonatedataのみ参照して計算されるようにする (#528)
- TargetedEffectのリファクタリング (#529)
- 経験値の計算式を更新 (#530)
- `com.github.unchama.util.modify` → `scala.util.chaining.tap` (#532)

### 改善 / Improved
- 公共施設サーバーで整地量ランキングを表示しないようにする (#509)
- `MebiusListener.java`のドキュメントを改善 (#494)
- 依存プラグインを明示的に指定する (#510)
- 説明を改善する (#512)


## \[1.2.13\] - 2020-04-07 \([#487](https://github.com/GiganticMinecraft/SeichiAssist/pull/487)\)
### 修正 / Fixed
- 整地ランキングの計算ロジックが正しく計算されないことがある問題
- エフェクトが購入できない問題
- 誤ったクラスの命名
- GiganticBerserkで確率の桁が長い問題
- 経験値瓶のハンドリング漏れ
## \[1.2.12\] - 2020-02-23 \([#463](https://github.com/GiganticMinecraft/SeichiAssist/pull/463)\)
### 修正 / Fixed
- スキルで破壊したブロックがマインスタックに格納されない問題

## \[1.2.11\] - 2020-02-17 \([#446](https://github.com/GiganticMinecraft/SeichiAssist/pull/446)\)
### 修正 / Fixed
- ガチャ景品が複製できてしまう問題

### その他 / Misc
- Docker対応
## \[1.2.10\] - 2020-02-16 \([#440](https://github.com/GiganticMinecraft/SeichiAssist/pull/440)\)
### 修正 / Fixed
- アクティブスキルのポイント計算

## \[1.2.9\] - 2020-02-16 \([#438](https://github.com/GiganticMinecraft/SeichiAssist/pull/438)\)
### 追加 / Added
- 実績

### 改善 / Improved
- インベントリからあふれるガチャ景品をマインスタックに格納するように

### 修正 / Fixed
- マインスタックのアイテム増殖

### 内部的変更 / Refactor
- `ResourceScope`による資源管理
- `MenuInventoryData`の整理

## \[1.2.8\] - 2020-01-29 \([#422](https://github.com/GiganticMinecraft/SeichiAssist/pull/422)\)
### 追加 / Added
- メニューに `/map` コマンドのボタンを追加 \([8b7517f](https://github.com/GiganticMinecraft/SeichiAssist/commit/8b7517ffbf5b6cfbaaeb8ffd98125ae027cfce3b)\)

### 修正 / Fixed
- 石のボタンと鉄のトラップドアの個数が共有されていた動作を修正\([0f3a228](https://github.com/GiganticMinecraft/SeichiAssist/commit/0f3a2281caf877915df33a1d8c027c510516e936)\)

### 内部的変更 / Refactor
- MenuInventoryDataの整理 \([#403](https://github.com/GiganticMinecraft/SeichiAssist/pull/403)\)

## \[1.2.7\] - 2020-01-18 \([#408](https://github.com/GiganticMinecraft/SeichiAssist/pull/410)\)
### 修正 / Fixed
- 複数回破壊スキルのクールダウンの計算方法を修正

## \[1.2.6\] - 2020-01-17 \([#408](https://github.com/GiganticMinecraft/SeichiAssist/pull/408)\)
### 追加 / Added
- `/gacha set`

### 修正 / Fixed
- ビルドスクリプト

## \[1.2.5\] - 2020-01-16 \([#405](https://github.com/GiganticMinecraft/SeichiAssist/pull/405)\)
### 追加 / Added
- いくつかの実績

### 改善 / Improve
- ビルドプロセス
- スキルの非同期化
## ~~\[1.2.4\] - 2019-11-30 \([#362](https://github.com/GiganticMinecraft/SeichiAssist/pull/362)\)~~
**\([#364](https://github.com/GiganticMinecraft/SeichiAssist/pull/364)で取り消し済み\)**
## \[1.2.3\] - 2019-11-30 \([#354](https://github.com/GiganticMinecraft/SeichiAssist/pull/354)\)
### 修正 / Fixed
- 実績システムほぼ書き直し (#327)
- 内部のパッケージングを改善 (#338 等)
- 各種ブロック破壊スキルのロジックを大幅に最適化 (#347)
- #350

## \[1.2.2\] - 2019-11-06 \([#330](https://github.com/GiganticMinecraft/SeichiAssist/pull/330)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
## \[1.2.1\] - 2019-11-05 \([#328](https://github.com/GiganticMinecraft/SeichiAssist/pull/328)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
## \[1.2.0\] - 2019-11-04 \([#324](https://github.com/GiganticMinecraft/SeichiAssist/pull/324)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
## \[1.1.5\] - 2019-09-22 \([#295](https://github.com/GiganticMinecraft/SeichiAssist/pull/295)\)
### 修正 / Fixed
- スキルポイントのバグ修正

### 内部的変更 / Refactor
- Kotlin+JavaをScala+Javaに移行 \([#285](https://github.com/GiganticMinecraft/SeichiAssist/pull/285)\)

- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
## \[1.1.4\] - 2019-09-19 \([#284](https://github.com/GiganticMinecraft/SeichiAssist/pull/284)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
## \[1.1.2\] - 2019-09-16 \([#255](https://github.com/GiganticMinecraft/SeichiAssist/pull/255)\)
### 修正 / Fixed
- メビウスの説明文を修正
- ガチャ品の引き出しをMineStackの引き出しのヒストリーに残さないようにする
- SeichiAssistのログイン時のゲーム内ロードをゼロタイムにする
- MineStack内の`水色`を`青緑色`に置換
- 実績#3011の定義を修正

### 内部的変更 / refactor
- ベースとなる言語をKotlinへ
  - それに伴い一部のJavaファイルをKotlinに自動変換
  - 新規作成するファイルはKotlinへ
- インデントを4-8から2-4に

- 不必要なインポート文を削減
- コメントに書いてある嘘を削減
- 使用されていない変数を削減
- 使用されていないコメントアウトを削減
- プログラムに無関係なコメントを削減
- 一部のコメントをアノテーションで統一
- 使用されていないクラス/コードを削減
- 深く根付いたネストを引き上げる
- 不必要なインスタンス生成を削減
- 比較をより安全に行う (===)
- あいまいに書かれた型をワイルドカード/型引数で明示
- 明らかにいるべきでないファイルをお引越し
- 列挙を用いてわかりやすくする
- 定数をcompanionに抽出

- インベントリを名前で判断せず型で判断し安全にする
- インベントリ外をクリックしたときのぬるぽを殺す
- アイコンに更新を動的に掛けられるようになる
- PlayerDataから機能を抽出し分離
  - Menuを切り離す

- BuildAssistをSeichiAssistへ単純に統合

- Menuを作り直す
  - 棒メニュー (左クリック1,2ページ目, 建築メニュー)
  - 範囲設置スキルメニュー
  - リージョンメニュー
  - MineStackメニュー
  - MineStackのカテゴリメニュー
- 操作をEffectに押し込むことで扱いやすく
- ボタンの操作をEffectで扱うようにして短く直感的にする

- ミュータブルなCoordinateをXYZTupleにして安全にする

- アイテム生成用のBuilderを作成し冗長なItemMetaの編集を削減

- コマンドのパッケージ構造を整理
- コマンドを再実装
  - ContributeCommand
  - EffectCommand
  - EventCommand
  - StickCommand
  - MineHeadCommand
  - HalfBlockCommand
  - LastQuitCommand
  - GiganticFeverCommand
  - RmpCommand
  - SubHomeCommand
  - ShareInvCommand
  - SeichiCommand
  - MebiusCommand (DSL-izeのみ)

- MebiusTaskを遅延して行わせる

### 追加 / Added
- 建築鯖への移動ボタンをサーバー一覧に加える
- 整地スキルの対応ブロックを追加

### 削除 / Removed
- levelコマンドを削除

## \[1.1.1\] - 2019-05-20 \([#143](https://github.com/GiganticMinecraft/SeichiAssist/pull/143)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***

## \[1.1.0\] - 2019-05-17 \([#134](https://github.com/GiganticMinecraft/SeichiAssist/pull/134)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
