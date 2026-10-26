package ktx.math

import com.badlogic.gdx.math.Polyline
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PolylineTest : FunSpec({
  test("should destructure Polyline") {
    val polyline = Polyline().apply { setPosition(1f, 2f) }

    val (x, y) = polyline

    x shouldBe 1f
    y shouldBe 2f
  }
})
