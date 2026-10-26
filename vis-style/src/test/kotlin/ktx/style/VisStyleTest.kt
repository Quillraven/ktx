package ktx.style

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.kotcrab.vis.ui.Sizes
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter.SimpleListAdapterStyle
import com.kotcrab.vis.ui.util.form.SimpleFormValidator.FormValidatorStyle
import com.kotcrab.vis.ui.widget.BusyBar.BusyBarStyle
import com.kotcrab.vis.ui.widget.LinkLabel.LinkLabelStyle
import com.kotcrab.vis.ui.widget.ListViewStyle
import com.kotcrab.vis.ui.widget.Menu.MenuStyle
import com.kotcrab.vis.ui.widget.MenuBar.MenuBarStyle
import com.kotcrab.vis.ui.widget.MenuItem.MenuItemStyle
import com.kotcrab.vis.ui.widget.MultiSplitPane.MultiSplitPaneStyle
import com.kotcrab.vis.ui.widget.PopupMenu.PopupMenuStyle
import com.kotcrab.vis.ui.widget.Separator.SeparatorStyle
import com.kotcrab.vis.ui.widget.Tooltip.TooltipStyle
import com.kotcrab.vis.ui.widget.VisCheckBox.VisCheckBoxStyle
import com.kotcrab.vis.ui.widget.VisImageButton.VisImageButtonStyle
import com.kotcrab.vis.ui.widget.VisImageTextButton.VisImageTextButtonStyle
import com.kotcrab.vis.ui.widget.VisSplitPane.VisSplitPaneStyle
import com.kotcrab.vis.ui.widget.VisTextButton.VisTextButtonStyle
import com.kotcrab.vis.ui.widget.VisTextField.VisTextFieldStyle
import com.kotcrab.vis.ui.widget.color.ColorPickerStyle
import com.kotcrab.vis.ui.widget.color.ColorPickerWidgetStyle
import com.kotcrab.vis.ui.widget.spinner.Spinner.SpinnerStyle
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane.TabbedPaneStyle
import com.kotcrab.vis.ui.widget.toast.Toast.ToastStyle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

/**
 * Tests building utilities of VisUI widget styles.
 */
