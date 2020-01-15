# SeichiAssist

[![CircleCI](https://circleci.com/gh/GiganticMinecraft/SeichiAssist/tree/master.svg?style=svg)](https://circleci.com/gh/GiganticMinecraft/SeichiAssist/tree/master)

## 開発環境
- [Intellij IDEA 2019.2](https://www.jetbrains.com/idea/) などの統合開発環境
- [JDK 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [mysql-connecter-java-5.1.35](https://downloads.mysql.com/archives/c-j/)
- [Scala 2.13](https://www.scala-lang.org/download/)
- [sbt 1.3.6](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Windows.html)
- Spigot 1.12

## 前提プラグイン
- [CoreProtect-2.14.4](https://www.spigotmc.org/resources/coreprotect.8631/download?version=231781)
- [item-nbt-api-plugin-1.8.2-SNAPSHOT](https://www.spigotmc.org/resources/item-entity-tile-nbt-api.7939/download?version=241690)
- [Multiverse-Core-2.5.0](https://dev.bukkit.org/projects/multiverse-core/files/2428161/download)
- [Multiverse-Portals-2.5.0](https://dev.bukkit.org/projects/multiverse-portals/files/2428333/download)
- [ParticleAPI_v2.1.1](http://dl.inventivetalent.org/download/?file=plugin/ParticleAPI_v2.1.1)
- [WorldBorder1.8.7](https://dev.bukkit.org/projects/worldborder/files/2415838/download)
- [worldedit-bukkit-6.1.9](https://dev.bukkit.org/projects/worldedit/files/2597538/download)
- [worldguard-bukkit-6.2.2](https://dev.bukkit.org/projects/worldguard/files/2610618/download)

## 前提プラグイン(整地鯖内製)
- RegenWorld_1.0 [jar](https://red.minecraftserver.jp/attachments/download/890/RegenWorld-1.0.jar)
- SeasonalEvents [リポジトリ](https://github.com/GiganticMinecraft/SeasonalEvents) | [jar](https://red.minecraftserver.jp/attachments/download/893/SeasonalEvents.jar)

## ビルド
まずは[sbtの公式ページ](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Windows.html)よりsbtのインストールをします。
sbtがコマンドラインで使える状態で`sbt assembly`を実行すると、`target/build`フォルダにjarが出力されます。

IntelliJ IDEAを開発に使用している場合、プロジェクトをsbtプロジェクトとして読み込み、
sbtタブからSeichiAssist -> SeichiAssist -> sbt tasks -> assemblyを実行すれば`build/lib`フォルダにjarが出力されます。

## DBの準備
初回起動後、DBが作成されますが、ガチャ景品およびMineStackに格納可能なガチャ景品のデータがありません。その為、以下SQLdumpをインポートしてください。
- [gachadata.sql](https://red.minecraftserver.jp/attachments/download/895/gachadata.sql) -> import to "gachadata" table.
- [msgachadata.sql](https://red.minecraftserver.jp/attachments/download/894/msgachadata.sql) -> import to "msgachadata" table.

## JavaDocs
publicなメソッドについては、JavaDocsを記載するよう心がけてください。
その他は各自が必要だと判断した場合のみ記載してください。

## Commit Style
1コミットあたりの情報は最小限としてください。
コミットメッセージは変更の方向性を表す英語の動詞(add, remove, clean等)から始めることを推奨しています。

## Branch Model
[Git-flow](https://qiita.com/KosukeSone/items/514dd24828b485c69a05)を簡略化したものを使用します。
新規に機能を開発する際は develop ブランチから <任意の文字列> ブランチを作り、そこで作業してください。
開発が終了したらdevelopブランチにマージします。
masterブランチは本番環境に反映されます。
本番環境を更新するタイミングでdevelopブランチをmasterブランチにマージします。

## 利用条件
- [GPLv3ライセンス](https://github.com/GiganticMinecraft/SeichiAssist/blob/develop/LICENSE)での公開です。ソースコードの使用規約等はGPLv3ライセンスに従います。
- 当リポジトリのコードの著作権はunchamaが所有しています。
- 独自機能の追加やバグの修正等、ギガンティック☆整地鯖(以下、当サーバー)の発展への寄与を目的としたコードの修正・改変を歓迎しています。その場合、当サーバーのDiscordコミュニティに参加して、当コードに関する詳細なサポートを受けることが出来ます。
