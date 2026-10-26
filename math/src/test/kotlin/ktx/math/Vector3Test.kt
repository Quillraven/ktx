package ktx.math

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

/**
 * Tests [Vector3]-related utilities.
 */
class Vector3Test : FunSpec({
  val floatTolerance = 0.00001f

  test("should create vector with default values") {
    val zero = vec3()

    zero.x shouldBe (0f plusOrMinus floatTolerance)
    zero.y shouldBe (0f plusOrMinus floatTolerance)
    zero.z shouldBe (0f plusOrMinus floatTolerance)
  }

  test("should create vector") {
    val vector = vec3(x = 10f, y = -10f, z = 5f)

    vector.x shouldBe (10f plusOrMinus floatTolerance)
    vector.y shouldBe (-10f plusOrMinus floatTolerance)
    vector.z shouldBe (5f plusOrMinus floatTolerance)
  }

  test("should create Vector3 with Vector2 instance") {
    val vector = vec3(Vector2(10f, 5f))

    vector.x shouldBe (10f plusOrMinus floatTolerance)
    vector.y shouldBe (5f plusOrMinus floatTolerance)
    vector.z shouldBe (0f plusOrMinus floatTolerance)
  }

  test("should create Vector3 with Vector2 instance and Z component") {
    val vector = vec3(Vector2(10f, 5f), z = -15f)

    vector.x shouldBe (10f plusOrMinus floatTolerance)
    vector.y shouldBe (5f plusOrMinus floatTolerance)
    vector.z shouldBe (-15f plusOrMinus floatTolerance)
  }

  test("should invert values with unary - operator") {
    val originalVector = Vector3(10f, 10f, -10f)

    val vector = -originalVector

    vector.x shouldBe (-10f plusOrMinus floatTolerance)
    vector.y shouldBe (-10f plusOrMinus floatTolerance)
    vector.z shouldBe (10f plusOrMinus floatTolerance)
    originalVector.x shouldBe (10f plusOrMinus floatTolerance)
    originalVector.y shouldBe (10f plusOrMinus floatTolerance)
    originalVector.z shouldBe (-10f plusOrMinus floatTolerance)
  }

  test("should add vectors with += operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector += Vector3(20f, -20f, -10f)

    vector.x shouldBe (30f plusOrMinus floatTolerance)
    vector.y shouldBe (-10f plusOrMinus floatTolerance)
    vector.z shouldBe (0f plusOrMinus floatTolerance)
  }

  test("should add Vector2 with += operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector += Vector2(20f, -20f)

    vector.x shouldBe (30f plusOrMinus floatTolerance)
    vector.y shouldBe (-10f plusOrMinus floatTolerance)
    vector.z shouldBe (10f plusOrMinus floatTolerance)
  }

  test("should add floats to vectors with += operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector += 10f

    vector.x shouldBe (20f plusOrMinus floatTolerance)
    vector.y shouldBe (20f plusOrMinus floatTolerance)
    vector.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should add ints to vectors with += operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector += 10

    vector.x shouldBe (20f plusOrMinus floatTolerance)
    vector.y shouldBe (20f plusOrMinus floatTolerance)
    vector.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should subtract vectors with -= operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector -= Vector3(20f, -20f, -10f)

    vector.x shouldBe (-10f plusOrMinus floatTolerance)
    vector.y shouldBe (30f plusOrMinus floatTolerance)
    vector.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should subtract Vector2 with -= operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector -= Vector2(20f, -20f)

    vector.x shouldBe (-10f plusOrMinus floatTolerance)
    vector.y shouldBe (30f plusOrMinus floatTolerance)
    vector.z shouldBe (10f plusOrMinus floatTolerance)
  }

  test("should subtract floats from vectors with += operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector -= 20f

    vector.x shouldBe (-10f plusOrMinus floatTolerance)
    vector.y shouldBe (-10f plusOrMinus floatTolerance)
    vector.z shouldBe (-10f plusOrMinus floatTolerance)
  }

  test("should subtract ints from vectors with += operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector -= 20f

    vector.x shouldBe (-10f plusOrMinus floatTolerance)
    vector.y shouldBe (-10f plusOrMinus floatTolerance)
    vector.z shouldBe (-10f plusOrMinus floatTolerance)
  }

  test("should multiply vectors with timesAssign operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector *= Vector3(3f, -1f, 0.5f)

    vector.x shouldBe (30f plusOrMinus floatTolerance)
    vector.y shouldBe (-10f plusOrMinus floatTolerance)
    vector.z shouldBe (5f plusOrMinus floatTolerance)
  }

  test("should multiply vectors by float scalars with timesAssign operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector *= 2.5f

    vector.x shouldBe (25f plusOrMinus floatTolerance)
    vector.y shouldBe (25f plusOrMinus floatTolerance)
    vector.z shouldBe (25f plusOrMinus floatTolerance)
  }

  test("should multiply vectors by int scalars with timesAssign operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector *= 2

    vector.x shouldBe (20f plusOrMinus floatTolerance)
    vector.y shouldBe (20f plusOrMinus floatTolerance)
    vector.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should divide vectors with divAssign operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector /= Vector3(2f, -5f, 0.5f)

    vector.x shouldBe (5f plusOrMinus floatTolerance)
    vector.y shouldBe (-2f plusOrMinus floatTolerance)
    vector.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should divide vectors by float scalars with divAssign operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector /= 2.5f

    vector.x shouldBe (4f plusOrMinus floatTolerance)
    vector.y shouldBe (4f plusOrMinus floatTolerance)
    vector.z shouldBe (4f plusOrMinus floatTolerance)
  }

  test("should divide vectors by int scalars with divAssign operator") {
    val vector = Vector3(10f, 10f, 10f)

    vector /= 2

    vector.x shouldBe (5f plusOrMinus floatTolerance)
    vector.y shouldBe (5f plusOrMinus floatTolerance)
    vector.z shouldBe (5f plusOrMinus floatTolerance)
  }

  test("should add vectors with + operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector + Vector3(20f, -20f, -10f)

    result.x shouldBe (30f plusOrMinus floatTolerance)
    result.y shouldBe (-10f plusOrMinus floatTolerance)
    result.z shouldBe (0f plusOrMinus floatTolerance)
  }

  test("should add Vector2 with + operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector + Vector2(20f, -20f)

    result.x shouldBe (30f plusOrMinus floatTolerance)
    result.y shouldBe (-10f plusOrMinus floatTolerance)
    result.z shouldBe (10f plusOrMinus floatTolerance)
  }

  test("should add vectors and floats with + operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector + 10f

    result.x shouldBe (20f plusOrMinus floatTolerance)
    result.y shouldBe (20f plusOrMinus floatTolerance)
    result.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should add vectors and ints with + operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector + 10

    result.x shouldBe (20f plusOrMinus floatTolerance)
    result.y shouldBe (20f plusOrMinus floatTolerance)
    result.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should subtract vectors with - operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector - Vector3(20f, -20f, -10f)

    result.x shouldBe (-10f plusOrMinus floatTolerance)
    result.y shouldBe (30f plusOrMinus floatTolerance)
    result.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should subtract Vector2 with - operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector - Vector2(20f, -20f)

    result.x shouldBe (-10f plusOrMinus floatTolerance)
    result.y shouldBe (30f plusOrMinus floatTolerance)
    result.z shouldBe (10f plusOrMinus floatTolerance)
  }

  test("should subtract vectors and floats with - operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector - 20f

    result.x shouldBe (-10f plusOrMinus floatTolerance)
    result.y shouldBe (-10f plusOrMinus floatTolerance)
    result.z shouldBe (-10f plusOrMinus floatTolerance)
  }

  test("should subtract vectors and ints with - operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector - 20

    result.x shouldBe (-10f plusOrMinus floatTolerance)
    result.y shouldBe (-10f plusOrMinus floatTolerance)
    result.z shouldBe (-10f plusOrMinus floatTolerance)
  }

  test("should multiply vectors with times operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector * Vector3(3f, -1f, 0.5f)

    result.x shouldBe (30f plusOrMinus floatTolerance)
    result.y shouldBe (-10f plusOrMinus floatTolerance)
    result.z shouldBe (5f plusOrMinus floatTolerance)
  }

  test("should divide vectors with div operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector / Vector3(2f, -5f, 0.5f)

    result.x shouldBe (5f plusOrMinus floatTolerance)
    result.y shouldBe (-2f plusOrMinus floatTolerance)
    result.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should multiply vectors by int scalars with times operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector * 2

    result.x shouldBe (20f plusOrMinus floatTolerance)
    result.y shouldBe (20f plusOrMinus floatTolerance)
    result.z shouldBe (20f plusOrMinus floatTolerance)
  }

  test("should divide vectors by int scalars with div operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector / 2

    result.x shouldBe (5f plusOrMinus floatTolerance)
    result.y shouldBe (5f plusOrMinus floatTolerance)
    result.z shouldBe (5f plusOrMinus floatTolerance)
  }

  test("should multiply vectors by float scalars with times operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector * 2.5f

    result.x shouldBe (25f plusOrMinus floatTolerance)
    result.y shouldBe (25f plusOrMinus floatTolerance)
    result.z shouldBe (25f plusOrMinus floatTolerance)
  }

  test("should divide vectors by float scalars with div operator") {
    val vector = Vector3(10f, 10f, 10f)

    val result = vector / 2.5f

    result.x shouldBe (4f plusOrMinus floatTolerance)
    result.y shouldBe (4f plusOrMinus floatTolerance)
    result.z shouldBe (4f plusOrMinus floatTolerance)
  }

  test("should increment vector values with ++ operator") {
    val originalVector = Vector3(10f, 10f, 10f)
    var vector = originalVector

    vector++

    vector.x shouldBe (11f plusOrMinus floatTolerance)
    vector.y shouldBe (11f plusOrMinus floatTolerance)
    vector.z shouldBe (11f plusOrMinus floatTolerance)
    originalVector.x shouldBe (10f plusOrMinus floatTolerance)
    originalVector.y shouldBe (10f plusOrMinus floatTolerance)
    originalVector.z shouldBe (10f plusOrMinus floatTolerance)
  }

  test("should decrement vector values with -- operator") {
    val originalVector = Vector3(10f, 10f, 10f)
    var vector = originalVector

    vector--

    vector.x shouldBe (9f plusOrMinus floatTolerance)
    vector.y shouldBe (9f plusOrMinus floatTolerance)
    vector.z shouldBe (9f plusOrMinus floatTolerance)
    originalVector.x shouldBe (10f plusOrMinus floatTolerance)
    originalVector.y shouldBe (10f plusOrMinus floatTolerance)
    originalVector.z shouldBe (10f plusOrMinus floatTolerance)
  }

  test("should destruct vector into three floats") {
    val (x, y, z) = Vector3(10f, 20f, 30f)

    x shouldBe (10f plusOrMinus floatTolerance)
    y shouldBe (20f plusOrMinus floatTolerance)
    z shouldBe (30f plusOrMinus floatTolerance)
  }

  test("should compare vectors by length") {
    val vec1 = Vector3(10f, 10f, 10f)
    val vec2 = Vector3(10f, -20f, 10f) // This vector has the greatest overall length.
    val vec3 = Vector3(10f, 10f, 10f)

    (vec1 < vec2) shouldBe true
    (vec1 <= vec2) shouldBe true
    (vec1 > vec2) shouldBe false
    (vec1 >= vec2) shouldBe false

    (vec1 == vec3) shouldBe true
    (vec1 >= vec3) shouldBe true
    (vec1 <= vec3) shouldBe true
    (vec1 < vec3) shouldBe false
    (vec1 > vec3) shouldBe false
  }

  test("should be the dot product of two vectors") {
    val vec1 = Vector3(2f, 1f, 3f)
    val vec2 = Vector3(3f, -4f, -3f)

    val result = vec1 dot vec2

    result shouldBe (-7f plusOrMinus floatTolerance)
  }

  test("should be the cross product of two vectors") {
    val vec1 = Vector3(2f, 1f, 3f)
    val vec2 = Vector3(3f, -4f, -3f)

    val result = vec1 x vec2

    result.x shouldBe (9f plusOrMinus floatTolerance)
    result.y shouldBe (15f plusOrMinus floatTolerance)
    result.z shouldBe (-11f plusOrMinus floatTolerance)
  }
})
