package ktx.scene2d

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Tree
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.VisUI
import io.kotest.assertions.throwables.shouldThrow
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
 * Tests [scene2d] DSL object.
 */
class Scene2DTest : ApplicationTest() {
  // Factory methods for other actors are tested separately in `factoryTest`.

  init {
    test("should not add an actor to a group") {
      val actor = Actor()

      val result = scene2d.storeActor(actor)

      result shouldBeSameInstanceAs actor
      actor.parent.shouldBeNull()
    }

    test("should support Scene2D DSL") {
      val label = scene2d.label("Test")

      label.text.toString() shouldBe "Test"
    }

    test("should support creating actors via DSL building block") {
      val label =
        scene2d {
          label("Test")
        }

      label.text.toString() shouldBe "Test"
    }

    test("should execute DSL building block exactly once") {
      val variable: Int
      val nestedLabel: Label

      val table =
        scene2d {
          variable = 42
          table {
            nestedLabel = label("Test")
          }
        }

      nestedLabel.parent shouldBeSameInstanceAs table
      nestedLabel.text.toString() shouldBe "Test"
      variable shouldBe 42
    }
  }
}

/**
 * Tests [KGroup] interface: base for all simple WidgetGroup-based parental actors.
 */
class KGroupTest : FunSpec({
  test("should add widget to group and return the actor instead of storage object") {
    val group = TestGroup()
    val actor = Actor()

    val result: Actor = group.storeActor(actor)

    (actor in group.children) shouldBe true
    result shouldBeSameInstanceAs actor
  }
})

class TestGroup :
  Group(),
  KGroup

/**
 * Tests [KTable] interface: base for all Table-based parental actors.
 */
class KTableTest : ApplicationTest() {
  class TestTable :
    Table(),
    KTable

  init {
    test("should add widget to group and return its cell") {
      val group = TestTable()
      val actor = Actor()

      val result: Cell<*> = group.storeActor(actor)

      (actor in group.children) shouldBe true
      result.actor shouldBeSameInstanceAs actor
    }

    test("should provide access to children cells") {
      val table = TestTable()
      table.apply {
        val label = label("Test")

        val cell: Cell<Label> = label.inCell

        cell.shouldNotBeNull()
        cell.actor shouldBeSameInstanceAs label
      }
    }

    test("should allow to configure children cells") {
      val table = TestTable()
      table.apply {
        val cell: Cell<Label> =
          label("Test") {}
            .cell(
              grow = true, // Overridden.
              growX = false, // Overridden.
              growY = false, // Overridden.
              expand = true, // Overridden.
              expandX = true,
              expandY = false,
              fill = true, // Overridden.
              fillX = false,
              fillY = true,
              uniform = true, // Overridden.
              uniformX = false,
              uniformY = true,
              align = Align.center,
              colspan = 3,
              width = 100f, // Overridden.
              minWidth = 10f,
              preferredWidth = 50f,
              maxWidth = 150f,
              height = 75f, // Overridden.
              minHeight = 5f,
              preferredHeight = 25f,
              maxHeight = 55f,
              pad = 42f, // Overridden.
              padTop = 6f,
              padLeft = 7f,
              padRight = 8f,
              padBottom = 9f,
              space = 24f, // Overridden.
              spaceTop = 26f,
              spaceLeft = 27f,
              spaceRight = 28f,
              spaceBottom = 29f,
              row = true,
            ).inCell

        cell.expandX shouldBe 1
        cell.expandY shouldBe 0
        cell.fillX shouldBe (0f plusOrMinus TOLERANCE)
        cell.fillY shouldBe (1f plusOrMinus TOLERANCE)
        cell.uniformX shouldBe false
        cell.uniformY shouldBe true
        cell.align shouldBe Align.center
        cell.colspan shouldBe 3
        cell.minWidth shouldBe (10f plusOrMinus TOLERANCE)
        cell.prefWidth shouldBe (50f plusOrMinus TOLERANCE)
        cell.maxWidth shouldBe (150f plusOrMinus TOLERANCE)
        cell.minHeight shouldBe (5f plusOrMinus TOLERANCE)
        cell.prefHeight shouldBe (25f plusOrMinus TOLERANCE)
        cell.maxHeight shouldBe (55f plusOrMinus TOLERANCE)
        cell.padTop shouldBe (6f plusOrMinus TOLERANCE)
        cell.padLeft shouldBe (7f plusOrMinus TOLERANCE)
        cell.padRight shouldBe (8f plusOrMinus TOLERANCE)
        cell.padBottom shouldBe (9f plusOrMinus TOLERANCE)
        cell.spaceTop shouldBe (26f plusOrMinus TOLERANCE)
        cell.spaceLeft shouldBe (27f plusOrMinus TOLERANCE)
        cell.spaceRight shouldBe (28f plusOrMinus TOLERANCE)
        cell.spaceBottom shouldBe (29f plusOrMinus TOLERANCE)
        cell.isEndRow shouldBe true // .row() was called.
      }
    }
  }
}

