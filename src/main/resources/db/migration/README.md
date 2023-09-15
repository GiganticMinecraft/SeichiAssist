# データベースのマイグレーション

## RDBMS
SeichiAssistはデータベースにMariaDBを使っています。

### スキーマ変更の他サービスへの影響

SeichiAssist の DB は整地鯖内の他のサービスに共有されています。カラムの変更 (削除・改名・型の変更) やテーブルの変更 (削除・改名) を行う時は、他のサービスに影響が及ばないか注意するようにしてください。

2023/05/20 現在、 SeichiAssist の DB を読み取っているサービスは以下の通りです。

- <https://github.com/GiganticMinecraft/seichi-game-data-server>
  - 整地量・建築量、ログイン時間、投票数に関するデータを読み取っています
