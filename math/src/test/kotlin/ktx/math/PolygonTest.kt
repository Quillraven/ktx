package ktx.math

import com.badlogic.gdx.math.Polygon
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PolygonTest : FunSpec({
  test("should destructure Polygon") {
    val polygon = Polygon().apply { setPosition(1f, 2f) }

    val (x, y) = polygon

    x shouldBe 1f
    y shouldBe 2f
  }
})
