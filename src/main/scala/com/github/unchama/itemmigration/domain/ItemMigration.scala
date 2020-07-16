package com.github.unchama.itemmigration.domain

/**
 * バージョン付きのアイテムスタック変換
 */
case class ItemMigration(version: ItemMigrationVersionNumber, conversion: ItemStackConversion)
