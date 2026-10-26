package ktx.math

import com.badlogic.gdx.math.Matrix3
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

/**
 * Tests [Matrix3]-related utilities.
 */
class Matrix3Test : FunSpec({
  val floatTolerance = 0.00001f

  fun assertMatrixEquals(
    matrix: Matrix3,
    m00: Float,
    m01: Float,
    m02: Float,
    m10: Float,
    m11: Float,
    m12: Float,
    m20: Float,
    m21: Float,
    m22: Float,
    tolerance: Float = floatTolerance,
  ) {
    val values = matrix.`val`
    values[Matrix3.M00] shouldBe (m00 plusOrMinus tolerance)
    values[Matrix3.M01] shouldBe (m01 plusOrMinus tolerance)
    values[Matrix3.M02] shouldBe (m02 plusOrMinus tolerance)
    values[Matrix3.M10] shouldBe (m10 plusOrMinus tolerance)
    values[Matrix3.M11] shouldBe (m11 plusOrMinus tolerance)
    values[Matrix3.M12] shouldBe (m12 plusOrMinus tolerance)
    values[Matrix3.M20] shouldBe (m20 plusOrMinus tolerance)
    values[Matrix3.M21] shouldBe (m21 plusOrMinus tolerance)
    values[Matrix3.M22] shouldBe (m22 plusOrMinus tolerance)
  }

  test("should create matrix") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
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
    )
  }

  test("should negate matrix values") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
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
    )
  }

  test("should invert matrix") {
    val matrix =
      mat3(
        1f,
        2f,
        1f,
        0f,
        1f,
        1f,
        0f,
        0f,
        1f,
      )

    val result = !matrix

    assertMatrixEquals(
      result,
      +1f,
      -2f,
      +1f,
      +0f,
      +1f,
      -1f,
      +0f,
      +0f,
      +1f,
    )
    assertMatrixEquals(
      matrix,
      1f,
      2f,
      1f,
      0f,
      1f,
      1f,
      0f,
      0f,
      1f,
    )
  }

  test("should addAssign matrices") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    matrix +=
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
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
    )
  }

  test("should subtractAssign matrices") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    matrix -=
      mat3(
        9f,
        8f,
        7f,
        6f,
        5f,
        4f,
        3f,
        2f,
        1f,
      )

    assertMatrixEquals(
      matrix,
      -8f,
      -6f,
      -4f,
      -2f,
      +0f,
      +2f,
      +4f,
      +6f,
      +8f,
    )
  }

  test("should multiplyAssign matrices") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    matrix *=
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    assertMatrixEquals(
      matrix,
      1f * 1f + 2f * 4f + 3f * 7f,
      1f * 2f + 2f * 5f + 3f * 8f,
      1f * 3f + 2f * 6f + 3f * 9f,
      4f * 1f + 5f * 4f + 6f * 7f,
      4f * 2f + 5f * 5f + 6f * 8f,
      4f * 3f + 5f * 6f + 6f * 9f,
      7f * 1f + 8f * 4f + 9f * 7f,
      7f * 2f + 8f * 5f + 9f * 8f,
      7f * 3f + 8f * 6f + 9f * 9f,
    )
  }

  test("should multiplyAssign matrices with scalar") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    matrix *= 3f

    assertMatrixEquals(
      matrix,
      3f,
      +2f,
      3f,
      4f,
      15f,
      6f,
      7f,
      +8f,
      9f,
    )
  }

  test("should multiply matricesAssign with Vector2 scale") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    matrix *= vec2(3f, 2f / 5f)

    assertMatrixEquals(
      matrix,
      3f,
      2f,
      3f,
      4f,
      2f,
      6f,
      7f,
      8f,
      9f,
    )
  }

  test("should multiplyAssign matrices with Vector3 scale") {
    val matrix =
      mat3(
        3f,
        2f,
        3f,
        4f,
        2f,
        6f,
        7f,
        8f,
        9f,
      )

    matrix *= vec3(3f, 2f, 0f) // Last value is ignored.

    assertMatrixEquals(
      matrix,
      9f,
      2f,
      3f,
      4f,
      4f,
      6f,
      7f,
      8f,
      9f,
    )
  }

  test("should multiplyAssign Vector2 with matrices") {
    val vector = vec2(1f, 2f)

    vector *=
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    vector.x shouldBe ((1f * 1f + 2f * 2f + 1f * 3f) plusOrMinus floatTolerance)
    vector.y shouldBe ((1f * 4f + 2f * 5f + 1f * 6f) plusOrMinus floatTolerance)
  }

  test("should add matrices") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    val result =
      matrix +
        mat3(
          1f,
          2f,
          3f,
          4f,
          5f,
          6f,
          7f,
          8f,
          9f,
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
    )
  }

  test("should subtract matrices") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    val result =
      matrix -
        mat3(
          9f,
          8f,
          7f,
          6f,
          5f,
          4f,
          3f,
          2f,
          1f,
        )

    assertMatrixEquals(
      result,
      -8f,
      -6f,
      -4f,
      -2f,
      +0f,
      +2f,
      +4f,
      +6f,
      +8f,
    )
  }

  test("should multiply matrices") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    val result =
      matrix *
        mat3(
          1f,
          2f,
          3f,
          4f,
          5f,
          6f,
          7f,
          8f,
          9f,
        )

    assertMatrixEquals(
      result,
      1f * 1f + 2f * 4f + 3f * 7f,
      1f * 2f + 2f * 5f + 3f * 8f,
      1f * 3f + 2f * 6f + 3f * 9f,
      4f * 1f + 5f * 4f + 6f * 7f,
      4f * 2f + 5f * 5f + 6f * 8f,
      4f * 3f + 5f * 6f + 6f * 9f,
      7f * 1f + 8f * 4f + 9f * 7f,
      7f * 2f + 8f * 5f + 9f * 8f,
      7f * 3f + 8f * 6f + 9f * 9f,
    )
  }

  test("should multiply matrices with scalar") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    val result = matrix * 3f

    assertMatrixEquals(
      result,
      3f,
      +2f,
      3f,
      4f,
      15f,
      6f,
      7f,
      +8f,
      9f,
    )
  }

  test("should multiply matrices with Vector2") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    val result = matrix * vec2(3f, 2f)

    result.x shouldBe ((3f * 1f + 2f * 2f + 1f * 3f) plusOrMinus floatTolerance)
    result.y shouldBe ((3f * 4f + 2f * 5f + 1f * 6f) plusOrMinus floatTolerance)
  }

  test("should destruct matrices into nine floats") {
    val matrix =
      mat3(
        1f,
        2f,
        3f,
        4f,
        5f,
        6f,
        7f,
        8f,
        9f,
      )

    val (
      x0y0, x0y1, x0y2,
      x1y0, x1y1, x1y2,
      x2y0, x2y1, x2y2,
    ) = matrix

    x0y0 shouldBe (1f plusOrMinus floatTolerance)
    x0y1 shouldBe (2f plusOrMinus floatTolerance)
    x0y2 shouldBe (3f plusOrMinus floatTolerance)

    x1y0 shouldBe (4f plusOrMinus floatTolerance)
    x1y1 shouldBe (5f plusOrMinus floatTolerance)
    x1y2 shouldBe (6f plusOrMinus floatTolerance)

    x2y0 shouldBe (7f plusOrMinus floatTolerance)
    x2y1 shouldBe (8f plusOrMinus floatTolerance)
    x2y2 shouldBe (9f plusOrMinus floatTolerance)
  }
})
