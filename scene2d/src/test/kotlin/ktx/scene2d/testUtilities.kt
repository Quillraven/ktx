package ktx.scene2d

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglFiles
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader
import com.kotcrab.vis.ui.VisUI
import io.kotest.core.spec.style.FunSpec
import org.mockito.kotlin.mock
import java.util.concurrent.atomic.AtomicInteger

/**
 * Utility value for numeric tests.
 */
const val TOLERANCE = 0.0001f

/**
 * Tests that require mocked libGDX environment should inherit from this class.
 */
private val activeSpecs = AtomicInteger()

abstract class ApplicationTest : FunSpec() {
  init {
    beforeSpec {
      if (activeSpecs.getAndIncrement() > 0) return@beforeSpec

      LwjglNativesLoader.load()

      Gdx.files = LwjglFiles()
      Gdx.graphics = mock()
      Gdx.gl20 = mock()
      Gdx.app = mock()
      Gdx.gl = mock()

      // Includes a default skin filled with styles of all Scene2D widgets:
      VisUI.load()
    }

    afterSpec {
      if (activeSpecs.decrementAndGet() > 0) return@afterSpec

      Gdx.graphics = null
      Gdx.files = null
      Gdx.gl20 = null
      Gdx.app = null
      Gdx.gl = null

      VisUI.dispose()
    }

    beforeTest {
      Scene2DSkin.defaultSkin = VisUI.getSkin()
    }
  }
}
