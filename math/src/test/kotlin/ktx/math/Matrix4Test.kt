package ktx.math

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.GdxNativesLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

/**
 * Tests [Matrix4]-related utilities.
 */
class Matrix4Test : FunSpec({
  val floatTolerance = 0.00001f

  fun assertMatrixEquals(
    matrix: Matrix4,
    m00: Float,
    m01: Float,
    m02: Float,
    m03: Float,
    m10: Float,
    m11: Float,
    m12: Float,
    m13: Float,
    m20: Float,
    m21: Float,
    m22: Float,
    m23: Float,
    m30: Float,
    m31: Float,
    m32: Float,
    m33: Float,
    tolerance: Float = floatTolerance,
  ) {
    val values = matrix.`val`
    values[Matrix4.M00] shouldBe (m00 plusOrMinus tolerance)
    values[Matrix4.M01] shouldBe (m01 plusOrMinus tolerance)
    values[Matrix4.M02] shouldBe (m02 plusOrMinus tolerance)
    values[Matrix4.M03] shouldBe (m03 plusOrMinus tolerance)
    values[Matrix4.M10] shouldBe (m10 plusOrMinus tolerance)
    values[Matrix4.M11] shouldBe (m11 plusOrMinus tolerance)
    values[Matrix4.M12] shouldBe (m12 plusOrMinus tolerance)
    values[Matrix4.M13] shouldBe (m13 plusOrMinus tolerance)
    values[Matrix4.M20] shouldBe (m20 plusOrMinus tolerance)
    values[Matrix4.M21] shouldBe (m21 plusOrMinus tolerance)
    values[Matrix4.M22] shouldBe (m22 plusOrMinus tolerance)
    values[Matrix4.M23] shouldBe (m23 plusOrMinus tolerance)
    values[Matrix4.M30] shouldBe (m30 plusOrMinus tolerance)
    values[Matrix4.M31] shouldBe (m31 plusOrMinus tolerance)
    values[Matrix4.M32] shouldBe (m32 plusOrMinus tolerance)
    values[Matrix4.M33] shouldBe (m33 plusOrMinus tolerance)
  }

  test("should create matrix") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    assertMatrixEquals(
      matrix,
      +1f,
      +2f,
      +3f,
      +4f,
      +5f,
      +6f,
      +7f,
      +8f,
      +9f,
      10f,
      11f,
      12f,
      13f,
      14f,
      15f,
      16f,
    )
  }

  test("should negate matrix values") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    val result = -matrix

    assertMatrixEquals(
      result,
      -1f,
      -2f,
      -3f,
      -4f,
      -5f,
      -6f,
      -7f,
      -8f,
      -9f,
      -10f,
      -11f,
      -12f,
      -13f,
      -14f,
      -15f,
      -16f,
    )
    assertMatrixEquals(
      matrix,
      1f,
      2f,
      3f,
      4f,
      5f,
      6f,
      7f,
      8f,
      9f,
      10f,
      11f,
      12f,
      13f,
      14f,
      15f,
      16f,
    )
  }

  test("should invert matrix") {
    val matrix =
      mat4(
        1f,
        0f,
        1f,
        0f,
        0f,
        1f,
        0f,
        1f,
        0f,
        0f,
        1f,
        0f,
        0f,
        0f,
        0f,
        1f,
      )

    val result = !matrix

    assertMatrixEquals(
      result,
      1f,
      0f,
      -1f,
      0f,
      0f,
      1f,
      0f,
      -1f,
      0f,
      0f,
      1f,
      0f,
      0f,
      0f,
      0f,
      1f,
    )
    assertMatrixEquals(
      matrix,
      1f,
      0f,
      1f,
      0f,
      0f,
      1f,
      0f,
      1f,
      0f,
      0f,
      1f,
      0f,
      0f,
      0f,
      0f,
      1f,
    )
  }

  test("should addAssign matrices") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    matrix +=
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    assertMatrixEquals(
      matrix,
      +2f,
      +4f,
      +6f,
      +8f,
      10f,
      12f,
      14f,
      16f,
      18f,
      20f,
      22f,
      24f,
      26f,
      28f,
      30f,
      32f,
    )
  }

  test("should subtractAssign matrices") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    matrix -=
      mat4(
        16f,
        15f,
        14f,
        13f,
        12f,
        11f,
        10f,
        +9f,
        +8f,
        +7f,
        +6f,
        +5f,
        +4f,
        +3f,
        +2f,
        +1f,
      )

    assertMatrixEquals(
      matrix,
      -15f,
      -13f,
      -11f,
      -9f,
      -7f,
      -5.0f,
      -3f,
      -1f,
      1.0f,
      3.0f,
      5.0f,
      7f,
      9.0f,
      11.0f,
      13f,
      15f,
    )
  }

  test("should multiplyAssign matrices") {
    GdxNativesLoader.load() // Matrix.mul4 is written with native code.

    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    matrix *=
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    assertMatrixEquals(
      matrix,
      1f * 1f + 2f * 5f + 3f * 9f + 4f * 13f,
      1f * 2f + 2f * 6f + 3f * 10f + 4f * 14f,
      1f * 3f + 2f * 7f + 3f * 11f + 4f * 15f,
      1f * 4f + 2f * 8f + 3f * 12f + 4f * 16f,
      5f * 1f + 6f * 5f + 7f * 9f + 8f * 13f,
      5f * 2f + 6f * 6f + 7f * 10f + 8f * 14f,
      5f * 3f + 6f * 7f + 7f * 11f + 8f * 15f,
      5f * 4f + 6f * 8f + 7f * 12f + 8f * 16f,
      9f * 1f + 10f * 5f + 11f * 9f + 12f * 13f,
      9f * 2f + 10f * 6f + 11f * 10f + 12f * 14f,
      9f * 3f + 10f * 7f + 11f * 11f + 12f * 15f,
      9f * 4f + 10f * 8f + 11f * 12f + 12f * 16f,
      13f * 1f + 14f * 5f + 15f * 9f + 16f * 13f,
      13f * 2f + 14f * 6f + 15f * 10f + 16f * 14f,
      13f * 3f + 14f * 7f + 15f * 11f + 16f * 15f,
      13f * 4f + 14f * 8f + 15f * 12f + 16f * 16f,
    )
  }

  test("should multiplyAssign matrices with scalar") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    matrix *= 3f

    assertMatrixEquals(
      matrix,
      +3f,
      +2f,
      +3f,
      +4f,
      +5f,
      18f,
      +7f,
      +8f,
      +9f,
      10f,
      33f,
      12f,
      13f,
      14f,
      15f,
      16f,
    )
  }

  test("should multiplyAssign matrices with Vector3 scalar") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    matrix *= vec3(3f, 4f, 5f)

    assertMatrixEquals(
      matrix,
      +3f,
      +2f,
      +3f,
      +4f,
      +5f,
      24f,
      +7f,
      +8f,
      +9f,
      10f,
      55f,
      12f,
      13f,
      14f,
      15f,
      16f,
    )
  }

  test("should multiplyAssign Vector3 with matrices") {
    val vector = vec3(1f, 2f, 3f)

    vector *=
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    vector.x shouldBe ((1f * 1f + 2f * 2f + 3f * 3f + 1f * 4f) plusOrMinus floatTolerance)
    vector.y shouldBe ((1f * 5f + 2f * 6f + 3f * 7f + 1f * 8f) plusOrMinus floatTolerance)
    vector.z shouldBe ((1f * 9f + 2f * 10f + 3f * 11f + 1f * 12f) plusOrMinus floatTolerance)
  }

  test("should add matrices") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    val result =
      matrix +
        mat4(
          +1f,
          +2f,
          +3f,
          +4f,
          +5f,
          +6f,
          +7f,
          +8f,
          +9f,
          10f,
          11f,
          12f,
          13f,
          14f,
          15f,
          16f,
        )

    assertMatrixEquals(
      result,
      +2f,
      +4f,
      +6f,
      +8f,
      10f,
      12f,
      14f,
      16f,
      18f,
      20f,
      22f,
      24f,
      26f,
      28f,
      30f,
      32f,
    )
  }

  test("should subtract matrices") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    val result =
      matrix -
        mat4(
          16f,
          15f,
          14f,
          13f,
          12f,
          11f,
          10f,
          +9f,
          +8f,
          +7f,
          +6f,
          +5f,
          +4f,
          +3f,
          +2f,
          +1f,
        )

    assertMatrixEquals(
      result,
      -15f,
      -13f,
      -11f,
      -9f,
      -7f,
      -5.0f,
      -3f,
      -1f,
      1.0f,
      3.0f,
      5.0f,
      7f,
      9.0f,
      11.0f,
      13f,
      15f,
    )
  }

  test("should multiply matrices") {
    GdxNativesLoader.load() // Matrix.mul4 is written with native code.

    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    val result =
      matrix *
        mat4(
          +1f,
          +2f,
          +3f,
          +4f,
          +5f,
          +6f,
          +7f,
          +8f,
          +9f,
          10f,
          11f,
          12f,
          13f,
          14f,
          15f,
          16f,
        )

    assertMatrixEquals(
      result,
      1f * 1f + 2f * 5f + 3f * 9f + 4f * 13f,
      1f * 2f + 2f * 6f + 3f * 10f + 4f * 14f,
      1f * 3f + 2f * 7f + 3f * 11f + 4f * 15f,
      1f * 4f + 2f * 8f + 3f * 12f + 4f * 16f,
      5f * 1f + 6f * 5f + 7f * 9f + 8f * 13f,
      5f * 2f + 6f * 6f + 7f * 10f + 8f * 14f,
      5f * 3f + 6f * 7f + 7f * 11f + 8f * 15f,
      5f * 4f + 6f * 8f + 7f * 12f + 8f * 16f,
      9f * 1f + 10f * 5f + 11f * 9f + 12f * 13f,
      9f * 2f + 10f * 6f + 11f * 10f + 12f * 14f,
      9f * 3f + 10f * 7f + 11f * 11f + 12f * 15f,
      9f * 4f + 10f * 8f + 11f * 12f + 12f * 16f,
      13f * 1f + 14f * 5f + 15f * 9f + 16f * 13f,
      13f * 2f + 14f * 6f + 15f * 10f + 16f * 14f,
      13f * 3f + 14f * 7f + 15f * 11f + 16f * 15f,
      13f * 4f + 14f * 8f + 15f * 12f + 16f * 16f,
    )
  }

  test("should multiply matrices with scalar") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    val result = matrix * 3f

    assertMatrixEquals(
      result,
      +3f,
      +2f,
      +3f,
      +4f,
      +5f,
      18f,
      +7f,
      +8f,
      +9f,
      10f,
      33f,
      12f,
      13f,
      14f,
      15f,
      16f,
    )
  }

  test("should multiply matrices with Vector3") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    val result = matrix * vec3(3f, 4f, 5f)

    result.x shouldBe ((3f * 1f + 4f * 2f + 5f * 3f + 1f * 4f) plusOrMinus floatTolerance)
    result.y shouldBe ((3f * 5f + 4f * 6f + 5f * 7f + 1f * 8f) plusOrMinus floatTolerance)
    result.z shouldBe ((3f * 9f + 4f * 10f + 5f * 11f + 1f * 12f) plusOrMinus floatTolerance)
  }

  test("should destruct matrices into sixteen floats") {
    val matrix =
      mat4(
        +1f,
        +2f,
        +3f,
        +4f,
        +5f,
        +6f,
        +7f,
        +8f,
        +9f,
        10f,
        11f,
        12f,
        13f,
        14f,
        15f,
        16f,
      )

    val (
      x0y0, x0y1, x0y2, x0y3,
      x1y0, x1y1, x1y2, x1y3,
      x2y0, x2y1, x2y2, x2y3,
      x3y0, x3y1, x3y2, x3y3,
    ) = matrix

    x0y0 shouldBe (1f plusOrMinus floatTolerance)
    x0y1 shouldBe (2f plusOrMinus floatTolerance)
    x0y2 shouldBe (3f plusOrMinus floatTolerance)
    x0y3 shouldBe (4f plusOrMinus floatTolerance)

    x1y0 shouldBe (5f plusOrMinus floatTolerance)
    x1y1 shouldBe (6f plusOrMinus floatTolerance)
    x1y2 shouldBe (7f plusOrMinus floatTolerance)
    x1y3 shouldBe (8f plusOrMinus floatTolerance)

    x2y0 shouldBe (9f plusOrMinus floatTolerance)
    x2y1 shouldBe (10f plusOrMinus floatTolerance)
    x2y2 shouldBe (11f plusOrMinus floatTolerance)
    x2y3 shouldBe (12f plusOrMinus floatTolerance)

    x3y0 shouldBe (13f plusOrMinus floatTolerance)
    x3y1 shouldBe (14f plusOrMinus floatTolerance)
    x3y2 shouldBe (15f plusOrMinus floatTolerance)
    x3y3 shouldBe (16f plusOrMinus floatTolerance)
  }
})
