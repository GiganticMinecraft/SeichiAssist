package com.github.unchama.seichiassist.listener

import org.scalatest.wordspec.AnyWordSpec

import com.github.unchama.seichiassist.util.TypeConverter
import com.github.unchama.seichiassist.listener.PlayerInventoryListener
class PageCalcSpec extends AnyWordSpec {
  "calcPage" should {
    "return page number if contain" in {
      val instance = new PlayerInventoryListener()
      assert(20 == instance.getPage("整地神ランキング20ページ目へ"))
      assert(1 == instance.getPage("ログイン神ランキング1ページ目へ"))
    }

    "throw NumberFormatException if don't contain" in {
      // assertThrows[NumberFormatException]
      val instance = new PlayerInventoryListener()

      assert(try {
        instance.getPage("投票神ランキングAB2Cページ目へ")
        false
      } catch {
        case e: NumberFormatException => true
        case _: Throwable => false
      })

      assert(try {
        instance.getPage("投票神ランキング2AB3Cページ目へ")
        false
      } catch {
        case e: NumberFormatException => true
        case _: Throwable => false
      })
    }
  }
}
