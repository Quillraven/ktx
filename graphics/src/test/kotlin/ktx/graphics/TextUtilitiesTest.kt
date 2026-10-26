package ktx.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglFiles
import com.badlogic.gdx.graphics.g2d.BitmapFont
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import com.badlogic.gdx.utils.Array as GdxArray

class TextUtilitiesTest : FunSpec({
  beforeTest {
    Gdx.files = LwjglFiles()
  }

  test("should center text on a rectangle") {
    val font = FakeFont()
    val width = 100f
    val height = 200f

    val position = font.center("text", width, height)

    position.x shouldBe (38.5f plusOrMinus 0.1f)
    position.y shouldBe (105.5f plusOrMinus 0.1f)
  }

  test("should center text on a rectangle at given position") {
    val font = FakeFont()

    val position = font.center("text", x = 100f, y = 200f, width = 100f, height = 200f)

    position.x shouldBe (138.5f plusOrMinus 0.1f)
    position.y shouldBe (305.5f plusOrMinus 0.1f)
  }

  afterTest {
    Gdx.files = null
  }
})

/**
 * Loads the .fnt settings file of the default libGDX Arial font, but
 * omits loading the textures. For testing purposes.
 */
class FakeFont :
  BitmapFont(
    BitmapFontData(Gdx.files.classpath("com/badlogic/gdx/utils/lsans-15.fnt"), true),
    GdxArray.with(mock()),
    true,
  ) {
  override fun load(data: BitmapFontData?) {
    // Do nothing.
  }
}
