package ktx.async

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tests [com.badlogic.gdx.utils.Timer] extensions.
 */
class TimerTest : AsyncTest() {
  init {
    // Most Timer.Task properties are private, unfortunately. Only scheduled status can be tested reliably.
    test("should schedule asynchronous task") {
      // When:
      val task = schedule(delaySeconds = 1f) {}

      // Then:
      task.isScheduled shouldBe true
    }

    test("should execute asynchronous task") {
      // Given:
      val result = AtomicInteger()

      // When:
      schedule(delaySeconds = 0.05f) {
        result.set(5)
      }

      // Then:
      delay(0.1f)
      result.get() shouldBe 5
    }

    test("should cancel asynchronous task") {
      // Given:
      val executed = AtomicBoolean()
      val task = schedule(delaySeconds = 0.05f) { executed.set(true) }

      // When:
      task.cancel()

      // Then:
      delay(0.1f)
      executed.get() shouldBe false
    }

    test("should schedule repeated asynchronous task") {
      // When:
      val task = interval(delaySeconds = 1f, intervalSeconds = 1f) {}

      // Then:
      task.isScheduled shouldBe true
    }

    test("should execute repeated asynchronous task") {
      // Given:
      val executions = AtomicInteger()

      // When:
      interval(delaySeconds = 0f, intervalSeconds = 0.05f) {
        executions.incrementAndGet()
      }

      // Then:
      delay(0.2f)
      assert(executions.get() > 2)
    }

    test("should execute repeated asynchronous task N times") {
      // Given:
      val executions = AtomicInteger()

      // When:
      interval(delaySeconds = 0f, intervalSeconds = 0.05f, repeatCount = 1) {
        executions.incrementAndGet()
      }

      // Then: executions count should be equal to 1+repeatCount (initial execution + repetitions).
      delay(0.2f)
      2 shouldBe executions.get()
    }
  }
}
