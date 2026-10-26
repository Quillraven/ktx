package ktx.textratypist

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Align
import com.github.tommyettinger.textra.FWSkin
import com.github.tommyettinger.textra.TextraLabel
import com.github.tommyettinger.textra.TypingLabel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import ktx.scene2d.ApplicationTest
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.scene2d
import ktx.scene2d.stack
import ktx.scene2d.table
import ktx.scene2d.tree

class BaseFactoryTest : ApplicationTest() {
  private fun <T : Actor> checkWidget(
    validate: (T) -> Unit = {},
    widget: KWidget<*>.() -> T?,
  ) {
    // Using a parental widget that allows to use invoke actors' factory methods:
    val parent = scene2d.table()
    // Invoking widget-specific factory method:
    val child = parent.widget()
    // Ensuring the child is not null and owned by the parent:
    child.shouldNotBeNull()
    (child in parent.children) shouldBe true
    validate(child!!) // Additional validation specific to widget.
  }

  init {
    beforeTest {
      // TextraTypist requires a skin with textra label styles defined. This requires loading a skin with either
      // FWSkin or FreeTypist.
      Scene2DSkin.defaultSkin = FWSkin(Gdx.files.internal("test-skin.json"))
    }

    afterTest {
      Scene2DSkin.defaultSkin.dispose()
    }

    test("should be able to create TextraLabel without init block") {

      checkWidget(
        widget = { textraLabel("TextraTest.") },
        validate = {
          it.storedText.toString() shouldBe "TextraTest."
        },
      )
    }

    test("should be able to create TypingLabel without init block") {

      checkWidget(
        widget = { typingLabel("TypingTest.") },
        validate = {
          it.storedText.toString() shouldBe "TypingTest."
        },
      )
    }

    test("should be able to create TextraLabel with init block") {

      checkWidget(
        widget = { textraLabel("TextraTest.") { alignment = Align.bottom } },
        validate = {
          it.storedText.toString() shouldBe "TextraTest."
          it.alignment shouldBe Align.bottom
        },
      )
    }

    test("should be able to create TypingLabel with init block") {

      checkWidget(
        widget = { typingLabel("TypingTest.") { alignment = Align.bottom } },
        validate = {
          it.storedText.toString() shouldBe "TypingTest."
          it.alignment shouldBe Align.bottom
        },
      )
    }
  }
}

class InlinedFactoryTest : ApplicationTest() {
  init {
    beforeTest {
      Scene2DSkin.defaultSkin = FWSkin(Gdx.files.internal("test-skin.json"))
    }

    afterTest {
      Scene2DSkin.defaultSkin.dispose()
    }

    // Tests that textraLabel and typingLabel work within the generic [KWidget] API
    test("textraLabel within a stack") {
      scene2d.stack {
        // In regular groups, children blocks point to the new actor as both 'this' and 'it'.
        textraLabel("Actor") {
          // No specialized storage objects - 'it' should point to the actor itself:
          (it is TextraLabel) shouldBe true
          (it as TextraLabel).storedText.toString() shouldBe "Actor"
        }
      }
    }

    test("textraLabel within a table") {
      scene2d.table {
        // In table-based groups, children blocks point to the new actor as 'this' and its cell as 'it'.
        textraLabel("Cell") {
          // Actors stored in cells:
          (it.actor is TextraLabel) shouldBe true
        }
      }
    }

    test("textraLabel within a tree") {
      scene2d.tree {
        // In trees, children blocks point to the new actor as 'this' and node is the lambda parameter.
        textraLabel("Node") { node ->
          // Actors stored in tree nodes:
          (node.actor is TextraLabel) shouldBe true
          node {
            textraLabel("NestedNode") {
              (it.actor is TextraLabel) shouldBe true
              (it.actor as TextraLabel).storedText.toString() shouldBe "NestedNode"
            }
          }
        }
      }
    }

    test("typingLabel within a stack") {
      scene2d.stack {
        // In regular groups, children blocks point to the new actor as both 'this' and 'it'.
        typingLabel("Actor") {
          // No specialized storage objects - 'it' should point to the actor itself:
          (it is TypingLabel) shouldBe true
          (it as TypingLabel).storedText.toString() shouldBe "Actor"
        }
      }
    }

    test("typingLabel within a table") {
      scene2d.table {
        // In table-based groups, children blocks point to the new actor as 'this' and its cell as 'it'.
        typingLabel("Cell") {
          // Actors stored in cells:
          (it.actor is TypingLabel) shouldBe true
        }
      }
    }

    test("typingLabel within a tree") {
      scene2d.tree {
        // In trees, children blocks point to the new actor as 'this' and node is the lambda parameter.
        typingLabel("Node") { node ->
          // Actors stored in tree nodes:
          (node.actor is TypingLabel) shouldBe true
          node {
            typingLabel("NestedNode") {
              (it.actor is TypingLabel) shouldBe true
              (it.actor as TypingLabel).storedText.toString() shouldBe "NestedNode"
            }
          }
        }
      }
    }
  }
}