/**
 * Tests [KTree] interface: base for all parental actors operating on tree nodes.
 */
class KTreeTest : ApplicationTest() {
  class TestTree :
    Tree<Node<*, *, *>, Any?>(VisUI.getSkin()),
    KTree {
    override fun <T : Actor> add(actor: T): KNode<T> {
      val node = KNode(actor)
      add(node)
      return node
    }
  }

  init {
    test("should add widget to group and return its node") {
      val group = TestTree()
      val actor = Actor()

      val result: KNode<Actor> = group.storeActor(actor)

      (actor in group.children) shouldBe true
      result.actor shouldBeSameInstanceAs actor
    }

    test("should provide access to children nodes") {
      val tree = TestTree()
      tree.apply {
        val label = label("Test")

        val node: KNode<Label> = label.inNode

        node.shouldNotBeNull()
        node.actor shouldBeSameInstanceAs label
      }
    }

    test("should allow to configure children nodes") {
      val tree = TestTree()
      val icon = mock<Drawable>()
      tree.apply {
        val node: KNode<Label> =
          label("Test") {}
            .node(
              icon = icon,
              selectable = false,
              expanded = true,
              userObject = "Test",
            ).inNode

        node.icon shouldBeSameInstanceAs icon
        node.isSelectable shouldBe false
        node.isExpanded shouldBe true
        node.value shouldBe "Test"
      }
    }
  }
}

/**
 * Tests KTX custom actor: [KButtonTable].
 */
class KButtonTableTest : ApplicationTest() {
  init {
    test("should add Buttons to ButtonGroup") {
      val buttonTable = KButtonTable(1, 2, Skin())

      val actor = Actor()
      buttonTable.add(actor)
      val button = Button()
      buttonTable.add(button)

      (actor in buttonTable.buttonGroup.buttons) shouldBe false
      (button in buttonTable.buttonGroup.buttons) shouldBe true
      (actor in buttonTable.children) shouldBe true
      (button in buttonTable.children) shouldBe true
    }

    test("should honor checked buttons constraints") {
      val buttonTable = KButtonTable(1, 2, Skin())
      val buttons = arrayOf(Button(), Button(), Button())

      buttons.forEach { buttonTable.add(it) }

      (buttons.filter { it.isChecked }.count()) shouldBe 1
      buttons.forEach { it.isChecked = true }
      (buttons.filter { it.isChecked }.count()) shouldBe 2
      buttons.forEach { it.isChecked = false }
      (buttons.filter { it.isChecked }.count()) shouldBe 1
    }
  }
}

/**
 * Testing KTX-adapted widget: [KContainer].
 */
class KContainerTest : FunSpec({
  test("should create container with an actor") {
    val actor = Actor()

    val container = KContainer(actor)

    container.actor shouldBeSameInstanceAs actor
    (actor in container.children) shouldBe true
  }

  test("should create container without an actor") {
    val actor: Actor? = null

    val container = KContainer(actor)

    container.actor.shouldBeNull()
  }

  test("should create container without an actor by default") {
    val container = KContainer<Actor>()

    container.actor.shouldBeNull()
  }

  test("should store child") {
    val container = KContainer<Actor>()
    val actor = Actor()

    container.addActor(actor)

    container.actor shouldBeSameInstanceAs actor
    (actor in container.children) shouldBe true
  }

  test("should fail to store multiple children") {
    val container = KContainer<Actor>()

    container.addActor(Actor())

    shouldThrow<IllegalStateException> {
      container.addActor(Actor())
    }
  }
})

/**
 * Testing KTX-adapted widget: [KListWidget].
 */
