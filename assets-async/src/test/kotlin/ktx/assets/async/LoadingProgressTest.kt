package ktx.assets.async

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoadingProgressTest : FunSpec({
  test("should increase count of scheduled assets") {
    // Given:
    val progress = LoadingProgress()

    // When:
    progress.registerScheduledAsset()

    // Then:
    progress.total shouldBe 1
    progress.loaded shouldBe 0
    progress.failed shouldBe 0
  }

  test("should increase count of loaded assets") {
    // Given:
    val progress = LoadingProgress()
    progress.registerScheduledAsset()

    // When:
    progress.registerLoadedAsset()

    // Then:
    progress.total shouldBe 1
    progress.loaded shouldBe 1
    progress.failed shouldBe 0
  }

  test("should increase count of added assets") {
    // Given:
    val progress = LoadingProgress()

    // When:
    progress.registerAddedAsset()

    // Then:
    progress.total shouldBe 1
    progress.loaded shouldBe 1
    progress.failed shouldBe 0
  }

  test("should increase count of failed assets") {
    // Given:
    val progress = LoadingProgress()
    progress.registerScheduledAsset()

    // When:
    progress.registerFailedAsset()

    // Then:
    progress.total shouldBe 1
    progress.loaded shouldBe 0
    progress.failed shouldBe 1
  }

  test("should decrease count of scheduled assets") {
    // Given:
    val progress = LoadingProgress()
    progress.registerScheduledAsset()

    // When:
    progress.removeScheduledAsset()

    // Then:
    progress.total shouldBe 0
    progress.loaded shouldBe 0
    progress.failed shouldBe 0
  }

  test("should decrease count of loaded assets") {
    // Given:
    val progress = LoadingProgress()
    progress.registerScheduledAsset()
    progress.registerLoadedAsset()

    // When:
    progress.removeLoadedAsset()

    // Then:
    progress.total shouldBe 0
    progress.loaded shouldBe 0
    progress.failed shouldBe 0
  }

  test("should decrease count of added assets") {
    // Given:
    val progress = LoadingProgress()
    progress.registerAddedAsset()

    // When:
    progress.removeLoadedAsset()

    // Then:
    progress.total shouldBe 0
    progress.loaded shouldBe 0
    progress.failed shouldBe 0
  }

  test("should decrease count of failed assets") {
    // Given:
    val progress = LoadingProgress()
    progress.registerScheduledAsset()
    progress.registerFailedAsset()

    // When:
    progress.removeFailedAsset()

    // Then:
    progress.total shouldBe 0
    progress.loaded shouldBe 0
    progress.failed shouldBe 0
  }

  test("should report progress") {
    // Given:
    val progress = LoadingProgress()

    // When:
    repeat(5) { progress.registerScheduledAsset() }
    // Then:
    progress.percent shouldBe 0f
    progress.isFinished shouldBe false
    progress.isFailed shouldBe false

    // When:
    progress.registerLoadedAsset()
    // Then:
    progress.percent shouldBe 0.2f
    progress.isFinished shouldBe false
    progress.isFailed shouldBe false

    // When:
    progress.registerLoadedAsset()
    // Then:
    progress.percent shouldBe 0.4f
    progress.isFinished shouldBe false
    progress.isFailed shouldBe false

    // When:
    progress.registerLoadedAsset()
    // Then:
    progress.percent shouldBe 0.6f
    progress.isFinished shouldBe false
    progress.isFailed shouldBe false

    // When:
    progress.registerLoadedAsset()
    // Then:
    progress.percent shouldBe 0.8f
    progress.isFinished shouldBe false
    progress.isFailed shouldBe false

    // When:
    progress.registerLoadedAsset()
    // Then:
    progress.percent shouldBe 1f
    progress.isFinished shouldBe true
    progress.isFailed shouldBe false
  }

  test("should report progress within 0, 1 range even when out of sync") {
    // Given:
    val progress = LoadingProgress()

    // When: loaded assets count is higher than scheduled assets:
    progress.registerScheduledAsset()
    progress.registerLoadedAsset()
    progress.registerLoadedAsset()

    // Then:
    progress.percent shouldBe 1f
  }

  test("should not include failed assets in progress") {
    // Given:
    val progress = LoadingProgress()
    repeat(4) { progress.registerScheduledAsset() }
    repeat(3) { progress.registerLoadedAsset() }

    // When:
    progress.registerFailedAsset()

    // Then:
    progress.percent shouldBe 0.75f
    progress.isFailed shouldBe true
    progress.isFinished shouldBe true
  }

  test("should reset the progress") {
    // Given:
    val progress = LoadingProgress()
    repeat(100) { progress.registerScheduledAsset() }
    repeat(75) { progress.registerLoadedAsset() }
    repeat(25) { progress.registerFailedAsset() }
    repeat(50) { progress.registerAddedAsset() }

    // When:
    progress.reset()

    // Then:
    progress.total shouldBe 0
    progress.loaded shouldBe 0
    progress.failed shouldBe 0
    progress.isFinished shouldBe false
    progress.isFailed shouldBe false
  }

  test("should handle concurrent updates") {
    // Given:
    val progress = LoadingProgress()

    // When:
    val jobs =
      (1..1000).map { id ->
        GlobalScope.launch {
          when (id % 4) {
            0 -> progress.registerScheduledAsset()
            1 -> progress.registerLoadedAsset()
            2 -> progress.registerFailedAsset()
            3 -> progress.registerAddedAsset()
          }
        }
      }

    // Then:
    runBlocking { jobs.joinAll() }
    progress.total shouldBe 500
    progress.loaded shouldBe 500
    progress.failed shouldBe 250
  }

  test("should handle concurrent registration and removal") {
    // Given:
    val progress = LoadingProgress()

    // When:
    val jobs =
      (1..1000).map { id ->
        GlobalScope.launch {
          when (id % 4) {
            0 -> progress.registerScheduledAsset()
            1 -> progress.registerLoadedAsset()
            2 -> progress.registerFailedAsset()
            3 -> progress.registerAddedAsset()
          }
          when (id % 5) {
            0 -> progress.removeScheduledAsset()
            1 -> progress.removeLoadedAsset()
            2 -> progress.removeFailedAsset()
            else -> progress.registerScheduledAsset()
          }
        }
      }

    // Then:
    runBlocking { jobs.joinAll() }
    progress.total shouldBe (900 - 600)
    progress.loaded shouldBe (500 - 200)
    progress.failed shouldBe (250 - 200)
  }
})
