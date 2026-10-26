package ktx.scene2d.vis

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.widget.tabbedpane.Tab
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import ktx.scene2d.ApplicationTest
import ktx.scene2d.KNode
import ktx.scene2d.defaultHorizontalStyle
import ktx.scene2d.defaultStyle
import ktx.scene2d.defaultVerticalStyle
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table
import ktx.scene2d.tree
import org.mockito.kotlin.mock

// Tests of customized VisUI parent widgets.

/**
 * Tests KTX-adapted widget: [KVisTree].
 */
class KVisTreeTest : ApplicationTest() {
  init {
    test("should add widget to group and return its node") {
      val tree = scene2d.visTree()
      val actor = Actor()

      val result: KNode<Actor> = tree.storeActor(actor)

      (actor in tree.children) shouldBe true
      result.actor shouldBeSameInstanceAs actor
    }

    test("should provide access to children nodes") {
      val tree = scene2d.visTree()
      tree.apply {
        val label = label("Test")

        val node: KNode<Label> = label.inNode

        node.shouldNotBeNull()
        node.actor shouldBeSameInstanceAs label
      }
    }

    test("should allow to configure children nodes") {
      val tree = scene2d.visTree()
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

    test("should spawn nodes") {
      val tree = KVisTree(defaultStyle)
      val actor = Actor()

      val node = tree.add(actor)

      node.actor shouldBeSameInstanceAs actor
      node.tree shouldBeSameInstanceAs tree
      node.actor.parent shouldBeSameInstanceAs tree
      tree.rootNodes.first() shouldBeSameInstanceAs node
    }
  }
}

/**
 * Tests KTX-adapted widget: [KDragPane].
 */
class KDragPaneTest : ApplicationTest() {
  init {
    test("should add actors to its table") {
      val label: Label
      val image: Image

      val pane =
        scene2d.dragPane {
          label = label("Test")
          image = visImage("white")
        }

      (label in pane.table.children) shouldBe true
      (image in pane.table.children) shouldBe true
    }
  }
}

/**
 * Tests KTX-adapted widget: [KTabbedPane].
 */
class KTabbedPaneTest : ApplicationTest() {
  init {
    test("should add tabs") {
      val tab: Tab
      val label: Label

      val pane =
        scene2d.tabbedPane {
          tab =
            tab(title = "Test", savable = true, closeableByUser = true) {
              label = label("Test")
            }
        }

      (tab in pane.tabs) shouldBe true
      (label in tab.contentTable.children) shouldBe true
      tab.pane shouldBeSameInstanceAs pane
    }

    test("should pass content table when added to group") {
      var invoked: Boolean

      scene2d.tabbedPane { contentTable ->
        contentTable shouldBeSameInstanceAs this.table
        invoked = true
      }

      invoked shouldBe true
    }

    test("should pass content table cell when added to table") {
      var invoked: Boolean

      scene2d.table {
        tabbedPane { cell ->
          cell.actor shouldBeSameInstanceAs this.table
          invoked = true
        }
      }

      invoked shouldBe true
    }

    test("should pass content table node when added to tree") {
      var invoked: Boolean

      scene2d.tree {
        tabbedPane { node ->
          node.actor shouldBeSameInstanceAs this.table
          invoked = true
        }
      }

      invoked shouldBe true
    }

    test("should allow to add content to Table") {
      val table = Table()
      val tab: Tab
      val pane =
        scene2d.tabbedPane {
          addTabContentsTo(table)
          tab("Inactive")
          tab = tab("Active")
        }

      pane.switchTab(tab)

      (tab.contentTable in table.children) shouldBe true
    }

    test("should allow to add content to Group") {
      val group = Group()
      val tab: Tab
      val pane =
        scene2d.tabbedPane {
          addTabContentsTo(group)
          tab("Inactive")
          tab = tab("Active")
        }

      pane.switchTab(tab)

      (tab.contentTable in group.children) shouldBe true
    }

    test("should allow to add content to Container") {
      val container = Container<Actor>()
      val tab: Tab
      val pane =
        scene2d.tabbedPane {
          addTabContentsTo(container)
          tab("Inactive")
          tab = tab("Active")
        }

      pane.switchTab(tab)

      (tab.contentTable in container.children) shouldBe true
    }
  }
}

/**
 * Tests KTX-adapted widget: [KVisList].
 */
class KVisListTest : ApplicationTest() {
  init {
    test("should add items") {
      val list = KVisList<String>(defaultStyle)

      list.apply {
        -"one"
        -"two"
        -"three"
      }

      list.items shouldBe Array.with("one", "two", "three")
    }

    test("should not clear items on refresh") {
      // Normally list.setItems(list.items) clears the items instead, as the internal "setter" implementation clear the
      // internal array and copies the one passed as the argument, even if both are the same object.
      val list = KVisList<String>(defaultStyle)

      list.items.apply {
        add("one")
        add("two")
        add("three")
      }

      list.items.size shouldBe 3
      list.refreshItems() // This implementation fails the test: list.setItems(list.items)
      list.items.size shouldBe 3
      list.items shouldBe Array.with("one", "two", "three")
    }
  }
}

/**
 * Tests KTX-adapted widget: [KVisScrollPane].
 */
class KVisScrollPaneTest : ApplicationTest() {
  init {
    test("should store child") {
      val scrollPane = KVisScrollPane(defaultStyle)
      val actor = Actor()

      scrollPane.addActor(actor)

      scrollPane.actor shouldBe actor
      (actor in scrollPane.children) shouldBe true
    }

    test("should fail to store multiple children") {
      val scrollPane = KVisScrollPane(defaultStyle)

      scrollPane.addActor(Actor())

      shouldThrow<IllegalStateException> {
        scrollPane.addActor(Actor())
      }
    }
  }
}

/**
 * Tests KTX-adapted widget: [KVisSelectBox].
 */
class KSelectBoxTest : ApplicationTest() {
  init {
    test("should add items") {
      val selectBox = KVisSelectBox<String>(defaultStyle)

      selectBox.apply {
        -"one"
        -"two"
        -"three"
      }

      selectBox.items shouldBe Array.with("one", "two", "three")
    }

    test("should not clear items on refresh") {
      // Normally actor.items = actor.items clears the items instead, as the internal "setter" implementation clear the
      // internal array and copies the one passed as the argument, even if both are the same object.
      val selectBox = KVisSelectBox<String>(defaultStyle)

      selectBox.items.apply {
        add("one")
        add("two")
        add("three")
      }

      selectBox.items.size shouldBe 3
      selectBox.refreshItems() // This implementation fails the test: selectBox.items = selectBox.items
      selectBox.items.size shouldBe 3
      selectBox.items shouldBe Array.with("one", "two", "three")
    }
  }
}

/**
 * Tests KTX-adapted widget: [KVisSplitPane].
 */
class KVisSplitPaneTest : ApplicationTest() {
  init {
    test("should store two children") {
      val splitPane = KVisSplitPane(false, defaultHorizontalStyle)
      val first = Actor()
      val second = Actor()

      splitPane.addActor(first)
      splitPane.addActor(second)

      (first in splitPane.children) shouldBe true
      (second in splitPane.children) shouldBe true
      // No way to access first and second widget managed internally by SplitPane (except for reflection...).
    }

    test("should fail to store more than two children") {
      val splitPane = KVisSplitPane(true, defaultVerticalStyle)

      splitPane.addActor(Actor())
      splitPane.addActor(Actor())

      shouldThrow<IllegalStateException> {
        splitPane.addActor(Actor())
      }
    }
  }
}

/**
 * Tests KTX-adapted widget: [KMultiSplitPane].
 */
class KMultiSplitPaneTest : ApplicationTest() {
  init {
    test("should store multiple children") {
      val splitPane = KMultiSplitPane(false, defaultHorizontalStyle)
      val first = Actor()
      val second = Actor()
      val third = Actor()

      splitPane.addActor(first)
      splitPane.addActor(second)
      splitPane.addActor(third)

      (first in splitPane.children) shouldBe true
      (second in splitPane.children) shouldBe true
      (third in splitPane.children) shouldBe true
    }
  }
}

/**
 * Tests KTX-adapted widget: [KCollapsible].
 */
class KCollapsibleTest : ApplicationTest() {
  init {
    test("should add actors to its table") {
      val label: Label
      val image: Image

      val collapsible =
        scene2d.collapsible {
          label = label("Test").cell(expand = true)
          image = visImage("white")
        }

      (label in collapsible.table.children) shouldBe true
      (image in collapsible.table.children) shouldBe true
    }
  }
}

/**
 * Tests KTX-adapted widget: [KHorizontalCollapsible].
 */
class KHorizontalCollapsibleTest : ApplicationTest() {
  init {
    test("should add actors to its table") {
      val label: Label
      val image: Image

      val collapsible =
        scene2d.horizontalCollapsible {
          label = label("Test").cell(expand = true)
          image = visImage("white")
        }

      (label in collapsible.table.children) shouldBe true
      (image in collapsible.table.children) shouldBe true
    }
  }
}

// Note: other extended VisUI widgets are not tested, as they do not implement any custom logic and simply inherit
// from KGroup or KTable, both of which are already tested. It is assumed that their addActor/add methods are properly
// implemented - we are basically relying on libGDX and VisUI to behave correctly.
