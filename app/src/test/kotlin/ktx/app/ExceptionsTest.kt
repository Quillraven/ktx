package ktx.app

import com.badlogic.gdx.utils.GdxRuntimeException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ExceptionsTest : FunSpec({
  test("should throw error") {
    shouldThrow<GdxRuntimeException> {
      gdxError()
    }
  }

  test("should throw error given a null message") {
    val exception =
      shouldThrow<GdxRuntimeException> {
        gdxError(null)
      }

    exception.message shouldBe "null"
  }

  test("should throw error given a string message") {
    val exception =
      shouldThrow<GdxRuntimeException> {
        gdxError("Test")
      }

    exception.message shouldBe "Test"
  }

  test("should throw error given an object message") {
    val exception =
      shouldThrow<GdxRuntimeException> {
        gdxError(42)
      }

    exception.message shouldBe "42"
  }

  test("should throw error given a cause") {
    val cause = Exception()

    val exception =
      shouldThrow<GdxRuntimeException> {
        gdxError("Message", cause)
      }

    exception.message shouldBe "Message"
    exception.cause shouldBeSameInstanceAs cause
  }
})
