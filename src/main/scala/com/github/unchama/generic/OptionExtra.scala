package com.github.unchama.generic

object OptionExtra {

  /**
   * @param condition 条件式
   * @param value 抽出対象のOption値
   * @param default conditionがfalseの場合に返す値
   * @return conditionを満たす場合はOptionの中身を、そうでない場合はdefault
   */
  def getOrDefault[A](condition: Boolean)(value: Option[A], default: A): A = {
    if (condition)
      value.getOrElse(default)
    else
      default
  }
}
