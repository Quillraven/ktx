package ktx.scene2d

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node
import com.kotcrab.vis.ui.VisUI
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.mockito.kotlin.mock
import com.badlogic.gdx.utils.Array as GdxArray

/**
 * Tests [RootWidget] factory methods for actors that are usually added directly to a stage.
 * Leverages [scene2d], as it implements [RootWidget] and does not modify the created actors.
 */
class RootActorFactoriesTest : ApplicationTest() {
  init {
    test("should create Window") {
      val window = scene2d.window(title = "test")

      window.shouldNotBeNull()
      window.skin shouldBe Scene2DSkin.defaultSkin
      window.titleLabel.text.toString() shouldBe "test"
    }

    test("should create Window with init block") {
      val window =
        scene2d.window(title = "test") {
          height = 100f
        }

      window.shouldNotBeNull()
      window.skin shouldBe Scene2DSkin.defaultSkin
      window.titleLabel.text.toString() shouldBe "test"
      window.height shouldBe (100f plusOrMinus TOLERANCE)
    }

    test("should create Window with nested children") {
      val label: Label

      val window =
        scene2d.window(title = "test") {
          label = label("Test")
        }

      (label in window.children) shouldBe true
    }

    test("should create Dialog") {
      val dialog = scene2d.dialog(title = "test")

      dialog.shouldNotBeNull()
      dialog.skin shouldBe Scene2DSkin.defaultSkin
      dialog.titleLabel.text.toString() shouldBe "test"
    }

    test("should create Dialog with init block") {
      val dialog =
        scene2d.dialog(title = "test") {
          height = 100f
        }

      dialog.shouldNotBeNull()
      dialog.skin shouldBe Scene2DSkin.defaultSkin
      dialog.titleLabel.text.toString() shouldBe "test"
      dialog.height shouldBe (100f plusOrMinus TOLERANCE)
    }

    test("should create Dialog with nested children") {
      val label: Label

      val dialog =
        scene2d.dialog(title = "test") {
          label = label("Test")
        }

      (label in dialog.children) shouldBe true
    }
  }
}

/**
 * Tests factory methods without init blocks.
 */
class NoInitBlockActorFactoriesTest : ApplicationTest() {
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
    test("should create Button") {
      checkWidget { button() }
    }

    test("should create ButtonGroup") {
      checkWidget { buttonGroup(minCheckedCount = 1, maxCheckedCount = 2) }
    }

