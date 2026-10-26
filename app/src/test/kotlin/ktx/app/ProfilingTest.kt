package ktx.app

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.PerformanceCounter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
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
    performanceCounter.time.mean.mean shouldBe (0.01f plusOrMinus 0.002f)
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
    performanceCounter.time.mean.mean shouldBe (0.01f plusOrMinus 0.002f)
  }

  afterTest {
    Gdx.app = null
  }
})
