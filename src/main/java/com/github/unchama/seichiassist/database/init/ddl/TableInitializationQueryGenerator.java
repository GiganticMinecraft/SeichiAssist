package com.github.unchama.seichiassist.database.init.ddl;

public interface TableInitializationQueryGenerator {

    String generateTableCreationQuery();
    String generateColumnCreationQuery();

}
