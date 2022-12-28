# SeichiAssist

[![GitHub Actions](https://github.com/GiganticMinecraft/SeichiAssist/actions/workflows/build_and_deploy.yml/badge.svg)](https://github.com/GiganticMinecraft/SeichiAssist/actions/workflows/build_and_deploy.yml)

## 開発環境
- [Intellij IDEA](https://www.jetbrains.com/idea/) などの統合開発環境
- [AdoptOpenJDK 1.8](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot)
- [Scala 2.13](https://www.scala-lang.org/download/)
- [sbt 1.6](https://www.scala-sbt.org/1.x/docs/Setup.html)
- Spigot 1.12.2

## 前提プラグイン
- [CoreProtect-2.14.4](https://www.spigotmc.org/resources/coreprotect.8631/download?version=231781)
- [item-nbt-api-plugin-1.8.2-SNAPSHOT](https://www.spigotmc.org/resources/item-entity-tile-nbt-api.7939/download?version=241690)
- [Multiverse-Core-2.5.0](https://dev.bukkit.org/projects/multiverse-core/files/2428161/download)
- [Multiverse-Portals-2.5.0](https://dev.bukkit.org/projects/multiverse-portals/files/2428333/download)
- [ParticleAPI_v2.1.1](https://dl.inventivetalent.org/download/?file=plugin/ParticleAPI_v2.1.1)
- [WorldBorder1.8.7](https://dev.bukkit.org/projects/worldborder/files/2415838/download)
- [worldedit-bukkit-6.1.9](https://dev.bukkit.org/projects/worldedit/files/2597538/download)
- [worldguard-bukkit-6.2.2](https://dev.bukkit.org/projects/worldguard/files/2610618/download)

## 前提プラグイン(整地鯖内製)
- RegenWorld [リポジトリ](https://github.com/GiganticMinecraft/RegenWorld) | [jar](https://redmine.seichi.click/attachments/download/890/RegenWorld-1.0.jar)

## ビルド

最初に、Java Development Kit (JDK) 8をインストールする必要があります。
[AdoptOpenJDK 1.8](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot) のインストールを推奨します。

[sbtの公式ページ](https://www.scala-sbt.org/1.x/docs/Setup.html) に従ってsbtのインストールをします。
sbtがコマンドラインで使える状態で`sbt assembly`を実行すると、`target/build`フォルダにjarが出力されます。

### IntelliJ IDEAの画面からビルドする

IntelliJ IDEAを開発に使用している場合、プロジェクトをsbtプロジェクトとして読み込み、
sbtタブからSeichiAssist -> SeichiAssist -> sbt tasks -> assemblyを実行すれば`target/build`フォルダにjarが出力されます。

## デバッグ用docker環境

`docker`、`docker-compose`及び`sbt`が実行可能であるとします。
Linux環境では、`./prepare-docker.sh`、Windowsでは`prepare-docker.bat`を実行することで
デバッグ用のBungeecord + Spigot環境を構築することができます。

初回起動時にはSpigotのビルドに時間がかかります。
さらに、[Minecraft EULA](https://account.mojang.com/documents/minecraft_eula) に同意する必要があるため実行が中断されます。
EULAに同意しデバッグを続行する場合、`./docker/spigot/serverfiles/eula.txt`を参照し、
`eula=false` を `eula=true` に書き換えてください。

サーバーやDB等を停止する場合、 `docker-compose down` を実行してください。

なお、SeichiAssistがJava 8以外でコンパイルされた場合は、実行時にエラーとなります。必ず揃えるようにしてください。

### デバッグ用環境への接続

DockerマシンのIPアドレス(Linux等なら`localhost`)を`DOCKER_IP`とします。

`docker`により各サービスが起動したら、`DOCKER_IP`へとMinecraftを接続することができます。
また、`DOCKER_IP:8080`へとWebブラウザでアクセスすることで、phpMyAdminを介してデータベースを操作することができます。

`op`やコマンド実行等などでSpigotのコンソールにアクセスする必要がある場合、
`spigota`または`spigotb`へのコンテナ名とともに `docker attach [CONTAINER_NAME]` を実行してください。
コンテナ名は `docker ps` を実行すると `seichiassist_spigotb_1` のような形式で表示されます。
コンソールからは `Ctrl+C` で抜けることができます(サーバーは停止されません)。

## DBの準備
初回起動後、DBが作成されますが、ガチャ景品およびMineStackに格納可能なガチャ景品のデータがありません。その為、以下SQLdumpをインポートしてください。
- [gachadata.sql](https://redmine.seichi.click/attachments/download/992/gachadata.sql) -> import to "gachadata" table.
- [msgachadata.sql](https://redmine.seichi.click/attachments/download/993/msgachadata.sql) -> import to "msgachadata" table.

### どうしてもローカルにJavaとかsbtを入れたくない人のための救済策

VSCode + WSLで開発している場合や、純粋にビルドして立ち上げたいだけの場合はランタイムの導入のコストが高いので、以下の方法を使うと便利です。

```bash
$ rm -rf target/build # 再ビルドしたいなら既存のターゲットは削除
$ docker run --rm -it -v `pwd`:/app ghcr.io/giganticminecraft/seichiassist-builder:1a64049 sh -c "cd /app && sbt assembly"
$ sudo chown -R `whoami` target/build # docker上でsbtを実行するとrootになってしまうため権限を変える
$ cp -n docker/spigot/eula.txt docker/spigot/serverfiles/eula.txt || true
$ docker-compose up --build -d
```

## protocolディレクトリ以下のクローン
protocol以下のファイルは`git clone`では入手することができません。以下のどちらかのコマンドを実行してください:
* `git clone --recursive`
* `git submodule update --init --recursive`

## ドキュメンテーション
publicなメソッドについては、ドキュメンテーションを記載するよう心がけてください。
その他は各自が必要だと判断した場合のみ記載してください。

## Commit Style
1コミットあたりの情報は最小限としてください。
コミットメッセージは[コンベンショナルコミット](https://www.conventionalcommits.org/ja/v1.0.0/)を採用することを推奨しています。

## Branch Model
[Git-flow](https://qiita.com/KosukeSone/items/514dd24828b485c69a05) を簡略化したものを使用します。
新規に機能を開発する際は develop ブランチから <任意の文字列> ブランチを作り、そこで作業してください。
開発が終了したらdevelopブランチにマージします。
masterブランチは本番環境に反映されます。
本番環境を更新するタイミングでdevelopブランチをmasterブランチにマージします。

## フォーマットおよびlintに関して
フォーマットには[scalafmt](https://scalameta.org/scalafmt)、lintには[scalafix](https://scalacenter.github.io/scalafix/)を利用しています。

コード品質を最低限保つため、PRが受け入れられるにはscalafmtとscalafixの両方のチェックが通る必要があります。そのため、
 - IntelliJ IDEAの設定でフォーマットに `scalafix` を使う
   - `Editor` > `Code Style` > `Scala` で
     - `Formatter` を `Scalafmt` に変更
     - `Reformat on file save` にチェックを付ける
 - PRを送った後は `sbt` コンソールで `scalafixAll` と `scalafmtAll` を実行する

ようにお願いします。

## AutoRelease
- developブランチが更新されると、そのコードを基に実行用jarがビルドされ、デバッグ環境に配布されます。デバッグ環境はjarの配布を検知すると自動で再起動し、最新のjarを使用して稼働します。
  - デバッグ環境へは、Minecraft Java Editionで`play-debug.seichi.click`に接続し、`T`キーでチャットを開き、`/server deb112`と入力して`Enter`を押すとアクセスできます。
- masterブランチが更新されると、そのコードを基に実行用jarがビルドされ、本番環境に配布されます。本番環境は翌再起動時に自動で最新のjarを取り込んで稼働します。
  - masterブランチの更新は必ず `develop` または `hotfix-*` からのPull Requestによって行ってください。
    また、 `develop` からのリリース用 Pull Request は [`create_new_release`](https://github.com/GiganticMinecraft/SeichiAssist/actions/workflows/create_new_release.yml) ワークフローを実行することで作成してください(`build.sbt` の自動更新などが行われます)。
- jar以外の自動リリースは未対応です(config.ymlなど)。運営チームへ更新を依頼する必要があります。
  - 各サーバーや環境で共通で構わないパラメータはconfig.ymlを読まず、コードへの直接実装を推奨します。

## 利用条件
- [GPLv3ライセンス](https://github.com/GiganticMinecraft/SeichiAssist/blob/develop/LICENSE) での公開です。ソースコードの使用規約等はGPLv3ライセンスに従います。
- 当リポジトリのコードの著作権はunchamaが所有しています。
- 独自機能の追加やバグの修正等、ギガンティック☆整地鯖(以下、当サーバー)の発展への寄与を目的としたコードの修正・改変を歓迎しています。その場合、当サーバーのDiscordコミュニティに参加して、当コードに関する詳細なサポートを受けることが出来ます。

## ライセンス
このプラグインは、[Apache 2.0ライセンス](https://www.apache.org/licenses/LICENSE-2.0)で配布されている以下の製作物が含まれています。

- [AJD4JP 日本用カレンダー処理 Javaクラスライブラリ](https://osdn.net/projects/ajd4jp/)
