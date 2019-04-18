package com.github.unchama.seichiassist.database.init.ddl;

public class GachaDataTableQueryGenerator implements TableInitializationQueryGenerator {

    private final String tableReferenceName;

    public GachaDataTableQueryGenerator(String tableReferenceName) {
        this.tableReferenceName = tableReferenceName;
    }

    @Override
    public String generateTableCreationQuery() {
        //テーブルが存在しないときテーブルを新規作成
        return "CREATE TABLE IF NOT EXISTS " + tableReferenceName +
                        "(id int auto_increment unique,"
                        + "amount int(11))";
    }

    @Override
    public String generateColumnCreationQuery() {
        return "alter table " + tableReferenceName +
                " add column if not exists probability double default 0.0" +
                ",add column if not exists itemstack blob default null";

    }
}
