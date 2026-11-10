package ktx.app

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.PerformanceCounter
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class ProfilingTest : FunSpec({
  beforeTest {
    Gdx.app = mock()
  }

  test("should profile operation") {
    var repeats = 0

    val performanceCounter =
      profile(name = "Thread.sleep", repeats = 10) {
        repeats++
        Thread.sleep(10L)
      }

    performanceCounter.name shouldBe "Thread.sleep"
    performanceCounter.time.mean.windowSize shouldBe 10
    performanceCounter.time.count shouldBe 10
    repeats shouldBe 10
    // Sleeping never takes less than requested, but can take much longer on slow or loaded machines:
    val mean = performanceCounter.time.mean.mean
    withClue("$mean must be between 0.0095 and 0.05 seconds.") { (mean in 0.0095f..0.05f) shouldBe true }
  }

  test("should profile operation with existing PerformanceCounter") {
    val performanceCounter = PerformanceCounter("Thread.sleep", 10)
    var repeats = 0

    performanceCounter.profile {
      repeats++
      Thread.sleep(10L)
    }

    performanceCounter.name shouldBe "Thread.sleep"
    performanceCounter.time.count shouldBe 10
    repeats shouldBe 10
    // Sleeping never takes less than requested, but can take much longer on slow or loaded machines:
    val mean = performanceCounter.time.mean.mean
    withClue("$mean must be between 0.0095 and 0.05 seconds.") { (mean in 0.0095f..0.05f) shouldBe true }
  }

  afterTest {
    Gdx.app = null
  }
})