class KListWidgetTest : ApplicationTest() {
  init {
    test("should add items") {
      val list = KListWidget<String>(VisUI.getSkin(), defaultStyle)

      list.apply {
        -"one"
        -"two"
        -"three"
      }

      list.items shouldBe GdxArray.with("one", "two", "three")
    }

    test("should not clear items on refresh") {
      // Normally list.setItems(list.items) clears the items instead, as the internal "setter" implementation clear the
      // internal array and copies the one passed as the argument, even if both are the same object.
      val list = KListWidget<String>(VisUI.getSkin(), defaultStyle)

      list.items.apply {
        add("one")
        add("two")
        add("three")
      }

      list.items.size shouldBe 3
      list.refreshItems() // This implementation fails the test: list.setItems(list.items)
      list.items.size shouldBe 3
      list.items shouldBe GdxArray.with("one", "two", "three")
    }
  }
}

/**
 * Testing "mock" actor - [KNode], based on tree's [Node] and implementing [KTree] interface for extra interface
 * building utility.
 */
class KNodeTest : FunSpec({
  test("should create nested nodes") {
    val node = KNode(Actor())
    val actor = Actor()

    val nested = node.add(actor)

    nested.actor shouldBeSameInstanceAs actor
    nested.parent shouldBeSameInstanceAs node
    node.children.first() shouldBeSameInstanceAs nested
  }
})

/**
 * Testing KTX-adapted widget: [KScrollPane].
 */
class KScrollPaneTest : ApplicationTest() {
  init {
    test("should store child") {
      val scrollPane = KScrollPane(VisUI.getSkin(), defaultStyle)
      val actor = Actor()

      scrollPane.addActor(actor)

      scrollPane.actor shouldBe actor
      (actor in scrollPane.children) shouldBe true
    }

    test("should fail to store multiple children") {
      val scrollPane = KScrollPane(VisUI.getSkin(), defaultStyle)

      scrollPane.addActor(Actor())

      shouldThrow<IllegalStateException> {
        scrollPane.addActor(Actor())
      }
    }
  }
}

/**
 * Testing KTX-adapted widget: [KSelectBox].
 */
class KSelectBoxTest : ApplicationTest() {
  init {
    test("should add items") {
      val selectBox = KSelectBox<String>(VisUI.getSkin(), defaultStyle)

      selectBox.apply {
        -"one"
        -"two"
        -"three"
      }

      selectBox.items shouldBe GdxArray.with("one", "two", "three")
    }

    test("should not clear items on refresh") {
      // Normally actor.items = actor.items clears the items instead, as the internal "setter" implementation clear the
      // internal array and copies the one passed as the argument, even if both are the same object.
      val selectBox = KSelectBox<String>(VisUI.getSkin(), defaultStyle)

      selectBox.items.apply {
        add("one")
        add("two")
        add("three")
      }

      selectBox.items.size shouldBe 3
      selectBox.refreshItems() // This implementation fails the test: selectBox.items = selectBox.items
      selectBox.items.size shouldBe 3
      selectBox.items shouldBe GdxArray.with("one", "two", "three")
    }
  }
}

/**
 * Testing KTX-adapted widget: [KSplitPane].
 */
class KSplitPaneTest : ApplicationTest() {
  init {
    test("should store two children") {
      val splitPane = KSplitPane(false, VisUI.getSkin(), defaultHorizontalStyle)
      val first = Actor()
      val second = Actor()

      splitPane.addActor(first)
      splitPane.addActor(second)

      (first in splitPane.children) shouldBe true
      (second in splitPane.children) shouldBe true
      // No way to access first and second widget managed internally by SplitPane (except for reflection...).
    }

    test("should fail to store more than two children") {
      val splitPane = KSplitPane(false, VisUI.getSkin(), defaultHorizontalStyle)

      splitPane.addActor(Actor())
      splitPane.addActor(Actor())

      shouldThrow<IllegalStateException> {
        splitPane.addActor(Actor())
      }
    }
  }
}

/**
 * Testing KTX-adapted widget: [KTreeWidget].
 */
class KTreeWidgetTest : ApplicationTest() {
  init {
    test("should spawn nodes") {
      val tree = KTreeWidget(VisUI.getSkin(), defaultStyle)
      val actor = Actor()

      val node = tree.add(actor)

      node.actor shouldBeSameInstanceAs actor
      node.tree shouldBeSameInstanceAs tree
      node.actor.parent shouldBeSameInstanceAs tree
      tree.rootNodes.first() shouldBeSameInstanceAs node
    }
  }
}

// Note: other extended Scene2D widgets are not tested, as they do not implement any custom logic and simply inherit
// from KGroup or KTable, both of which are already tested. It is assumed that their addActor/add methods are properly
// implemented - we are basically relying on libGDX to behave correctly.
