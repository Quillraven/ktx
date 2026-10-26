package ktx.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ShapeRendererTest : FunSpec({
  test("should translate") {
    val tested = mock<ShapeRenderer>()

    tested.translate(Vector3(1f, 2f, 3f))

    verify(tested).translate(1f, 2f, 3f)
  }

  test("should scale") {
    val tested = mock<ShapeRenderer>()

    tested.scale(Vector2(10f, 20f))

    verify(tested).scale(10f, 20f, 1f)
  }

  test("should scale with z") {
    val tested = mock<ShapeRenderer>()

    tested.scale(Vector3(1f, 2f, 3f))

    verify(tested).scale(1f, 2f, 3f)
  }

  test("should rotate") {
    val tested = mock<ShapeRenderer>()

    tested.rotate(Vector3(1f, 2f, 3f), degrees = 90f)

    verify(tested).rotate(1f, 2f, 3f, 90f)
  }

  test("should render box") {
    val tested = mock<ShapeRenderer>()

    tested.box(position = Vector3(1f, 2f, 3f), width = 10f, height = 20f, depth = 30f)

    verify(tested).box(1f, 2f, 3f, 10f, 20f, 30f)
  }

  test("should render circle") {
    val tested = mock<ShapeRenderer>()

    tested.circle(position = Vector2(1f, 2f), radius = 3f)

    verify(tested).circle(1f, 2f, 3f)
  }

  test("should render circle with segments") {
    val tested = mock<ShapeRenderer>()

    tested.circle(position = Vector2(1f, 2f), radius = 3f, segments = 42)

    verify(tested).circle(1f, 2f, 3f, 42)
  }

  test("should render rectangle") {
    val tested = mock<ShapeRenderer>()

    tested.rect(position = Vector2(1f, 2f), width = 10f, height = 20f)

    verify(tested).rect(1f, 2f, 10f, 20f)
  }

  test("should render rectangle with Vector2 size") {
    val tested = mock<ShapeRenderer>()

    tested.rect(position = Vector2(1f, 2f), size = Vector2(10f, 20f))

    verify(tested).rect(1f, 2f, 10f, 20f)
  }

  test("should render rectangle line") {
    val tested = mock<ShapeRenderer>()

    tested.rectLine(
      positionA = Vector2(1f, 2f),
      positionB = Vector2(3f, 4f),
      width = 10f,
      colorA = Color.BLACK,
      colorB = Color.WHITE,
    )

    verify(tested).rectLine(1f, 2f, 3f, 4f, 10f, Color.BLACK, Color.WHITE)
  }

  test("should render cone") {
    val tested = mock<ShapeRenderer>()

    tested.cone(position = Vector3(1f, 2f, 3f), radius = 4f, height = 5f)

    verify(tested).cone(1f, 2f, 3f, 4f, 5f)
  }

  test("should render cone with segments") {
    val tested = mock<ShapeRenderer>()

    tested.cone(position = Vector3(1f, 2f, 3f), radius = 4f, height = 5f, segments = 42)

    verify(tested).cone(1f, 2f, 3f, 4f, 5f, 42)
  }

  test("should render arc") {
    val tested = mock<ShapeRenderer>()

    tested.arc(position = Vector2(1f, 2f), radius = 3f, start = 4f, degrees = 90f)

    verify(tested).arc(1f, 2f, 3f, 4f, 90f)
  }

  test("should render arc with segments") {
    val tested = mock<ShapeRenderer>()

    tested.arc(position = Vector2(1f, 2f), radius = 3f, start = 4f, degrees = 90f, segments = 42)

    verify(tested).arc(1f, 2f, 3f, 4f, 90f, 42)
  }

  test("should render ellipse") {
    val tested = mock<ShapeRenderer>()

    tested.ellipse(position = Vector2(1f, 2f), width = 3f, height = 4f, degrees = 90f)

    verify(tested).ellipse(1f, 2f, 3f, 4f, 90f)
  }

  test("should render ellipse with segments") {
    val tested = mock<ShapeRenderer>()

    tested.ellipse(position = Vector2(1f, 2f), width = 3f, height = 4f, degrees = 90f, segments = 42)

    verify(tested).ellipse(1f, 2f, 3f, 4f, 90f, 42)
  }

  test("should render triangle") {
    val tested = mock<ShapeRenderer>()

    tested.triangle(pointA = Vector2(1f, 2f), pointB = Vector2(3f, 4f), pointC = Vector2(5f, 6f))

    verify(tested).triangle(1f, 2f, 3f, 4f, 5f, 6f)
  }

  test("should render triangle with colored corners") {
    val tested = mock<ShapeRenderer>()

    tested.triangle(
      pointA = Vector2(1f, 2f),
      pointB = Vector2(3f, 4f),
      pointC = Vector2(5f, 6f),
      colorA = Color.WHITE,
      colorB = Color.GRAY,
      colorC = Color.BLACK,
    )

    verify(tested).triangle(1f, 2f, 3f, 4f, 5f, 6f, Color.WHITE, Color.GRAY, Color.BLACK)
  }

  test("should begin and end ShapeRenderer") {
    val shapeRenderer = mock<ShapeRenderer>()

    shapeRenderer.use(ShapeType.Filled) {
      verify(shapeRenderer).begin(ShapeType.Filled)
      it shouldBeSameInstanceAs shapeRenderer
      verify(shapeRenderer, never()).end()
    }
    verify(shapeRenderer).end()
  }

  test("should set projection matrix") {
    val shapeRenderer = mock<ShapeRenderer>()
    val matrix = Matrix4((0..15).map { it.toFloat() }.toFloatArray())

    shapeRenderer.use(ShapeType.Filled, matrix) {
      verify(shapeRenderer).projectionMatrix = matrix
      verify(shapeRenderer).begin(ShapeType.Filled)
      it shouldBeSameInstanceAs shapeRenderer
      verify(shapeRenderer, never()).end()
    }
    verify(shapeRenderer).end()
  }

  test("should use ShapeRenderer exactly once") {
    val shapeRenderer = mock<ShapeRenderer>()
    val variable: Int

    shapeRenderer.use(ShapeType.Filled) {
      variable = 42
    }

    variable shouldBe 42
  }

  test("should set projection matrix if a camera is passed") {
    val shapeRenderer = mock<ShapeRenderer>()
    val camera = OrthographicCamera()

    shapeRenderer.use(ShapeType.Filled, camera) {
      verify(shapeRenderer).projectionMatrix = camera.combined
      verify(shapeRenderer).begin(ShapeType.Filled)
      it shouldBeSameInstanceAs shapeRenderer
      verify(shapeRenderer, never()).end()
    }
    verify(shapeRenderer).end()
  }
})
