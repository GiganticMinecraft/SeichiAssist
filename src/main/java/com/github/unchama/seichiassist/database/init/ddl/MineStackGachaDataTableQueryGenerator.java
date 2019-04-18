package com.github.unchama.seichiassist.database.init.ddl;

/**
 * playerdataテーブルの初期化クエリを計算するクラス
 */
public class MineStackGachaDataTableQueryGenerator {
    private final String tableReferenceName;

    public MineStackGachaDataTableQueryGenerator(String tableReferenceName) {
        this.tableReferenceName = tableReferenceName;
    }

    public String generateCreateQuery() {
        //テーブルが存在しないときテーブルを新規作成
        return "CREATE TABLE IF NOT EXISTS " + tableReferenceName +
                "(id int auto_increment unique,"
                + "amount int(11))";
    }

    public String generateColumnCreationQuery() {
        //必要なcolumnを随時追加
        return "alter table " + tableReferenceName +
                " add column if not exists probability double default 0.0" +
                ",add column if not exists level int(11) default 0" +
                ",add column if not exists obj_name tinytext default null" +
                ",add column if not exists itemstack blob default null";
    }
}
