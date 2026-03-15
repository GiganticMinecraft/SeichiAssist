# 開発への貢献ガイドライン

このドキュメントは、SeichiAssist プロジェクトへの開発貢献に関心のある方向けのガイドラインです。スムーズな開発参加のためにご一読ください。

## 1. 開発環境の準備

開発を始めるにあたり、以下のツールと環境を準備してください。

### 1.1. 必要なツール一覧

| ツールカテゴリ        | ツール名・バージョン                                  | 入手先 / 備考                                                                                                |
| :-------------------- | :---------------------------------------------------- | :----------------------------------------------------------------------------------------------------------- |
| **統合開発環境 (IDE)** | IntelliJ IDEA                                         | [公式サイト](https://www.jetbrains.com/idea/) を推奨                                                            |
| **Java Development Kit** | AdoptOpenJDK 17 (Temurin)                             | [Adoptium](https://adoptium.net/temurin/releases/?version=17)                                                |
| **ビルドツール** | sbt 1.9                                               | [公式ドキュメント](https://www.scala-sbt.org/1.x/docs/Setup.html)                                                  |
| **プログラミング言語** | Scala 2.13                                            | sbt により自動でインストールされます                                                                             |
| **Minecraft サーバー** | Paper 1.18.2                                         | Docker コンテナ経由で提供されます                                                                                |
| **コンテナ** | Docker                                                | [公式サイト](https://www.docker.com/) (Docker Desktop など)                                                       |
| **バージョン管理** | Git                                                   | [公式サイト](https://git-scm.com/)                                                                               |
| **アカウント** | GitHub アカウント                                     | [GitHub](https://github.com/join)                                                                              |

---

### 1.2. 各ツールのセットアップ詳細

#### 1.2.1. Java Development Kit (JDK)
JDK 17 をインストールします。上記リストの [AdoptOpenJDK 17 (Temurin)](https://adoptium.net/temurin/releases/?version=17) を推奨します。

#### 1.2.2. 統合開発環境 (IDE) - IntelliJ IDEA
[IntelliJ IDEA](https://www.jetbrains.com/idea/) の導入を推奨します。

> [!NOTE]
>
> 学生の方は、[学生ライセンス](https://www.jetbrains.com/community/education/#students) を申請することで Ultimate Edition を無料で利用できます。

* **エディション**: 有料版 (Ultimate) と無料版 (Community) がありますが、SeichiAssist の開発は無料版で十分です。
    * [ダウンロードはこちら](https://www.jetbrains.com/idea/download/)

* **初期設定**:
    1.  インストール時に **Git プラグインを有効**にしてください。
    2.  Scala 用の[プラグイン](https://plugins.jetbrains.com/plugin/1347-scala)を導入してください。

#### 1.2.3. sbt (ビルドツール)
[sbt 公式ページ](https://www.scala-sbt.org/1.x/docs/Setup.html) の指示に従い、sbt をインストールします。
インストール後、プロジェクトのルートディレクトリで `sbt assembly` コマンドを実行すると、`target/build` フォルダに成果物 (jar ファイル) が出力されます。

#### 1.2.4. Scala (プログラミング言語)
Scala は sbt によって適切なバージョンが自動的にダウンロード・インストールされるため、手動でのインストールは不要です。

#### 1.2.5. Docker
Paper サーバーの Docker コンテナを起動するために Docker をインストールします。
* [Docker の概要](https://docs.docker.com/get-started/overview/)
* [Docker のインストール](https://docs.docker.com/get-docker/)

#### 1.2.6. Paper サーバー
開発用の Paper サーバーは Docker コンテナを通じて提供されるため、個別の手動セットアップは基本的に不要です。

#### 1.2.7. GitHub アカウント
お持ちでない場合は、[こちらから登録](https://github.com/join)してください。
参考: [GitHubアカウント作成方法 (Qiita)](https://qiita.com/ayatokura/items/9eabb7ae20752e6dc79d)

#### 1.2.8. Git (バージョン管理システム)
Git をインストールします。公式の[インストールガイド (日本語)](https://git-scm.com/book/ja/v2/%E4%BD%BF%E3%81%84%E5%A7%8B%E3%82%81%E3%82%8B-Git%E3%81%AE%E3%82%A4%E3%83%B3%E3%82%B9%E3%83%88%E3%83%BC%E3%83%AB) を参照してください。

---

### 1.3. 🛠️ 上級者向け: ローカル環境への直接インストールを避ける方法

> [!WARNING]
>
> この手順は、Docker とシェルスクリプトの動作を理解している上級者向けです。自信がない場合は、この手順をスキップしてください。

ローカルに JDK や sbt を直接インストールしたくない場合 (例: VSCode + WSL 環境での開発、ビルドと起動のみが目的の場合)、以下のシェルスクリプトで Docker コンテナ内でビルドと実行準備ができます。

```bash
# 1. 既存のビルド成果物を削除 (再ビルドする場合)
$ rm -rf target/build

# 2. Dockerコンテナ内でsbt assemblyを実行
$ docker run --rm -it -v `pwd`:/app ghcr.io/giganticminecraft/seichiassist-builder-v2:776da10 sh -c "cd /app && sbt assembly"

# 3. 生成されたファイルの所有権を変更 (Docker内でrootとして実行されるため)
$ sudo chown -R `whoami` target/build

# 4. EULA同意ファイルが存在しない場合コピー
$ cp -n docker/paper/eula.txt docker/paper/serverfiles/eula.txt || true

# 5. Docker Composeでサーバー群を起動 (バックグラウンド、必要ならビルド)
$ docker compose up --build -d
```

## 2. リポジトリの準備とコードの取得

### 2.1. SeichiAssist リポジトリをフォーク

開発に参加するには、まず SeichiAssist の公式リポジトリを自身の GitHub アカウントにフォーク (コピー) します。

> [!IMPORTANT]
>
>   * このフォーク作業は、**最初の1回のみ**必要です。
>   * [GiganticMinecraft の GitHub Organization](https://github.com/GiganticMinecraft) のメンバーは、この手順を行う必要がありません。不明な場合は、このまま進めてください。

1.  [GiganticMinecraft/SeichiAssist のリポジトリページ](https://github.com/GiganticMinecraft/SeichiAssist)を開きます。
2.  画面右上の **Fork** ボタンをクリックします。
3.  「Create a new fork」画面が表示されたら、特に設定を変更せずに「**Create fork**」ボタンをクリックします。
4.  フォークが完了すると、自身のアカウント配下のリポジトリ (例: `your-username/SeichiAssist`) に移動します。これを確認してください。

### 2.2. フォークしたリポジトリをローカルにクローン

次に、フォークしたリポジトリを自分のPCにクローン (ダウンロード) します。

  * **IntelliJ IDEA を使用する場合**: [JetBrains のヘルプ (英語)](https://www.jetbrains.com/help/idea/cloning-repository.html) を参考にクローンしてください。
  * **コマンドラインを使用する場合**: ターミナルで以下のコマンドを実行します (`your-username` は実際のGitHubユーザー名に置き換えてください)。
    ```bash
    git clone --recursive https://github.com/your-username/SeichiAssist.git
    cd SeichiAssist # クローンしたディレクトリに移動
    ```

### 2.3. サブモジュール (protocol) の取得

> [!IMPORTANT]
>
>   * この作業は、**最初の1回のみ**必要です。
>   * `git clone` で **`--recursive` オプションを付けなかった場合に**実行してください。

`protocol` ディレクトリ以下のファイルはサブモジュールとして管理されているため、別途取得が必要です。クローンした `SeichiAssist` ディレクトリ内で以下のコマンドを実行します。

```bash
git submodule update --init --recursive
```

(または、クローン時に `git clone --recursive https://github.com/your-username/SeichiAssist.git` を実行したのであれば不要です。)

## 3. 開発の進め方

### 3.1. Issue (開発タスク) の確認

開発タスクは GitHub の Issue で管理されています。

  * [**全てのOpenなIssue一覧**](https://github.com/GiganticMinecraft/SeichiAssist/issues)
  * 特に初めて貢献する方は、[`good first issue` ラベルの付いた Issue](https://github.com/GiganticMinecraft/SeichiAssist/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22) から探すのがおすすめです。
  * Issue には、[Redmine で承認されたタスク](https://github.com/GiganticMinecraft/SeichiAssist/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22Tracked%3A%20Redmine%22) や [バグ報告](https://github.com/GiganticMinecraft/SeichiAssist/issues?q=is%3Aissue%20state%3Aopen%20label%3Abug) が含まれます。

### 3.2. IntelliJ IDEA の推奨設定 (scalafmt)

コードの一貫性を保つため、`scalafmt` によるフォーマット設定を行います。

1.  IntelliJ IDEA の設定 (Settings/Preferences) を開きます。
2.  `Editor` > `Code Style` > `Scala` を選択します。
3.  `Formatter:` ドロップダウンリストから `Scalafmt` を選択します。
4.  `Reformat on file save` (ファイル保存時に自動フォーマット) にチェックを入れます。

### 3.3. コーディングとコミット

1.  **ブランチの作成**: `develop` ブランチから作業用の新しいブランチを作成します。推奨されるブランチ名の形式は以下の通りです（対応する issue のタグを参考にしてください）。

    * 新機能追加: `feat/[issue番号]` または `feature/[issue番号]`
    * バグ修正: `fix/[issue番号]`
    * 機能改善: `improvement/[issue番号]`
    * 機能に無関係な変更: `development/[issue番号]`
    * リファクタリング: `refactor/[issue番号]`

    ```bash
    git switch develop                 # developブランチに切り替え
    git pull origin develop            # 最新のdevelopブランチの内容を取得
    git switch -c <ブランチ名>           # 新しいブランチを作成して切り替え
    ```

  これらの形式に従うと、Pull Request作成時に自動でラベルが付与されます。メンテナは、マージ前に適切なタグが付いていることを確認してください（自動付与されない場合）。

2.  **コードの変更**: 機能追加やバグ修正など、必要な変更を加えます。

3.  **コミット**: 変更内容をコミットします。コミットメッセージは[コンベンショナルコミット](https://www.conventionalcommits.org/ja/v1.0.0/)規約に従うことを推奨します。

    **コミットメッセージの基本構造:**

    ```
    <type>[optional scope]: <description>

    [optional body]

    [optional footer(s)]
    ```

    **主な `<type>`:**

    | Type       | 説明                                                                 | 例                                            |
    | :--------- | :------------------------------------------------------------------- | :-------------------------------------------- |
    | `feat`     | 新機能の追加                                                           | `feat: ユーザープロフィール機能を追加`              |
    | `fix`      | バグ修正                                                               | `fix: ガチャが引けないのを修正`               |
    | `docs`     | ドキュメントのみの変更                                                     | `docs: READMEのセットアップ手順を更新`          |
    | `style`    | コードの動作に影響しない変更 (フォーマットなど)                         | `style: scalafmt によるコード整形`             |
    | `refactor` | 機能に影響を与えないコード品質の向上                               | `refactor: 変数名を明確にする`          |
    | `perf`     | パフォーマンスを向上させるコード変更                                                | `perf: クエリの実行速度を改善`                |
    | `test`     | 不足しているテストの追加や既存テストの修正                                            | `test: MineStack のテストを追加`     |
    | `chore`    | ビルドプロセスや補助ツール、ライブラリの変更など (ソースコードの変更を含まない)                   | `chore: sbt のバージョンを更新`                |
    | `ci`       | CI設定ファイルやスクリプトの変更 (GitHub Actionsなど)                               | `ci: ビルドワークフローのトリガーを修正`          |

      * **1行目 (件名)**: 45文字以内を推奨。`<type>: 要約` の形式で記述します。
      * **本文 (任意)**: より詳細な説明や変更理由を記述します。
      * メッセージは日本語または英語で記述してください。
      * 1つのコミットには、関連する1つの変更のみを含めるように心がけてください。

4.  **繰り返し**: 満足のいく変更ができるまで、ステップ 2 と 3 を繰り返します。

-----

### 3.4. ローカル環境でのデバッグ

開発中のプラグインは、ローカルの Docker 環境で動作確認できます。

> [!IMPORTANT]
>
> SeichiAssist が Java 17 未満でコンパイルされた場合、実行時にエラーが発生します。JDK のバージョンが 17 であることを確認してください。

#### 3.4.1. Docker デバッグ環境の起動・停止

  * **起動**:
      * Linux/macOS: プロジェクトルートで `./prepare-docker.sh` を実行します。
      * Windows: プロジェクトルートで `prepare-docker.bat` を実行します。
        これらのスクリプトで、デバッグ用の Bungeecord と Paper サーバー環境が構築されます。
  * **ガチャデータ更新オプション**:
    `./prepare-docker.sh update-gachadata` (または `prepare-docker.bat update-gachadata`) のように引数を指定すると、最新のガチャ景品データがダウンロードされ、開発環境のデータが置き換えられます。

> [!WARNING]
>
> 初回起動時は `gachadata` テーブルが空です。ガチャ景品データが必要な場合、まずオプションなしで一度サーバーを起動し、その後 `update-gachadata` オプション付きで再度起動してください。初回起動時に `update-gachadata` を指定すると、Flyway によるマイグレーションと競合し、起動できません(2025/06/02 時点)。

  * **停止**: `docker compose down` コマンドを実行します。

#### 3.4.2. デバッグ環境への接続と操作

| 対象サービス        | アクセス方法                                      | 備考                                           |
| :------------------ | :------------------------------------------------ | :--------------------------------------------- |
| **Minecraft サーバー** | マルチプレイから `localhost` (または Docker IP) に接続 |                                                |
| **phpMyAdmin** | Webブラウザで `http://localhost:8080`             | データベース (MariaDB) の操作が可能です           |

#### 3.4.3. Paper サーバーコンソールへのアクセス

OP権限の付与など、サーバーコンソールでの操作が必要な場合：

1.  `docker ps` を実行し、Paper コンテナのID (例: `seichiassist_paperb_1`) を確認します。
2.  `docker attach (コンテナID)` を実行してアタッチします。
3.  コンソールから抜けるには <kbd>Ctrl</kbd> + <kbd>C</kbd> を押します (サーバーは停止しません)。

#### 3.4.4. 整地ワールドの作成・移動

デバッグ環境の初期スポーン地点は整地ワールドではありません。

1.  **整地ワールド作成**: OP権限を持つプレイヤーかコンソールから `mvcreate world_SW normal` を実行します。
2.  **整地ワールドへ移動**: `mvtp world_SW` を実行します。

#### 3.4.5. 外部プラグインのテスト

`/plugins` ディレクトリに他の `.jar` ファイルを配置すると、SeichiAssist とそれらのプラグインを同時に起動して動作確認できます。
(整地鯖で使用しているプラグインは GiganticMinecraft メンバー限定で MinIO から入手可能です。詳細は Discord でお問い合わせください。)

## 4. 変更内容をプロジェクトに反映 (Pull Request)

### 4.1. コードのフォーマットと静的解析の実行

コミットをプッシュする前に、ローカルで以下のコマンドを実行し、コードスタイルを統一します。

```bash
sbt scalafixAll
sbt scalafmtAll
```

### 4.2. 変更を自身の GitHub リポジトリにプッシュ

ローカルの変更内容を、GitHub 上の自身のフォークしたリポジトリにプッシュします。

```bash
git push origin <作業ブランチ名>
```

### 4.3. Pull Request (PR) の作成

1.  自身の GitHub リポジトリページ (例: `https://github.com/<あなたのID>/SeichiAssist`) をブラウザで開きます。

2.  プッシュしたブランチに関する通知が表示されていれば、「**Compare & pull request**」ボタンをクリックします。
    または、「**Pull requests**」タブを開き、「**New pull request**」ボタンをクリックします。

3.  **比較ブランチの確認**:

      * `base repository`: `GiganticMinecraft/SeichiAssist`
      * `base`: `develop` (マージ先のブランチ)
      * `head repository`: `<あなたのID>/SeichiAssist` (自分のリポジトリ)
      * `compare`: `<作業ブランチ名>` (自分が作業したブランチ)
        上記になっていることを確認します。

4.  **PR のタイトルと説明を記述**: 以下のテンプレートを参考に、変更内容がレビュアーに伝わるように記述します。

    ```markdown
    close # (関連するIssue番号)

    ----

    ### このPRの変更点と理由:

    (ここにPRで行った変更内容とその理由を具体的に記述します。自明な場合は省略可能ですが、できるだけ詳細に書きましょう。)

    ### 補足情報:

    (その他、メンテナーに伝えたいことがあれば自由に記述してください。)
    ```

      * **`close #`**:
          * この Pull Request に関連し、マージ後に自動でクローズしたい Issue の番号を指定します (例: `close #123`)。
          * 複数指定する場合はカンマ区切りで記述します (例: `close #123, close #456`)。
          * 詳細は [キーワードを使用してPull RequestをIssueにリンクする - GitHub Docs](https://docs.github.com/ja/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue) を参照してください。
      * **`このPRの変更点と理由:`**:
          * 行った変更内容とその背景・理由を具体的に説明します。
      * **`補足情報:`**:
          * レビューアに特に注意してほしい点や、その他共有事項があれば記述します。

5.  記述後、「**Create pull request**」ボタンをクリックします。

### 4.4. コードレビューとマージ

PR を作成すると、GitHub Actions による自動チェックが実行され、プロジェクトメンテナーによるレビューが行われます。

**PR がマージされるための条件:**

| 条件項目                      | 詳細 / 対応                                                                      |
| :---------------------------- | :------------------------------------------------------------------------------- |
| **自動チェックの成功** | 全てのCIチェック (コンパイル、テスト、lint等) がパスすること。                              |
|    ↳ コンパイルエラー        | エラーがないこと。                                                                 |
|    ↳ `scalafix` チェック     | パスすること。エラー時はローカルで `sbt scalafixAll` を実行して修正。                     |
|    ↳ `scalafmt` チェック     | パスすること。エラー時はローカルで `sbt scalafmtAll` を実行して修正。                     |
| **メンテナーによる承認** | 1人以上のメンテナー ([@kory33](https://github.com/kory33), [@rito528](https://github.com/rito528), [@KisaragiEffective](https://github.com/KisaragiEffective), [@Lucky3028](https://github.com/Lucky3028) 等) から承認 (Approve) をもらうこと。 |

レビューで修正依頼があった場合は、ローカルで修正し、再度コミット・プッシュしてください。

## 5. サーバーへの反映プロセス

### 5.1. デバッグサーバーへの自動反映

`develop` ブランチにPRがマージされると、変更は自動的にデバッグサーバーへデプロイされます。

  * **進捗確認**: [Discord の専用チャンネル](https://discord.com/channels/237758724121427969/959323550878138368) で確認できます。
  * **反映時間**: 通常、数分程度で完了します。
  * **接続方法**:
    1.  Minecraft Java Edition でサーバーアドレス `play-debug.seichi.click` に接続します。
    2.  チャットを開き (<kbd>T</kbd> キー)、`/server debug-s1` と入力して <kbd>Enter</kbd> を押します。

#### 5.1.1. 自動リリースの対象範囲

SeichiAssist の自動リリースは、SeichiAssist のプログラム本体 (`.jar` ファイル) のみを対象としています。
`config.yml` などの設定ファイルを変更したい場合は [seichi_infra](https://github.com/GiganticMinecraft/seichi_infra) リポジトリを参照してください。

> [!IMPORTANT]
>
> サーバーや環境間で共通の設定値は、極力 `config.yml` ではなくコード内に直接記述することを推奨します。

-----

### 5.2. 運営メンバー向け: 本番サーバーへの反映

本番サーバーへのリリースは、主に GitHub Actions を介して行われます。

| リリース種別        | 手順                                                                                                                              | ブランチ戦略                                 | 反映タイミング                             |
| :------------------ | :-------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------- | :----------------------------------------- |
| **通常リリース** | 1. [Create New Release ワークフロー](https://github.com/GiganticMinecraft/SeichiAssist/actions/workflows/create-pr-to-release.yml) を実行。<br>2. 自動作成された `master <- develop` のPRを適切なタイミングでマージ。 | `develop` → `master`                         | 原則、毎朝4時のサーバー再起動時                |
| **緊急リリース (Hotfix)** | 1. `hotfix-*` (例: `hotfix-critical-issue`) ブランチを `master` から作成。<br>2. 修正をコミットし、`master` へ直接PRを作成してマージ。                                   | `master` → `hotfix-*` → `master`             | マージ後、手動または次回の再起動 (状況による) |

> [!CAUTION]
>
> `develop` ブランチへの直接プッシュは、CIによるチェックが事後処理となるため避けてください。

