package ktx.scene2d.vis

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import ktx.scene2d.ApplicationTest

class ValidationTest : ApplicationTest() {
  init {
    test("should create FormValidator") {
      var invoked: Boolean

      val formValidator =
        validator {
          invoked = true
        }

      formValidator.shouldNotBeNull()
      invoked shouldBe true
    }
  }
}