    test("should create CheckBox") {

      checkWidget(
        widget = { checkBox("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create Container") {
      checkWidget { container() }
    }

    test("should create HorizontalGroup") {
      checkWidget { horizontalGroup() }
    }

    test("should create Image with drawable name") {

      checkWidget(
        widget = { image(drawableName = "button") },
        validate = {
          it.drawable shouldBe VisUI.getSkin().getDrawable("button")
        },
      )
    }

    test("should create Image with nine patch") {
      checkWidget { image(VisUI.getSkin().getPatch("button")) }
    }

    test("should create Image with texture region") {
      checkWidget { image(VisUI.getSkin().getRegion("button")) }
    }

    test("should create Image with texture") {
      checkWidget { image(VisUI.getSkin().getRegion("button").texture) }
    }

    test("should create Image with drawable") {

      checkWidget(
        widget = { image(VisUI.getSkin().getDrawable("button")) },
        validate = {
          it.drawable shouldBeSameInstanceAs VisUI.getSkin().getDrawable("button")
        },
      )
    }

    test("should create Image with null drawable passing null argument") {

      checkWidget(
        widget = { image(drawable = null) },
        validate = {
          it.drawable.shouldBeNull()
        },
      )
    }

    test("should create Image with null drawable passing no arguments") {

      checkWidget(
        widget = { image() },
        validate = {
          it.drawable.shouldBeNull()
        },
      )
    }

    test("should create ImageButton") {
      checkWidget { imageButton() }
    }

    test("should create ImageTextButton") {

      checkWidget(
        widget = { imageTextButton("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create Label") {

      checkWidget(
        widget = { label("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create List") {
      checkWidget { listWidgetOf<String>() }
    }

    test("should create List with items") {

      checkWidget(
        widget = { listWidgetOf(GdxArray.with("one", "two", "three")) },
        validate = {
          it.items shouldBe GdxArray.with("one", "two", "three")
        },
      )
    }

    test("should create ProgressBar") {

      checkWidget(
        widget = { progressBar(min = 1f, max = 2f, step = 0.5f) },
        validate = {
          it.minValue shouldBe (1f plusOrMinus TOLERANCE)
          it.maxValue shouldBe (2f plusOrMinus TOLERANCE)
          it.stepSize shouldBe (0.5f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create ScrollPane") {
      checkWidget { scrollPane() }
    }

    test("should create SelectBox") {
      checkWidget { selectBoxOf<String>() }
    }

    test("should create SelectBox with items") {

      checkWidget(
        widget = { selectBoxOf(GdxArray.with("one", "two", "three")) },
        validate = {
          it.items shouldBe GdxArray.with("one", "two", "three")
        },
      )
    }

    test("should create Slider") {

      checkWidget(
        widget = { slider(min = 1f, max = 2f, step = 0.5f) },
        validate = {
          it.minValue shouldBe (1f plusOrMinus TOLERANCE)
          it.maxValue shouldBe (2f plusOrMinus TOLERANCE)
          it.stepSize shouldBe (0.5f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create SplitPane") {
      checkWidget { splitPane() }
    }

    test("should create Stack") {
      checkWidget { stack() }
    }

    test("should create Table") {
      checkWidget { table() }
    }

    test("should create TextArea") {

      checkWidget(
        widget = { textArea("Test.") },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create TextButton") {

      checkWidget(
        widget = { textButton("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create TextField") {

      checkWidget(
        widget = { textField("Test.") },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create Touchpad") {
      checkWidget { touchpad(deadzone = 10f) }
    }

    test("should create Tree") {
      checkWidget { tree() }
    }

    test("should create VerticalGroup") {
      checkWidget { verticalGroup() }
    }
  }
}

/**
 * Tests inlined factory methods with init blocks.
 */
class InlinedInitBlockActorFactoriesTest : ApplicationTest() {
  private fun <T : Actor> checkWidget(
    validate: (T) -> Unit = {},
    widget: KWidget<Cell<*>>.() -> T?,
  ) {
    // Creating builder context that allows to use actors factory methods:
    val parent = scene2d.table()

    // Invoking widget-specific factory method:
    val child = parent.widget()

    // Ensuring the child is not null and owned by the parent:
    child.shouldNotBeNull()
    (child in parent.children) shouldBe true
    withClue("For the purpose of this test, the actor must include 'color = Color.BLUE' in its init block.") {
      child!!.color shouldBe
        Color.BLUE
    }
    validate(child) // Performing widget-specific validation.
  }

  init {
    // Tests and showcases how generic [KWidget] API - and its additional inlined methods - give access to specific actor
    // storage objects like [Cell] or [Node] through init block parameter.
    test("should give access to widget specific storage objects") {
      scene2d.stack {
        // In regular groups, children blocks point to the new actor as both 'this' and 'it'.
        label("Actor") {
          // No specialized storage objects - 'it' should point to the actor itself:
          (it is Label) shouldBe true
          (it as Label).text.toString() shouldBe "Actor"
        }
      }
      scene2d.table {
        // In table-based groups, children blocks point to the new actor as 'this' and its cell as 'it'.
        label("Cell") {
          // Actors stored in cells:
          (it.actor is Label) shouldBe true
        }
      }
      scene2d.tree {
        // In trees, children blocks point to the new actor as 'this' and node is the lambda parameter.
        label("Node") { node ->
          // Actors stored in tree nodes:
          (node.actor is Label) shouldBe true
          node {
            label("NestedNode") {
              (it.actor is Label) shouldBe true
              (it.actor as Label).text.toString() shouldBe "NestedNode"
            }
          }
        }
      }
    }

    test("should create Button") {

      checkWidget {
        button {
          color = Color.BLUE
        }
      }
    }

    test("should create ButtonGroup") {

      checkWidget {
        buttonGroup(minCheckedCount = 1, maxCheckedCount = 2) {
          color = Color.BLUE
        }
      }
    }

    test("should create CheckBox") {

      checkWidget(
        widget = {
          checkBox("Test.") {
            color = Color.BLUE
          }
        },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create Container") {

      checkWidget {
        container {
          color = Color.BLUE
        }
      }
    }

    test("should create Container with a custom actor") {

      checkWidget(
        widget = {
          container(scene2d.label("Test.")) {
            color = Color.BLUE
          }
        },
        validate = {
          (it.actor is Label) shouldBe true
          val label = it.actor as Label
          label.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create HorizontalGroup") {

      checkWidget {
        horizontalGroup {
          color = Color.BLUE
        }
      }
    }

    test("should create Image") {

      checkWidget(
        widget = {
          image(drawableName = "button") {
            color = Color.BLUE
          }
        },
        validate = {
          it.drawable shouldBe VisUI.getSkin().getDrawable("button")
        },
      )
    }

    test("should create ImageButton") {

      checkWidget {
        imageButton {
          color = Color.BLUE
        }
      }
    }

    test("should create ImageTextButton") {

      checkWidget(
        widget = {
          imageTextButton("Test.") {
            color = Color.BLUE
          }
        },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create Label") {

      checkWidget(
        widget = {
          label("Test.") {
            color = Color.BLUE
          }
        },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create List") {

      checkWidget(
        widget = {
          listWidget<String> {
            color = Color.BLUE
            // Adding list items:
            -"one"
            -"two"
            -"three"
          }
        },
        validate = {
          it.items shouldBe GdxArray.with("one", "two", "three")
        },
      )
    }

    test("should create ParticleEffectActor") {

      checkWidget(
        widget = {
          particleEffect(mock(), resetOnStart = true) {
            color = Color.BLUE
          }
        },
        validate = {
          it.isResetOnStart shouldBe true
        },
      )
    }

    test("should create ProgressBar") {

      checkWidget(
        widget = {
          progressBar(min = 1f, max = 2f, step = 0.5f) {
            color = Color.BLUE
          }
        },
        validate = {
          it.minValue shouldBe (1f plusOrMinus TOLERANCE)
          it.maxValue shouldBe (2f plusOrMinus TOLERANCE)
          it.stepSize shouldBe (0.5f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create ScrollPane") {

      checkWidget {
        scrollPane {
          color = Color.BLUE
        }
      }
    }

    test("should create SelectBox") {

      checkWidget(
        widget = {
          selectBox<String> {
            color = Color.BLUE
            // Adding select box items:
            -"one"
            -"two"
            -"three"
          }
        },
        validate = {
          it.items shouldBe GdxArray.with("one", "two", "three")
        },
      )
    }

    test("should create Slider") {

      checkWidget(
        widget = {
          slider(min = 1f, max = 2f, step = 0.5f) {
            color = Color.BLUE
          }
        },
        validate = {
          it.minValue shouldBe (1f plusOrMinus TOLERANCE)
          it.maxValue shouldBe (2f plusOrMinus TOLERANCE)
          it.stepSize shouldBe (0.5f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create SplitPane") {

      checkWidget {
        splitPane {
          color = Color.BLUE
        }
      }
    }

    test("should create Stack") {

      checkWidget {
        stack {
          color = Color.BLUE
        }
      }
    }

    test("should create Table") {

      checkWidget(
        widget = {
          table {
            color = Color.BLUE
          }
        },
        validate = {
          it.skin shouldBe Scene2DSkin.defaultSkin
        },
      )
    }

    test("should create TextArea") {

      checkWidget(
        widget = {
          textArea("Test.") {
            color = Color.BLUE
          }
        },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create TextButton") {

      checkWidget(
        widget = {
          textButton("Test.") {
            color = Color.BLUE
          }
        },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create TextField") {

      checkWidget(
        widget = {
          textField("Test.") {
            color = Color.BLUE
          }
        },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create Touchpad") {

      checkWidget {
        touchpad(deadzone = 10f) {
          color = Color.BLUE
        }
      }
    }

    test("should create Tree") {

      checkWidget {
        tree {
          color = Color.BLUE
        }
      }
    }

    test("should create VerticalGroup") {

      checkWidget {
        verticalGroup {
          color = Color.BLUE
        }
      }
    }
  }
}