class VisStyleTest : FunSpec({
  test("should add Sizes") {
    val skin =
      skin {
        sizes {
          borderSize = 1f
        }
      }

    val style = skin.get<Sizes>(defaultStyle)
    style.borderSize shouldBe 1f
  }

  test("should extend Sizes") {
    val skin =
      skin {
        sizes("base") {
          borderSize = 1f
        }
        sizes("new", extend = "base") {
          buttonBarSpacing = 1f
        }
      }

    val style = skin.get<Sizes>("new")
    style.borderSize shouldBe 1f
    style.buttonBarSpacing shouldBe 1f
  }

  test("should add BusyBarStyle") {
    val skin =
      skin {
        busyBar {
          height = 1
        }
      }

    val style = skin.get<BusyBarStyle>(defaultStyle)
    style.height shouldBe 1
  }

  test("should extend BusyBarStyle") {
    val skin =
      skin {
        busyBar("base") {
          height = 1
        }
        busyBar("new", extend = "base") {
          segmentWidth = 1
        }
      }

    val style = skin.get<BusyBarStyle>("new")
    style.height shouldBe 1
    style.segmentWidth shouldBe 1
  }

  test("should add ColorPickerStyle") {
    val skin =
      skin {
        colorPicker {
          titleFontColor = Color.RED
        }
      }

    val style = skin.get<ColorPickerStyle>(defaultStyle)
    style.titleFontColor shouldBe Color.RED
  }

  test("should extend ColorPickerStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        colorPicker("base") {
          pickerStyle = it.colorPickerWidget()
          titleFontColor = Color.RED
        }
        colorPicker("new", extend = "base") {
          stageBackground = drawable
        }
      }

    val style = skin.get<ColorPickerStyle>("new")
    style.titleFontColor shouldBe Color.RED
    style.stageBackground shouldBe drawable
  }

  test("should add ColorPickerWidgetStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        colorPickerWidget {
          barSelector = drawable
        }
      }

    val style = skin.get<ColorPickerWidgetStyle>(defaultStyle)
    style.barSelector shouldBe drawable
  }

  test("should extend ColorPickerWidgetStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        colorPickerWidget("base") {
          barSelector = drawable
        }
        colorPickerWidget("new", extend = "base") {
          cross = drawable
        }
      }

    val style = skin.get<ColorPickerWidgetStyle>("new")
    style.barSelector shouldBe drawable
    style.cross shouldBe drawable
  }

  test("should add FormValidatorStyle") {
    val skin =
      skin {
        formValidator {
          colorTransitionDuration = 1f
        }
      }

    val style = skin.get<FormValidatorStyle>(defaultStyle)
    style.colorTransitionDuration shouldBe 1f
  }

  test("should extend FormValidatorStyle") {
    val skin =
      skin {
        formValidator("base") {
          colorTransitionDuration = 1f
        }
        formValidator("new", extend = "base") {
          errorLabelColor = Color.RED
        }
      }

    val style = skin.get<FormValidatorStyle>("new")
    style.colorTransitionDuration shouldBe 1f
    style.errorLabelColor shouldBe Color.RED
  }

  test("should add LinkLabelStyle") {
    val skin =
      skin {
        linkLabel {
          fontColor = Color.RED
        }
      }

    val style = skin.get<LinkLabelStyle>(defaultStyle)
    style.fontColor shouldBe Color.RED
  }

  test("should extend LinkLabelStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        linkLabel("base") {
          fontColor = Color.RED
        }
        linkLabel("new", extend = "base") {
          background = drawable
        }
      }

    val style = skin.get<LinkLabelStyle>("new")
    style.fontColor shouldBe Color.RED
    style.background shouldBe drawable
  }

  test("should add ListViewStyle") {
    val scrollPane = mock<ScrollPaneStyle>()
    val skin =
      skin {
        listView {
          scrollPaneStyle = scrollPane
        }
      }

    val style = skin.get<ListViewStyle>(defaultStyle)
    style.scrollPaneStyle shouldBe scrollPane
  }

  test("should extend ListViewStyle") {
    val scrollPane = ScrollPaneStyle()
    val drawable = mock<Drawable>()
    scrollPane.background = drawable
    val skin =
      skin {
        listView("base") {
          scrollPaneStyle = scrollPane
        }
        listView("new", extend = "base") {
          scrollPaneStyle.corner = drawable
        }
      }

    val style = skin.get<ListViewStyle>("new")
    // ScrollPaneStyle is copied, so nested properties are checked:
    style.scrollPaneStyle.background shouldBe drawable
    style.scrollPaneStyle.corner shouldBe drawable
  }

  test("should add MenuStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        menu {
          border = drawable
        }
      }

    val style = skin.get<MenuStyle>(defaultStyle)
    style.border shouldBe drawable
  }

  test("should extend MenuStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        menu("base") {
          border = drawable
        }
        menu("new", extend = "base") {
          background = drawable
        }
      }

    val style = skin.get<MenuStyle>("new")
    style.border shouldBe drawable
    style.background shouldBe drawable
  }

  test("should add MenuBarStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        menuBar {
          background = drawable
        }
      }

    val style = skin.get<MenuBarStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend MenuBarStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        menuBar("base") {
          background = drawable
        }
        menuBar("new", extend = "base")
      }

    val style = skin.get<MenuBarStyle>("new")
    style.background shouldBe drawable
  }

  test("should add MenuItemStyle") {
    val skin =
      skin {
        menuItem {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<MenuItemStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend MenuItemStyle") {
    val skin =
      skin {
        menuItem("base") {
          pressedOffsetX = 1f
        }
        menuItem("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<MenuItemStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add MultiSplitPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        multiSplitPane {
          handle = drawable
        }
      }

    val style = skin.get<MultiSplitPaneStyle>(defaultStyle)
    style.handle shouldBe drawable
  }

  test("should extend MultiSplitPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        multiSplitPane("base") {
          handle = drawable
        }
        multiSplitPane("new", extend = "base") {
          handleOver = drawable
        }
      }

    val style = skin.get<MultiSplitPaneStyle>("new")
    style.handle shouldBe drawable
    style.handleOver shouldBe drawable
  }

  test("should add PopupMenuStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        popupMenu {
          background = drawable
        }
      }

    val style = skin.get<PopupMenuStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend PopupMenuStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        popupMenu("base") {
          background = drawable
        }
        popupMenu("new", extend = "base") {
          border = drawable
        }
      }

    val style = skin.get<PopupMenuStyle>("new")
    style.background shouldBe drawable
    style.border shouldBe drawable
  }

  test("should add SeparatorStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        separator {
          background = drawable
        }
      }

    val style = skin.get<SeparatorStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend SeparatorStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        separator("base") {
          background = drawable
        }
        separator("new", extend = "base") {
          thickness = 1
        }
      }

    val style = skin.get<SeparatorStyle>("new")
    style.background shouldBe drawable
    style.thickness shouldBe 1
  }

  test("should add SimpleListAdapterStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        simpleListAdapter {
          background = drawable
        }
      }

    val style = skin.get<SimpleListAdapterStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend SimpleListAdapterStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        simpleListAdapter("base") {
          background = drawable
        }
        simpleListAdapter("new", extend = "base") {
          selection = drawable
        }
      }

    val style = skin.get<SimpleListAdapterStyle>("new")
    style.background shouldBe drawable
    style.selection shouldBe drawable
  }

  test("should add SpinnerStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        spinner {
          down = drawable
        }
      }

    val style = skin.get<SpinnerStyle>(defaultStyle)
    style.down shouldBe drawable
  }

  test("should extend SpinnerStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        spinner("base") {
          down = drawable
        }
        spinner("new", extend = "base") {
          up = drawable
        }
      }

    val style = skin.get<SpinnerStyle>("new")
    style.down shouldBe drawable
    style.up shouldBe drawable
  }

  test("should add TabbedPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        tabbedPane {
          background = drawable
        }
      }

    val style = skin.get<TabbedPaneStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend TabbedPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        tabbedPane("base") {
          background = drawable
        }
        tabbedPane("new", extend = "base") {
          vertical = true
        }
      }

    val style = skin.get<TabbedPaneStyle>("new")
    style.background shouldBe drawable
    style.vertical shouldBe true
  }

  test("should add ToastStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        toast {
          background = drawable
        }
      }

    val style = skin.get<ToastStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend ToastStyle") {
    val drawable = mock<Drawable>()
    val buttonStyle = mock<VisImageButtonStyle>()

    val skin =
      skin {
        toast("base") {
          background = drawable
        }
        toast("new", extend = "base") {
          closeButtonStyle = buttonStyle
        }
      }

    val style = skin.get<ToastStyle>("new")
    style.background shouldBe drawable
    style.closeButtonStyle shouldBe buttonStyle
  }

  test("should add VisCheckBoxStyle") {
    val skin =
      skin {
        visCheckBox {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<VisCheckBoxStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend VisCheckBoxStyle") {
    val skin =
      skin {
        visCheckBox("base") {
          pressedOffsetX = 1f
        }
        visCheckBox("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<VisCheckBoxStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add VisImageButtonStyle") {
    val skin =
      skin {
        visImageButton {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<VisImageButtonStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend VisImageButtonStyle") {
    val skin =
      skin {
        visImageButton("base") {
          pressedOffsetX = 1f
        }
        visImageButton("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<VisImageButtonStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add VisImageTextButtonStyle") {
    val skin =
      skin {
        visImageTextButton {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<VisImageTextButtonStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend VisImageTextButtonStyle") {
    val skin =
      skin {
        visImageTextButton("base") {
          pressedOffsetX = 1f
        }
        visImageTextButton("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<VisImageTextButtonStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add VisSplitPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        visSplitPane {
          handle = drawable
        }
      }

    val style = skin.get<VisSplitPaneStyle>(defaultStyle)
    style.handle shouldBe drawable
  }

  test("should extend VisSplitPaneStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        visSplitPane("base") {
          handle = drawable
        }
        visSplitPane("new", extend = "base") {
          handleOver = drawable
        }
      }

    val style = skin.get<VisSplitPaneStyle>("new")
    style.handle shouldBe drawable
    style.handleOver shouldBe drawable
  }

  test("should add VisTextButtonStyle") {
    val skin =
      skin {
        visTextButton {
          pressedOffsetX = 1f
        }
      }

    val style = skin.get<VisTextButtonStyle>(defaultStyle)
    style.pressedOffsetX shouldBe 1f
  }

  test("should extend VisTextButtonStyle") {
    val skin =
      skin {
        visTextButton("base") {
          pressedOffsetX = 1f
        }
        visTextButton("new", extend = "base") {
          pressedOffsetY = 1f
        }
      }

    val style = skin.get<VisTextButtonStyle>("new")
    style.pressedOffsetX shouldBe 1f
    style.pressedOffsetY shouldBe 1f
  }

  test("should add VisTextFieldStyle") {
    val skin =
      skin {
        visTextField {
          fontColor = Color.CYAN
        }
      }

    val style = skin.get<VisTextFieldStyle>(defaultStyle)
    style.fontColor shouldBe Color.CYAN
  }

  test("should extend VisTextFieldStyle") {
    val skin =
      skin {
        visTextField("base") {
          fontColor = Color.CYAN
        }
        visTextField("new", extend = "base") {
          disabledFontColor = Color.BLACK
        }
      }

    val style = skin.get<VisTextFieldStyle>("new")
    style.fontColor shouldBe Color.CYAN
    style.disabledFontColor shouldBe Color.BLACK
  }

  test("should add VisTooltipStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        visTooltip {
          background = drawable
        }
      }

    val style = skin.get<TooltipStyle>(defaultStyle)
    style.background shouldBe drawable
  }

  test("should extend VisTooltipStyle") {
    val drawable = mock<Drawable>()
    val skin =
      skin {
        visTooltip("base") {
          background = drawable
        }
        visTooltip("new", extend = "base")
      }

    val style = skin.get<TooltipStyle>("new")
    style.background shouldBe drawable
  }
})
