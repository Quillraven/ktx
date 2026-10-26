package ktx.math

import com.badlogic.gdx.math.Rectangle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RectangleTest : FunSpec({
  test("should destructure Rectangle") {
    val rect = Rectangle(1f, 2f, 3f, 4f)

    val (x, y, w, h) = rect

    x shouldBe 1f
    y shouldBe 2f
    w shouldBe 3f
    h shouldBe 4f
  }
})
