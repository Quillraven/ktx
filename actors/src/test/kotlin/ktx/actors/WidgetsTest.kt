package ktx.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglFiles
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class WidgetsTest : FunSpec({
  beforeTest {
    // Mocking BitmapFont is tedious, if not next to impossible, because of direct fields usage in related classes
    // constructors. Label will not successfully construct an instance without a BitmapFont.
    LwjglNativesLoader.load()

    Gdx.graphics = mock()
    Gdx.app = mock()
    Gdx.gl20 = mock()
    Gdx.files = LwjglFiles()
    Gdx.gl = Gdx.gl20
  }

  test("should read text property of a Label") {
    val label = Label("text", LabelStyle(BitmapFont(), Color.BLACK))

    label.txt shouldBe "text"
  }

  test("should write text property of a Label") {
    val label = Label("text", LabelStyle(BitmapFont(), Color.BLACK))

    label.txt = "replaced"

    val text = label.text.toString()
    text shouldBe "replaced"
  }

  test("should read text property of a TextButton") {
    val button = TextButton("text", TextButtonStyle().apply { font = BitmapFont() })

    button.txt shouldBe "text"
  }

  test("should write text property of a TextButton") {
    val button = TextButton("text", TextButtonStyle().apply { font = BitmapFont() })

    button.txt = "replaced"

    val text = button.text.toString()
    text shouldBe "replaced"
  }
})
