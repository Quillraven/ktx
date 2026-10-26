package ktx.math

import com.badlogic.gdx.math.Circle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CircleTest : FunSpec({
  test("should destructure Circle") {
    val circle = Circle(1f, 2f, 3f)

    val (x, y, radius) = circle

    x shouldBe 1f
    y shouldBe 2f
    radius shouldBe 3f
  }
})
