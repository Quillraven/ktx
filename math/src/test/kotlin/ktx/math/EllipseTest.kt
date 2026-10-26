package ktx.math

import com.badlogic.gdx.math.Ellipse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EllipseTest : FunSpec({
  test("should destructure Ellipse") {
    val ellipse = Ellipse(1f, 2f, 3f, 4f)

    val (x, y, w, h) = ellipse

    x shouldBe 1f
    y shouldBe 2f
    w shouldBe 3f
    h shouldBe 4f
  }
})
