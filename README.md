# SeichiAssist

[![GitHub Actions](https://github.com/GiganticMinecraft/SeichiAssist/actions/workflows/build_and_deploy.yml/badge.svg)](https://github.com/GiganticMinecraft/SeichiAssist/actions/workflows/build_and_deploy.yml)

## 開発環境
- [Intellij IDEA](https://www.jetbrains.com/idea/) などの統合開発環境
- [AdoptOpenJDK 1.8](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot)
- [Scala 2.13](https://www.scala-lang.org/download/)
- [sbt 1.6](https://www.scala-sbt.org/1.x/docs/Setup.html)
- Spigot 1.13.2

## 前提プラグイン
- [CoreProtect-2.15.0](https://www.spigotmc.org/resources/coreprotect.8631/download?version=231781)
- [item-nbt-api-plugin-2.11.2](https://www.spigotmc.org/resources/item-entity-tile-nbt-api.7939/download?version=241690)
- [Multiverse-Core-2.5.0](https://dev.bukkit.org/projects/multiverse-core/files/2428161/download)
- [Multiverse-Portals-2.5.0](https://dev.bukkit.org/projects/multiverse-portals/files/2428333/download)
- [ParticleAPI_v2.1.1](https://dl.inventivetalent.org/download/?file=plugin/ParticleAPI_v2.1.1)
- [WorldBorder1.8.7](https://dev.bukkit.org/projects/worldborder/files/2415838/download)
- [worldedit-bukkit-7.0.0](https://dev.bukkit.org/projects/worldedit/files/2597538/download)
- [worldguard-bukkit-7.0.0](https://dev.bukkit.org/projects/worldguard/files/2610618/download)

## 前提プラグイン(整地鯖内製)
- RegenWorld [リポジトリ](https://github.com/GiganticMinecraft/RegenWorld) | [jar](https://redmine.seichi.click/attachments/download/890/RegenWorld-1.0.jar)

## コントリビューション
どのように貢献するかは[CONTRIBUTING.md](./CONTRIBUTING.md)へ移動しました。

慣れている開発者向け：
1. 自分のGitHubアカウントへ[fork](https://github.com/GiganticMinecraft/SeichiAssist/fork)
2. `git clone https://github.com/${YOUR_GITHUB_ID}/SeichiAssist`
3. `cd SeichiAssist`
4. `git checkout -b ${YOUR_BRANCH_NAME} develop`
5. `git commit -am "feat: 〇〇を実装"` (コンベンショナルコミットを推奨。本文は英語または日本語を推奨)
6. `git push`
7. GiganticMinecraftのレポジトリに向かってPull Requestを作成
8. CIとコードレビューを待つ
9. マージされる
10. CDが実行される
11. (本番サーバーへのデプロイはGiganticMinecraftのメンバーがPRによって行う)

## 利用条件
- [GPLv3ライセンス](https://github.com/GiganticMinecraft/SeichiAssist/blob/develop/LICENSE) での公開です。ソースコードの使用規約等はGPLv3ライセンスに従います。
- 当リポジトリのコードの著作権はunchamaが所有しています。
- 独自機能の追加やバグの修正等、ギガンティック☆整地鯖(以下、当サーバー)の発展への寄与を目的としたコードの修正・改変を歓迎しています。その場合、当サーバーのDiscordコミュニティに参加して、当コードに関する詳細なサポートを受けることが出来ます。

## ライセンス
このプラグインは、[Apache 2.0ライセンス](https://www.apache.org/licenses/LICENSE-2.0)で配布されている以下の製作物が含まれています。

- [AJD4JP 日本用カレンダー処理 Javaクラスライブラリ](https://osdn.net/projects/ajd4jp/)
