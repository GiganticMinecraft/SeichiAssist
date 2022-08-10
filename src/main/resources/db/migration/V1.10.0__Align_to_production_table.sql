# # このマイグレーションが生まれた歴史的経緯の説明
#
# SeichiAssist は元々、Sql.java という DB へのデータ保存/読み込みを集約したファイルで
# データ定義言語(DDL)を起動時に流し込むことでテーブルのカラムを変更していた。
# 参考: https://github.com/GiganticMinecraft/SeichiAssist/blob/5f6a83de44cdd3c19ae412348008c6530f98890a/src/com/github/unchama/seichiassist/Sql.java
#
# これは、マイグレーションが困難といった運用上の難しさを伴うという事で、現在では
# マイグレーション内容を Git のバージョン管理に入れた上で、アプリケーション起動時に
# Flyway によるマイグレーション適用を行うようにしている。
#
# さて、起動時に DDL を流し込んでいたところから Flyway によるマイグレーション適用に移行する時には、
# https://github.com/GiganticMinecraft/SeichiAssist/blob/5f6a83de44cdd3c19ae412348008c6530f98890a/src/com/github/unchama/seichiassist/Sql.java
# の版を参考にすることで、実際のスキーマを再現できるだろうと考えたため、
# V1.0.0 のマイグレーションは当該コードが実行するであろう add column if not exists を列挙することで対応した。
#
# しかし、なんと https://github.com/GiganticMinecraft/SeichiAssist/blob/5f6a83de44cdd3c19ae412348008c6530f98890a/src/com/github/unchama/seichiassist/Sql.java
# は本番環境のテーブル状態と一致していないことが 2022/08/10 に判明した。
#
# 以下のマイグレーションスクリプトは、playerdata テーブル内の
# 「本番環境のテーブル状態」を「開発環境でマイグレーションスクリプトによって再現されるテーブル状態」
# に合わせるためのものである。

# https://github.com/GiganticMinecraft/SeichiAssist/blob/5f6a83de44cdd3c19ae412348008c6530f98890a/src/com/github/unchama/seichiassist/Sql.java
# の時点で存在していなかったが playerdata テーブルに残っていたカラム。
# 2022/08/10 現在本番環境にも残っている。
use seichiassist;

alter table playerdata drop column if exists areaflag;
alter table playerdata drop column if exists assaultareaflag;
alter table playerdata drop column if exists VotingFairyTime;
alter table playerdata drop column if exists subhome_name;

# https://github.com/GiganticMinecraft/SeichiAssist/blob/5f6a83de44cdd3c19ae412348008c6530f98890a/src/com/github/unchama/seichiassist/Sql.java
# の時点では build_count は double のはずであったが、
#
#  - 実はdouble になっているのは
#    https://github.com/GiganticMinecraft/SeichiAssist/commit/5f6a83de44cdd3c19ae412348008c6530f98890a
#    によって DDL が変更された結果であり
#  - 55f6a83de44cdd3c19ae412348008c6530f98890a よりも前の DDL は既に実行されていた
#  - その後誰もカラムの型のマイグレーションを行わなかった
#
# 結果、本番環境では int(11) のままであった。デバッグ環境に合わせるために double に変更する。
alter table playerdata modify build_count double;

# https://github.com/GiganticMinecraft/SeichiAssist/blob/5f6a83de44cdd3c19ae412348008c6530f98890a/src/com/github/unchama/seichiassist/Sql.java
# の時点では effectflag は tinyint のはずであったが、**何故か** smallint になっていた。
#
# 0以上5以下の値しか入っていないのでこのマイグレーションは成功する。
#
# 参考(2022/08/10 現在の永続化層のコード):
# https://github.com/GiganticMinecraft/SeichiAssist/blob/fdd279a33d11cc5d0d7f3e03ef3790f4117beef7/src/main/scala/com/github/unchama/seichiassist/subsystems/fastdiggingeffect/infrastructure/JdbcFastDiggingEffectSuppressionStatePersistence.scala#L20-L25
#
# 参考(本番データのクエリ結果):
#
# MariaDB [seichiassist]> select effectflag from playerdata order by effectflag asc limit 1;
# +------------+
# | effectflag |
# +------------+
# |          0 |
# +------------+
# 1 row in set (0.018 sec)
#
# MariaDB [seichiassist]> select effectflag from playerdata order by effectflag desc limit 1;
# +------------+
# | effectflag |
# +------------+
# |          5 |
# +------------+
# 1 row in set (0.019 sec)
alter table playerdata modify effectflag tinyint;
