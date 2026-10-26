package ktx.math

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlin.math.abs

/**
 * Tests [ClosedRange]-related utilities.
 */
class RangesTest : FunSpec({
  test("should produce expected int range") {
    val center = -300
    val tolerance = 10000
    val range = center amid tolerance
    (center - tolerance) shouldBe range.first
    (center + tolerance) shouldBe range.last
  }

  test("should multiply int range") {
    val original = -20..500
    val multiplier = 37
    val scaled = original * multiplier
    val invertedScaled = multiplier * original
    scaled.first shouldBe (original.first * multiplier)
    scaled.last shouldBe (original.last * multiplier)
    invertedScaled.first shouldBe (original.first * multiplier)
    invertedScaled.last shouldBe (original.last * multiplier)
  }

  test("should divide int range") {
    val original = -20..500
    val divisor = 37
    val divided = original / divisor
    divided.first shouldBe (original.first / divisor)
    divided.last shouldBe (original.last / divisor)
  }

  test("should add to int range") {
    val original = -20..500
    val addend = 37
    val shifted = original + addend
    shifted.first shouldBe (original.first + addend)
    shifted.last shouldBe (original.last + addend)
  }

  test("should subtract from int range") {
    val original = -20..500
    val subtrahend = 37
    val shifted = original - subtrahend
    shifted.first shouldBe (original.first - subtrahend)
    shifted.last shouldBe (original.last - subtrahend)
  }

  test("should produce uniform int distribution") {
    val count = 100000
    val allowableError = 0.03f

    val range = -4060..24500
    val values =
      Array(count) {
        range.random(MathUtils.random)
      }

    val numInnerRanges = 5
    val expectedCountEach = count / numInnerRanges.toFloat()
    (range step ((range.last - range.first) / numInnerRanges))
      .zipWithNext()
      .map { it.first until it.second }
      .forEach { innerRange ->
        val innerCount = values.count { it in innerRange }
        (abs(innerCount.toFloat() - expectedCountEach) / expectedCountEach <= allowableError) shouldBe true
      }
    (values.all { it in range }) shouldBe true
  }

  test("should produce expected float range") {
    val center = -325f
    val tolerance = 6000f
    val range = center amid tolerance
    (center - tolerance) shouldBe range.start
    (center + tolerance) shouldBe range.endInclusive
  }

  test("should multiply float range") {
    val original = -20f..500f
    val multiplier = 37f
    val scaled = original * multiplier
    val invertedScaled = multiplier * original
    scaled.start shouldBe (original.start * multiplier)
    scaled.endInclusive shouldBe (original.endInclusive * multiplier)
    invertedScaled.start shouldBe (original.start * multiplier)
    invertedScaled.endInclusive shouldBe (original.endInclusive * multiplier)
  }

  test("should divide float range") {
    val original = -20f..500f
    val divisor = 37f
    val divided = original / divisor
    divided.start shouldBe (original.start / divisor)
    divided.endInclusive shouldBe (original.endInclusive / divisor)
  }

  test("should add to float range") {
    val original = -20f..500f
    val addend = 37f
    val shifted = original + addend
    shifted.start shouldBe (original.start + addend)
    shifted.endInclusive shouldBe (original.endInclusive + addend)
  }

  test("should subtract from float range") {
    val original = -20f..500f
    val subtrahend = 37f
    val shifted = original - subtrahend
    shifted.start shouldBe (original.start - subtrahend)
    shifted.endInclusive shouldBe (original.endInclusive - subtrahend)
  }

  test("should produce uniform float distribution") {
    val count = 100000
    val allowableError = 0.03f

    val range = -4060f..24500f
    val values =
      Array(count) {
        range.random()
      }

    val numInnerRanges = 5
    val expectedCountEach = count / numInnerRanges.toFloat()
    List(numInnerRanges + 1) { it * (range.endInclusive - range.start) / numInnerRanges + range.start }
      .zipWithNext()
      .map { it.first..it.second }
      .forEach { innerRange ->
        val innerCount = values.count { it in innerRange }
        (abs(innerCount.toFloat() - expectedCountEach) / expectedCountEach <= allowableError) shouldBe true
      }
    (values.all { it in range }) shouldBe true
  }

  test("should produce gaussian distribution") {
    val count = 100000
    val allowableError = 0.03f

    val center = 350f
    val tolerance = 6300f
    val range = center amid tolerance
    val fourSigma = center amid (tolerance * 2f / 3f)
    val twoSigma = center amid (tolerance / 3f)
    var withinRange = 0
    var withinFourSigma = 0
    var withinTwoSigma = 0
    repeat(count) {
      val value = range.randomGaussian(false)
      if (value in range) withinRange++
      if (value in fourSigma) withinFourSigma++
      if (value in twoSigma) withinTwoSigma++
    }

    val resultsToExpected =
      listOf(
        withinRange.toFloat() / count to 0.9973f,
        withinFourSigma.toFloat() / count to 0.9545f,
        withinTwoSigma.toFloat() / count to 0.6827f,
      )
    for ((result, expected) in resultsToExpected) {
      (abs(result - expected) / expected <= allowableError) shouldBe true
    }
  }

  test("should produce symmetric triangular distribution") {
    val count = 100000
    val allowableError = 0.03f

    val range = -240f..9800f
    val span = range.endInclusive - range.start
    val values =
      Array(count) {
        range.randomTriangular()
      }

    val center = 0.5 * (range.endInclusive + range.start)
    val beforeModeSpan = center - range.start
    val afterModeSpan = range.endInclusive - center
    for (fraction in listOf(0.2f, 0.4f, 0.6f, 0.8f)) {
      val innerRange = (beforeModeSpan * fraction + range.start)..(range.endInclusive - afterModeSpan * fraction)
      val leftTriangleBase = innerRange.start - range.start
      val leftTriangleArea = 0.5f * leftTriangleBase * (2 * leftTriangleBase / (span * beforeModeSpan))
      val probability = 1f - leftTriangleArea * 2
      val result = values.count { it in innerRange }.toFloat() / count
      (abs(result - probability) / probability <= allowableError) shouldBe true
    }
    (values.all { it in range }) shouldBe true
  }

  test("should produce triangular distribution") {
    val count = 100000
    val allowableError = 0.03f

    val range = -240f..9800f
    val span = range.endInclusive - range.start
    val fractionalMode = 0.73f
    val values =
      Array(count) {
        range.randomTriangular(fractionalMode)
      }

    val expectedMode = fractionalMode * (range.endInclusive - range.start) + range.start
    val beforeModeSpan = expectedMode - range.start
    val afterModeSpan = range.endInclusive - expectedMode
    for (fraction in listOf(0.2f, 0.4f, 0.6f, 0.8f)) {
      // For simplicity, all checked ranges span the mode
      val innerRange = (beforeModeSpan * fraction + range.start)..(range.endInclusive - afterModeSpan * fraction)
      val leftTriangleBase = innerRange.start - range.start
      val leftTriangleArea = 0.5f * leftTriangleBase * (2 * leftTriangleBase / (span * beforeModeSpan))
      val rightTriangleBase = range.endInclusive - innerRange.endInclusive
      val rightTriangleArea = 0.5f * rightTriangleBase * (2 * rightTriangleBase / (span * afterModeSpan))
      val probability = 1f - leftTriangleArea - rightTriangleArea
      val result = values.count { it in innerRange }.toFloat() / count
      (abs(result - probability) / probability <= allowableError) shouldBe true
    }
    (values.all { it in range }) shouldBe true
  }

  test("should interpolate linearly") {
    val start = 150f
    val end = 300f
    val progress = 0.75f

    val range = start..end

    262.5f shouldBe (range.lerp(progress) plusOrMinus 0.000001f)
  }

  test("should interpolate") {
    val start = 0f
    val end = 1f
    val progress = 0.5f

    val range = start..end

    0.7071f shouldBe (range.interpolate(progress, Interpolation.sineOut) plusOrMinus 0.001f)
  }
})
