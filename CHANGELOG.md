**参照:** [masterへのマージ履歴](https://github.com/GiganticMinecraft/SeichiAssist/pulls?utf8=✓&q=is%3Apr+sort%3Aupdated-desc+is%3Aclosed+base%3Amaster)
## \[Unreleased\]
- KotlinファイルをScalaにマイグレーション
## ~~\[1.2.4\] - 2019-11-30 \([#362](https://github.com/GiganticMinecraft/SeichiAssist/pull/362)\)~~
**\([#364](https://github.com/GiganticMinecraft/SeichiAssist/pull/364)で取り消し済み\)**
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
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
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
## \[1.1.4\] - 2019-09-19 \([#284](https://github.com/GiganticMinecraft/SeichiAssist/pull/284)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
## \[1.1.2\] - 2019-09-16 \([#255](https://github.com/GiganticMinecraft/SeichiAssist/pull/255)\)
### 修正 / Fixed
- メビウスの説明文を修正
- ガチャ品の引き出しをMineStackの引き出しをヒストリーに残さないようにする
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

### 機能追加 / Added
- 建築鯖への移動ボタンをサーバー一覧に加える
- 整地スキルの対応ブロックを追加

### 機能削除 / Removed
- levelコマンドを削除

## \[1.1.1\] - 2019-05-20 \([#143](https://github.com/GiganticMinecraft/SeichiAssist/pull/143)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***

## \[1.1.0\] - 2019-05-17 \([#134](https://github.com/GiganticMinecraft/SeichiAssist/pull/134)\)
- ***TODO: [執筆者募集中～](https://github.com/GiganticMinecraft/SeichiAssist/edit/develop/CHANGELOG.md)***
