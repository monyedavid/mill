package mill.scalalib

import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

class ScalaTestSpec3 extends AnyFreeSpec {

  "A Set 3" - {
    "when empty" - {
      "should have size 0" in {
        assert(Set.empty.size == 0)
      }

      "should produce NoSuchElementException when head is invoked" in {
        assertThrows[NoSuchElementException] {
          Set.empty.head
        }
      }

      "should be tagged" taggedAs(TaggedTest) in {
        assert(true)
      }
    }
  }
}