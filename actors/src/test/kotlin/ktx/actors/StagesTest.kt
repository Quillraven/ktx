package ktx.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Scaling.stretch
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.badlogic.gdx.utils.viewport.Viewport
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.nio.IntBuffer

class StagesTest : FunSpec({
  fun assertIsDefaultViewport(viewport: Viewport) {
    assert(viewport is ScalingViewport)
    (viewport as ScalingViewport).scaling shouldBeSameInstanceAs stretch
    viewport.worldWidth shouldBe Gdx.graphics.width.toFloat()
    viewport.worldHeight shouldBe Gdx.graphics.height.toFloat()
    assert(viewport.camera is OrthographicCamera)
  }

  beforeTest {
    LwjglNativesLoader.load()
    Gdx.app = mock()
    Gdx.gl =
      mock {
        // Mocking shader compilation, so SpriteBatch can be initiated:
        on(it.glCreateShader(any())) doReturn 1
        on(it.glCreateProgram()) doReturn 1
        on(it.glGetProgramiv(any(), any(), any())) doAnswer { invocation ->
          if (invocation.arguments[1] == GL20.GL_LINK_STATUS) {
            invocation.getArgument<IntBuffer>(2).put(0, 1)
          }
        }
        on(it.glGetShaderiv(any(), any(), any())) doAnswer { invocation ->
          if (invocation.arguments[1] == GL20.GL_COMPILE_STATUS) {
            invocation.getArgument<IntBuffer>(2).put(0, 1)
            Unit
          }
        }
        on(it.glGenBuffer()) doReturn 1
      }
    Gdx.gl20 = Gdx.gl
    Gdx.graphics =
      mock {
        on(it.width) doReturn 800
        on(it.height) doReturn 600
      }
  }

  test("should initiate Stage") {
    // When:
    val stage = stage()

    // Then:
    assert(stage.batch is SpriteBatch)
    assertIsDefaultViewport(stage.viewport)
  }

  test("should initiate Stage with custom Viewport") {
    // Given:
    val viewport = mock<Viewport>()

    // When:
    val stage = stage(viewport = viewport)

    // Then:
    assert(stage.batch is SpriteBatch)
    stage.viewport shouldBeSameInstanceAs viewport
  }

  test("should initiate Stage with custom Batch") {
    // Given:
    val batch = mock<Batch>()

    // When:
    val stage = stage(batch)

    // Then:
    stage.batch shouldBeSameInstanceAs batch
    assertIsDefaultViewport(stage.viewport)
  }

  test("should initiate Stage with custom Batch and Viewport") {
    // Given:
    val batch = mock<Batch>()
    val viewport = mock<Viewport>()

    // When:
    val stage = stage(batch, viewport)

    // Then:
    stage.batch shouldBeSameInstanceAs batch
    stage.viewport shouldBeSameInstanceAs viewport
  }

  afterTest {
    Gdx.gl = null
    Gdx.app = null
    Gdx.gl20 = null
    Gdx.graphics = null
  }
})
