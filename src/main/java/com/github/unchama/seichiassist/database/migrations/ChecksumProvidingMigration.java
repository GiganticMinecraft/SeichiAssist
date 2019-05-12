package com.github.unchama.seichiassist.database.migrations;

import org.flywaydb.core.api.migration.BaseJavaMigration;

abstract /* package-protected */ class ChecksumProvidingMigration extends BaseJavaMigration {
    @Override
    public Integer getChecksum() {
        // TODO 実際のチェックサムを返す
        return super.getChecksum();
    }
}
