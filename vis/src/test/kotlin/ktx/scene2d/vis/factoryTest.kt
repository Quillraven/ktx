package ktx.scene2d.vis

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter
import com.kotcrab.vis.ui.widget.ButtonBar
import com.kotcrab.vis.ui.widget.VisDialog
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisWindow
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.toast.ToastTable
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import ktx.scene2d.ApplicationTest
import ktx.scene2d.KWidget
import ktx.scene2d.TOLERANCE
import ktx.scene2d.actors
import ktx.scene2d.scene2d
import ktx.scene2d.table
import org.mockito.kotlin.mock
import com.badlogic.gdx.utils.Array as GdxArray

class TopLevelActorFactoriesTest : ApplicationTest() {
  init {
    test("should create VisWindow") {
      val window = scene2d.visWindow(title = "test")

      window.shouldNotBeNull()
      window.titleLabel.text.toString() shouldBe "test"
    }

    test("should create VisWindow with init block") {
      val window =
        scene2d.visWindow(title = "test") {
          height = 100f
        }

      window.shouldNotBeNull()
      window.titleLabel.text.toString() shouldBe "test"
      window.height shouldBe (100f plusOrMinus TOLERANCE)
    }

    test("should create VisWindow with nested children") {
      lateinit var label: VisLabel

      val window =
        scene2d.visWindow(title = "test") {
          label = visLabel("Test")
        }

      (label in window.children) shouldBe true
    }

    test("should create VisDialog") {
      val dialog = scene2d.visDialog(title = "test")

      dialog.shouldNotBeNull()
      dialog.titleLabel.text.toString() shouldBe "test"
    }

    test("should create VisDialog with init block") {
      val dialog =
        scene2d.visDialog(title = "test") {
          height = 100f
        }

      dialog.shouldNotBeNull()
      dialog.titleLabel.text.toString() shouldBe "test"
      dialog.height shouldBe (100f plusOrMinus TOLERANCE)
    }

    test("should create VisDialog with nested children") {
      lateinit var label: VisLabel

      val dialog =
        scene2d.visDialog(title = "test") {
          label = visLabel("Test")
        }

      (label in dialog.children) shouldBe true
    }

    test("should create ToastTable") {
      val table = scene2d.toastTable()

      table.shouldNotBeNull()
      table.skin shouldBeSameInstanceAs VisUI.getSkin()
    }

    test("should create ToastTable with init block") {
      val table =
        scene2d.toastTable {
          height = 100f
        }

      table.shouldNotBeNull()
      table.height shouldBe (100f plusOrMinus TOLERANCE)
    }

    test("should create ToastTable with nested children") {
      lateinit var label: VisLabel

      val table =
        scene2d.toastTable {
          label = visLabel("Test")
        }

      (label in table.children) shouldBe true
    }

    test("should add top-level actors to Stage") {
      val stage = Stage(mock(), mock())
      lateinit var window: VisWindow
      lateinit var dialog: VisDialog
      lateinit var table: ToastTable
      lateinit var label: VisLabel

      stage.actors {
        window = visWindow("Test")
        dialog = visDialog("Test")
        table =
          toastTable {
            label = visLabel("Test")
          }
      }

      (window in stage.actors) shouldBe true
      (window in stage.root.children) shouldBe true
      (dialog in stage.actors) shouldBe true
      (dialog in stage.root.children) shouldBe true
      (table in stage.actors) shouldBe true
      (table in stage.root.children) shouldBe true
      (label in stage.actors) shouldBe false
      (label in stage.root.children) shouldBe false
      (label in table.children) shouldBe true
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
    val parent = scene2d.visTable()

    // Invoking widget-specific factory method:
    val child = parent.widget()

    // Ensuring the child is not null and owned by the parent:
    child.shouldNotBeNull()
    (child in parent.children) shouldBe true
    validate(child!!) // Additional validation specific to widget.
  }

  init {
    test("should create VisLabel") {

      checkWidget(
        widget = { visLabel("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create LinkLabel") {

      checkWidget(
        widget = { linkLabel("Text.", "URL") },
        validate = {
          it.text.toString() shouldBe "Text."
          it.url shouldBe "URL"
        },
      )
    }

    test("should create VisImage with drawable") {

      checkWidget(
        widget = { visImage(VisUI.getSkin().getDrawable("button")) },
        validate = {
          it.drawable shouldBeSameInstanceAs VisUI.getSkin().getDrawable("button")
        },
      )
    }

    test("should create VisImage with drawable name") {

      checkWidget(
        widget = { visImage("button") },
        validate = {
          it.drawable shouldBeSameInstanceAs VisUI.getSkin().getDrawable("button")
        },
      )
    }

    test("should create VisImage with nine patch") {
      checkWidget { visImage(VisUI.getSkin().getPatch("button")) }
    }

    test("should create VisImage with texture") {
      checkWidget { visImage(VisUI.getSkin().getRegion("button").texture) }
    }

    test("should create VisImage with texture region") {
      checkWidget { visImage(VisUI.getSkin().getRegion("button")) }
    }

    test("should create VisList") {
      checkWidget { visListOf<String>() }
    }

    test("should create VisList with items") {

      checkWidget(
        widget = { visListOf(GdxArray.with("one", "two", "three")) },
        validate = {
          it.items shouldBe GdxArray.with("one", "two", "three")
        },
      )
    }

    test("should create VisProgressBar") {

      checkWidget(
        widget = { visProgressBar(min = 1f, max = 2f, step = 0.5f) },
        validate = {
          it.minValue shouldBe (1f plusOrMinus TOLERANCE)
          it.maxValue shouldBe (2f plusOrMinus TOLERANCE)
          it.stepSize shouldBe (0.5f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create VisSelectBox") {
      checkWidget { visSelectBoxOf<String>() }
    }

    test("should create VisSelectBox with items") {

      checkWidget(
        widget = { visSelectBoxOf(GdxArray.with("one", "two", "three")) },
        validate = {
          it.items shouldBe GdxArray.with("one", "two", "three")
        },
      )
    }

    test("should create VisSlider") {

      checkWidget(
        widget = { visSlider(min = 1f, max = 2f, step = 0.5f) },
        validate = {
          it.minValue shouldBe (1f plusOrMinus TOLERANCE)
          it.maxValue shouldBe (2f plusOrMinus TOLERANCE)
          it.stepSize shouldBe (0.5f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create VisTextArea") {

      checkWidget(
        widget = { visTextArea("Test.") },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create HighlightTextArea") {

      checkWidget(
        widget = { highlightTextArea("Test.") },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create ScrollableTextArea") {

      checkWidget(
        widget = { scrollableTextArea("Test.") },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create VisTextField") {

      checkWidget(
        widget = { visTextField("Test.") },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create ValidatableTextField") {

      checkWidget(
        widget = { visValidatableTextField("Test.") },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create BusyBar") {
      checkWidget { busyBar() }
    }

    test("should create Separator") {
      checkWidget { separator() }
    }

    test("should create VisTextButton") {

      checkWidget(
        widget = { visTextButton("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create VisImageButton") {
      checkWidget { visImageButton() }
    }

    test("should create VisImageTextButton") {

      checkWidget(
        widget = { visImageTextButton("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create VisCheckBox") {

      checkWidget(
        widget = { visCheckBox("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create VisRadioButton") {

      checkWidget(
        widget = { visRadioButton("Test.") },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create VisTree") {
      checkWidget { visTree() }
    }

    // Unable to compile shader in test environment.
    test("should create BasicColorPicker").config(enabled = false) { checkWidget { basicColorPicker() } }

    // Unable to compile shader in test environment.
    test("should create ExtendedColorPicker").config(enabled = false) { checkWidget { extendedColorPicker() } }

    test("should create Spinner") {

      checkWidget(
        widget = { spinner("Name", IntSpinnerModel(0, 0, 10)) },
        validate = {
          (it.children[0] as Label).text.toString() shouldBe "Name"
        },
      )
    }

    test("should create VisTable") {
      checkWidget { visTable() }
    }

    test("should create HorizontalFlowGroup") {

      checkWidget(
        widget = { horizontalFlowGroup(spacing = 10f) },
        validate = {
          it.spacing shouldBe (10f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create VerticalFlowGroup") {

      checkWidget(
        widget = { verticalFlowGroup(spacing = 10f) },
        validate = {
          it.spacing shouldBe (10f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create FlowGroup") {

      checkWidget(
        widget = { flowGroup(vertical = true, spacing = 10f) },
        validate = {
          it.isVertical shouldBe true
          it.spacing shouldBe (10f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create GridGroup") {

      checkWidget(
        widget = { gridGroup(itemSize = 100f, spacing = 10f) },
        validate = {
          it.spacing shouldBe (10f plusOrMinus TOLERANCE)
          it.itemWidth shouldBe (100f plusOrMinus TOLERANCE)
          it.itemHeight shouldBe (100f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create FloatingGroup") {
      checkWidget { floatingGroup() }
    }

    test("should create DragPane") {
      checkWidget { dragPane() }
    }

    test("should create VisScrollPane") {
      checkWidget { visScrollPane() }
    }

    test("should create VisSplitPane") {
      checkWidget { visSplitPane() }
    }

    test("should create MultiSplitPane") {
      checkWidget { multiSplitPane() }
    }

    test("should create CollapsibleWidget") {
      checkWidget { collapsible() }
    }

    test("should create HorizontalCollapsibleWidget") {
      checkWidget { horizontalCollapsible() }
    }

    test("should create ButtonBar") {
      val parent = scene2d.visTable()

      val buttonBar = parent.buttonBar()

      buttonBar.shouldNotBeNull()
      parent.children.size shouldBe 1
      (parent.children[0] is Table) shouldBe true
    }

    test("should create ButtonBar with order") {
      val parent = scene2d.visTable()

      val buttonBar = parent.buttonBar(ButtonBar.LINUX_ORDER)

      buttonBar.shouldNotBeNull()
      parent.children.size shouldBe 1
      (parent.children[0] is Table) shouldBe true
    }

    test("should create ListView") {
      val parent = scene2d.visTable()

      val listView = parent.listView(SimpleListAdapter(GdxArray.with("1", "2")))

      listView.shouldNotBeNull()
      listView.adapter.iterable().toList() shouldBe listOf("1", "2")
      (listView.mainTable in parent.children) shouldBe true
    }

    test("should create TabbedPane") {
      val parent = scene2d.visTable()

      val pane = parent.tabbedPane()

      pane.shouldNotBeNull()
      (pane.table in parent.children) shouldBe true
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
    test("should create VisLabel") {

      checkWidget(
        widget = { visLabel("Test.") { color = Color.BLUE } },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create LinkLabel") {

      checkWidget(
        widget = { linkLabel("Text.", "URL") { color = Color.BLUE } },
        validate = {
          it.text.toString() shouldBe "Text."
          it.url shouldBe "URL"
        },
      )
    }

    test("should create VisImage with drawable") {

      checkWidget(
        widget = { visImage(VisUI.getSkin().getDrawable("button")) { color = Color.BLUE } },
        validate = {
          it.drawable shouldBeSameInstanceAs VisUI.getSkin().getDrawable("button")
        },
      )
    }

    test("should create VisImage with drawable name") {

      checkWidget(
        widget = { visImage("button") { color = Color.BLUE } },
        validate = {
          it.drawable shouldBeSameInstanceAs VisUI.getSkin().getDrawable("button")
        },
      )
    }

    test("should create VisImage with nine patch") {

      checkWidget {
        visImage(VisUI.getSkin().getPatch("button")) { color = Color.BLUE }
      }
    }

    test("should create VisImage with texture") {

      checkWidget {
        visImage(VisUI.getSkin().getRegion("button").texture) { color = Color.BLUE }
      }
    }

    test("should create VisImage with texture region") {

      checkWidget {
        visImage(VisUI.getSkin().getRegion("button")) { color = Color.BLUE }
      }
    }

    test("should create VisList") {

      checkWidget(
        widget = {
          visList<String> {
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

    test("should create VisProgressBar") {

      checkWidget(
        widget = { visProgressBar(min = 1f, max = 2f, step = 0.5f) { color = Color.BLUE } },
        validate = {
          it.minValue shouldBe (1f plusOrMinus TOLERANCE)
          it.maxValue shouldBe (2f plusOrMinus TOLERANCE)
          it.stepSize shouldBe (0.5f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create VisSelectBox") {

      checkWidget(
        widget = {
          visSelectBox<String> {
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

    test("should create VisSlider") {

      checkWidget(
        widget = { visSlider(min = 1f, max = 2f, step = 0.5f) { color = Color.BLUE } },
        validate = {
          it.minValue shouldBe (1f plusOrMinus TOLERANCE)
          it.maxValue shouldBe (2f plusOrMinus TOLERANCE)
          it.stepSize shouldBe (0.5f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create VisTextArea") {

      checkWidget(
        widget = { visTextArea("Test.") { color = Color.BLUE } },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create HighlightTextArea") {

      checkWidget(
        widget = { highlightTextArea("Test.") { color = Color.BLUE } },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create ScrollableTextArea") {

      checkWidget(
        widget = { scrollableTextArea("Test.") { color = Color.BLUE } },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create VisTextField") {

      checkWidget(
        widget = { visTextField("Test.") { color = Color.BLUE } },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create ValidatableTextField") {

      checkWidget(
        widget = { visValidatableTextField("Test.") { color = Color.BLUE } },
        validate = {
          it.text shouldBe "Test."
        },
      )
    }

    test("should create BusyBar") {
      checkWidget { busyBar { color = Color.BLUE } }
    }

    test("should create Separator") {
      checkWidget { separator { color = Color.BLUE } }
    }

    test("should create VisTextButton") {

      checkWidget(
        widget = { visTextButton("Test.") { color = Color.BLUE } },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create VisImageButton") {
      checkWidget { visImageButton { color = Color.BLUE } }
    }

    test("should create VisImageTextButton") {

      checkWidget(
        widget = { visImageTextButton("Test.") { color = Color.BLUE } },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create VisCheckBox") {

      checkWidget(
        widget = { visCheckBox("Test.") { color = Color.BLUE } },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create VisRadioButton") {

      checkWidget(
        widget = { visRadioButton("Test.") { color = Color.BLUE } },
        validate = {
          it.text.toString() shouldBe "Test."
        },
      )
    }

    test("should create VisTree") {
      checkWidget { visTree { color = Color.BLUE } }
    }

    // Unable to compile shader in test environment.
    test("should create BasicColorPicker").config(enabled = false) { checkWidget { basicColorPicker { color = Color.BLUE } } }

    // Unable to compile shader in test environment.
    test("should create ExtendedColorPicker").config(enabled = false) { checkWidget { extendedColorPicker { color = Color.BLUE } } }

    test("should create Spinner") {

      checkWidget(
        widget = {
          spinner("Name", IntSpinnerModel(0, 0, 10)) {
            color = Color.BLUE
          }
        },
        validate = {
          (it.children[0] as Label).text.toString() shouldBe "Name"
        },
      )
    }

    test("should create VisTable") {
      checkWidget { visTable { color = Color.BLUE } }
    }

    test("should create HorizontalFlowGroup") {

      checkWidget(
        widget = { horizontalFlowGroup(spacing = 10f) { color = Color.BLUE } },
        validate = {
          it.spacing shouldBe (10f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create VerticalFlowGroup") {

      checkWidget(
        widget = { verticalFlowGroup(spacing = 10f) { color = Color.BLUE } },
        validate = {
          it.spacing shouldBe (10f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create FlowGroup") {

      checkWidget(
        widget = { flowGroup(vertical = false, spacing = 10f) { color = Color.BLUE } },
        validate = {
          it.isVertical shouldBe false
          it.spacing shouldBe (10f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create GridGroup") {

      checkWidget(
        widget = { gridGroup(itemSize = 100f, spacing = 10f) { color = Color.BLUE } },
        validate = {
          it.spacing shouldBe (10f plusOrMinus TOLERANCE)
          it.itemWidth shouldBe (100f plusOrMinus TOLERANCE)
          it.itemHeight shouldBe (100f plusOrMinus TOLERANCE)
        },
      )
    }

    test("should create FloatingGroup") {
      checkWidget { floatingGroup { color = Color.BLUE } }
    }

    test("should create DragPane") {
      checkWidget { dragPane { color = Color.BLUE } }
    }

    test("should create VisScrollPane") {
      checkWidget { visScrollPane { color = Color.BLUE } }
    }

    test("should create VisSplitPane") {
      checkWidget { visSplitPane { color = Color.BLUE } }
    }

    test("should create MultiSplitPane") {
      checkWidget { multiSplitPane { color = Color.BLUE } }
    }

    test("should create CollapsibleWidget") {
      checkWidget { collapsible { color = Color.BLUE } }
    }

    test("should create HorizontalCollapsibleWidget") {
      checkWidget { horizontalCollapsible { color = Color.BLUE } }
    }

    test("should create ButtonBar") {
      val parent = scene2d.visTable()

      val buttonBar =
        parent.buttonBar {
          isIgnoreSpacing = true
        }

      buttonBar.shouldNotBeNull()
      buttonBar.isIgnoreSpacing shouldBe true
      parent.children.size shouldBe 1
      (parent.children[0] is Table) shouldBe true
    }

    test("should create ButtonBar with order") {
      val parent = scene2d.visTable()

      val buttonBar =
        parent.buttonBar(ButtonBar.LINUX_ORDER) {
          isIgnoreSpacing = true
        }

      buttonBar.shouldNotBeNull()
      buttonBar.isIgnoreSpacing shouldBe true
      parent.children.size shouldBe 1
      (parent.children[0] is Table) shouldBe true
    }

    test("should create ListView") {
      val parent = scene2d.visTable()

      val listView =
        parent.listView(SimpleListAdapter(GdxArray.with("1", "2"))) {
          mainTable.color = Color.BLUE
        }

      listView.shouldNotBeNull()
      Color.BLUE shouldBe listView.mainTable.color
      listView.adapter.iterable().toList() shouldBe listOf("1", "2")
      (listView.mainTable in parent.children) shouldBe true
    }

    test("should create TabbedPane") {
      val parent = scene2d.visTable()

      val pane =
        parent.tabbedPane {
          table.color = Color.BLUE
        }

      pane.shouldNotBeNull()
      Color.BLUE shouldBe pane.table.color
      (pane.table in parent.children) shouldBe true
    }
  }
}
