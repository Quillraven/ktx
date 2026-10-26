package ktx.app

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.GdxRuntimeException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * Tests [KtxGame]: KTX equivalent of [com.badlogic.gdx.Game].
 */
class KtxGameTest : FunSpec({
  beforeTest {
    Gdx.gl20 = mock()
    Gdx.gl = Gdx.gl20
  }

  test("should display firstScreen without registration") {
    val screen = mock<Screen>()
    val game = KtxGame(firstScreen = screen)
    Gdx.graphics =
      mock {
        on(it.width) doReturn 800
        on(it.height) doReturn 600
      }

    game.create()

    game.shownScreen shouldBeSameInstanceAs screen
    verify(screen).show()
    verify(screen).resize(800, 600)
    // addScreen must be called manually to keep firstScreen in context - should not contain initial Screen:
    game.containsScreen<Screen>() shouldBe false
  }

  test("should clear screen on render") {
    val game = KtxGame(firstScreen = MockScreen())
    Gdx.graphics = mock()

    game.render()

    verify(Gdx.gl).glClearColor(0f, 0f, 0f, 1f)
    verify(Gdx.gl).glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
  }

  test("should not clear screen on render if screen clearing is turned off") {
    val game = KtxGame(clearScreen = false, firstScreen = MockScreen())
    Gdx.graphics = mock()

    game.render()

    verify(Gdx.gl, never()).glClearColor(any(), any(), any(), any())
    verify(Gdx.gl, never()).glClear(any())
  }

  test("should delegate resize call to current screen") {
    val screen = mock<Screen>()
    val game = KtxGame(screen)

    game.resize(100, 200)

    verify(screen).resize(100, 200)
  }

  test("should delegate pause call to current screen") {
    val screen = mock<Screen>()
    val game = KtxGame(screen)

    game.pause()

    verify(screen).pause()
  }

  test("should delegate resume call to current screen") {
    val screen = mock<Screen>()
    val game = KtxGame(screen)

    game.resume()

    verify(screen).resume()
  }

  test("should fail to provide non-registered Screen") {
    shouldThrow<GdxRuntimeException> {
      val game = KtxGame<Screen>()

      game.getScreen<KtxScreen>()
    }
  }

  test("should register Screen instance") {
    val screen = mock<KtxScreen>()
    val game = KtxGame<Screen>()

    game.addScreen(screen)

    game.containsScreen<KtxScreen>() shouldBe true
    game.getScreen<KtxScreen>() shouldBeSameInstanceAs screen
  }

  test("should fail to register Screen instance to same type multiple times") {
    shouldThrow<GdxRuntimeException> {
      val game = KtxGame<Screen>()

      game.addScreen(mock<KtxScreen>())
      game.addScreen(mock<KtxScreen>())
    }
  }

  test("should not throw exception when trying to remove unregistered Screen") {
    val game = KtxGame<Screen>()

    val removed = game.removeScreen<KtxScreen>()

    removed.shouldBeNull()
    game.containsScreen<KtxScreen>() shouldBe false
  }

  test("should remove Screen") {
    val screen = mock<KtxScreen>()
    val game = KtxGame<Screen>()
    game.addScreen(screen)

    val removed = game.removeScreen<KtxScreen>()

    removed shouldBeSameInstanceAs screen
    game.containsScreen<KtxScreen>() shouldBe false
    verify(screen, never()).dispose() // Should not dispose of Screen upon removal.
  }

  test("should reassign Screen") {
    val game = KtxGame<Screen>()
    val initialScreen = mock<KtxScreen>()
    val replacement = mock<KtxScreen>()
    game.addScreen(initialScreen)

    game.removeScreen<KtxScreen>()
    game.addScreen(replacement)

    game.containsScreen<KtxScreen>() shouldBe true
    game.getScreen<KtxScreen>() shouldBeSameInstanceAs replacement
    verify(initialScreen, never()).dispose() // Should not dispose of previous KtxScreen upon removal.
  }

  test("should set current Screen") {
    val firstScreen = mock<Screen>()
    val secondScreen = mock<KtxScreen>()
    val game = KtxGame(firstScreen)
    game.addScreen(secondScreen)
    Gdx.graphics =
      mock {
        on(it.width) doReturn 800
        on(it.height) doReturn 600
      }

    game.setScreen<KtxScreen>()

    game.shownScreen shouldBeSameInstanceAs secondScreen
    verify(firstScreen).hide()
    verify(secondScreen).show()
    verify(secondScreen).resize(800, 600)
  }

  test("should fail to set unregistered Screen") {
    shouldThrow<GdxRuntimeException> {
      val game = KtxGame<Screen>()

      game.setScreen<MockScreen>()
    }
  }

  test("should dispose of all registered Screen instances") {
    val screen = mock<Screen>()
    val ktxScreen = mock<KtxScreen>()
    val game = KtxGame<Screen>()
    game.addScreen(screen)
    game.addScreen(ktxScreen)

    game.dispose()

    verify(screen).dispose()
    verify(ktxScreen).dispose()
  }

  test("should dispose of all registered Screen instances with error handling") {
    Gdx.app = mock()
    val screen = mock<Screen>()
    val ktxScreen =
      mock<KtxScreen> {
        on(it.dispose()) doThrow GdxRuntimeException("Expected.")
      }
    val mockScreen =
      mock<MockScreen> {
        on(it.dispose()) doThrow GdxRuntimeException("Expected.")
      }
    val game = KtxGame<Screen>()
    game.addScreen(screen)
    game.addScreen(ktxScreen)
    game.addScreen(mockScreen)

    game.dispose()

    verify(screen).dispose()
    verify(ktxScreen).dispose()
    // Ensures exceptions were logged:
    verify(Gdx.app, times(2)).error(eq("KTX"), any(), argThat { this is GdxRuntimeException })
  }

  afterTest {
    Gdx.graphics = null
    Gdx.gl = null
    Gdx.gl20 = null
    Gdx.app = null
  }
})

/** [Screen] implementation that tracks how many times it was rendered. */
open class MockScreen : KtxScreen {
  private var rendered = false
  private var renderedTimes = 0

  override fun render(delta: Float) {
    rendered = true
    renderedTimes++
  }
}

/**
 * Tests [Screen] utilities.
 */
class KtxScreenTest : FunSpec({
  test("should provide mock-up screen instance") {
    val screen = emptyScreen()

    Screen::class.isInstance(screen) shouldBe true
  }
})

@Suppress("unused", "ClassName")
class `should implement KtxScreen with no methods overridden` : KtxScreen {
  // Guarantees all KtxScreen methods are optional to implement.
}
