package com.github.unchama.seichiassist.database.migration.V1_1_0;

import java.sql.Connection;
import java.util.Set;

/**
 * 1.1.0におけるマイグレーションタスクの抽象インターフェース
 */
/* package-private */ interface Migration {

    /**
     * マイグレーションを行う。
     * @param connection 使用するConnectionオブジェクト
     * @param playerDataColumnNames seichiassist.playerdataのカラム名すべてを含むSet
     */
    /* package-private */ void migrate(final Connection connection, final Set<String> playerDataColumnNames);
}
