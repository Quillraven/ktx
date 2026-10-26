package ktx.scene2d.vis

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.Menu
import com.kotcrab.vis.ui.widget.MenuBar
import com.kotcrab.vis.ui.widget.MenuItem
import com.kotcrab.vis.ui.widget.PopupMenu
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import ktx.scene2d.ApplicationTest
import ktx.scene2d.scene2d
import ktx.scene2d.table

/**
 * Tests menu extensions.
 */
class MenusTest : ApplicationTest() {
  init {
    test("should create PopupMenu") {
      var initInvoked: Boolean

      val menu =
        scene2d.popupMenu {
          initInvoked = true
        }

      menu.shouldNotBeNull()
      initInvoked shouldBe true
    }

    test("should create MenuBar") {
      var initInvoked: Boolean

      val menu =
        scene2d.menuBar {
          initInvoked = true
        }

      menu.shouldNotBeNull()
      initInvoked shouldBe true
    }

    test("should add MenuBar table to parent") {
      val menuBar: MenuBar

      val table =
        scene2d.table {
          menuBar = menuBar()
        }

      (menuBar.table in table.children) shouldBe true
    }

    test("should create Menu") {
      var initInvoked: Boolean
      val menu: Menu

      val menuBar =
        scene2d.menuBar {
          menu =
            menu("Test menu") {
              initInvoked = true
            }
        }

      menu.title shouldBe "Test menu"
      1 shouldBe menuBar.table.children.size
      initInvoked shouldBe true
    }

    test("should create MenuItem") {
      var initInvoked: Boolean
      val menuItem: MenuItem

      scene2d.popupMenu {
        menuItem =
          menuItem("Test item") {
            initInvoked = true
          }
      }

      menuItem.text.toString() shouldBe "Test item"
      initInvoked shouldBe true
    }

    test("should create MenuItem with Drawable") {
      var initInvoked: Boolean
      val drawable = VisUI.getSkin().getDrawable("white")
      val menuItem: MenuItem

      scene2d.popupMenu {
        menuItem =
          menuItem("Test item", drawable) {
            initInvoked = true
          }
      }

      menuItem.text.toString() shouldBe "Test item"
      menuItem.image.drawable shouldBeSameInstanceAs drawable
      initInvoked shouldBe true
    }

    test("should create MenuItem with Drawable name") {
      var initInvoked: Boolean
      val drawableName = "white"
      val menuItem: MenuItem

      scene2d.popupMenu {
        menuItem =
          menuItem("Test item", drawableName = drawableName) {
            initInvoked = true
          }
      }

      menuItem.text.toString() shouldBe "Test item"
      menuItem.image.drawable shouldBeSameInstanceAs VisUI.getSkin().getDrawable(drawableName)
      initInvoked shouldBe true
    }

    test("should create MenuItem with Image") {
      var initInvoked: Boolean
      val image = Image(VisUI.getSkin().getDrawable("white"))
      val menuItem: MenuItem

      scene2d.popupMenu {
        menuItem =
          menuItem("Test item", image) {
            initInvoked = true
          }
      }

      menuItem.text.toString() shouldBe "Test item"
      menuItem.image shouldBeSameInstanceAs image
      initInvoked shouldBe true
    }

    test("should create submenu") {
      var initInvoked: Boolean
      val menuItem: MenuItem
      val subMenu: PopupMenu

      scene2d.popupMenu {
        menuItem =
          menuItem("") {
            subMenu =
              subMenu {
                initInvoked = true
              }
          }
      }

      subMenu.shouldNotBeNull()
      menuItem.subMenu shouldBeSameInstanceAs subMenu
      initInvoked shouldBe true
    }
  }
}
