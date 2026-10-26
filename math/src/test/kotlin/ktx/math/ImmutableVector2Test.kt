package ktx.math

import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAtLeastOne
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.HashSet
import java.util.Random
import kotlin.math.sqrt

class ImmutableVector2Test : FunSpec({
  /** List of vector to use in tests */
  val vectors =
    sequenceOf(
      // vector zero
      ImmutableVector2.ZERO,
      // axis
      ImmutableVector2.X,
      -ImmutableVector2.X,
      ImmutableVector2.Y,
      -ImmutableVector2.Y,
      // quadrants
      ImmutableVector2(3f, 4f),
      ImmutableVector2(3f, -4f),
      ImmutableVector2(-3f, 4f),
      ImmutableVector2(-3f, -4f),
      // small vectors
      ImmutableVector2(0.001f, 0f),
      ImmutableVector2(0f, 0.001f),
      ImmutableVector2(0f, -0.001f),
    )

  /** List scalar values to use in tests */
  val scalars = sequenceOf(0f, Float.MIN_VALUE, 0.42f, 1f, 42f)
  val interpolations =
    sequenceOf(
      Interpolation.bounce,
      Interpolation.bounceIn,
      Interpolation.bounceOut,
      Interpolation.circle,
      Interpolation.circleIn,
      Interpolation.circleOut,
      Interpolation.pow2,
      Interpolation.pow2In,
      Interpolation.pow2Out,
      Interpolation.smooth,
      Interpolation.smooth2,
    )

  test("equals should return true for equivalent vectors") {
    ImmutableVector2.X shouldBe ImmutableVector2.X
    ImmutableVector2.Y shouldBe ImmutableVector2.Y
    ImmutableVector2.ZERO shouldBe ImmutableVector2.ZERO
    ImmutableVector2(-3f, 42f) shouldBe ImmutableVector2(-3f, 42f)

    vectors.forEach {
      it shouldBe it
    }
  }

  test("hashCode should return same hashCode for equivalent vectors") {
    ImmutableVector2.X.hashCode() shouldBe ImmutableVector2.X.hashCode()
    ImmutableVector2.Y.hashCode() shouldBe ImmutableVector2.Y.hashCode()
    ImmutableVector2.ZERO.hashCode() shouldBe ImmutableVector2.ZERO.hashCode()
    ImmutableVector2(-3f, 42f).hashCode() shouldBe ImmutableVector2(-3f, 42f).hashCode()

    vectors.forEach {
      it.hashCode() shouldBe it.hashCode()
    }
  }

  test("equals should return false for different vectors") {
    ImmutableVector2.Y shouldNotBe ImmutableVector2.X
    ImmutableVector2.X shouldNotBe ImmutableVector2.Y
    ImmutableVector2.X shouldNotBe ImmutableVector2.ZERO
    ImmutableVector2.Y shouldNotBe ImmutableVector2.ZERO
    ImmutableVector2(-3f, 42f) shouldNotBe ImmutableVector2(42f, -3f)

    vectors.forEach { v1 ->
      vectors.filterNot { v1 === it }.forEach { v2 ->
        v2 shouldNotBe v1
      }
    }
  }

  test("equals should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v2 == v1) shouldBe (v1 == v2)
      }
    }
  }

  test("toMutable should return the equivalent Vector2") {
    vectors.forEach {
      val vector2 = it.toMutable()
      vector2.x shouldBe it.x
      vector2.y shouldBe it.y
      vector2 shouldBe Vector2(it.x, it.y)
    }
  }

  test("toImmutable should return an equivalent of the Vector2") {
    vectors.forEach {
      val vector2 = Vector2(it.x, it.y).toImmutable()
      vector2.x shouldBe it.x
      vector2.y shouldBe it.y
      vector2 shouldBe it
    }
  }

  test("fromString should parse result toString") {
    vectors.forEach {
      ImmutableVector2.fromString(it.toString()) shouldBe it
      ImmutableVector2.fromString(it.toMutable().toString()) shouldBe it
    }
  }

  test("len should return same value as Vector2") {
    vectors.forEach {
      it.len shouldBe it.toMutable().len()
    }
  }

  test("len should return length") {
    ImmutableVector2.X.len shouldBe 1.0f
    ImmutableVector2.Y.len shouldBe 1.0f
    ImmutableVector2.ZERO.len shouldBe 0.0f
    ImmutableVector2(-3.0f, 4.0f).len shouldBe 5.0f
  }

  test("len2 should return same value as Vector2") {
    vectors.forEach {
      it.len2 shouldBe it.toMutable().len2()
    }
  }

  test("len2 should return squared length") {
    ImmutableVector2.X.len2 shouldBe 1.0f
    ImmutableVector2.Y.len2 shouldBe 1.0f
    ImmutableVector2.ZERO.len2 shouldBe 0.0f
    ImmutableVector2(-3.0f, 4.0f).len2 shouldBe 25.0f
  }

  test("isZero should returns same result than Vector2") {
    vectors.forEach { vector ->
      vector.isZero() shouldBe vector.toMutable().isZero
      scalars.filter { it > 0f }.forEach { margin ->
        vector.isZero(margin) shouldBe vector.toMutable().isZero(margin)
      }
    }
  }

  test("isZero should return true for vector zero") {
    ImmutableVector2.ZERO.isZero() shouldBe true
  }

  test("isZero should return false for non-zero vectors") {
    ImmutableVector2.X.isZero() shouldBe false
    (-ImmutableVector2.X).isZero() shouldBe false
    ImmutableVector2.Y.isZero() shouldBe false
    (-ImmutableVector2.Y).isZero() shouldBe false
    ImmutableVector2(3f, 4f).isZero() shouldBe false
  }

  test("isUnit with margin should return same value as Vector2") {
    vectors.forEach { vector ->
      vector.isUnit() shouldBe vector.toMutable().isUnit
      scalars.filter { it > 0f }.forEach { margin ->
        withClue("$vector, $margin") { vector.isUnit(margin) shouldBe vector.toMutable().isUnit(margin) }
      }
    }
  }

  test("isUnit should return true for axis") {
    ImmutableVector2.X.isUnit() shouldBe true
    (-ImmutableVector2.X).isUnit() shouldBe true
    ImmutableVector2.Y.isUnit() shouldBe true
    (-ImmutableVector2.Y).isUnit() shouldBe true
  }

  test("isUnit should return false for non-unit vectors") {
    ImmutableVector2.ZERO.isUnit() shouldBe false
    ImmutableVector2(-3.0f, 4.0f).isUnit() shouldBe false
    ImmutableVector2(0.5f, 0.5f).isUnit() shouldBe false
  }

  test("nor should return same value as Vector2") {
    vectors.forEach {
      it.nor shouldBe it.toMutable().nor().toImmutable()
    }
  }

  test("nor should return normalized vectors") {
    ImmutableVector2(0.4f, 0f).nor shouldBe ImmutableVector2.X
    ImmutableVector2(3f, 0f).nor shouldBe ImmutableVector2.X
    ImmutableVector2.ZERO.nor shouldBe ImmutableVector2.ZERO
    ImmutableVector2(3f, -4f).nor shouldBe ImmutableVector2(3f / 5f, -4f / 5f)
  }

  test("nor should return unit vectors") {
    vectors.forEach {
      (it.isZero() || it.nor.isUnit()) shouldBe true
    }
  }

  test("unaryMinus should return same value as Vector2 scaled by -1") {
    vectors.forEach {
      -it shouldBe it.toMutable().scl(-1f).toImmutable()
    }
  }

  test("minus should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v1 - v2) shouldBe v1.toMutable().minus(v2.toMutable()).toImmutable()
      }
    }
  }

  test("minus should return difference between the two vectors") {
    (ImmutableVector2.X - ImmutableVector2.X) shouldBe ImmutableVector2.ZERO
    (-ImmutableVector2.X - ImmutableVector2.X) shouldBe ImmutableVector2(-2f, -0f)
    (ImmutableVector2.Y - ImmutableVector2.X) shouldBe ImmutableVector2(-1f, 1f)
    (ImmutableVector2(3f, 4f) - ImmutableVector2(2f, 5f)) shouldBe ImmutableVector2(1f, -1f)
  }

  test("plus should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v1 + v2) shouldBe v1.toMutable().plus(v2.toMutable()).toImmutable()
      }
    }
  }

  test("plus should return sum of the two vectors") {
    (ImmutableVector2.X + ImmutableVector2.X) shouldBe ImmutableVector2(2f, 0f)
    ImmutableVector2.X.plus(1f, 0f) shouldBe ImmutableVector2(2f, 0f)

    ((-ImmutableVector2.X) + ImmutableVector2.X) shouldBe ImmutableVector2.ZERO
    (-ImmutableVector2.X).plus(1f, 0f) shouldBe ImmutableVector2.ZERO

    (ImmutableVector2.Y + ImmutableVector2.X) shouldBe ImmutableVector2(1f, 1f)
    ImmutableVector2.Y.plus(1f, 0f) shouldBe ImmutableVector2(1f, 1f)

    (ImmutableVector2(3f, 4f) + ImmutableVector2(2f, 5f)) shouldBe ImmutableVector2(5f, 9f)
    ImmutableVector2(3f, 4f).plus(2f, 5f) shouldBe ImmutableVector2(5f, 9f)
  }

  test("plus should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v2 + v1) shouldBe (v1 + v2)
      }
    }
  }

  test("times should return same value as Vector2 scale") {
    scalars.forEach { factor ->
      vectors.forEach { vector ->
        (vector * factor) shouldBe vector.toMutable().scl(factor).toImmutable()
      }
    }
  }

  test("times should return scaled vector") {
    (ImmutableVector2.ZERO * 42f) shouldBe ImmutableVector2.ZERO
    (ImmutableVector2.Y * 2f) shouldBe ImmutableVector2(0f, 2f)
    (ImmutableVector2(5f, -2f) * 3f) shouldBe ImmutableVector2(15f, -6f)
    (ImmutableVector2(5f, -2f) * 0.5f) shouldBe ImmutableVector2(2.5f, -1f)
  }

  test("times should return same value as scl does for Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v1 * v2) shouldBe v1.toMutable().scl(v2.toMutable()).toImmutable()
        v1.times(v2.x, v2.y) shouldBe v1.toMutable().scl(v2.x, v2.y).toImmutable()
      }
    }
  }

  test("times should return scaled vector on both axis") {
    (ImmutableVector2.X * ImmutableVector2.ZERO) shouldBe ImmutableVector2.ZERO
    ImmutableVector2.X.times(0f, 0f) shouldBe ImmutableVector2.ZERO

    (ImmutableVector2.X * ImmutableVector2.Y) shouldBe ImmutableVector2.ZERO
    ImmutableVector2.X.times(0f, 1f) shouldBe ImmutableVector2.ZERO

    (ImmutableVector2(-2f, 3f) * ImmutableVector2(-2f, -5f)) shouldBe ImmutableVector2(4f, -15f)
    ImmutableVector2(-2f, 3f).times(-2f, -5f) shouldBe ImmutableVector2(4f, -15f)

    (ImmutableVector2(2f, 3f) * ImmutableVector2(2f, 5f)) shouldBe ImmutableVector2(4f, 15f)
    ImmutableVector2(2f, 3f).times(2f, 5f) shouldBe ImmutableVector2(4f, 15f)
  }

  test("dot should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v1 dot v2) shouldBe v1.toMutable().dot(v2.toMutable())
        v1.dot(v2.x, v2.y) shouldBe v1.toMutable().dot(v2.x, v2.y)
      }
    }
  }

  test("dot should return dot product") {
    (ImmutableVector2.X dot ImmutableVector2.X) shouldBe 1f
    ImmutableVector2.X.dot(1f, 0f) shouldBe 1f

    (ImmutableVector2.Y dot ImmutableVector2(0f, 1f)) shouldBe 1f
    ImmutableVector2.Y.dot(0f, 1f) shouldBe 1f

    (ImmutableVector2.X dot ImmutableVector2(0f, 1f)) shouldBe 0f
    ImmutableVector2.X.dot(0f, 1f) shouldBe 0f

    (ImmutableVector2.X dot ImmutableVector2(0f, 0f)) shouldBe 0f
    ImmutableVector2.X.dot(0f, 0f) shouldBe 0f

    (ImmutableVector2(1f, 1f) dot ImmutableVector2(1f, 1f)) shouldBe 2f
    ImmutableVector2(1f, 1f).dot(1f, 1f) shouldBe 2f

    (ImmutableVector2(-2f, 3f) dot ImmutableVector2(4f, 5f)) shouldBe 7f
    ImmutableVector2(-2f, 3f).dot(4f, 5f) shouldBe 7f
  }

  test("dot should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v2 dot v1) shouldBe (v1 dot v2)
      }
    }
  }

  test("dst2 should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.dst2(v2) shouldBe v1.toMutable().dst2(v2.toMutable())
        v1.dst2(v2.x, v2.y) shouldBe v1.toMutable().dst2(v2.x, v2.y)
      }
    }
  }

  test("dst2 should return squared distance") {
    (ImmutableVector2.ZERO dst2 ImmutableVector2.X) shouldBe 1f
    ImmutableVector2.ZERO.dst2(1f, 0f) shouldBe 1f

    (ImmutableVector2.X dst2 ImmutableVector2.Y) shouldBe 2f
    ImmutableVector2.X.dst2(0f, 1f) shouldBe 2f

    (ImmutableVector2.ZERO dst2 ImmutableVector2(3f, 4f)) shouldBe 25f
    ImmutableVector2.ZERO.dst2(3f, 4f) shouldBe 25f

    (ImmutableVector2(2f, -3f) dst2 ImmutableVector2(3f, 4f)) shouldBe 50f
    ImmutableVector2(2f, -3f).dst2(3f, 4f) shouldBe 50f
  }

  test("dst2 should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v2 dst2 v1) shouldBe (v1 dst2 v2)
      }
    }
  }

  test("dst should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.dst(v2) shouldBe v1.toMutable().dst(v2.toMutable())
        v1.dst(v2.x, v2.y) shouldBe v1.toMutable().dst(v2.x, v2.y)
      }
    }
  }

  test("dst should return squared distance") {
    (ImmutableVector2.ZERO dst ImmutableVector2.X) shouldBe 1f
    ImmutableVector2.ZERO.dst(1f, 0f) shouldBe 1f

    (ImmutableVector2.X dst ImmutableVector2.Y) shouldBe sqrt(2f)
    ImmutableVector2.X.dst(0f, 1f) shouldBe sqrt(2f)

    (ImmutableVector2.ZERO dst ImmutableVector2(3f, 4f)) shouldBe 5f
    ImmutableVector2.ZERO.dst(3f, 4f) shouldBe 5f

    (ImmutableVector2(2f, -3f) dst ImmutableVector2(3f, 4f)) shouldBe sqrt(50f)
    ImmutableVector2(2f, -3f).dst(3f, 4f) shouldBe sqrt(50f)
  }

  test("dst should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v2 dst v1) shouldBe (v1 dst v2)
      }
    }
  }

  test("withLimit should return same value as Vector2") {
    scalars.forEach { l ->
      vectors.forEach { v ->
        v.withLimit(l) shouldBe v.toMutable().limit(l).toImmutable()
      }
    }
  }

  test("withLimit should return a vector within the limit") {
    ImmutableVector2.ZERO.withLimit(42f) shouldBe ImmutableVector2.ZERO
    ImmutableVector2(42f, 0f).withLimit(1f) shouldBe ImmutableVector2.X
    ImmutableVector2(0.5f, 0f).withLimit(1f) shouldBe ImmutableVector2(0.5f, 0f)
    ImmutableVector2(6f, -8f).withLimit(5f) shouldBe ImmutableVector2(3f, -4f)
    ImmutableVector2(2f, -3f).withLimit(5f) shouldBe ImmutableVector2(2f, -3f)
  }

  test("withLimit2 should return same value as Vector2") {
    scalars.forEach { l ->
      vectors.forEach { v ->
        v.withLimit2(l) shouldBe v.toMutable().limit2(l).toImmutable()
      }
    }
  }

  test("withLimit2 should return a vector within the limit") {
    ImmutableVector2.ZERO.withLimit2(42f) shouldBe ImmutableVector2.ZERO
    ImmutableVector2(42f, 0f).withLimit2(1f) shouldBe ImmutableVector2.X
    ImmutableVector2(0.5f, 0f).withLimit2(1f) shouldBe ImmutableVector2(0.5f, 0f)
    ImmutableVector2(6f, -8f).withLimit2(25f) shouldBe ImmutableVector2(3f, -4f)
    ImmutableVector2(2f, -3f).withLimit2(25f) shouldBe ImmutableVector2(2f, -3f)
  }

  test("withLength should return same value as Vector2") {
    scalars.forEach { l ->
      vectors.forEach { v ->
        v.withLength(l) shouldBe v.toMutable().setLength(l).toImmutable()
      }
    }
  }

  test("withLength should return a vector of given length") {
    ImmutableVector2.ZERO.withLength(5f) shouldBe ImmutableVector2.ZERO
    ImmutableVector2.X.withLength(3f) shouldBe ImmutableVector2(3f, 0f)
    ImmutableVector2.Y.withLength(0.5f) shouldBe ImmutableVector2(0f, 0.5f)
    ImmutableVector2(3f, -4f).withLength(10f) shouldBe ImmutableVector2(6f, -8f)
    ImmutableVector2(-3f, 4f).withLength(2.5f) shouldBe ImmutableVector2(-1.5f, 2f)
  }

  test("withLength2 should return same value as Vector2") {
    scalars.forEach { l ->
      vectors.forEach { v ->
        v.withLength2(l) shouldBe v.toMutable().setLength2(l).toImmutable()
      }
    }
  }

  test("withLength2 should return a vector of given squared length") {
    ImmutableVector2.ZERO.withLength2(25f) shouldBe ImmutableVector2.ZERO
    ImmutableVector2.X.withLength2(9f) shouldBe ImmutableVector2(3f, 0f)
    ImmutableVector2.Y.withLength2(0.25f) shouldBe ImmutableVector2(0f, 0.5f)
    ImmutableVector2(3f, -4f).withLength2(100f) shouldBe ImmutableVector2(6f, -8f)
    ImmutableVector2(-3f, 4f).withLength2(6.25f) shouldBe ImmutableVector2(-1.5f, 2f)
  }

  test("withClamp should return same value as Vector2") {
    scalars.forEach { min ->
      scalars.filter { it >= min }.forEach { max ->
        vectors.forEach { v ->
          v.withClamp(min, max) shouldBe v.toMutable().clamp(min, max).toImmutable()
        }
      }
    }
  }

  test("withClamp should return a vector of length between min and max") {
    ImmutableVector2.ZERO.withClamp(1f, 2f) shouldBe ImmutableVector2.ZERO
    ImmutableVector2.X.withClamp(2f, 3f) shouldBe ImmutableVector2(2f, 0f)
    ImmutableVector2.X.withClamp(0.5f, 0.8f) shouldBe ImmutableVector2(0.8f, 0f)
    ImmutableVector2.X.withClamp(0.5f, 2f) shouldBe ImmutableVector2.X

    ImmutableVector2(3f, -4f).withClamp(4f, 6f) shouldBe ImmutableVector2(3f, -4f)
    ImmutableVector2(3f, -4f).withClamp(1f, 2.5f) shouldBe ImmutableVector2(1.5f, -2f)
    ImmutableVector2(3f, -4f).withClamp(10f, 12f) shouldBe ImmutableVector2(6f, -8f)
  }

  test("withClamp2 should return a vector of length between min2 and max2") {
    ImmutableVector2.ZERO.withClamp2(1f, 4f) shouldBe ImmutableVector2.ZERO
    ImmutableVector2.X.withClamp2(4f, 9f) shouldBe ImmutableVector2(2f, 0f)
    ImmutableVector2.X.withClamp2(0.5f, 0.64f) shouldBe ImmutableVector2(0.8f, 0f)
    ImmutableVector2.X.withClamp2(0.5f, 2f) shouldBe ImmutableVector2.X

    ImmutableVector2(3f, -4f).withClamp2(16f, 36f) shouldBe ImmutableVector2(3f, -4f)
    ImmutableVector2(3f, -4f).withClamp2(1f, 6.25f) shouldBe ImmutableVector2(1.5f, -2f)
    ImmutableVector2(3f, -4f).withClamp2(100f, 110f) shouldBe ImmutableVector2(6f, -8f)
  }

  test("times should return same value as Vector2 mul") {
    val transformations =
      listOf(
        Affine2(),
        Affine2().apply {
          preTranslate(-2f, 3f)
          preRotate(18f)
          preScale(1.5f, 2f)
        },
      )

    transformations.forEach { transformation ->
      vectors.forEach { vector ->
        val matrix = Matrix3().set(transformation)
        (vector * transformation) shouldBe vector.toMutable().mul(matrix).toImmutable()
      }
    }
  }

  test("applying affine transformation should be equivalent of doing the operations") {
    val vector = ImmutableVector2(3f, 4f)

    val transformation =
      Affine2().apply {
        preTranslate(-2f, 3f) // (1f, 7f)
        preRotate(90f) // (-7f, 1f)
        preScale(0.5f, 2f) // (-3.5f, 2f)
      }

    ImmutableVector2(-3.5f, 2f).epsilonEquals(vector * transformation, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    (
      vector
        .plus(-2f, 3f)
        .withRotationDeg(90f)
        .times(0.5f, 2f)
        .epsilonEquals(vector * transformation, MathUtils.FLOAT_ROUNDING_ERROR)
    ) shouldBe true
  }

  test("crs should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        (v1 x v2) shouldBe v1.toMutable().crs(v2.toMutable())
        v1.crs(v2) shouldBe v1.toMutable().crs(v2.toMutable())
        v1.crs(v2.x, v2.y) shouldBe v1.toMutable().crs(v2.x, v2.y)
      }
    }
  }

  test("crs should return cross product") {
    (ImmutableVector2(2f, 3f) x ImmutableVector2(4f, 5f)) shouldBe -2f
    (ImmutableVector2(4f, 5f) x ImmutableVector2(2f, 3f)) shouldBe 2f
    (ImmutableVector2(2f, 3f) x ImmutableVector2.ZERO) shouldBe 0f
    (ImmutableVector2.ZERO x ImmutableVector2(4f, 5f)) shouldBe 0f
  }

  test("zero vector should have undefined direction") {
    ImmutableVector2.ZERO.angleDeg().isNaN() shouldBe true
    ImmutableVector2.ZERO.angleRad().isNaN() shouldBe true
  }

  test("angleDeg should return correct angles between axes") {
    (ImmutableVector2.Y).angleDeg(ImmutableVector2.X) shouldBe
      (90f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    (-ImmutableVector2.X).angleDeg(ImmutableVector2.Y) shouldBe
      (90f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    (-ImmutableVector2.Y).angleDeg(-ImmutableVector2.X) shouldBe
      (90f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    (ImmutableVector2.X).angleDeg(-ImmutableVector2.Y) shouldBe
      (90f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)

    (ImmutableVector2.Y).angleDeg(-ImmutableVector2.X) shouldBe
      (-90f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    (-ImmutableVector2.X).angleDeg(-ImmutableVector2.Y) shouldBe
      (-90f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    (-ImmutableVector2.Y).angleDeg(ImmutableVector2.X) shouldBe
      (-90f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    (ImmutableVector2.X).angleDeg(ImmutableVector2.Y) shouldBe
      (-90f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)

    (ImmutableVector2.X).angleDeg(-ImmutableVector2.X) shouldBe
      (180f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    (ImmutableVector2.Y).angleDeg(-ImmutableVector2.Y) shouldBe
      (180f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
  }

  test("angleDeg should return expected angles") {
    ImmutableVector2(1f, 1f).angleDeg(ImmutableVector2.X) shouldBe
      (45f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    ImmutableVector2.Y.angleDeg(ImmutableVector2(1f, 1f)) shouldBe
      (45f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
    ImmutableVector2.X.angleDeg(ImmutableVector2(1f, 1f)) shouldBe
      (-45f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
  }

  test("angleDeg should return value between -180 and 180") {
    vectors.filterNot { it.isZero(0f) }.forEach { v1 ->
      (v1.angleDeg() in (-180f)..(180f)) shouldBe true
      vectors.filterNot { it.isZero(0f) }.forEach { v2 ->
        (v1.angleDeg(v2) in (-180f)..(180f)) shouldBe true
      }
    }
  }

  test("angleDeg should return angle of applied rotation") {
    vectors.filter { it.len2.isFinite() && it.len2 > 0.5 }.forEach { initialVector ->

      // rotate from positive x-axis to positive y-axis (typically counter-clockwise)
      var deltaAngle = 60f
      var previousVector = initialVector
      var currentVector = previousVector.withRotationDeg(deltaAngle)
      currentVector.angleDeg(previousVector) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)

      repeat(4) {
        previousVector = currentVector
        currentVector = previousVector.withRotationDeg(deltaAngle)
        currentVector.angleDeg(previousVector) shouldBe
          (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
      }

      // rotate from positive x-axis to negative y-axis (typically clockwise)
      deltaAngle = -60f
      previousVector = initialVector
      currentVector = previousVector.withRotationDeg(deltaAngle)
      currentVector.angleDeg(previousVector) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)

      repeat(4) {
        previousVector = currentVector
        currentVector = previousVector.withRotationDeg(deltaAngle)
        currentVector.angleDeg(previousVector) shouldBe
          (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR * MathUtils.radiansToDegrees)
      }
    }
  }

  test("angleRad to x-axis should return same value as Vector2") {
    // libGDX returns an arbitrary (incorrect) angle when using zero vector. (vector zero's angle is undefined)
    vectors.filterNot { it.isZero(0f) }.forEach { v1 ->
      withClue("$v1") { v1.angleRad() shouldBe v1.toMutable().angleRad() }
    }
  }

  test("angleRad should return correct angles between axises") {
    (ImmutableVector2.Y).angleRad(ImmutableVector2.X) shouldBe ((MathUtils.PI / 2f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    (-ImmutableVector2.X).angleRad(ImmutableVector2.Y) shouldBe ((MathUtils.PI / 2f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    (-ImmutableVector2.Y).angleRad(-ImmutableVector2.X) shouldBe ((MathUtils.PI / 2f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    (ImmutableVector2.X).angleRad(-ImmutableVector2.Y) shouldBe ((MathUtils.PI / 2f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)

    (ImmutableVector2.Y).angleRad(-ImmutableVector2.X) shouldBe ((-MathUtils.PI / 2f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    (-ImmutableVector2.X).angleRad(-ImmutableVector2.Y) shouldBe ((-MathUtils.PI / 2f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    (-ImmutableVector2.Y).angleRad(ImmutableVector2.X) shouldBe ((-MathUtils.PI / 2f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    (ImmutableVector2.X).angleRad(ImmutableVector2.Y) shouldBe ((-MathUtils.PI / 2f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)

    (ImmutableVector2.X).angleRad(-ImmutableVector2.X) shouldBe (MathUtils.PI plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    (ImmutableVector2.Y).angleRad(-ImmutableVector2.Y) shouldBe (MathUtils.PI plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
  }

  test("angleRad should return expected angles") {
    ImmutableVector2(1f, 1f).angleRad(ImmutableVector2.X) shouldBe ((MathUtils.PI / 4f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    ImmutableVector2.Y.angleRad(ImmutableVector2(1f, 1f)) shouldBe ((MathUtils.PI / 4f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    ImmutableVector2.X.angleRad(ImmutableVector2(1f, 1f)) shouldBe ((-MathUtils.PI / 4f) plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
  }

  test("angleRad should returns value between -PI and PI") {
    vectors.filterNot { it.isZero(0f) }.forEach { v1 ->
      vectors.filterNot { it.isZero(0f) }.forEach { v2 ->
        (v1.angleRad(v2) in (-MathUtils.PI)..(MathUtils.PI)) shouldBe true
      }
    }
  }

  test("angleRad should return angle of applied rotation") {
    vectors.filter { it.len2.isFinite() && it.len2 > 0.5 }.forEach { initialVector ->

      // rotate from positive x-axis to positive y-axis (typically counter-clockwise)
      var deltaAngle = MathUtils.PI / 3
      var previousVector = initialVector
      var currentVector = previousVector.withRotationRad(deltaAngle)
      currentVector.angleRad(previousVector) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
      currentVector.angleRad(previousVector.x, previousVector.y) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)

      repeat(4) {
        previousVector = currentVector
        currentVector = previousVector.withRotationRad(deltaAngle)
        currentVector.angleRad(previousVector) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
        currentVector.angleRad(previousVector.x, previousVector.y) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
      }

      // rotate from positive x-axis to negative y-axis (typically clockwise)
      deltaAngle = -MathUtils.PI / 3
      previousVector = initialVector
      currentVector = previousVector.withRotationRad(deltaAngle)
      currentVector.angleRad(previousVector) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
      currentVector.angleRad(previousVector.x, previousVector.y) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)

      repeat(4) {
        previousVector = currentVector
        currentVector = previousVector.withRotationRad(deltaAngle)
        currentVector.angleRad(previousVector) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
        currentVector.angleRad(previousVector.x, previousVector.y) shouldBe (deltaAngle plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
      }
    }
  }

  test("angleRad with x axis should return same value as Vector2") {
    // libGDX returns an arbitrary (incorrect) angle when using zero vector. (vector zero's angle is undefined)
    vectors.filterNot { it.isZero(0f) }.forEach {
      it.angleRad() shouldBe it.toMutable().angleRad()
    }
  }

  test("withAngle should return same value as Vector2") {
    scalars.forEach { angle ->
      vectors.forEach { vector ->
        vector.withAngleDeg(angle) shouldBe vector.toMutable().setAngleDeg(angle).toImmutable()
      }
    }
  }

  test("withAngleDeg should return rotated vector") {
    (
      ImmutableVector2(0.6f, 0.8f)
        .withAngleDeg(90f)
        .epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR)
    ) shouldBe true
    (
      ImmutableVector2(0.6f, 0.8f)
        .withAngleDeg(-90f)
        .epsilonEquals(-ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR)
    ) shouldBe true
    (
      ImmutableVector2(
        0.6f,
        0.8f,
      ).withAngleDeg(45f)
        .epsilonEquals(ImmutableVector2(sqrt(0.5f), sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR)
    ) shouldBe true
    (
      ImmutableVector2(
        0.6f,
        0.8f,
      ).withAngleDeg(-45f)
        .epsilonEquals(ImmutableVector2(sqrt(0.5f), -sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR)
    ) shouldBe true
  }

  test("withAngleRad should return same value as Vector2") {
    scalars.forEach { angle ->
      vectors.forEach { vector ->
        vector.withAngleRad(angle) shouldBe vector.toMutable().setAngleRad(angle).toImmutable()
      }
    }
  }

  test("withAngleRad should return rotated vector") {
    ImmutableVector2(0.6f, 0.8f).withAngleDeg(90f).epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2(0.6f, 0.8f).withAngleDeg(-90f).epsilonEquals(-ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2(
      0.6f,
      0.8f,
    ).withAngleDeg(45f).epsilonEquals(ImmutableVector2(sqrt(0.5f), sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2(
      0.6f,
      0.8f,
    ).withAngleDeg(-45f).epsilonEquals(ImmutableVector2(sqrt(0.5f), -sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
  }

  test("withRotationDeg should return same value as Vector2") {
    scalars.forEach { angle ->
      vectors.forEach { vector ->
        vector.withRotationDeg(angle) shouldBe vector.toMutable().rotateDeg(angle).toImmutable()
      }
    }
  }

  test("withRotationDeg should return rotated vector") {
    ImmutableVector2.X.withRotationDeg(90f).epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2.X.withRotationDeg(-90f).epsilonEquals(-ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true

    ImmutableVector2.X
      .withRotationDeg(
        45f,
      ).epsilonEquals(ImmutableVector2(sqrt(0.5f), sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true
    ImmutableVector2.X
      .withRotationDeg(
        -45f,
      ).epsilonEquals(ImmutableVector2(sqrt(0.5f), -sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true

    ImmutableVector2(
      sqrt(0.5f),
      sqrt(0.5f),
    ).withRotationDeg(45f).epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true
    ImmutableVector2(
      sqrt(0.5f),
      sqrt(0.5f),
    ).withRotationDeg(-45f).epsilonEquals(ImmutableVector2.X, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true
  }

  test("withRotationRad should return same value as Vector2") {
    scalars.forEach { angle ->
      vectors.forEach { vector ->
        vector.withRotationRad(angle) shouldBe vector.toMutable().rotateRad(angle).toImmutable()
      }
    }
  }

  test("withRotationRad should return rotated vector") {
    ImmutableVector2.X.withRotationRad(MathUtils.PI / 2f).epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2.X.withRotationRad(-MathUtils.PI / 2f).epsilonEquals(-ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true

    (
      ImmutableVector2.X
        .withRotationRad(
          MathUtils.PI / 4f,
        ).epsilonEquals(ImmutableVector2(sqrt(0.5f), sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR)
    ) shouldBe true
    (
      ImmutableVector2.X
        .withRotationRad(
          -MathUtils.PI / 4f,
        ).epsilonEquals(ImmutableVector2(sqrt(0.5f), -sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR)
    ) shouldBe true

    ImmutableVector2(
      sqrt(0.5f),
      sqrt(0.5f),
    ).withRotationRad(MathUtils.PI / 4f).epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2(
      sqrt(0.5f),
      sqrt(0.5f),
    ).withRotationRad(-MathUtils.PI / 4f).epsilonEquals(ImmutableVector2.X, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
  }

  test("withRotation90 should return same value as Vector2") {
    vectors.forEach { vector ->
      vector.withRotation90(-1) shouldBe vector.toMutable().rotate90(-1).toImmutable()
      vector.withRotation90(1) shouldBe vector.toMutable().rotate90(1).toImmutable()
    }
  }

  test("withRotation90 should return rotated vector") {
    ImmutableVector2.X.withRotation90(1).epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2.X.withRotation90(-1).epsilonEquals(-ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true

    ImmutableVector2.Y.withRotation90(1).epsilonEquals(-ImmutableVector2.X, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2.Y.withRotation90(-1).epsilonEquals(ImmutableVector2.X, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true

    ImmutableVector2(
      sqrt(0.5f),
      sqrt(0.5f),
    ).withRotation90(1).epsilonEquals(ImmutableVector2(-sqrt(0.5f), sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
    ImmutableVector2(
      sqrt(0.5f),
      sqrt(0.5f),
    ).withRotation90(-1).epsilonEquals(ImmutableVector2(sqrt(0.5f), -sqrt(0.5f)), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
  }

  test("withLerp should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        scalars.forEach { ratio ->
          v1.withLerp(v2, ratio) shouldBe v1.toMutable().lerp(v2.toMutable(), ratio).toImmutable()
        }
      }
    }
  }

  test("withLerp should return interpolated vector") {
    ImmutableVector2.X
      .withLerp(
        ImmutableVector2.Y,
        0.5f,
      ).epsilonEquals(ImmutableVector2(0.5f, 0.5f), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true
    ImmutableVector2.X
      .withLerp(
        ImmutableVector2.Y,
        0.3f,
      ).epsilonEquals(ImmutableVector2(0.7f, 0.3f), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true

    ImmutableVector2.X.withLerp(-ImmutableVector2.X, 0.5f).epsilonEquals(ImmutableVector2.ZERO, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true
    ImmutableVector2.X
      .withLerp(
        -ImmutableVector2.X,
        0.3f,
      ).epsilonEquals(ImmutableVector2(0.4f, 0f), MathUtils.FLOAT_ROUNDING_ERROR) shouldBe
      true
  }

  test("withLerp with a factor of 0 should return the origin vector") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.withLerp(v2, 0f).epsilonEquals(v1, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
      }
    }
  }

  test("withLerp with a factor of 1 should return the target vector") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.withLerp(v2, 1f).epsilonEquals(v2, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
      }
    }
  }

  test("withInterpolation should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        scalars.forEach { alpha ->
          interpolations.forEach { interpolation ->
            v1.withInterpolation(v2, alpha, interpolation) shouldBe
              v1.toMutable().interpolate(v2.toMutable(), alpha, interpolation).toImmutable()
          }
        }
      }
    }
  }

  test("withInterpolation with a factor of 0 should return the origin vector") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        interpolations.forEach { interpolation ->
          v1.withInterpolation(v2, 0f, interpolation).epsilonEquals(v1, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
        }
      }
    }
  }

  test("withInterpolation with a factor of 1 should return the target vector") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        interpolations.forEach { interpolation ->
          v1.withInterpolation(v2, 1f, interpolation).epsilonEquals(v2, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
        }
      }
    }
  }

  test("withRandomDirection should return a vector of same length") {
    val rng = Random(42)

    vectors.forEach { vector ->
      vector.len shouldBe (vector.withRandomDirection(rng).len plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    }
  }

  test("withRandomDirection should return a variety of random vectors") {
    val rng = Random(42)

    val vectors = HashSet<ImmutableVector2>()
    repeat(1000) {
      vectors += ImmutableVector2.X.withRandomDirection(rng)
    }

    (vectors.size > 900) shouldBe true
    vectors.forAtLeastOne { it.x > 0 && it.y > 0 }
    vectors.forAtLeastOne { it.x > 0 && it.y < 0 }
    vectors.forAtLeastOne { it.x < 0 && it.y > 0 }
    vectors.forAtLeastOne { it.x < 0 && it.y < 0 }
  }

  test("epsilonEquals should return same than Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        scalars.forEach { e ->
          v1.epsilonEquals(v2, e) shouldBe v1.toMutable().epsilonEquals(v2.toMutable(), e)
          v1.epsilonEquals(v2.x, v2.y, e) shouldBe v1.toMutable().epsilonEquals(v2.x, v2.y, e)
        }
      }
    }
  }

  test("epsilonEquals should return true for equals vectors") {
    vectors.forEach { v ->
      v.epsilonEquals(v, 0f) shouldBe true
    }
  }

  test("epsilonEquals should consider epsilon argument") {
    vectors.forEach { v1 ->
      v1.copy(x = v1.x + Float.MIN_VALUE, y = v1.y + Float.MIN_VALUE).epsilonEquals(v1, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true
      v1.copy(x = v1.x - Float.MIN_VALUE, y = v1.y - Float.MIN_VALUE).epsilonEquals(v1, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe true

      vectors.forEach { v2 ->
        v1.epsilonEquals(v2, 10f) shouldBe true
      }
    }

    ImmutableVector2.X.epsilonEquals(ImmutableVector2.Y, 1f) shouldBe true
  }

  test("epsilonEquals should return false for different vectors") {
    ImmutableVector2.ZERO.epsilonEquals(ImmutableVector2.X, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe false
    ImmutableVector2.ZERO.epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe false
    ImmutableVector2.X.epsilonEquals(ImmutableVector2.Y, MathUtils.FLOAT_ROUNDING_ERROR) shouldBe false
    ImmutableVector2.X.epsilonEquals(ImmutableVector2.Y, 0.8f) shouldBe false
    ImmutableVector2(2f, 3f).epsilonEquals(ImmutableVector2(2.1f, 3f), 0.09f) shouldBe false
  }

  test("epsilonEquals should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        scalars.forEach { s ->
          v2.epsilonEquals(v1, s) shouldBe v1.epsilonEquals(v2, s)
        }
      }
    }
  }

  test("isOnLine should return same than Vector2") {
    // libGDX returns false (incorrect) when compared with vector zero.
    vectors.filterNot { it.isZero(0f) }.forEach { v1 ->
      vectors.filterNot { it.isZero(0f) }.forEach { v2 ->
        v1.isOnLine(v2) shouldBe v1.toMutable().isOnLine(v2.toMutable())
        scalars.forEach { e ->
          v1.isOnLine(v2, e) shouldBe v1.toMutable().isOnLine(v2.toMutable(), e)
        }
      }
    }
  }

  test("isOnLine should return false when compared with vector zero") {
    vectors.forEach { v ->
      v.isOnLine(ImmutableVector2.ZERO) shouldBe false
      ImmutableVector2.ZERO.isOnLine(v) shouldBe false
    }
  }

  test("isOnLine should return true for same non-zero vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.isOnLine(v) shouldBe true
    }
  }

  test("isOnLine should return true for opposite vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.isOnLine(-v) shouldBe true
      v.isOnLine(v * -2f) shouldBe true
      v.isOnLine(v * -0.5f) shouldBe true
    }
  }

  test("isOnLine should return false for on-line vector which are not on line") {
    ImmutableVector2.X.isOnLine(ImmutableVector2.Y) shouldBe false
    ImmutableVector2.Y.isOnLine(ImmutableVector2.X) shouldBe false
    ImmutableVector2(2f, 3f).isOnLine(ImmutableVector2(3f, 2f)) shouldBe false
  }

  test("isOnline should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v2.isOnLine(v1) shouldBe v1.isOnLine(v2)
      }
    }
  }

  test("isCollinear should return same value as Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.isCollinear(v2) shouldBe v1.toMutable().isCollinear(v2.toMutable())
        scalars.forEach { e ->
          v1.isCollinear(v2, e) shouldBe v1.toMutable().isCollinear(v2.toMutable(), e)
        }
      }
    }
  }

  test("isCollinear should return false when compared with vector zero") {
    vectors.forEach { v ->
      v.isCollinear(ImmutableVector2.ZERO) shouldBe false
      ImmutableVector2.ZERO.isCollinear(v) shouldBe false
    }
  }

  test("isCollinear should return true for same vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.isCollinear(v) shouldBe true
    }
  }

  test("isCollinear should return true for same scaled vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.isCollinear(v * 2f) shouldBe true
      v.isCollinear(v * 0.5f) shouldBe true
    }
  }

  test("isCollinear should return false for opposite vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.isCollinear(-v) shouldBe false
      v.isCollinear(v * -2f) shouldBe false
      v.isCollinear(v * -0.5f) shouldBe false
    }
  }

  test("isCollinear should return false for non collinear vectors") {
    ImmutableVector2.X.isCollinear(-ImmutableVector2.X) shouldBe false
    ImmutableVector2.X.isCollinear(ImmutableVector2.Y) shouldBe false
    ImmutableVector2(2f, 3f).isCollinear(ImmutableVector2(3f, 2f)) shouldBe false
    ImmutableVector2(2f, 3f).isCollinear(ImmutableVector2(-2f, -3f)) shouldBe false
  }

  test("isCollinear should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v2.isCollinear(v1) shouldBe v1.isCollinear(v2)
      }
    }
  }

  test("isCollinearOpposite should return same than Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.isCollinearOpposite(v2) shouldBe v1.toMutable().isCollinearOpposite(v2.toMutable())
        scalars.forEach { e ->
          v1.isCollinearOpposite(v2, e) shouldBe v1.toMutable().isCollinearOpposite(v2.toMutable(), e)
        }
      }
    }
  }

  test("isCollinearOpposite should return false when compared with vector zero") {
    vectors.forEach { v ->
      v.isCollinearOpposite(ImmutableVector2.ZERO) shouldBe false
      ImmutableVector2.ZERO.isCollinearOpposite(v) shouldBe false
    }
  }

  test("isCollinearOpposite should return false for same vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.isCollinearOpposite(v) shouldBe false
    }
  }

  test("isCollinearOpposite should return false for same scaled vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.isCollinearOpposite(v * 2f) shouldBe false
      v.isCollinearOpposite(v * 0.5f) shouldBe false
    }
  }

  test("isCollinearOpposite should return true for opposite vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.isCollinearOpposite(-v) shouldBe true
      v.isCollinearOpposite(v * -2f) shouldBe true
      v.isCollinearOpposite(v * -0.5f) shouldBe true
    }
  }

  test("isCollinearOpposite should return false for non collinear vectors") {
    ImmutableVector2.X.isCollinearOpposite(ImmutableVector2.Y) shouldBe false
    ImmutableVector2(2f, 3f).isCollinearOpposite(ImmutableVector2(3f, 2f)) shouldBe false
    ImmutableVector2(2f, 3f).isCollinearOpposite(ImmutableVector2(-3f, -3f)) shouldBe false
  }

  test("isCollinearOpposite should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v2.isCollinearOpposite(v1) shouldBe v1.isCollinearOpposite(v2)
      }
    }
  }

  test("isPerpendicular should return same than Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.isPerpendicular(v2) shouldBe v1.toMutable().isPerpendicular(v2.toMutable())
        scalars.forEach { e ->
          v1.isPerpendicular(v2, e) shouldBe v1.toMutable().isPerpendicular(v2.toMutable(), e)
        }
      }
    }
  }

  test("isPerpendicular should return false for same vector") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.isPerpendicular(v) shouldBe false
    }
  }

  test("isPerpendicular should return false for opposite vector") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.isPerpendicular(-v) shouldBe false
      v.isPerpendicular(v * -2f) shouldBe false
    }
  }

  test("isPerpendicular should return true for axises") {
    ImmutableVector2.X.isPerpendicular(ImmutableVector2.Y) shouldBe true
    ImmutableVector2.X.isPerpendicular(-ImmutableVector2.Y) shouldBe true
    ImmutableVector2.Y.isPerpendicular(ImmutableVector2.X) shouldBe true
    ImmutableVector2.Y.isPerpendicular(-ImmutableVector2.X) shouldBe true
  }

  test("isPerpendicular should return true for result of 90 degrees rotation") {
    vectors.filterNot { it.isZero(0f) }.forEach { vector ->
      vector.isPerpendicular(vector.withRotation90(1)) shouldBe true
      vector.isPerpendicular(vector.withRotation90(-1)) shouldBe true
    }
  }

  test("isPerpendicular should return false for non-perpendicular vectors") {
    ImmutableVector2.X.isPerpendicular(ImmutableVector2(1f, 2f)) shouldBe false
    ImmutableVector2.Y.isPerpendicular(ImmutableVector2(-3f, 2f)) shouldBe false
  }

  test("isPerpendicular should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v2.isPerpendicular(v1) shouldBe v1.isPerpendicular(v2)
      }
    }
  }

  test("hasSameDirection should return same than Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.hasSameDirection(v2) shouldBe v1.toMutable().hasSameDirection(v2.toMutable())
      }
    }
  }

  test("hasSameDirection should return false when compared with zero") {
    vectors.forEach { v ->
      v.hasSameDirection(ImmutableVector2.ZERO) shouldBe false
      ImmutableVector2.ZERO.hasSameDirection(v) shouldBe false
    }
  }

  test("hasSameDirection should return true for same vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.hasSameDirection(v) shouldBe true
    }
  }

  test("hasSameDirection should return true for same scaled vector") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.hasSameDirection(v * 2f) shouldBe true
      v.hasSameDirection(v * 0.5f) shouldBe true
    }
  }

  test("hasSameDirection should return true for same vector rotated by less than 90 degrees") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.hasSameDirection(v.withRotationDeg(80f)) shouldBe true
      v.hasSameDirection(v.withRotationDeg(-80f)) shouldBe true
    }
  }

  test("hasSameDirection should return false for opposite vector") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.hasSameDirection(-v) shouldBe false
      v.hasSameDirection(v * -2f) shouldBe false
      v.hasSameDirection(v * -0.5f) shouldBe false
    }
  }

  test("hasSameDirection should return false for same vector rotated by more than 90 degrees") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.hasSameDirection(v.withRotationDeg(100f)) shouldBe false
      v.hasSameDirection(v.withRotationDeg(-100f)) shouldBe false
    }
  }

  test("hasSameDirection should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v2.hasSameDirection(v1) shouldBe v1.hasSameDirection(v2)
      }
    }
  }

  test("hasOppositeDirection should return same than Vector2") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v1.hasOppositeDirection(v2) shouldBe v1.toMutable().hasOppositeDirection(v2.toMutable())
      }
    }
  }

  test("hasOppositeSameDirection should return false when compared with zero") {
    vectors.forEach { v ->
      v.hasOppositeDirection(ImmutableVector2.ZERO) shouldBe false
      ImmutableVector2.ZERO.hasOppositeDirection(v) shouldBe false
    }
  }

  test("hasOppositeDirection should return false for same vector") {
    vectors.filterNot { it.isZero() }.forEach { v ->
      v.hasOppositeDirection(v) shouldBe false
    }
  }

  test("hasOppositeDirection should return false for same scaled vector") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.hasOppositeDirection(v * 2f) shouldBe false
      v.hasOppositeDirection(v * 0.5f) shouldBe false
    }
  }

  test("hasOppositeDirection should return false for same vector rotated by less than 90 degrees") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.hasOppositeDirection(v.withRotationDeg(80f)) shouldBe false
      v.hasOppositeDirection(v.withRotationDeg(-80f)) shouldBe false
    }
  }

  test("hasOppositeDirection should return true for opposite vector") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.hasOppositeDirection(-v) shouldBe true
      v.hasOppositeDirection(v * -2f) shouldBe true
      v.hasOppositeDirection(v * -0.5f) shouldBe true
    }
  }

  test("hasOppositeDirection should return true for same vector rotated by more than 90 degrees") {
    vectors.filterNot { it.isZero(0f) }.forEach { v ->
      v.hasOppositeDirection(v.withRotationDeg(100f)) shouldBe true
      v.hasOppositeDirection(v.withRotationDeg(-100f)) shouldBe true
    }
  }

  test("hasOppositeDirection should be commutative") {
    vectors.forEach { v1 ->
      vectors.forEach { v2 ->
        v2.hasOppositeDirection(v1) shouldBe v1.hasOppositeDirection(v2)
      }
    }
  }

  test("copy should return vector with same properties") {
    vectors.forEach { vector ->
      vector.copy(y = 42f).x shouldBe vector.x
      vector.copy(y = 42f).y shouldBe 42f
      vector.copy(x = 42f).x shouldBe 42f
      vector.copy(x = 42f).y shouldBe vector.y
      vector.copy(x = 1f, y = 2f) shouldBe ImmutableVector2(1f, 2f)
    }
  }

  test("should destruct vector into two floats") {
    vectors.forEach { vector ->
      val (x, y) = vector

      x shouldBe vector.x
      y shouldBe vector.y
    }
  }

  test("should compare vectors by length") {
    var previousLength = -1f
    var previousLength2 = -1f

    vectors.toList().shuffled().sorted().forEach { vector ->
      (vector.len >= previousLength) shouldBe true
      (vector.len2 >= previousLength2) shouldBe true
      previousLength = vector.len
      previousLength2 = vector.len2
    }
  }

  test("should increment vector values with ++ operator") {
    var vector = ImmutableVector2(10f, 10f)

    vector++

    vector.x shouldBe (11f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    vector.y shouldBe (11f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
  }

  test("should decrement vector values with -- operator") {
    var vector = ImmutableVector2(10f, 10f)

    vector--

    vector.x shouldBe (9f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    vector.y shouldBe (9f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
  }

  test("should divide vectors by float scalars with div operator") {
    val vector = ImmutableVector2(10f, 10f)

    val result = vector / 2.5f

    result.x shouldBe (4f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
    result.y shouldBe (4f plusOrMinus MathUtils.FLOAT_ROUNDING_ERROR)
  }
})
