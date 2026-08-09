package com.github.unchama.seichiassist.tools.pastgacha

/**
 * 1.12.2形式のgiveコマンドを安全に変換できなかった理由。
 */
enum GiveCommandConversionError:
  case InvalidCommand(detail: String)
  case InvalidSnbt(position: Int, detail: String)
  case InvalidField(field: String, detail: String)
  case UnsupportedEnchantmentId(id: Int)
  case UnsupportedDataValue(itemId: String, dataValue: Int)

  def message: String = this match
    case InvalidCommand(detail)                  => s"giveコマンドを解釈できません: $detail"
    case InvalidSnbt(position, detail)           => s"NBTの${position}文字目を解釈できません: $detail"
    case InvalidField(field, detail)             => s"NBTフィールド $field を変換できません: $detail"
    case UnsupportedEnchantmentId(id)            => s"未対応の1.12.2エンチャントIDです: $id"
    case UnsupportedDataValue(itemId, dataValue) =>
      s"$itemId のデータ値 $dataValue が耐久値か種類IDかを判定できません"
