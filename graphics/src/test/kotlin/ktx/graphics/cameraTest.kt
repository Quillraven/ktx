package ktx.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class CameraUtilitiesTest : FunSpec({
  beforeTest {
    LwjglNativesLoader.load()
    Gdx.graphics =
      mock {
        on { width } doReturn 800
        on { height } doReturn 600
      }
  }

  test("should center camera to screen center") {
    val camera = OrthographicCamera()

    camera.center()
    camera.update()

    camera.position shouldBe Vector3(400f, 300f, 0f)
  }

  test("should center camera to rectangle center with offset") {
    val camera = OrthographicCamera()

    camera.center(x = 100f, y = 50f, width = 1000f, height = 500f)
    camera.update()

    camera.position shouldBe Vector3(600f, 300f, 0f)
  }

  test("should immediately move camera to target") {
    val target = Vector2(100f, 200f)
    val camera = OrthographicCamera()

    camera.moveTo(target)
    camera.update()

    camera.position shouldBe Vector3(100f, 200f, 0f)
  }

  test("should immediately move camera to target with offset") {
    val target = Vector2(100f, 200f)
    val camera = OrthographicCamera()

    camera.moveTo(target, x = 50f, y = -25f)
    camera.update()

    camera.position shouldBe Vector3(150f, 175f, 0f)
  }

  test("should smoothly move the camera") {
    val target = Vector2(200f, 400f)
    val camera = OrthographicCamera()
    camera.position.set(100f, 100f, 0f)
    camera.update()

    camera.lerpTo(target, lerp = 0.5f)
    camera.update()

    camera.position shouldBe Vector3(150f, 250f, 0f)
  }

  test("should smoothly move the camera with non-interpolated offsets") {
    val target = Vector2(200f, 400f)
    val camera = OrthographicCamera()
    camera.position.set(100f, 100f, 0f)
    camera.update()

    camera.lerpTo(target, lerp = 0.5f, x = -25f, y = 100f)
    camera.update()

    camera.position shouldBe Vector3(125f, 350f, 0f)
  }

  test("should update camera automatically") {
    val camera = spy(OrthographicCamera())
    var operationCalls = 0

    camera.update {
      operationCalls++
      verify(camera, never()).update()
    }

    operationCalls shouldBe 1
    verify(camera, times(1)).update()
  }

  test("should perform camera update operation exactly once") {
    val camera = OrthographicCamera()
    val variable: Int

    camera.update {
      variable = 42
    }

    variable shouldBe 42
  }
})

/**
 * Tests [LetterboxingViewport] class, which merges ScreenViewport and FitViewport behavior.
 */
class LetterboxingViewportTest : FunSpec({
  val tolerance = 0.00001f

  beforeTest {
    LwjglNativesLoader.load()
    Gdx.graphics =
      mock {
        on(it.ppiX) doReturn 100f
        on(it.ppiY) doReturn 100f
        on(it.width) doReturn 800
        on(it.height) doReturn 600
      }
    Gdx.gl20 = mock()
    Gdx.gl = Gdx.gl20
  }

  test("should calculate scale") {
    val tested = LetterboxingViewport(targetPpiX = 50f, targetPpiY = 100f)

    tested.updateScale()

    tested.scaleX shouldBe (0.5f plusOrMinus tolerance)
    tested.scaleY shouldBe (1f plusOrMinus tolerance)
  }

  test("should update world size") {
    val tested = LetterboxingViewport(targetPpiX = 50f, targetPpiY = 50f, aspectRatio = 4f / 3f)

    tested.update(800, 600, true)

    tested.worldWidth shouldBe (400f plusOrMinus tolerance)
    tested.worldHeight shouldBe (300f plusOrMinus tolerance)
  }

  test("should update world size with values provided from graphics") {
    val tested = LetterboxingViewport(targetPpiX = 200f, targetPpiY = 200f, aspectRatio = 8f / 3f)

    tested.updateWorldSize() // Graphics.width == 800, Graphics.height = 600

    tested.worldWidth shouldBe (1600f plusOrMinus tolerance)
    // Height is letterboxed - 1200/2:
    tested.worldHeight shouldBe (600f plusOrMinus tolerance)
  }

  test("should update scale after target PPI is modified manually") {
    val tested = LetterboxingViewport(targetPpiX = 50f, targetPpiY = 50f)
    tested.scaleX shouldBe (0.5f plusOrMinus tolerance)
    tested.scaleY shouldBe (0.5f plusOrMinus tolerance)

    tested.targetPpiX = 150f
    tested.targetPpiY = 150f
    tested.updateScale()

    tested.scaleX shouldBe (1.5f plusOrMinus tolerance)
    tested.scaleY shouldBe (1.5f plusOrMinus tolerance)
  }
})
