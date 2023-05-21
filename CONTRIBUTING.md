# 開発体制

## 開発を始めるために必要なもの
- [Intellij IDEA](https://www.jetbrains.com/idea/) などの統合開発環境
- [AdoptOpenJDK 1.8](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot)
- [sbt 1.6](https://www.scala-sbt.org/1.x/docs/Setup.html)
- [Scala 2.13](https://www.scala-lang.org/download/)
- Spigot 1.12.2
- Docker
- GitHubのアカウント
- Git

### 準備
#### Java Development Kit
最初に、Java Development Kit (JDK) 8をインストールする必要があります。
[AdoptOpenJDK 1.8](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot) のインストールを推奨します。

#### 統合開発環境
次に、[Intellij IDEA](https://www.jetbrains.com/idea/)などの統合開発環境を導入します。

有料版 **Ultimate Edition** と機能が制限された無料版 **Community Edition** が2つありますが、SeichiAssist を開発する上では無料版で十分です。

ダウンロードは [こちら](https://www.jetbrains.com/idea/download/) から

> **Note**
>
> 学生の場合は、[学生ライセンス](https://www.jetbrains.com/community/education/#students)を申請することで Ultimate Edition を無料で利用できます。

##### Intellij
* インストールする時、Gitプラグインを有効にします。
* Scala用の[プラグイン](https://plugins.jetbrains.com/plugin/1347-scala)を導入してください。

#### sbt
それが終わった後、[sbtの公式ページ](https://www.scala-sbt.org/1.x/docs/Setup.html) に従ってsbtのインストールをします。
sbtがコマンドラインで使える状態で`sbt assembly`を実行すると、`target/build`フォルダにjarが出力されます。

#### Scala
Scalaはsbtによって自動的にダウンロード及びインストールされます。

#### Docker
SpigotサーバーのDockerコンテナを立ち上げるために、Dockerのインストールが必要です。

詳しくは [こちら](https://docs.docker.com/get-started/overview/) をご確認ください。

#### Spigot
SpigotサーバーはDockerコンテナによって提供されます。

#### GitHubのアカウント
GitHubにアカウントを[登録](https://github.com/join)します。
詳細な手順は有志の方の[記事](https://qiita.com/ayatokura/items/9eabb7ae20752e6dc79d)をご覧ください。

#### 上級者向け：ローカルにJavaとかsbtを入れたくない場合

> **Warning**
>
> 自分が何をしているのかわかっていないのであれば、この手順を飛ばしてください。

* VSCode + WSLで開発している
* ビルドして立ち上げたいだけ
* ランタイムの導入のコストが高いと考えている

場合は、以下のシェルスクリプトを使うと便利です。

```bash
$ rm -rf target/build # 再ビルドしたいなら既存のターゲットは削除
$ docker run --rm -it -v `pwd`:/app ghcr.io/giganticminecraft/seichiassist-builder:1a64049 sh -c "cd /app && sbt assembly"
$ sudo chown -R `whoami` target/build # docker上でsbtを実行するとrootになってしまうため権限を変える
$ cp -n docker/spigot/eula.txt docker/spigot/serverfiles/eula.txt || true
$ docker compose up --build -d
```

#### Git
最後に、Gitのインストールも必要です。公式の[ガイド](https://git-scm.com/book/ja/v2/%E4%BD%BF%E3%81%84%E5%A7%8B%E3%82%81%E3%82%8B-Git%E3%81%AE%E3%82%A4%E3%83%B3%E3%82%B9%E3%83%88%E3%83%BC%E3%83%AB)をご覧ください。

### SeichiAssistを自分のGitHubアカウントにコピーする

> **Warning**
> 
> この手順は [GiganticMinecraft][gm-gh-organization] のメンバーである場合は行う必要はありません。
> よくわからない場合は、この注意書きを無視して先へ進んでください。
>
> また、この手順を行うのは、一回だけです。二回目以降は、この手順を行う必要はありません。

変更を加える前に、SeichiAssistを自分の手元に「コピー」する必要があります。
最初に、[GiganticMinecraftのページ][gm-gh-repo]を開いて、画面右上にある「fork」と書かれた枝分かれしているアイコンがあるボタンを押します。

すると「Create a new fork」と書かれた画面に移動します。

<!-- GitHub の issue とかに貼れば写真が GitHub の CDN 等に置かれるのでそのリンクを使う -->
![img.png](https://user-images.githubusercontent.com/127779256/226674317-3ad07000-a272-4f2e-905a-15e07b394bae.png)

いくつか入力欄がありますが、何も触らずにCreate forkを押します。

また画面が切り替わります。画面左上に書かれた文字が「GiganticMinecraft/SeichiAssist」ではなく、「(あなたのID)/SeichiAssist」になっていることを確認できたら次へ進みます。

### SeichiAssistを自分の手元にコピーする
SeichiAssistは、Gitというバージョンを管理するシステムを使っています。そのため、どうにかして自分のパソコンにSeichiAssistを自分の手元にコピーしてくる必要があります。

#### IntelliJの場合
[JetBrainsのヘルプ](https://www.jetbrains.com/help/idea/cloning-repository.html) (英語) をご覧ください。


#### protocol以下のファイルを入手

> **Warning**
> 
> ・この手順はコマンドラインから直接クローンした場合の手順になります。
>
> ・この手順を行うのは、一回だけです。二回目以降は、この手順を行う必要はありません。

protocol以下のファイルは`git clone`では入手できません。以下のどちらかのコマンドを実行してください:

* `git clone --recursive`
* `git submodule update --init --recursive`

### issueを見る
一旦[GiganticMinecraftのページ][gm-gh-repo]へ戻って、画面上部の[Issues](https://github.com/GiganticMinecraft/SeichiAssist/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc)と書かれたタブをクリックしてみましょう。

すると画面が切り替わり、たくさんのやりたいこと (主に[Redmineで承認されたもの](https://github.com/GiganticMinecraft/SeichiAssist/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc+label%3A%22Status%2FIdea%3A+Accepted%E2%9C%85%22+label%3A%22Tracked%3A+Redmine%22)) や、[バグ報告](https://github.com/GiganticMinecraft/SeichiAssist/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc+label%3Abug)が出てきます。

はじめはこの中からできそうなものを探すと良いと思います。初めての場合は、"good first issue"というラベルがつけられた中から探すのがおすすめです。[
ここ](https://github.com/GiganticMinecraft/SeichiAssist/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22)から飛べます。

### IntelliJ特有の手順
IntelliJ IDEAの設定でフォーマットに `scalafmt` を使う
- `Editor` > `Code Style` > `Scala` で
    - `Formatter` を `Scalafmt` に変更
    - `Reformat on file save` にチェックを付ける


### 変更を加える
1. developというブランチ (セーブデータのスロットのようなもの) を元に、新しいブランチを作ります。
2. 必要な変更を加えてファイルを保存します
3. コミット (Gitに対するセーブ) します。コミットする時、メッセージを書くように求められます。[コンベンショナルコミット](https://www.conventionalcommits.org/ja/v1.0.0/)という、メッセージの書き方を推奨しています。わからなければ、今は飛ばしても大丈夫です。
    * メッセージの1行目には変更した点の大まかな内容を書くように心がけてみてください。
        * 1行目は45文字以内で書くことを推奨します。
        * コンベンショナルコミット：1行目は「変更の区分」、半角コロン、半角スペースで始める方法を推奨しています。
            * 新しい機能を実装したときは`feat: [大まかな内容]`とします。
            * バグを修正したときは`fix: [大まかな内容]`とします。
            * ドキュメントを触ったときは`docs: [大まかな内容]`とします。
            * GitHub Actionsを触ったときは`ci: [大まかな内容]`とします。
            * リファクタリングしたときは`refactor: [大まかな内容]`とします。
            * テストを書いたときは`test: [大まかな内容]`とします。
            * scalafmtやscalafixを反映したときは`style: [大まかな内容]`とします。
            * パフォーマンスを改善したときは`perf: [大まかな内容]`とします。
            * その他のコード品質に関わらない変更をしたときは、`chore: [大まかな内容]`とします。
        * 発展的な内容：コンベンショナルコミットにおいて複数の種別に該当する場合、引き返して複数のコミットに分割することが推奨されています。
    * メッセージの2行目以降は、より詳しい内容を書けます。必要に応じて記入してください。
    * メッセージは日本語か英語で書くことを推奨します。
4. 良くなるまで繰り返します

#### 手元でデバッグ
SeichiAssistは手元でデバッグできる環境を整えています。環境を立ち上げるためには、Dockerが必要です。

##### Dockerを立ち上げる

Linux環境では、`./prepare-docker.sh`、Windowsでは`prepare-docker.bat`を実行することで
デバッグ用のBungeecordとSpigotの環境を構築することができます。

サーバーやDB等を停止する場合、 `docker compose down` を実行してください。

なお、SeichiAssistがJDK 8以外でコンパイルされた場合は、実行時にエラーとなります。必ずJDKのバージョンを揃えるようにしてください。

##### データベースの初期化

> **Warning**
>
> この手順は初めてDockerを立ち上げた場合のみに必要です。

初回起動後、データベースが作成されますが、ガチャ景品のデータがありません。そのため、次のSQLのダンプをインポートします。
- [`gachadata.sql`](https://redmine.seichi.click/attachments/download/995/gachadata.sql)

SQLのダンプをインポートする手順は以下の通りです。
1. 一旦サーバーを起動させる
2. phpMyAdminを開く
3. トップ画面の上部メニューから「データベース」を開く
4. `seichiassist`と`flyway_managed_schema`にチェックを入れて、「削除」、「OK」
5. 「データベースを作成する」の下にあるテキストボックスに`seichiassist`と入力し、「作成」
6. `seichiassist`のデータベースを開き、上部メニューから「インポート」
7. 「File to import」の「ファイルを選択」から、ダウンロードした`gachadata.sql`を選択
8. 画面下部の「実行」
9. サーバーを再起動させる

##### デバッグ用環境への接続

DockerマシンのIPアドレス(Linux等なら`localhost`)を`DOCKER_IP`とします。

`docker`により各サービスが起動したら、マルチプレイヤーのメニューで`DOCKER_IP`へと接続できます。
また、`DOCKER_IP:8080`へとWebブラウザでアクセスすることで、phpMyAdminを介してデータベースを操作できます。

##### コンソールにアクセスする

自分のアカウントに管理者権限(OP)を与える時など、Spigotのコンソールにアクセスする場合は、
`spigota` または `spigotb` のコンテナにアタッチする必要があります。

アタッチするには `docker attach [CONTAINER_NAME]` を実行します。
コンテナを指定する際に使用するIDはコマンドプロンプトで `docker ps` を実行すると `seichiassist_spigotb_1` のような形式で表示されます。

コンソールからは <kbd>Ctrl</kbd>キーと<kbd>C</kbd>キーを同時押しすることで出ることができます。サーバーは停止されません。

##### 整地ワールドの作成

初めてデバッグ環境のSpigotに接続した際にスポーンするワールドは整地ワールドではないため、そのままブロックを破壊しても整地レベルは上昇しません。

整地ワールドを作成する場合、OP権限を付与したプレイヤーかアタッチしたコンソールからコマンドで `mvcreate world_SW normal` を実行します。

整地ワールドへ行くには、コマンドで `mvtp world_SW` を実行します。

### 反映する
さあ、いよいよ反映の時間がやってきました。
まずは手元で`sbt scalafixAll`をします。次に`sbt scalafmtAll`をします。
その後、自分の手元から自分のGitHubアカウントへ内容を反映します。

#### Pull Requestの作成
次に、自分のGitHubアカウントにあるSeichiAssistを開いて、変更を依頼する手続き (Pull Request) の準備画面へ移動します。
画面が切り替わります。右上のCreate pull requestと書かれたボタンを押してください。
そうすると、高さが狭い場所と広い場所が表示されます。
狭い場所には、変更の概要を50文字以内で簡単に書いてください。 (TODO: コンベンショナルコミット)
広い場所には、書くべきだと思ったことを書いてください。詳しすぎるということはありません。
一通り書き終わったら、長い場所の右下にある「Create pull request」と書かれたボタンを押してください。

### コードレビューを待つ
Pull Requestが作成されたら、GitHubのサーバーでコンパイルやファイルのチェックが自動的に始まります。
また、[確認できる人](https://github.com/orgs/GiganticMinecraft/people) (主に@kory33、@rito528、@KisaragiEffective、@Lucky3028)が方向性が正しいかどうかレビューを行います。それまでは優雅に紅茶を飲んだり踊ったりして待つことができます。

SeichiAssistでPull Requestが受け入れられる (マージされる) には、下の条件をすべてクリアする必要があります。
* コンパイルやファイルのチェックがすべてエラーなく終わる
  * コンパイル
  * scalafix - エラーになった場合は`sbt scalafixAll`をしてください
  * scalafmt - エラーになった場合は`sbt scalafmtAll`をしてください
* 誰かから変更を承認してもらう

### 承認とサーバーへの反映
条件がすべてクリアされると、Pull Requestがマージ (変更依頼手続きが完了) されます。

#### デバッグサーバーへの反映
マージされた後、自動的にデバッグサーバーへの反映手続きが始まります。[Discord](https://discord.com/channels/237758724121427969/959323550878138368)で進捗を確認できます。
通常、デバッグサーバーへの反映は数分程度で終わります。
デバッグ環境へは、以下の手順で接続できます。
1. Minecraft Java Editionで`play-debug.seichi.click`に接続
2. <kbd>T</kbd>キーでチャットを開く
3. `/server deb112`と入力して`Enter`を押す

#### 自動リリースの範囲
自動リリースはSeichiAssistのプログラムの部分のみ行われます。より正確に言うのであれば、jar以外の自動リリースは未対応です(`config.yml`など)。運営チームへ更新を依頼する必要があります。
この問題点があるため、各サーバーや環境で共通で構わないパラメータは`config.yml`を読まず、コードへの直接実装を推奨します。

----

#### 運営メンバー向け: 本番サーバーへの反映

本番サーバーへの反映は通常GitHub ActionsでPull Requestを作成し、それをマージすることで行います。
1. [GitHub Actionsのタブ](https://github.com/GiganticMinecraft/SeichiAssist/actions/workflows/create_new_release.yml)へ移動します。
2. 画面右の「Run workflow」を押します。
3. しばらくすると`master <- develop`のPull Requestが作成されます。
4. 本番サーバーへ反映したいタイミングでマージします。
5. マージした後、朝4時の再起動で変更が反映されます。

緊急を要する場合は、`hotfix-*`ブランチを作成し、そのブランチから`master`ブランチへ向けてPull Requestを作成してください。
`develop`ブランチへの直プッシュは、CIによるチェックが事後となってしまうため避けてください。

[gm-gh-organization]: https://github.com/GiganticMinecraft
[gm-gh-repo]: https://github.com/GiganticMinecraft/SeichiAssist
