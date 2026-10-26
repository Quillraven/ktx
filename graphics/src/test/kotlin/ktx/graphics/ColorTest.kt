package ktx.graphics

import com.badlogic.gdx.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

class ColorTest : FunSpec({
  test("should construct Color") {
    val color = color(red = 0.25f, green = 0.5f, blue = 0.75f, alpha = 0.6f)

    color.r shouldBe 0.25f
    color.g shouldBe 0.5f
    color.b shouldBe 0.75f
    color.a shouldBe 0.6f
  }

  test("should construct Color with optional alpha") {
    val color = color(red = 0.25f, green = 0.5f, blue = 0.75f)

    color.r shouldBe 0.25f
    color.g shouldBe 0.5f
    color.b shouldBe 0.75f
    color.a shouldBe 1f
  }

  test("should copy Color values") {
    val color = Color(0.4f, 0.5f, 0.6f, 0.7f)

    val copy = color.copy()

    copy shouldNotBeSameInstanceAs color
    copy.r shouldBe 0.4f
    copy.g shouldBe 0.5f
    copy.b shouldBe 0.6f
    copy.a shouldBe 0.7f
    copy shouldBe color
  }

  test("should override chosen Color values when copying") {
    val color = Color(0.4f, 0.5f, 0.6f, 0.7f)

    val copy = color.copy(red = 0.25f, green = 0.35f, blue = 0.45f, alpha = 0.55f)

    copy shouldNotBeSameInstanceAs color
    copy.r shouldBe 0.25f
    copy.g shouldBe 0.35f
    copy.b shouldBe 0.45f
    copy.a shouldBe 0.55f
    copy shouldNotBe color
  }

  test("should destruct Color into red, green, blue and alpha components") {
    val color = Color(0.1f, 0.2f, 0.3f, 0.4f)

    val (red, green, blue, alpha) = color

    red shouldBe color.r
    green shouldBe color.g
    blue shouldBe color.b
    alpha shouldBe color.a
  }
})
