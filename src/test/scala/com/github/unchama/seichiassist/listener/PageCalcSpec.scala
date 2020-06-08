package com.github.unchama.seichiassist.listener

import org.scalatest.wordspec.AnyWordSpec

import com.github.unchama.seichiassist.util.TypeConverter
import com.github.unchama.seichiassist.listener.PlayerInventoryListener
class PageCalcSpec extends AnyWordSpec {
  "calcPage" should {
    "convert title to page" in {
      val instance = new PlayerInventoryListener()
      assert(20 == instance.getPage("整地神ランキング20ページ目へ"))
      assert(1 == instance.getPage("ログイン神ランキング1ページ目へ"))
      // assertThrows[NumberFormatException]
      assert(try {
        instance.getPage("投票神ランキングABCページ目へ")
        false
      } catch {
        case _: NumberFormatException => true
        case _ => false
      })
      assert(try {
        instance.getPage("投票神ランキング0ABCページ目へ")
        false
      } catch {
        case _: NumberFormatException => true
        case _ => false
      })
    }
  }
}
